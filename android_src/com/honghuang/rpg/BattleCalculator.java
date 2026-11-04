package com.honghuang.rpg;

import java.util.Random;

public class BattleCalculator {
    public static final double DEF_CONSTANT = 600.0; // 双曲减伤分母常数
    private static final Random rng = new Random();

    public static class HitResult {
        public int finalDamage;
        public boolean isCrit;
        public boolean isDodged; // 是否被闪避
        public double elemModifier;
        public String elemDescription;
        public int tenacityDamage;
        public String statusEffect = ""; // 概率附加状态: "【冰冻】", "【灼伤】", "【撕裂】"
        public String randomFluctuationText = ""; // 波动说明
    }

    /**
     * 五行相克系数计算：
     * 金克木，木克土，土克水，水克火，火克金
     */
    public static double getElementModifier(String atkElem, String defElem) {
        if (atkElem == null || defElem == null) return 1.0;
        atkElem = atkElem.trim();
        defElem = defElem.trim();

        boolean isAdv = ("金".equals(atkElem) && "木".equals(defElem)) ||
                        ("木".equals(atkElem) && "土".equals(defElem)) ||
                        ("土".equals(atkElem) && "水".equals(defElem)) ||
                        ("水".equals(atkElem) && "火".equals(defElem)) ||
                        ("火".equals(atkElem) && "金".equals(defElem));

        if (isAdv) return 1.35; // 克制增伤 35%

        boolean isDis = ("木".equals(atkElem) && "金".equals(defElem)) ||
                        ("土".equals(atkElem) && "木".equals(defElem)) ||
                        ("水".equals(atkElem) && "土".equals(defElem)) ||
                        ("火".equals(atkElem) && "水".equals(defElem)) ||
                        ("金".equals(atkElem) && "火".equals(defElem));

        if (isDis) return 0.75; // 被克减伤 25%

        return 1.0;
    }

    /**
     * 具备真随机概率波动 (±15% 伤害浮动、命中/闪避判定、概率暴击与异常附着) 的真实属性伤害引擎
     */
    public static HitResult calculateDamageWithRng(
        int attackerAtk,
        double skillRate,
        String skillElem,
        int defenderDef,
        String defenderElem,
        boolean isDefenderBroken,
        double critChance,
        double critMultiplier,
        double defenderDodgeChance
    ) {
        HitResult result = new HitResult();

        // 1. 闪避概率判定 (Dodge Check)
        if (defenderDodgeChance > 0 && rng.nextDouble() < defenderDodgeChance) {
            result.isDodged = true;
            result.finalDamage = 0;
            result.tenacityDamage = 0;
            result.elemDescription = "【灵巧身法·完美闪避 MISSED】";
            return result;
        }

        // 2. 双曲减伤率：Reduction = Def / (DefConstant + Def)
        double defReduction = (double)defenderDef / (DEF_CONSTANT + defenderDef);
        double baseDmg = (double)attackerAtk * skillRate * (1.0 - defReduction);

        // 3. 真实数值随机波动率 (Random Fluctuation: 0.88 ~ 1.15)
        double fluctuation = 0.88 + (rng.nextDouble() * 0.27);
        baseDmg *= fluctuation;

        // 4. 五行生克属性判定
        result.elemModifier = getElementModifier(skillElem, defenderElem);
        if (result.elemModifier > 1.1) {
            result.elemDescription = "【属性克制 +35%】";
        } else if (result.elemModifier < 0.9) {
            result.elemDescription = "【属性被克 -25%】";
        } else {
            result.elemDescription = "";
        }

        // 5. 暴击概率判定 (Crit Check)
        result.isCrit = rng.nextDouble() < critChance;
        double effectiveCritMult = result.isCrit ? Math.max(1.65, critMultiplier) : 1.0;

        // 6. 破韧虚弱加成 (处于 Break 状态额外增伤 50%)
        double breakMultiplier = isDefenderBroken ? 1.50 : 1.0;

        // 7. 最终生命值扣减真实伤害
        double rawDmg = baseDmg * result.elemModifier * effectiveCritMult * breakMultiplier;
        result.finalDamage = Math.max(1, (int)Math.round(rawDmg));

        // 8. 削减定力韧性 (带随机浮动)
        int baseTenDmg = (int)(skillRate * 25);
        result.tenacityDamage = (int)(baseTenDmg * (0.9 + rng.nextDouble() * 0.25));

        // 9. 概率异常状态随机赋予 (30% 几率附加五行特殊异常)
        if (rng.nextDouble() < 0.30) {
            if ("水".equals(skillElem)) {
                result.statusEffect = "【玄冰寒气·冻结迟滞】";
            } else if ("火".equals(skillElem)) {
                result.statusEffect = "【三昧真火·熔甲灼烧】";
            } else if ("木".equals(skillElem)) {
                result.statusEffect = "【青莲剑煞·破甲撕裂】";
            } else if ("金".equals(skillElem)) {
                result.statusEffect = "【庚金锐气·定力重创】";
                result.tenacityDamage += 20;
            } else if ("土".equals(skillElem)) {
                result.statusEffect = "【戊土沉岳·重力封印】";
            }
        }

        return result;
    }
}
