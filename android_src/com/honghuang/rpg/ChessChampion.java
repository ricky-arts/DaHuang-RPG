package com.honghuang.rpg;

import java.util.ArrayList;
import java.util.List;

public class ChessChampion {
    public String id;
    public String name;
    public int cost; // 1 ~ 5 费
    public String origin; // 阵营羁绊: 阐教, 截教, 巫族, 妖庭, 散仙
    public String roleClass; // 职业羁绊: 剑修, 法尊, 体修, 神行
    public String background; // 背景生平传记
    public String skillName; // 专属神通名称
    public String skillDesc; // 满蓝大招说明
    public int maxMana; // 满蓝阈值 (如 60/80/100)
    public int startingMana; // 初始蓝量

    public int star = 1; // 1星 / 2星 / 3星
    public int baseHp;
    public int baseAtk;
    public int baseArmor;      // 物理护甲 (Armor)
    public int baseMagicResist;// 魔法抗性 (Magic Resist)
    public double baseAttackSpeed; // 基础攻速 (次/秒)
    public int baseApPercent;  // 法术强度 (初始 100%)
    public int attackRange;    // 攻击距离: 1为近战格, 2~4为远程格
    public String element;     // 金木水火土

    // 金铲铲标准 3 件装备槽位
    public List<TFTItem> items = new ArrayList<>(3);

    public ChessChampion(
        String id, String name, int cost, String origin, String roleClass,
        String background, String skillName, String skillDesc, int maxMana, int startMana,
        int hp, int atk, int armor, int mr, double atkSpeed, int ap, int range, String element
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
        this.startingMana = startMana;
        this.baseHp = hp;
        this.baseAtk = atk;
        this.baseArmor = armor;
        this.baseMagicResist = mr;
        this.baseAttackSpeed = atkSpeed;
        this.baseApPercent = ap;
        this.attackRange = range;
        this.element = element;
    }

    public ChessChampion copy() {
        ChessChampion c = new ChessChampion(
            this.id, this.name, this.cost, this.origin, this.roleClass,
            this.background, this.skillName, this.skillDesc, this.maxMana, this.startingMana,
            this.baseHp, this.baseAtk, this.baseArmor, this.baseMagicResist, this.baseAttackSpeed,
            this.baseApPercent, this.attackRange, this.element
        );
        c.star = this.star;
        for (TFTItem it : this.items) {
            c.items.add(new TFTItem(it.id));
        }
        return c;
    }

    public int getMaxHp() {
        double starMult = (star == 1) ? 1.0 : (star == 2 ? 1.8 : 3.2);
        int total = (int)(baseHp * starMult);
        for (TFTItem it : items) {
            if (it.id == TFTItem.ItemID.GIANTS_BELT) total += 150;
            if (it.id == TFTItem.ItemID.WARMOG_ARMOR) total += 800;
            if (it.id == TFTItem.ItemID.SUNFIRE_CAPE) total += 250;
        }
        return total;
    }

    public int getAttack() {
        double starMult = (star == 1) ? 1.0 : (star == 2 ? 1.75 : 3.0);
        int total = (int)(baseAtk * starMult);
        for (TFTItem it : items) {
            total += it.id.bonusAtk;
        }
        return total;
    }

    public int getArmor() {
        int total = baseArmor * (star == 1 ? 1 : (star == 2 ? 2 : 3));
        for (TFTItem it : items) {
            total += it.id.bonusArmor;
        }
        return total;
    }

    public int getMagicResist() {
        int total = baseMagicResist * (star == 1 ? 1 : (star == 2 ? 2 : 3));
        for (TFTItem it : items) {
            total += it.id.bonusMR;
        }
        return total;
    }

    public double getAttackSpeed() {
        double spd = baseAttackSpeed;
        for (TFTItem it : items) {
            spd *= (1.0 + it.id.bonusAtkSpeed / 100.0);
        }
        if ("剑修".equals(roleClass)) spd *= 1.15;
        return Math.min(2.5, Math.max(0.4, spd));
    }

    public int getApPercent() {
        int total = baseApPercent;
        if ("法尊".equals(roleClass)) total += 35;
        for (TFTItem it : items) {
            total += it.id.bonusAP;
        }
        return total;
    }

    public int getStartingManaBonus() {
        int bonus = 0;
        for (TFTItem it : items) {
            bonus += it.id.bonusMana;
        }
        return bonus;
    }

    public boolean canEquip() {
        return items.size() < 3;
    }

    /**
     * 装备穿戴与金铲铲散件两两合成大件
     */
    public boolean equipItem(TFTItem newItem) {
        if (newItem == null) return false;

        // 若棋子已有散件，且新装也是散件 -> 自动融合成金铲铲大件！
        for (int i = 0; i < items.size(); i++) {
            TFTItem existing = items.get(i);
            if (!existing.id.isCompleted && !newItem.id.isCompleted) {
                TFTItem combined = TFTItem.combine(existing, newItem);
                if (combined != null) {
                    items.set(i, combined);
                    return true;
                }
            }
        }

        if (items.size() < 3) {
            items.add(newItem);
            return true;
        }
        return false;
    }

    public static List<ChessChampion> getAllPoolChampions() {
        List<ChessChampion> list = new ArrayList<>();

        // 1费棋子
        list.add(new ChessChampion(
            "c_baihe", "白鹤童子", 1, "阐教", "法尊",
            "南极仙翁座下仙禽童子，生性纯良，常伴玉虚宫讲道。身姿轻灵，吞吐紫气。",
            "【白鹤亮翅·玉虚清辉】", "扇动仙羽射出3道清辉风刃，对直线敌方造成 240/380/750 法术伤害并击退 1 格",
            60, 20, 650, 50, 25, 30, 0.70, 100, 4, "木"
        ));
        list.add(new ChessChampion(
            "c_linghu", "青丘灵狐", 1, "妖庭", "神行",
            "大荒青丘九尾遗脉幼狐，擅长遁空借影，在敌阵后排神出鬼没。",
            "【魅影摄魂·利爪突刺】", "闪烁突进至气血最低敌人身后造成 220/360/680 伤害并令其定身 1.5 秒",
            50, 15, 580, 62, 20, 20, 0.80, 100, 1, "火"
        ));
        list.add(new ChessChampion(
            "c_wubing", "九幽巫兵", 1, "巫族", "体修",
            "九幽地界刑天麾下战卒，天生铜皮铁骨，不知生死疲倦。",
            "【地煞震地·坚壁】", "撼地顿挫对周围敌人造成 180/300/600 伤害并获得 300/500/1000 护体骨盾",
            70, 0, 780, 45, 45, 35, 0.60, 100, 1, "土"
        ));

        // 2费棋子
        list.add(new ChessChampion(
            "c_nezha", "哪吒", 2, "阐教", "剑修",
            "太乙真人门下灵珠子转世，削骨还父削肉还母，终成三头六臂莲花神躯，反骨弑神。",
            "【三昧真火·混天破阵】", "挥动火尖枪对周围敌人旋风扫击，造成 320/520/1100 伤害并附加【灼伤】",
            70, 25, 820, 72, 35, 30, 0.85, 100, 1, "火"
        ));
        list.add(new ChessChampion(
            "c_leizhenzi", "雷震子", 2, "阐教", "神行",
            "周文王百子，燕山食两枚仙杏化作风雷二翅，手握黄金棍，疾如闪电。",
            "【风雷激荡·九天霹雳】", "展翅呼啸召唤天雷轰击两名敌方后排，造成 350/580/1250 伤害与短暂破韧",
            65, 30, 750, 78, 30, 35, 0.75, 100, 3, "金"
        ));

        // 3费棋子
        list.add(new ChessChampion(
            "c_yangjian", "杨戬", 3, "阐教", "体修",
            "二郎显圣真君，听调不听宣。修成八九玄功七十二变，肉身成圣，天眼洞悉万物虚妄。",
            "【天眼破妄·三尖刀怒劈】", "开启第三神眼射出神光破除敌方护甲，随后三尖两刃刀劈斩造成 480/780/1800 巨大真伤并霸体3秒",
            80, 40, 1000, 85, 60, 50, 0.70, 100, 1, "金"
        ));
        list.add(new ChessChampion(
            "c_zhaogongming", "赵公明", 3, "截教", "法尊",
            "峨眉山罗浮洞截教大仙，执掌二十四颗定海神珠与黑虎金鞭，豪迈重义，法力通玄。",
            "【二十四颗定海神珠】", "祭出定海神珠砸向敌阵，引发海啸狂澜，造成 450/720/1650 范围冻结冰霜伤害",
            90, 30, 900, 88, 40, 45, 0.65, 100, 3, "水"
        ));

        // 4费棋子
        list.add(new ChessChampion(
            "c_kongxuan", "孔宣", 4, "妖庭", "神行",
            "天地间第一只孔雀，元凤嫡子。先天五行精气凝为五色神光，无物不刷，桀骜傲视圣人。",
            "【先天五色神光·刷宝决】", "刷出五道神光，缴械并封印敌方全体装备3秒，造成 650/1100/2800 极高范围伤害",
            80, 40, 950, 102, 45, 50, 0.85, 100, 3, "木"
        ));
        list.add(new ChessChampion(
            "c_houtu", "后土娘娘", 4, "巫族", "体修",
            "十二祖巫之一，心怀洪荒众生慈悲，舍弃肉身化作九幽六道轮回，地道至圣平心娘娘。",
            "【六道轮回·往生护持】", "在大地召唤六道轮回漩涡，吸扯全场敌方击退聚拢，并为全体友军提供 500/900/2500 护盾",
            100, 50, 1300, 75, 75, 70, 0.55, 100, 1, "土"
        ));

        // 5费核心大能
        list.add(new ChessChampion(
            "c_tongtian", "通天教主", 5, "截教", "剑修",
            "三清之一，截教教主，执掌诛仙四剑与诛仙阵图。为天下截取一线生机，敢逆天道乾坤。",
            "【诛仙剑阵·四方俱灭】", "祭出诛仙、戮仙、陷仙、绝仙四剑插天立地！全屏持续落下漫天剑雨，造成 1200/2400/9999 毁天灭地真实伤害",
            110, 60, 1200, 135, 55, 60, 0.90, 100, 4, "金"
        ));
        list.add(new ChessChampion(
            "c_donghuang", "东皇太一", 5, "妖庭", "体修",
            "太阳星金乌化身，上古妖皇，伴生先天至宝混沌钟。镇压鸿蒙大千气运，威慑八荒六合。",
            "【混沌钟响·万物定格】", "敲响天地至宝混沌钟，全场敌方陷入时空定格禁锢 3/4/10 秒，每秒遭受太阳真火焚烧 300/600/3000 点真伤",
            120, 60, 1400, 125, 70, 70, 0.65, 100, 2, "火"
        ));

        return list;
    }
}
