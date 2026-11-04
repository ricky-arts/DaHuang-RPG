package com.honghuang.rpg;

public class BattleCalculator {
    public static final double DEF_CONSTANT = 600.0; // 双曲减伤分母常数

    public static class HitResult {
        public int finalDamage;
        public boolean isCrit;
        public double elemModifier;
        public String elemDescription;
        public int tenacityDamage;
        public boolean triggeredBreak;
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
     * 严谨双曲渐近减伤与真实属性伤害计算公式
     */
    public static HitResult calculateDamage(
        int attackerAtk,
        double skillRate,
        String skillElem,
        int defenderDef,
        String defenderElem,
        boolean isDefenderBroken,
        double critChance
    ) {
        HitResult result = new HitResult();

        // 1. 双曲减伤率：Reduction = Def / (DefConstant + Def)
        double defReduction = (double)defenderDef / (DEF_CONSTANT + defenderDef);
        double baseDmg = (double)attackerAtk * skillRate * (1.0 - defReduction);

        // 2. 五行生克属性判定
        result.elemModifier = getElementModifier(skillElem, defenderElem);
        if (result.elemModifier > 1.1) {
            result.elemDescription = "【五行生克·属性克制 +35%】";
        } else if (result.elemModifier < 0.9) {
            result.elemDescription = "【五行逆乱·属性被克 -25%】";
        } else {
            result.elemDescription = "";
        }

        // 3. 暴击判定
        result.isCrit = Math.random() < critChance;
        double critMultiplier = result.isCrit ? 1.65 : 1.0;

        // 4. 破韧虚弱加成 (处于 Break 状态额外增伤 50%)
        double breakMultiplier = isDefenderBroken ? 1.50 : 1.0;

        // 5. 最终生命值扣减真实伤害
        double rawDmg = baseDmg * result.elemModifier * critMultiplier * breakMultiplier;
        result.finalDamage = Math.max(1, (int)Math.round(rawDmg));

        // 6. 定力韧性削减
        result.tenacityDamage = (int)(skillRate * 25);

        return result;
    }
}
