package com.honghuang.rpg;

/**
 * 纯正金铲铲之战标准装备体系：
 * 8大基础散件 (Component) -> 两两合成经典大件装备 (Completed Items)
 */
public class TFTItem {
    public enum ItemID {
        // 8 大基础散件 (金铲铲标准散件)
        BF_SWORD("青铜剑 (暴风大剑)", "+10 物理攻击", 10, 0, 0, 0, 0, 0, false),
        RECURVE_BOW("射日弓 (反曲之弓)", "+10% 攻击速度", 0, 0, 0, 10, 0, 0, false),
        NEEDLESS_ROD("火龙杖 (无用大棒)", "+10 法术强度", 0, 0, 0, 0, 10, 0, false),
        TEAR("太虚泪 (女神之泪)", "+15 初始法力值", 0, 0, 0, 0, 0, 15, false),
        CHAIN_VEST("玄龟甲 (锁子甲)", "+20 物理护甲", 0, 20, 0, 0, 0, 0, false),
        NEGATRON_CLOAK("辟邪斗篷 (负极斗篷)", "+20 魔法抗性", 0, 0, 20, 0, 0, 0, false),
        GIANTS_BELT("气血腰带 (巨人腰带)", "+150 生命上限", 0, 0, 0, 0, 0, 0, false),
        SPARRING_GLOVES("乾坤拳套 (拳套)", "+10% 暴击率, +10% 闪避率", 0, 0, 0, 0, 0, 0, false),

        // 代表性大件合成装备 (Completed Items)
        INFINITY_EDGE("诛仙刃 (无尽之刃)", "大剑+拳套: 暴击率+35%, 技能可产生暴击", 35, 0, 0, 0, 0, 0, true),
        GUINSOO_RAGEBLADE("纯阳狂暴剑 (鬼索羊刀)", "大棒+反曲弓: 每次普攻无限叠加 5% 攻速", 10, 0, 0, 15, 15, 0, true),
        BLUE_BUFF("造化蓝霸符 (蓝Buff)", "眼泪+眼泪: 最大法力值减少10点, 施法后瞬间回蓝20点", 0, 0, 0, 0, 10, 30, true),
        WARMOG_ARMOR("盘古玄躯 (狂徒铠甲)", "腰带+腰带: 生命值巨额+800, 每秒回复自身1%最大生命值", 0, 0, 0, 0, 0, 0, true),
        SUNFIRE_CAPE("日炎战甲 (日炎斗篷)", "锁子甲+腰带: 护甲+30, 生命+250, 周围敌人持续灼烧与重伤减疗", 0, 30, 0, 0, 0, 0, true),
        STATIKK_SHIV("雷灵电刃 (斯塔缇克电刃)", "反曲弓+眼泪: 每3次普攻弹射天雷, 削减4名敌人30%魔抗", 0, 0, 0, 15, 0, 15, true),
        DEATHBLADE("灭世神剑 (死亡之刃)", "大剑+大剑: 攻击力大幅提升+55, 击杀敌人获得滚雪球攻伐加成", 55, 0, 0, 0, 0, 0, true),
        DRAGON_CLAW("祖龙逆鳞 (巨龙之爪)", "斗篷+斗篷: 魔抗+65, 受到法术伤害大幅减免并周期性治疗", 0, 0, 65, 0, 0, 0, true);

        public final String name;
        public final String description;
        public final int bonusAtk;
        public final int bonusArmor;
        public final int bonusMR;
        public final int bonusAtkSpeed; // %
        public final int bonusAP;       // %
        public final int bonusMana;
        public final boolean isCompleted;

        ItemID(String name, String desc, int atk, int armor, int mr, int spd, int ap, int mana, boolean comp) {
            this.name = name;
            this.description = desc;
            this.bonusAtk = atk;
            this.bonusArmor = armor;
            this.bonusMR = mr;
            this.bonusAtkSpeed = spd;
            this.bonusAP = ap;
            this.bonusMana = mana;
            this.isCompleted = comp;
        }
    }

    public ItemID id;

    public TFTItem(ItemID id) {
        this.id = id;
    }

    /**
     * 金铲铲核心两两散件合成配方逻辑
     */
    public static TFTItem combine(TFTItem a, TFTItem b) {
        if (a == null || b == null) return null;
        if (a.id.isCompleted || b.id.isCompleted) return null;

        ItemID idA = a.id;
        ItemID idB = b.id;

        // 大剑 + 拳套 = 无尽之刃
        if ((idA == ItemID.BF_SWORD && idB == ItemID.SPARRING_GLOVES) ||
            (idB == ItemID.BF_SWORD && idA == ItemID.SPARRING_GLOVES)) {
            return new TFTItem(ItemID.INFINITY_EDGE);
        }
        // 大棒 + 反曲弓 = 鬼索狂暴之刃
        if ((idA == ItemID.NEEDLESS_ROD && idB == ItemID.RECURVE_BOW) ||
            (idB == ItemID.NEEDLESS_ROD && idA == ItemID.RECURVE_BOW)) {
            return new TFTItem(ItemID.GUINSOO_RAGEBLADE);
        }
        // 眼泪 + 眼泪 = 蓝霸符
        if (idA == ItemID.TEAR && idB == ItemID.TEAR) {
            return new TFTItem(ItemID.BLUE_BUFF);
        }
        // 腰带 + 腰带 = 狂徒铠甲
        if (idA == ItemID.GIANTS_BELT && idB == ItemID.GIANTS_BELT) {
            return new TFTItem(ItemID.WARMOG_ARMOR);
        }
        // 锁子甲 + 腰带 = 日炎斗篷
        if ((idA == ItemID.CHAIN_VEST && idB == ItemID.GIANTS_BELT) ||
            (idB == ItemID.CHAIN_VEST && idA == ItemID.GIANTS_BELT)) {
            return new TFTItem(ItemID.SUNFIRE_CAPE);
        }
        // 反曲弓 + 眼泪 = 电刃
        if ((idA == ItemID.RECURVE_BOW && idB == ItemID.TEAR) ||
            (idB == ItemID.RECURVE_BOW && idA == ItemID.TEAR)) {
            return new TFTItem(ItemID.STATIKK_SHIV);
        }
        // 大剑 + 大剑 = 死亡之刃
        if (idA == ItemID.BF_SWORD && idB == ItemID.BF_SWORD) {
            return new TFTItem(ItemID.DEATHBLADE);
        }
        // 斗篷 + 斗篷 = 巨龙之爪
        if (idA == ItemID.NEGATRON_CLOAK && idB == ItemID.NEGATRON_CLOAK) {
            return new TFTItem(ItemID.DRAGON_CLAW);
        }

        // 默认合成日炎或无尽通用大件
        return new TFTItem(ItemID.INFINITY_EDGE);
    }
}
