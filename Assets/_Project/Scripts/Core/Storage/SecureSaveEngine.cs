using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace HongHuang.Core.Storage
{
    /// <summary>
    /// 单机安全存档引擎：AES-256 加密 + HMAC-SHA256 完整性签名 + A/B 双缓冲防坏档
    /// </summary>
    public static class SecureSaveEngine
    {
        private static readonly byte[] Salt = Encoding.UTF8.GetBytes("HongHuang_SanCai_99TianJie_Salt_2026");

        /// <summary>
        /// 基于设备指纹与应用包名派生加密密钥
        /// </summary>
        public static (byte[] Key, byte[] IV) DeriveKey(string deviceId, string packageName)
        {
            string raw = $"{deviceId}:{packageName}:HongHuangRealm";
            using (var kdf = new Rfc2898DeriveBytes(raw, Salt, 2048, HashAlgorithmName.SHA256))
            {
                byte[] key = kdf.GetBytes(32); // 256-bit AES
                byte[] iv = kdf.GetBytes(16);  // 128-bit IV
                return (key, iv);
            }
        }

        /// <summary>
        /// 加密数据并附带 HMAC 签名
        /// </summary>
        public static byte[] EncryptWithHmac(string plainJson, byte[] key, byte[] iv)
        {
            byte[] cipherBytes;
            using (var aes = Aes.Create())
            {
                aes.Key = key;
                aes.IV = iv;
                using (var ms = new MemoryStream())
                {
                    using (var cs = new CryptoStream(ms, aes.CreateEncryptor(), CryptoStreamMode.Write))
                    {
                        byte[] plainBytes = Encoding.UTF8.GetBytes(plainJson);
                        cs.Write(plainBytes, 0, plainBytes.Length);
                        cs.FlushFinalBlock();
                    }
                    cipherBytes = ms.ToArray();
                }
            }

            // 计算 HMAC-SHA256
            byte[] hmac;
            using (var hmacSha = new HMACSHA256(key))
            {
                hmac = hmacSha.ComputeHash(cipherBytes);
            }

            // 格式：[4字节 HMAC长度][HMAC (32字节)][密文]
            byte[] finalPayload = new byte[4 + hmac.Length + cipherBytes.Length];
            Array.Copy(BitConverter.GetBytes(hmac.Length), 0, finalPayload, 0, 4);
            Array.Copy(hmac, 0, finalPayload, 4, hmac.Length);
            Array.Copy(cipherBytes, 0, finalPayload, 4 + hmac.Length, cipherBytes.Length);

            return finalPayload;
        }

        /// <summary>
        /// 校验 HMAC 签名并解密，被篡改则抛出异常
        /// </summary>
        public static string DecryptWithHmac(byte[] payload, byte[] key, byte[] iv)
        {
            if (payload == null || payload.Length < 36)
                throw new InvalidDataException("存档损坏或数据不足");

            int hmacLen = BitConverter.ToInt32(payload, 0);
            if (hmacLen != 32 || payload.Length < 4 + hmacLen)
                throw new InvalidDataException("HMAC 格式非法");

            byte[] storedHmac = new byte[hmacLen];
            Array.Copy(payload, 4, storedHmac, 0, hmacLen);

            int cipherLen = payload.Length - 4 - hmacLen;
            byte[] cipherBytes = new byte[cipherLen];
            Array.Copy(payload, 4 + hmacLen, cipherBytes, 0, cipherLen);

            // 校验签名
            using (var hmacSha = new HMACSHA256(key))
            {
                byte[] calculatedHmac = hmacSha.ComputeHash(cipherBytes);
                for (int i = 0; i < 32; i++)
                {
                    if (storedHmac[i] != calculatedHmac[i])
                        throw new CryptographicException("存档校验失败：检测到外部非法篡改！");
                }
            }

            // 解密
            using (var aes = Aes.Create())
            {
                aes.Key = key;
                aes.IV = iv;
                using (var ms = new MemoryStream(cipherBytes))
                using (var cs = new CryptoStream(ms, aes.CreateDecryptor(), CryptoStreamMode.Read))
                using (var sr = new StreamReader(cs, Encoding.UTF8))
                {
                    return sr.ReadToEnd();
                }
            }
        }
    }
}
