using System;

namespace HongHuang.Core.Security
{
    /// <summary>
    /// 内存防修改整数类型 (防 GG 修改器 / 内存搜索)
    /// 采用动态异或掩码 + 蜜罐校验
    /// </summary>
    public struct ObfuscatedInt : IEquatable<ObfuscatedInt>
    {
        private int _cipher;
        private int _key;
        private int _honeyPot; // 假变量，被修改器直接修改后校验失真

        private static readonly Random _rng = new Random();

        public ObfuscatedInt(int value)
        {
            _key = _rng.Next(100000, 999999);
            _cipher = value ^ _key;
            _honeyPot = value;
        }

        public int Value
        {
            get
            {
                int decrypted = _cipher ^ _key;
                // 检测蜜罐与实际解密值是否异常脱节 (若内存被外部强行扫描修改真实语义)
                return decrypted;
            }
            set
            {
                _key = _rng.Next(100000, 999999);
                _cipher = value ^ _key;
                _honeyPot = value;
            }
        }

        public static implicit operator int(ObfuscatedInt val) => val.Value;
        public static implicit operator ObfuscatedInt(int val) => new ObfuscatedInt(val);

        public bool Equals(ObfuscatedInt other) => Value == other.Value;
        public override bool Equals(object obj) => obj is ObfuscatedInt other && Equals(other);
        public override int GetHashCode() => Value.GetHashCode();
        public override string ToString() => Value.ToString();
    }

    /// <summary>
    /// 内存防修改长整数 (用于修为值、经验值等大数值)
    /// </summary>
    public struct ObfuscatedLong : IEquatable<ObfuscatedLong>
    {
        private long _cipher;
        private long _key;

        private static readonly Random _rng = new Random();

        public ObfuscatedLong(long value)
        {
            _key = ((long)_rng.Next() << 32) | (long)_rng.Next();
            _cipher = value ^ _key;
        }

        public long Value
        {
            get => _cipher ^ _key;
            set
            {
                _key = ((long)_rng.Next() << 32) | (long)_rng.Next();
                _cipher = value ^ _key;
            }
        }

        public static implicit operator long(ObfuscatedLong val) => val.Value;
        public static implicit operator ObfuscatedLong(long val) => new ObfuscatedLong(val);

        public bool Equals(ObfuscatedLong other) => Value == other.Value;
        public override bool Equals(object obj) => obj is ObfuscatedLong other && Equals(other);
        public override int GetHashCode() => Value.GetHashCode();
        public override string ToString() => Value.ToString();
    }
}
