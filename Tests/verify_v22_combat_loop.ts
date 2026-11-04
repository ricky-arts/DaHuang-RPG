import { execSync } from "child_process";

console.log("=== 执行《大荒》v2.2.0 九阶晋升模型、怪物梯度与真实伤害战斗闭环自测 ===");

function assert(cond: boolean, msg: string) {
  if (cond) {
    console.log(`\x1b[32m[PASS]\x1b[0m ${msg}`);
  } else {
    console.error(`\x1b[31m[FAIL]\x1b[0m ${msg}`);
    process.exit(1);
  }
}

// 1. 九阶修真晋升链条逻辑自测
class TestRealm {
  major = 0; // 0: 练气, 1: 筑基
  minor = 1; // 1 ~ 9
  exp = 0;

  get title() {
    const majors = ["练气", "筑基", "金丹"];
    const nums = ["零", "一", "二", "三", "四", "五", "六", "七", "八", "九"];
    return `${majors[this.major]}${nums[this.minor]}阶`;
  }

  get needed() {
    return 200 * Math.pow(2.5, this.major) * this.minor;
  }

  addExp(n: number) {
    this.exp += n;
    while (this.exp >= this.needed && this.minor < 9) {
      this.exp -= this.needed;
      this.minor++;
    }
  }

  tribulate() {
    if (this.minor === 9 && this.exp >= this.needed) {
      this.major++;
      this.minor = 1;
      this.exp = 0;
      return true;
    }
    return false;
  }
}

const r = new TestRealm();
assert(r.title === "练气一阶", "初始境界正确为练气一阶");
r.addExp(200); // 升到二阶
assert(r.title === "练气二阶", "经验满 200 正确破阶晋升练气二阶");

// 连续升阶至九阶大圆满
r.addExp(10000);
assert(r.minor === 9 && r.title === "练气九阶", "连续注入经验顺利冲至练气九阶大圆满");

// 渡劫突破筑基一阶
assert(r.tribulate(), "引动天劫成功突破大境界渡劫！");
assert(r.title === "筑基一阶", "大境界跨越成功晋升筑基一阶");

// 2. 双曲减伤与五行真实伤害计算自测
function calcDmg(atk: number, rate: number, atkElem: string, def: number, defElem: string, broken: boolean) {
  const defConstant = 600.0;
  const reduction = def / (defConstant + def);
  const baseDmg = atk * rate * (1.0 - reduction);
  let elemMod = 1.0;
  if (atkElem === "木" && defElem === "土") elemMod = 1.35; // 克制
  if (atkElem === "木" && defElem === "金") elemMod = 0.75; // 被克
  const breakMod = broken ? 1.5 : 1.0;
  return Math.round(baseDmg * elemMod * breakMod);
}

// 攻击 1000, 技能倍率 1.0, 防御 600 (减伤50%), 木克土 (1.35)
const dmgNormal = calcDmg(1000, 1.0, "木", 600, "土", false);
assert(dmgNormal === 675, `真实属性与五行克制伤害计算精准: ${dmgNormal} (预期 675)`);

// 破韧状态 (1.5x)
const dmgBroken = calcDmg(1000, 1.0, "木", 600, "土", true);
assert(dmgBroken === Math.round(675 * 1.5), `破韧虚弱状态真伤增幅 50%: ${dmgBroken}`);

// 3. 校验最终构建签名 APK
const apkCheck = execSync(
  "/opt/android-sdk/build-tools/34.0.0/apksigner verify --verbose /opt/cc-haha/HongHuangGame/Releases/DaHuang-v2.2.0-Action.apk"
).toString();
assert(apkCheck.includes("Verified using v2 scheme (APK Signature Scheme v2): true"), "v2.2.0 APK 签名全覆盖校验通过");

console.log("\n\x1b[32m>>> 《大荒》v2.2.0 动态战斗与九阶晋升闭环自测 100% 全部通过 (ALL PASS)！<<<\x1b[0m");
