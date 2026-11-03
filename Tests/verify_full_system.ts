import crypto from "crypto";

console.log("==========================================================");
console.log("=== 洪荒 RPG 完整游戏核心全量回归测试套件 (Bun Runner) ===");
console.log("==========================================================\n");

function assert(condition: boolean, msg: string) {
  if (condition) {
    console.log(`\x1b[32m[PASS]\x1b[0m ${msg}`);
  } else {
    console.error(`\x1b[31m[FAIL]\x1b[0m ${msg}`);
    process.exit(1);
  }
}

// ========================
// 1. 本命法宝养成与器灵觉醒测试
// ========================
console.log("--- 模块 1: 本命法宝祭炼、升星、突破与神威测试 ---");
enum NatalVesselType {
  FlyingSword = 1,
  HeavenlySeal = 2,
  NineCauldron = 3,
  ArrayBanner = 4,
  AncientZither = 5,
}

class NatalArtifact {
  tier = 1;
  starLevel = 0;
  bloodBond = 0.0;
  isAwakened = false;

  constructor(public name: string, public vesselType: NatalVesselType) {}

  get bonusAttack(): number {
    return (100 * this.tier * (1 + this.starLevel * 0.15)) * (1.0 + this.bloodBond * 0.5);
  }

  cultivateWithBlood(amount: number): boolean {
    this.bloodBond = Math.min(1.0, this.bloodBond + amount);
    if (this.bloodBond >= 0.8 && !this.isAwakened) {
      this.isAwakened = true;
      return true;
    }
    return false;
  }

  upgradeStar(): boolean {
    if (this.starLevel >= 10) return false;
    this.starLevel++;
    return true;
  }

  advanceTier(playerRealm: number): boolean {
    if (this.starLevel < 10) return false;
    if (this.tier >= 9 || this.tier >= playerRealm) return false;
    this.tier++;
    this.starLevel = 0;
    return true;
  }

  getSkillName(): string {
    return this.vesselType === NatalVesselType.FlyingSword
      ? "【万剑归宗·一剑破万法】"
      : "【翻天覆地·不周残岳镇】";
  }
}

const sword = new NatalArtifact("诛仙青云剑", NatalVesselType.FlyingSword);
assert(sword.tier === 1 && sword.starLevel === 0, "法宝初始为 1阶0星");

// 精血祭炼
sword.cultivateWithBlood(0.5);
assert(!sword.isAwakened && sword.bloodBond === 0.5, "精血契合度正常累积 (未达到80%暂未觉醒)");
const awakened = sword.cultivateWithBlood(0.35);
assert(awakened && sword.isAwakened, "契合度达 85% 成功触发器灵初次觉醒！");

// 异火淬炼升星到 10 星
for (let i = 0; i < 10; i++) {
  sword.upgradeStar();
}
assert(sword.starLevel === 10, "本命法宝升星至 10星满阶");
assert(!sword.upgradeStar(), "满星后无法再次升星");

// 突破到 2 阶 (玩家需达到筑基/金丹以上境界，如 Realm = 2)
assert(sword.advanceTier(2), "玩家境界达标，本命法宝顺利突破晋升至 2阶！");
assert(sword.tier === 2 && sword.starLevel === 0, "破阶后阶位+1，星级重置为0星重新洗练");
assert(sword.bonusAttack > 200, "本命法宝对角色攻伐数值加成随阶位健康倍增");
assert(sword.getSkillName().includes("万剑归宗"), "飞剑专属神威大招配置无误");

// ========================
// 2. 剧情分支树、因果善恶与终局判定测试
// ========================
console.log("\n--- 模块 2: 剧情分支树、山海图志收录与四重终局测试 ---");

enum GameEndingType {
  FengShenYongChang = 1,
  ChongLiDiXian = 2,
  TaiChuPoXu = 3,
  WanJieJuMie = 4,
}

class NarrativeEngine {
  karma = 0;
  flags = new Set<string>();
  records = new Map<string, string>();

  applyKarma(delta: number) {
    this.karma = Math.max(-10000, Math.min(10000, this.karma + delta));
  }

  setFlag(flag: string) {
    this.flags.add(flag);
  }

  unlockRecord(id: string, text: string) {
    this.records.set(id, text);
  }

  evaluateEnding(): GameEndingType {
    if (this.flags.has("DESTROY_HEAVEN_AND_EARTH") || this.karma <= -7000) {
      return GameEndingType.WanJieJuMie;
    }
    if (this.flags.has("ASCEND_TO_VOID_CHAOS") && this.karma > -2000 && this.karma < 2000) {
      return GameEndingType.TaiChuPoXu;
    }
    if (this.flags.has("RESTORE_EARTH_REINCARNATION") || (this.karma >= 3000 && this.flags.has("DEFEND_DI_DAO"))) {
      return GameEndingType.ChongLiDiXian;
    }
    if (this.karma >= 5000 && this.flags.has("UPHOLD_HEAVEN_ORDER")) {
      return GameEndingType.FengShenYongChang;
    }
    return this.karma >= 0 ? GameEndingType.ChongLiDiXian : GameEndingType.WanJieJuMie;
  }
}

const narrative = new NarrativeEngine();
narrative.unlockRecord("shan_hai_01", "昆仑玉虚宫古石刻");
assert(narrative.records.size === 1, "山海图志博物古籍碎片成功解锁收录");

// 善业线测试：拥护地道轮回
narrative.applyKarma(3500);
narrative.setFlag("DEFEND_DI_DAO");
assert(narrative.evaluateEnding() === GameEndingType.ChongLiDiXian, "因果向善且守护地道轮回，成功通向【重立地仙】终局！");

// 恶业毁灭线测试
const darkNarrative = new NarrativeEngine();
darkNarrative.applyKarma(-8000);
assert(darkNarrative.evaluateEnding() === GameEndingType.WanJieJuMie, "业障极深引动地水火风，成功触发【万劫俱灭】终局！");

// 超脱混沌线测试
const voidNarrative = new NarrativeEngine();
voidNarrative.setFlag("ASCEND_TO_VOID_CHAOS");
assert(voidNarrative.evaluateEnding() === GameEndingType.TaiChuPoXu, "不染红尘因果、斩断万古宿命，成功达成【太初破虚】超脱终局！");

// ========================
// 3. 核心战斗双曲伤害与防御防爆表测试
// ========================
console.log("\n--- 模块 3: 双曲减伤防御无溢出极限压测 ---");
function defReduction(def: number): number {
  const defConst = 800.0;
  return def / (defConst + def);
}

assert(defReduction(0) === 0, "0点防御无减伤");
assert(defReduction(800) === 0.5, "800点防御提供 50% 减伤");
assert(defReduction(8000) < 1.0 && defReduction(8000) > 0.9, "8000点超高防御减伤趋近 90.9%，不爆表溢出");
assert(defReduction(10000000) < 1.0, "千亿级极端数值防御始终渐进趋近 1.0，杜绝负伤与除零溢出");

console.log("\n\x1b[32m>>> 全系统集成与回归测试 100% 全部通过 (ALL GREEN)！<<<\x1b[0m");
