package com.honghuang.rpg;

public class ChessEquipment {
    public enum EquipType {
        // 基础散件
        SHEN_TIAN_JIAN("青萍灵剑", "攻伐+25, 暴击+15%", 25, 0, 0, 15, 0, false),
        XUAN_WU_JIA("玄武厚甲", "气血+300, 护体+30", 0, 30, 300, 0, 0, false),
        TAI_YI_FU("太乙神符", "初始灵气+25, 法强+20%", 0, 0, 0, 0, 25, false),
        HOU_TU_JING("厚土晶石", "气血+400, 受到伤害反震15%", 0, 15, 400, 0, 0, false),

        // 合成神级法宝
        ZHU_XIAN_JIAN("【神兵】诛仙古剑", "青萍剑+青萍剑: 攻伐+80, 暴击+35%, 普攻附加目标最大气血8%真实伤害", 80, 0, 0, 35, 0, true),
        FAN_TIAN_YIN("【法宝】番天宝印", "灵剑+玄武甲: 攻伐+40, 护体+50, 释放技能粉碎全场敌方50点定力韧性", 40, 50, 200, 10, 0, true),
        DING_HAI_ZHU("【后天至宝】定海神珠", "神符+灵剑: 初始灵气+40, 普攻额外回蓝15点, 技能附带冻结", 35, 0, 0, 10, 40, true),
        TAI_JI_TU("【混沌至宝】太极神图", "玄武甲+厚土晶: 气血+900, 护体+80, 获得30%绝对免伤与免控", 0, 80, 900, 0, 0, true);

        public final String name;
        public final String description;
        public final int bonusAtk;
        public final int bonusDef;
        public final int bonusHp;
        public final int bonusCrit;
        public final int bonusMana;
        public final boolean isCombined;

        EquipType(String name, String desc, int atk, int def, int hp, int crit, int mana, boolean combined) {
            this.name = name;
            this.description = desc;
            this.bonusAtk = atk;
            this.bonusDef = def;
            this.bonusHp = hp;
            this.bonusCrit = crit;
            this.bonusMana = mana;
            this.isCombined = combined;
        }
    }

    public EquipType type;

    public ChessEquipment(EquipType type) {
        this.type = type;
    }
}
