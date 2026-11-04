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
    public double baseCritChance; // 初始暴击率差异
    public double baseDodgeChance;// 初始闪避率差异
    public String passiveSkill;   // 专属被动
    public String[] skillNames;   // 专属技能列表 (普攻/技能/大招)
    public String[] skillDescs;
    public String visualTheme;    // 专属视觉主题 (SWORD, FOX, TITAN)

    public CharacterProfile(
        String id, String name, String title, String element, String weapon, String lore,
        int hp, int atk, int def, double crit, double dodge, String passive,
        String[] skillNames, String[] skillDescs, String visualTheme
    ) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.element = element;
        this.weapon = weapon;
        this.lore = lore;
        this.baseHp = hp;
        this.baseAtk = atk;
        this.baseDef = def;
        this.baseCritChance = crit;
        this.baseDodgeChance = dodge;
        this.passiveSkill = passive;
        this.skillNames = skillNames;
        this.skillDescs = skillDescs;
        this.visualTheme = visualTheme;
    }

    public static CharacterProfile[] getProfiles() {
        return new CharacterProfile[] {
            // 1. 太微 - 飘逸高暴刺客剑修 (木系金煞，御剑疾风)
            new CharacterProfile(
                "char_sword",
                "太微",
                "青莲剑仙 · 傲骨道种",
                "木",
                "太初辟易剑",
                "混沌青莲瓣化生之剑道极境。剑意无双，踏空而行，身周青莲剑气时刻呼啸流转。追求一剑破万法的极致暴击与极速突刺。",
                1500, 260, 95,
                0.25, // 25% 超高初始暴击率
                0.15, // 15% 身法闪避率
                "【剑心通明】暴击伤害恒定提升至 200%，暴击时 30% 概率刷新神通冷却！",
                new String[] { "【流光飞剑决】", "【青莲破军刺】", "【诛仙万剑归宗】" },
                new String[] {
                    "御剑破空，向前飞掠贯穿敌人，造成 1.1x 伤害",
                    "身剑合一瞬移突进，必定削减 50 点定力并有 35% 几率附加裂伤",
                    "诛仙古阵降临，万剑齐发，造成 3.8x 伤害并附加【禁疗】"
                },
                "SWORD"
            ),
            // 2. 望舒 - 灵动群伤控场狐仙 (水火变幻，九尾护体)
            new CharacterProfile(
                "char_fox",
                "望舒",
                "青丘狐仙 · 遁天灵姬",
                "水",
                "太虚惑心铃",
                "轩辕坟与青丘纯血九尾帝姬。身披朱红霞帔，背后九尾红云灵弧流转。擅长大荒奇门幻术，水火相生，能惑乱妖邪并形成灵狐护体障。",
                1300, 310, 80,
                0.12, // 12% 暴击
                0.22, // 22% 极高身法闪避
                "【九尾天狐障】受到致命伤害时有 25% 几率化为幻影完全免疫，且每次施法为自身附加 150 点气血护盾！",
                new String[] { "【灵狐吐纳珠】", "【太虚惑心咒】", "【九幽红莲幻灭引】" },
                new String[] {
                    "祭出狐族本命妖丹法球，远距离轰击造成 1.2x 水系伤害",
                    "摇动摄魂古铃，造成 1.7x 伤害并使敌方陷入【眩晕混乱】无法反击 1 回合",
                    "九尾齐展召唤红莲业火，造成 3.5x 全屏水火交融轰炸，概率触发【冰冻】或【灼伤】"
                },
                "FOX"
            ),
            // 3. 断岳 - 狂暴高防反伤战尊 (戊土重铠，开天神斧)
            new CharacterProfile(
                "char_titan",
                "断岳",
                "荒古战尊 · 盘古遗民",
                "土",
                "开山辟地神斧",
                "继承十二祖巫玄金重煞的上古蛮尊。赤膊金甲，手握开天巨斧，力拔山兮。拥有傲视天地的极厚身魄，以血还血，逆风成圣。",
                2400, 200, 190,
                0.08, // 8% 暴击
                0.05, // 5% 闪避（重装不擅闪避）
                "【巫祖煞体】受到任何攻击均将 20% 伤害反震给攻击者；自身气血越低，防御与破韧力越强！",
                new String[] { "【开山断岳斩】", "【撼地蛮荒吼】", "【盘古真身·天崩地裂】" },
                new String[] {
                    "手持开山重斧重力劈砍，造成 1.0x 伤害与巨大打击硬直",
                    "仰天咆哮爆发大地煞气，造成 1.6x 伤害并直接粉碎敌方 60 点定力韧性",
                    "凝聚盘古巨人法相，巨斧劈碎虚空造成 3.4x 真实伤害并获得 3 回合【绝对霸体】"
                },
                "TITAN"
            )
        };
    }
}
