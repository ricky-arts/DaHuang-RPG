package com.honghuang.rpg;

public class MapLocation {
    public String id;
    public String name;
    public String zone;
    public String description;
    public String dangerTier;
    public String bossName;
    public String bossElement;
    public int bossHp;
    public int bossAtk;
    public int bossDef;
    public int expReward;
    public boolean isUnlocked;

    public MapLocation(String id, String name, String zone, String desc, String danger, String boss, String elem, int hp, int atk, int def, int exp, boolean unlocked) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.description = desc;
        this.dangerTier = danger;
        this.bossName = boss;
        this.bossElement = elem;
        this.bossHp = hp;
        this.bossAtk = atk;
        this.bossDef = def;
        this.expReward = exp;
        this.isUnlocked = unlocked;
    }

    public static MapLocation[] getDefaultLocations() {
        return new MapLocation[] {
            new MapLocation(
                "loc_zhongnan",
                "终南山·青莲仙脉",
                "南瞻部洲",
                "云海翻涌、灵峰奇拔。开天辟地后灵气充沛之地，隐伏着吸收怨煞的阴灵与地精，适宜初窥仙门之修真者历练。",
                "【入门 · 练气境】",
                "九幽阴煞残魂",
                "水",
                1200, 110, 70,
                300,
                true
            ),
            new MapLocation(
                "loc_buzhou",
                "不周山·盘古天柱残址",
                "大荒中央天极",
                "天柱倾折，四极废，九州裂。断岳绝壁间充斥着巫妖大战遗留的上古凶煞罡风，寻常金丹触之即碎，极度危险！",
                "【险恶 · 筑基/金丹境】",
                "裂空金翅大鹏分身",
                "金",
                3800, 260, 160,
                800,
                false
            ),
            new MapLocation(
                "loc_donghai",
                "东海·极深古龙墟",
                "四海汪洋之极",
                "万丈海眼深处的封神战场废墟，万仙阵遗落的残阵暗涌激荡，盘踞着吞吐海啸烈焰的上古凶兽。",
                "【凶险 · 元婴大成境】",
                "赤火覆海恶蛟",
                "火",
                8500, 420, 240,
                2000,
                false
            ),
            new MapLocation(
                "loc_xuehai",
                "幽冥血海·六道轮回之畔",
                "九幽无间之渊",
                "血海不干，杀伐无尽。阿修罗族与地狱恶鬼横行之所，天道神算不可触及之地，稍有不慎身死道消！",
                "【绝境 · 渡劫真仙境】",
                "幽冥修罗战神投影",
                "土",
                18000, 850, 480,
                5000,
                false
            )
        };
    }
}
