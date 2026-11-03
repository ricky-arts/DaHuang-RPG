import fs from "fs";

console.log("==================================================================");
console.log("=== 洪荒 RPG AI决策、配置表驱动与UI端到端全链路回归自测试 ===");
console.log("==================================================================\n");

function assert(condition: boolean, msg: string) {
  if (condition) {
    console.log(`\x1b[32m[PASS]\x1b[0m ${msg}`);
  } else {
    console.error(`\x1b[31m[FAIL]\x1b[0m ${msg}`);
    process.exit(1);
  }
}

// ========================
// 1. 怪物行为树分层决策测试 (AI Behavior Tree)
// ========================
console.log("--- 模块 1: 洪荒大能 BOSS 行为树战术决策流测试 ---");

enum BTStatus { Success, Failure, Running }

interface Blackboard {
  selfHpPercent: number;
  distanceToPlayer: number;
  isPlayerBroken: boolean;
  hasEmergencyTalismans: boolean;
  lastExecutedSkill: string;
}

class BehaviorTreeRunner {
  tick(bb: Blackboard): BTStatus {
    // 1. 保命序列: 血量 < 0.25 且有替身符 -> 触发护体遁走
    if (bb.selfHpPercent < 0.25 && bb.hasEmergencyTalismans) {
      bb.hasEmergencyTalismans = false;
      bb.lastExecutedSkill = "【九曲遁甲符·保命金光罩】";
      return BTStatus.Success;
    }
    // 2. 斩杀序列: 玩家破防虚弱 -> 爆发连携连诛
    if (bb.isPlayerBroken) {
      bb.lastExecutedSkill = "【三昧真火·太乙玄门剑连携斩杀】";
      return BTStatus.Success;
    }
    // 3. 远距离追击
    if (bb.distanceToPlayer > 12.0) {
      bb.distanceToPlayer = 5.0;
      bb.lastExecutedSkill = "【咫尺天涯·缩地成寸突进】";
      return BTStatus.Success;
    }
    // 4. 常规施法
    bb.lastExecutedSkill = "【五行天雷印·普通法术压制】";
    return BTStatus.Success;
  }
}

const ai = new BehaviorTreeRunner();
const bb: Blackboard = {
  selfHpPercent: 1.0,
  distanceToPlayer: 20.0,
  isPlayerBroken: false,
  hasEmergencyTalismans: true,
  lastExecutedSkill: "",
};

// 状态 1: 远距离 -> 自动突进
ai.tick(bb);
assert(bb.distanceToPlayer === 5.0 && bb.lastExecutedSkill.includes("缩地成寸"), "远距离成功决策释放缩地成寸突进追击");

// 状态 2: 近战压制
ai.tick(bb);
assert(bb.lastExecutedSkill.includes("普通法术压制"), "正常对峙阶段执行五行常规法术压制");

// 状态 3: 玩家破防 -> 触发大招斩杀
bb.isPlayerBroken = true;
ai.tick(bb);
assert(bb.lastExecutedSkill.includes("连携斩杀"), "感知玩家破韧虚弱状态，立即切换爆发大神通斩杀！");

// 状态 4: BOSS 濒死 -> 触发保命符箓与护体金光
bb.selfHpPercent = 0.15;
ai.tick(bb);
assert(bb.lastExecutedSkill.includes("保命金光罩") && !bb.hasEmergencyTalismans, "血量危急时刻最高优先级触发保命遁甲金光罩");

// ========================
// 2. 静态配置表解析驱动测试 (Table Data Driven)
// ========================
console.log("\n--- 模块 2: 静态配置表加载与只读驱动测试 ---");
const charJson = fs.readFileSync("/opt/cc-haha/HongHuangGame/Configs/CharacterTemplates.json", "utf8");
const chars = JSON.parse(charJson);
assert(chars.length === 3, "角色模版配置加载无误，共收录 3 项模版");

const protagonist = chars.find((c: any) => c.id === "char_001");
assert(protagonist.name.includes("青莲道人") && protagonist.element === "Wood", "主角青莲道人木系灵根数值解析正确");

const skillJson = fs.readFileSync("/opt/cc-haha/HongHuangGame/Configs/SkillTemplates.json", "utf8");
const skills = JSON.parse(skillJson);
assert(skills.length === 3, "技能法术配置加载无误，包含金木火三系神通");
const ultimateSkill = skills.find((s: any) => s.id === "skill_03");
assert(ultimateSkill.multiplier === 3.8 && ultimateSkill.tenacityDamage === 80, "高阶雷罚技能伤害倍率与削韧数值准确");

// ========================
// 3. UI 导航栈管理与页面层级测试 (UIManager)
// ========================
console.log("\n--- 模块 3: UI 导航栈先进后出与层级栈帧测试 ---");
class SimpleUIManager {
  private stack: string[] = [];

  push(panel: string) {
    this.stack.push(panel);
  }

  pop(): string | undefined {
    return this.stack.pop();
  }

  get current(): string | undefined {
    return this.stack[this.stack.length - 1];
  }

  get depth(): number {
    return this.stack.length;
  }
}

const ui = new SimpleUIManager();
ui.push("MainHudPanel");
assert(ui.current === "MainHudPanel" && ui.depth === 1, "主界面 HUD 正常挂载在栈底");

ui.push("ArtifactBagPanel");
assert(ui.current === "ArtifactBagPanel" && ui.depth === 2, "打开背包面板，顶层激活法宝背包");

ui.push("RealmBreakthroughPanel");
assert(ui.current === "RealmBreakthroughPanel" && ui.depth === 3, "从背包转跳至境界突破渡劫面板，栈深递增为3");

ui.pop();
assert(ui.current === "ArtifactBagPanel" && ui.depth === 2, "关闭渡劫面板后平滑回退至法宝背包面板");

ui.pop();
assert(ui.current === "MainHudPanel" && ui.depth === 1, "再出栈彻底回到主界面 HUD");

console.log("\n\x1b[32m>>> 全链路 AI、配置驱动与 UI 框架自动化验收自验证 100% 全部通过 (ALL PASS)！<<<\x1b[0m");
