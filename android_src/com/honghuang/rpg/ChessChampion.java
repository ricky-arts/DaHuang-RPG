package com.honghuang.rpg;

import java.util.ArrayList;
import java.util.List;

public class ChessChampion {
    public String id;
    public String name;
    public int cost; // 1 ~ 5 费
    public String origin; // 阵营羁绊: 阐教, 截教, 巫族, 妖庭, 散仙
    public String roleClass; // 职业羁绊: 剑修, 法尊, 体修, 神行
    public String background; // 背景生平人物传记
    public String skillName; // 专属神通名称
    public String skillDesc; // 满蓝释放的大招效果
    public int maxMana; // 满蓝阈值 (如 80 / 100)

    public int star = 1; // 1星 / 2星 / 3星
    public int baseHp;
    public int baseAtk;
    public int baseDef;
    public int attackRange; // 攻击距离: 1为近战, 3~4为远程
    public String element; // 金木水火土

    // 装备槽位 (最多 3 件装备)
    public List<ChessEquipment> equipments = new ArrayList<>(3);

    public ChessChampion(
        String id, String name, int cost, String origin, String roleClass,
        String background, String skillName, String skillDesc, int maxMana,
        int hp, int atk, int def, int range, String element
    ) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.origin = origin;
        this.roleClass = roleClass;
        this.background = background;
        this.skillName = skillName;
        this.skillDesc = skillDesc;
        this.maxMana = maxMana;
        this.baseHp = hp;
        this.baseAtk = atk;
        this.baseDef = def;
        this.attackRange = range;
        this.element = element;
    }

    public ChessChampion copy() {
        ChessChampion c = new ChessChampion(
            this.id, this.name, this.cost, this.origin, this.roleClass,
            this.background, this.skillName, this.skillDesc, this.maxMana,
            this.baseHp, this.baseAtk, this.baseDef, this.attackRange, this.element
        );
        c.star = this.star;
        for (ChessEquipment eq : this.equipments) {
            c.equipments.add(new ChessEquipment(eq.type));
        }
        return c;
    }

    public int getMaxHp() {
        double starMult = (star == 1) ? 1.0 : (star == 2 ? 1.8 : 3.2);
        int total = (int)(baseHp * starMult);
        for (ChessEquipment eq : equipments) {
            total += eq.type.bonusHp;
        }
        return total;
    }

    public int getAttack() {
        double starMult = (star == 1) ? 1.0 : (star == 2 ? 1.75 : 3.0);
        int total = (int)(baseAtk * starMult);
        for (ChessEquipment eq : equipments) {
            total += eq.type.bonusAtk;
        }
        return total;
    }

    public int getDefense() {
        int total = baseDef * star;
        for (ChessEquipment eq : equipments) {
            total += eq.type.bonusDef;
        }
        return total;
    }

    public boolean canEquip() {
        return equipments.size() < 3;
    }

    public boolean addEquipment(ChessEquipment eq) {
        if (canEquip()) {
            equipments.add(eq);
            return true;
        }
        return false;
    }

    /**
     * 预设洪荒棋子大卡池 (1~5费，人物背景与专属大招)
     */
    public static List<ChessChampion> getAllPoolChampions() {
        List<ChessChampion> list = new ArrayList<>();

        // 1费棋子
        list.add(new ChessChampion(
            "c_baihe", "白鹤童子", 1, "阐教", "法尊",
            "南极仙翁座下仙禽童子，生性纯良，常伴玉虚宫讲道。身姿轻灵，吞吐紫气。",
            "【白鹤亮翅·玉虚清辉】", "扇动仙羽射出3道清辉风刃，对直线敌方造成 240/380/750 法术伤害并击退 1 格",
            60, 650, 55, 30, 4, "木"
        ));
        list.add(new ChessChampion(
            "c_linghu", "青丘灵狐", 1, "妖庭", "神行",
            "大荒青丘九尾遗脉幼狐，擅长遁空借影，在敌阵后排神出鬼没。",
            "【魅影摄魂·利爪突刺】", "闪烁突进至气血最低敌人身后造成 220/360/680 伤害并令其定身 1.5 秒",
            50, 580, 68, 25, 1, "火"
        ));
        list.add(new ChessChampion(
            "c_wubing", "九幽巫兵", 1, "巫族", "体修",
            "九幽地界刑天麾下战卒，天生铜皮铁骨，不知生死疲倦。",
            "【地煞震地·坚壁】", "撼地顿挫对周围敌人造成 180/300/600 伤害并获得 300/500/1000 护体骨盾",
            70, 750, 48, 45, 1, "土"
        ));

        // 2费棋子
        list.add(new ChessChampion(
            "c_nezha", "哪吒", 2, "阐教", "剑修",
            "太乙真人门下灵珠子转世，削骨还父削肉还母，终成三头六臂莲花神躯，反骨弑神。",
            "【三昧真火·混天破阵】", "挥动火尖枪对周围敌人旋风扫击，造成 320/520/1100 伤害并附加【灼伤】",
            70, 800, 75, 35, 1, "火"
        ));
        list.add(new ChessChampion(
            "c_leizhenzi", "雷震子", 2, "阐教", "神行",
            "周文王百子，燕山食两枚仙杏化作风雷二翅，手握黄金棍，疾如闪电。",
            "【风雷激荡·九天霹雳】", "展翅呼啸召唤天雷轰击两名敌方后排，造成 350/580/1250 伤害与短暂破韧",
            65, 720, 80, 28, 3, "金"
        ));

        // 3费棋子
        list.add(new ChessChampion(
            "c_yangjian", "杨戬", 3, "阐教", "体修",
            "二郎显圣真君，听调不听宣。修成八九玄功七十二变，肉身成圣，天眼洞悉万物虚妄。",
            "【天眼破妄·三尖刀怒劈】", "开启第三神眼射出神光破除敌方护甲，随后三尖两刃刀劈斩造成 480/780/1800 巨大真伤并霸体3秒",
            80, 950, 88, 55, 1, "金"
        ));
        list.add(new ChessChampion(
            "c_zhaogongming", "赵公明", 3, "截教", "法尊",
            "峨眉山罗浮洞截教大仙，执掌二十四颗定海神珠与黑虎金鞭，豪迈重义，法力通玄。",
            "【二十四颗定海神珠】", "祭出定海神珠砸向敌阵，引发海啸狂澜，造成 450/720/1650 范围冻结冰霜伤害",
            90, 850, 92, 40, 3, "水"
        ));

        // 4费棋子
        list.add(new ChessChampion(
            "c_kongxuan", "孔宣", 4, "妖庭", "神行",
            "天地间第一只孔雀，元凤嫡子。先天五行精气凝为五色神光，无物不刷，桀骜傲视圣人。",
            "【先天五色神光·刷宝决】", "刷出五道神光，缴械并封印敌方全体装备3秒，造成 650/1100/2800 极高范围伤害",
            80, 920, 105, 45, 3, "木"
        ));
        list.add(new ChessChampion(
            "c_houtu", "后土娘娘", 4, "巫族", "体修",
            "十二祖巫之一，心怀洪荒众生慈悲，舍弃肉身化作九幽六道轮回，地道至圣平心娘娘。",
            "【六道轮回·往生护持】", "在大地召唤六道轮回漩涡，吸扯全场敌方击退聚拢，并为全体友军提供 500/900/2500 护盾",
            100, 1200, 75, 70, 1, "土"
        ));

        // 5费核心大能
        list.add(new ChessChampion(
            "c_tongtian", "通天教主", 5, "截教", "剑修",
            "三清之一，截教教主，执掌诛仙四剑与诛仙阵图。为天下截取一线生机，敢逆天道乾坤。",
            "【诛仙剑阵·四方俱灭】", "祭出诛仙、戮仙、陷仙、绝仙四剑插天立地！全屏持续落下漫天剑雨，造成 1200/2400/9999 毁天灭地真实伤害",
            110, 1150, 140, 60, 4, "金"
        ));
        list.add(new ChessChampion(
            "c_donghuang", "东皇太一", 5, "妖庭", "体修",
            "太阳星金乌化身，上古妖皇，伴生先天至宝混沌钟。镇压鸿蒙大千气运，威慑八荒六合。",
            "【混沌钟响·万物定格】", "敲响天地至宝混沌钟，全场敌方陷入时空定格禁锢 3/4/10 秒，每秒遭受太阳真火焚烧 300/600/3000 点真伤",
            120, 1300, 130, 65, 2, "火"
        ));

        return list;
    }
}
