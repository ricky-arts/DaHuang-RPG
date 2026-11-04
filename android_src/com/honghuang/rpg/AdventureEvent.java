package com.honghuang.rpg;

import java.util.Random;

public class AdventureEvent {
    public enum EventType {
        ANCIENT_CAVE("古修秘境洞府", "#F1C40F"),
        MYSTIC_HERB("天地灵药异草", "#2ECC71"),
        WANDERING_DAOIST("云游散仙奇遇", "#3498DB"),
        BEAST_AMBUSH("上古凶煞伏击", "#E74C3C"),
        KARMA_TRIAL("因果问心试炼", "#9B59B6");

        public final String title;
        public final String colorHex;
        EventType(String title, String colorHex) {
            this.title = title;
            this.colorHex = colorHex;
        }
    }

    public EventType type;
    public String title;
    public String description;
    public String optionA;
    public String optionB;
    public int expDeltaA;
    public int expDeltaB;
    public int hpDeltaA;
    public int hpDeltaB;
    public String resultTextA;
    public String resultTextB;

    private static final Random rng = new Random();

    public static AdventureEvent triggerRandomEvent(String mapName) {
        int r = rng.nextInt(5);
        AdventureEvent ev = new AdventureEvent();

        if (r == 0) {
            ev.type = EventType.ANCIENT_CAVE;
            ev.title = "【古修遗府 · 禁制灵光】";
            ev.description = "你在" + mapName + "峭壁悬崖后发现一座被上古太极两仪残阵封印的仙家洞府，阵法流光若隐若现，隐隐传出丹药药香。";
            ev.optionA = "【强行破阵】运起真元暴力轰击阵眼";
            ev.optionB = "【细心参悟】静坐推演阵法生门";
            if (rng.nextBoolean()) {
                ev.expDeltaA = 600;
                ev.hpDeltaA = -150;
                ev.resultTextA = "强破残阵虽被反震真气负伤 150，但成功搜刮到古修士遗留的【九转洗髓残丹】，暴增 600 修为！";
            } else {
                ev.expDeltaA = 200;
                ev.hpDeltaA = -350;
                ev.resultTextA = "阵法触发护山神雷，遭受猛烈雷击损失 350 气血，仅抢救得些许灵石碎片 (+200 修为)。";
            }
            ev.expDeltaB = 450;
            ev.hpDeltaB = 0;
            ev.resultTextB = "你以大智慧顺应两仪阴阳推开石门，无伤汲取洞府内温养万年的天地纯阳灵气 (+450 修为)！";
        } else if (r == 1) {
            ev.type = EventType.MYSTIC_HERB;
            ev.title = "【天地灵珍 · 千年朱果】";
            ev.description = "灵气氤氲的山涧峭壁上，生长着一株通体赤红如火的千年龙血朱果，浓郁药香扑鼻，但根茎缠绕着一条剧毒五彩斑斓蟒。";
            ev.optionA = "【斩蛇夺宝】先发制人击杀毒蟒采摘";
            ev.optionB = "【移花接木】施展御风身法强行掠走";
            ev.expDeltaA = 500;
            ev.hpDeltaA = 0;
            ev.resultTextA = "你剑斩毒蟒，连同蟒蛇妖丹与千年朱果一并服下炼化，肉身大畅，修为暴涨 +500！";
            ev.expDeltaB = 300;
            ev.hpDeltaB = -120;
            ev.resultTextB = "身法虽快，但仍被毒蟒喷吐的毒瘴扫中微伤(-120 气血)，嚼服半枚朱果增进 +300 修为。";
        } else if (r == 2) {
            ev.type = EventType.WANDERING_DAOIST;
            ev.title = "【云游散仙 · 赌石论道】";
            ev.description = "一位骑着青牛的邋遢老道拦住你的去路，笑眯眯拿出两枚未开封的混沌古矿原石，邀你赌一局道缘仙机。";
            ev.optionA = "【择取左侧青色石胎】(耗损 200 修为下注)";
            ev.optionB = "【择取右侧赤金石胎】(耗损 200 修为下注)";
            boolean win = rng.nextBoolean();
            if (win) {
                ev.expDeltaA = 800; // 净赚 600
                ev.hpDeltaA = 0;
                ev.resultTextA = "青石剖开，内藏一滴天道青莲玉髓！老道抚掌大笑折节称奇，为你注入 +800 浑厚修为！";
                ev.expDeltaB = -200;
                ev.hpDeltaB = 0;
                ev.resultTextB = "赤金石胎切开尽是顽铁废料，老道大笑收走你的赌注御风离去。";
            } else {
                ev.expDeltaA = -200;
                ev.hpDeltaA = 0;
                ev.resultTextA = "青石崩碎化作飞灰，运气欠佳，输掉 200 修为。";
                ev.expDeltaB = 800;
                ev.hpDeltaB = 0;
                ev.resultTextB = "赤金石内赫然孕育一枚太阳真精！瞬间炼化，获赠 +800 澎湃修为！";
            }
        } else if (r == 3) {
            ev.type = EventType.BEAST_AMBUSH;
            ev.title = "【狂煞突袭 · 太古凶兽】";
            ev.description = "大荒地底深处突然裂开，一尊嗜血的【荒古骨魔残躯】破土而出，腥风扑鼻，试图吞噬你的道家金身真灵！";
            ev.optionA = "【绝地反击】祭起本命法宝正面硬撼";
            ev.optionB = "【避其锋芒】施展遁术且战且退";
            ev.expDeltaA = 550;
            ev.hpDeltaA = -220;
            ev.resultTextA = "激烈惨战！虽被骨魔利爪重创扣减 220 气血，但最终崩碎魔躯，吸收太古凶煞精魄 (+550 修为)！";
            ev.expDeltaB = 250;
            ev.hpDeltaB = -50;
            ev.resultTextB = "身法神妙，以最小代价击退骨魔，从容脱困获得 +250 战斗领悟。";
        } else {
            ev.type = EventType.KARMA_TRIAL;
            ev.title = "【天地问心 · 凡仙因果】";
            ev.description = "山脚下一群洪荒凡俗部落凡人正被数头赤尾火狼围猎哀嚎。天地不仁以万物为刍狗，仙家当斩断红尘抑或伸手匡扶？";
            ev.optionA = "【天道无情】视而不见，一心求道";
            ev.optionB = "【慈悲济世】御剑下凡诛灭火狼搭救凡人";
            ev.expDeltaA = 300;
            ev.hpDeltaA = 0;
            ev.resultTextA = "你道心坚硬如铁，心无旁骛，参悟出几分无为天道法则 (+300 修为)。";
            ev.expDeltaB = 600;
            ev.hpDeltaB = -80;
            ev.resultTextB = "你以仙家剑气扫平狼群，凡俗百姓顶礼膜拜，万家香火功德加身，心魔退散，气运大增 (+600 修为，祥云度厄护体)！";
        }

        return ev;
    }
}
