package com.honghuang.rpg;

/**
 * 自走棋双轨伤害计算引擎：
 * 1. 物理伤害：基于物理抗性双曲减伤：Reduction = Armor / (100 + Armor)
 * 2. 法术伤害：基于法术抗性双曲减伤：Reduction = MR / (100 + MR) * (AP% / 100)
 * 3. 真实伤害：无视双抗直接扣减
 */
public class ChessCombatFormulas {

    public enum DamageType {
        PHYSICAL, // 物理伤害
        MAGIC,    // 法术伤害
        TRUE_DMG  // 真实伤害
    }

    /**
     * 计算单次攻击或技能真实扣血
     */
    public static int calculateDamage(
        DamageType type,
        int rawAmount,
        int targetArmor,
        int targetMagicResist,
        int attackerApPercent,
        boolean isCrit
    ) {
        double finalDmg = rawAmount;

        if (type == DamageType.PHYSICAL) {
            // 物理双曲抗性减伤
            double reduction = (double)targetArmor / (100.0 + targetArmor);
            finalDmg = rawAmount * (1.0 - reduction);
        } else if (type == DamageType.MAGIC) {
            // 法术强度加成与法抗双曲减伤
            double apMultiplier = (double)attackerApPercent / 100.0;
            double reduction = (double)targetMagicResist / (100.0 + targetMagicResist);
            finalDmg = (rawAmount * apMultiplier) * (1.0 - reduction);
        } else {
            // 真实伤害
            finalDmg = rawAmount;
        }

        // 暴击加成 (默认 1.5 倍)
        if (isCrit) {
            finalDmg *= 1.5;
        }

        return Math.max(1, (int)Math.round(finalDmg));
    }
}
