package com.honghuang.rpg;

public class MonsterEntity {
    public enum MonsterTier {
        NORMAL("普通", "#95A5A6", 1.0f, 0),
        ELITE("精英", "#3498DB", 1.25f, 50),
        BOSS("首领", "#E74C3C", 1.55f, 100);

        public final String title;
        public final String colorHex;
        public final float scale;
        public final int baseTenacity;

        MonsterTier(String title, String colorHex, float scale, int tenacity) {
            this.title = title;
            this.colorHex = colorHex;
            this.scale = scale;
            this.baseTenacity = tenacity;
        }
    }

    public String name;
    public MonsterTier tier;
    public String element; // 金、木、水、火、土
    public int maxHp;
    public int curHp;
    public int attack;
    public int defense;
    public int maxTenacity;
    public int curTenacity;
    public boolean isBroken = false;
    public int expReward;
    public String specialAffix; // 特殊词缀，如【嗜血】、【反震】、【护体】

    public MonsterEntity(String name, MonsterTier tier, String element, int hp, int atk, int def, int exp, String affix) {
        this.name = name;
        this.tier = tier;
        this.element = element;
        this.maxHp = hp;
        this.curHp = hp;
        this.attack = atk;
        this.defense = def;
        this.maxTenacity = tier.baseTenacity;
        this.curTenacity = tier.baseTenacity;
        this.expReward = exp;
        this.specialAffix = affix;
    }

    public static MonsterEntity[] getAreaMonsters(String mapId) {
        if ("loc_buzhou".equals(mapId)) {
            return new MonsterEntity[] {
                new MonsterEntity("不周裂地蛮猿", MonsterTier.NORMAL, "土", 2200, 180, 120, 320, "普通凶兽"),
                new MonsterEntity("金羽裂空雕·狂暴", MonsterTier.ELITE, "金", 4500, 260, 180, 750, "【狂暴】攻伐暴增"),
                new MonsterEntity("裂空金翅大鹏分身", MonsterTier.BOSS, "金", 9800, 380, 260, 2200, "【金煞领域】反弹15%伤害")
            };
        } else if ("loc_donghai".equals(mapId)) {
            return new MonsterEntity[] {
                new MonsterEntity("东海巡海夜叉", MonsterTier.NORMAL, "水", 5000, 320, 220, 800, "普通海精"),
                new MonsterEntity("覆海巡渊玄龟·重铠", MonsterTier.ELITE, "水", 9500, 390, 380, 1800, "【玄甲】大幅减伤"),
                new MonsterEntity("赤火覆海恶蛟·灭世", MonsterTier.BOSS, "火", 19500, 560, 420, 4800, "【火神怒】狂暴蓄力秒杀")
            };
        } else if ("loc_xuehai".equals(mapId)) {
            return new MonsterEntity[] {
                new MonsterEntity("血海阿修罗鬼卒", MonsterTier.NORMAL, "火", 9000, 520, 360, 1500, "幽冥卒兵"),
                new MonsterEntity("幽冥罗刹恶将·嗜血", MonsterTier.ELITE, "木", 16000, 680, 480, 3500, "【吸髓】攻击吸血"),
                new MonsterEntity("幽冥修罗战神投影·寂灭", MonsterTier.BOSS, "土", 38000, 950, 680, 10000, "【修罗真身】三头六臂破韧无常")
            };
        } else {
            // 终南山默认怪物
            return new MonsterEntity[] {
                new MonsterEntity("终南山青狐精", MonsterTier.NORMAL, "木", 850, 85, 45, 150, "寻常精怪"),
                new MonsterEntity("千年树妖守卫·坚韧", MonsterTier.ELITE, "木", 1850, 130, 85, 380, "【生生不息】每秒回血"),
                new MonsterEntity("九幽阴煞残魂·暴怒", MonsterTier.BOSS, "水", 3600, 190, 120, 950, "【阴煞噬魄】全屏范围冻结")
            };
        }
    }
}
