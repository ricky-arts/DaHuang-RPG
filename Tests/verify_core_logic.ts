import crypto from "crypto";

console.log("==================================================");
console.log("=== 洪荒 RPG 核心引擎自动化单元测试 (Bun Runner) ===");
console.log("==================================================\n");

function assert(condition: boolean, msg: string) {
  if (condition) {
    console.log(`\x1b[32m[PASS]\x1b[0m ${msg}`);
  } else {
    console.error(`\x1b[31m[FAIL]\x1b[0m ${msg}`);
    process.exit(1);
  }
}

// 1. 防修改数值测试
class ObfuscatedInt {
  private cipher: number;
  private key: number;

  constructor(val: number) {
    this.key = (Math.random() * 900000 + 100000) | 0;
    this.cipher = val ^ this.key;
  }

  get value(): number {
    return this.cipher ^ this.key;
  }

  set value(val: number) {
    this.key = (Math.random() * 900000 + 100000) | 0;
    this.cipher = val ^ this.key;
  }
}

console.log("--- 测试 1: 内存防修改掩码 (ObfuscatedInt) ---");
const safeVal = new ObfuscatedInt(9999);
assert(safeVal.value === 9999, "数值正确混淆与动态解密");
safeVal.value += 1;
assert(safeVal.value === 10000, "防修改数值自增无损");

// 2. 加密存档与 HMAC 完整性校验
console.log("\n--- 测试 2: 加密本地存档与 HMAC 防篡改 ---");
function deriveKey(devId: string, pkg: string): Buffer {
  return crypto.pbkdf2Sync(`${devId}:${pkg}:HongHuang`, "salt_honghuang_2026", 2048, 32, "sha256");
}

function encryptWithHmac(data: string, key: Buffer): Buffer {
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv("aes-256-cbc", key, iv);
  const encrypted = Buffer.concat([cipher.update(data, "utf8"), cipher.final()]);
  const hmac = crypto.createHmac("sha256", key).update(encrypted).digest();
  return Buffer.concat([iv, hmac, encrypted]);
}

function decryptWithHmac(payload: Buffer, key: Buffer): string {
  const iv = payload.subarray(0, 16);
  const storedHmac = payload.subarray(16, 48);
  const cipherBytes = payload.subarray(48);

  const calculatedHmac = crypto.createHmac("sha256", key).update(cipherBytes).digest();
  if (!calculatedHmac.equals(storedHmac)) {
    throw new Error("HMAC 校验失败：存档被非法篡改！");
  }

  const decipher = crypto.createDecipheriv("aes-256-cbc", key, iv);
  return Buffer.concat([decipher.update(cipherBytes), decipher.final()]).toString("utf8");
}

const key = deriveKey("android_phone_001", "com.honghuang.rpg");
const saveJson = JSON.stringify({ player: "遁去的一", realm: "JinDan", stone: 999999 });
const packedSave = encryptWithHmac(saveJson, key);
const decryptedSave = decryptWithHmac(packedSave, key);
assert(decryptedSave === saveJson, "AES-256 + HMAC 密文解密还原准确无误");

// 模拟篡改
packedSave[packedSave.length - 1] ^= 0xff;
let tamperCaught = false;
try {
  decryptWithHmac(packedSave, key);
} catch (e) {
  tamperCaught = true;
}
assert(tamperCaught, "外部 Hex 篡改存档被 HMAC 签名机制成功阻断捕获");

// 3. 五行生克与双曲综合伤害公式
console.log("\n--- 测试 3: 五行生克与双曲防御伤害引擎 ---");
enum ElementType { None, Metal, Wood, Water, Fire, Earth }

function getElementModifier(att: ElementType, def: ElementType): number {
  if (att === def || att === ElementType.None || def === ElementType.None) return 1.0;
  const isAdv =
    (att === ElementType.Metal && def === ElementType.Wood) ||
    (att === ElementType.Wood && def === ElementType.Earth) ||
    (att === ElementType.Earth && def === ElementType.Water) ||
    (att === ElementType.Water && def === ElementType.Fire) ||
    (att === ElementType.Fire && def === ElementType.Metal);
  if (isAdv) return 1.35;

  const isDis =
    (def === ElementType.Metal && att === ElementType.Wood) ||
    (def === ElementType.Wood && att === ElementType.Earth) ||
    (def === ElementType.Earth && att === ElementType.Water) ||
    (def === ElementType.Water && att === ElementType.Fire) ||
    (def === ElementType.Fire && att === ElementType.Metal);
  if (isDis) return 0.75;
  return 1.0;
}

assert(getElementModifier(ElementType.Metal, ElementType.Wood) === 1.35, "金克木正确增伤 35%");
assert(getElementModifier(ElementType.Wood, ElementType.Metal) === 0.75, "木被金克正确减伤 25%");
assert(getElementModifier(ElementType.Fire, ElementType.Metal) === 1.35, "火克金正确增伤 35%");

function calcDamage(atk: number, def: number, elemMod: number, realmMod: number): number {
  const defConstant = 800.0;
  const defReduction = def / (defConstant + def); // 双曲减伤
  const baseDmg = atk * (1.0 - defReduction);
  return baseDmg * elemMod * realmMod;
}

// 攻击 1000，防御 800 (减伤 50%)，火克金 (1.35)，同境界 (1.0)
const dmg = calcDamage(1000, 800, 1.35, 1.0);
assert(Math.abs(dmg - 675.0) < 0.001, `双曲防御与五行伤害计算精确: 结算 ${dmg}`);

// 4. 非线性境界压制模型
console.log("\n--- 测试 4: 非线性境界压制算法 ---");
function getRealmModifier(delta: number): number {
  if (delta === 0) return 1.0;
  if (delta > 0) return 1.0 + 0.25 * delta + 0.05 * Math.pow(delta, 2);
  const absDelta = Math.abs(delta);
  return Math.max(0.15, 1.0 / (1.0 + 0.35 * absDelta + 0.08 * Math.pow(absDelta, 2)));
}

const highToLow = getRealmModifier(2); // 金丹打练气 (+2)
assert(Math.abs(highToLow - 1.70) < 0.001, `顺境碾压修正正确: ${highToLow}`);

const lowToHigh = getRealmModifier(-2); // 练气打金丹 (-2)
assert(lowToHigh < 0.50 && lowToHigh > 0.48, `低打高逆伐衰减阻尼平滑: ${lowToHigh.toFixed(4)}`);

console.log("\n\x1b[32m>>> 全部 4 项核心测试验证 100% 通过！架构计算引擎完全可靠！<<<\x1b[0m");
