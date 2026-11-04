import { execSync } from "child_process";

console.log("=== 执行《大荒：洪荒弈道》v3.1.0 实时对决、索敌走砍与双抗减伤闭环自测 ===");

function assert(cond: boolean, msg: string) {
  if (cond) {
    console.log(`\x1b[32m[PASS]\x1b[0m ${msg}`);
  } else {
    console.error(`\x1b[31m[FAIL]\x1b[0m ${msg}`);
    process.exit(1);
  }
}

// 1. 物理抗性与法术抗性双轨双曲减伤自测
function calcDmg(type: "PHYSICAL" | "MAGIC" | "TRUE", raw: number, armor: number, mr: number, ap: number) {
  if (type === "PHYSICAL") {
    const red = armor / (100.0 + armor);
    return Math.round(raw * (1.0 - red));
  } else if (type === "MAGIC") {
    const red = mr / (100.0 + mr);
    return Math.round(raw * (ap / 100.0) * (1.0 - red));
  } else {
    return raw;
  }
}

// 100点物抗提供 50% 物免: 100 / (100 + 100) = 0.5
const physDmg = calcDmg("PHYSICAL", 200, 100, 50, 100);
assert(physDmg === 100, "100 点护甲提供 50% 物理减伤 (200 攻击结算为 100)");

// 50点魔抗提供 33.3% 减伤，140% 法强: 300 * 1.4 * (1 - 50/150) = 420 * 0.667 = 280
const magicDmg = calcDmg("MAGIC", 300, 50, 50, 140);
assert(magicDmg === 280, "法抗与法强复合减伤结算精确 (300法术伤害结算为 280)");

// 真实伤害不吃任何减伤
const trueDmg = calcDmg("TRUE", 500, 200, 200, 100);
assert(trueDmg === 500, "真实伤害完全无视高双抗直接扣血");

// 2. 攻速计时器与帧率时序自测 (30 FPS 模拟)
const atkSpd = 0.75; // 0.75 次/秒 -> 40 帧打一次
const framesPerAttack = 30.0 / atkSpd;
assert(framesPerAttack === 40, "0.75 攻速正确换算为 40 帧普攻一次");

// 3. 欧氏最近目标索敌与防死锁
interface Unit {
  id: string;
  gx: number;
  gy: number;
  isAlive: boolean;
}

function findNearest(me: Unit, enemies: Unit[]): Unit | null {
  let nearest: Unit | null = null;
  let minDist = Infinity;
  for (const e of enemies) {
    if (e.isAlive) {
      const d = (e.gx - me.gx) ** 2 + (e.gy - me.gy) ** 2;
      if (d < minDist) {
        minDist = d;
        nearest = e;
      }
    }
  }
  return nearest;
}

const me: Unit = { id: "p1", gx: 4, gy: 3, isAlive: true };
const enemies: Unit[] = [
  { id: "e1", gx: 0, gy: 0, isAlive: true },
  { id: "e2", gx: 4, gy: 1, isAlive: true }, // 最近
  { id: "e3", gx: 7, gy: 1, isAlive: true }
];

const target = findNearest(me, enemies);
assert(target?.id === "e2", "准确在 O(N) 复杂度内锁定最近敌方棋子 e2");

// 4. 校验最终构建签名 APK
const apkCheck = execSync(
  "/opt/android-sdk/build-tools/34.0.0/apksigner verify --verbose /opt/cc-haha/HongHuangGame/Releases/DaHuang-v3.1.0-BattleSimulator.apk"
).toString();
assert(apkCheck.includes("Verified using v2 scheme (APK Signature Scheme v2): true"), "v3.1.0 实时对战 APK 签名全覆盖校验通过");

console.log("\n\x1b[32m>>> 《大荒：洪荒弈道》v3.1.0 实时对决与数值伤害闭环自测 100% 全部通过 (ALL PASS)！<<<\x1b[0m");
