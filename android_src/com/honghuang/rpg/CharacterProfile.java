package com.honghuang.rpg;

public class CharacterProfile {
    public String id;
    public String name;
    public String title;
    public String element;
    public String weapon;
    public String lore;
    public int baseHp;
    public int baseAtk;
    public int baseDef;
    public String svgAvatar;

    public CharacterProfile(String id, String name, String title, String element, String weapon, String lore, int hp, int atk, int def, String svgAvatar) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.element = element;
        this.weapon = weapon;
        this.lore = lore;
        this.baseHp = hp;
        this.baseAtk = atk;
        this.baseDef = def;
        this.svgAvatar = svgAvatar;
    }

    public static CharacterProfile[] getProfiles() {
        return new CharacterProfile[] {
            new CharacterProfile(
                "char_sword",
                "太微",
                "青莲剑仙 · 傲骨道种",
                "木灵根 (金气内敛)",
                "太初辟易剑",
                "混沌青莲瓣化生的无双剑修。踏歌行万里，一剑荡九幽。擅长绝高暴击与疾风破军突刺，剑气如青莲盛绽。",
                1600, 240, 110,
                "sword"
            ),
            new CharacterProfile(
                "char_fox",
                "望舒",
                "青丘狐仙 · 遁天灵姬",
                "水火双生 (阴阳变幻)",
                "太虚惑心铃",
                "纯血九尾天狐帝姬。精通大荒奇门遁甲与九尾幻术，身姿绰约灵动。擅长全屏法术轰炸、定身打断与护体灵盾。",
                1350, 280, 85,
                "fox"
            ),
            new CharacterProfile(
                "char_titan",
                "断岳",
                "荒古战尊 · 盘古遗民",
                "土灵根 (戊土煞体)",
                "开山辟地神斧",
                "继承十二祖巫玄金重煞的狂蛮战狂。以身作盾，撼天动地。拥有傲视天地的极高气血与霸体破甲，逆风肉身成圣。",
                2200, 190, 180,
                "titan"
            )
        };
    }
}
