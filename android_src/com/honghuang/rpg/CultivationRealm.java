package com.honghuang.rpg;

public class CultivationRealm {
    public static final String[] MAJOR_REALMS = {
        "练气", "筑基", "金丹", "元婴", "化神", "炼虚", "合体", "大乘", "渡劫真仙"
    };

    private int majorIndex = 0; // 0: 练气, 1: 筑基 ...
    private int minorStage = 1; // 1 ~ 9 阶 (一阶至九阶)
    private int currentExp = 0;

    public CultivationRealm(int major, int minor) {
        this.majorIndex = Math.max(0, Math.min(major, MAJOR_REALMS.length - 1));
        this.minorStage = Math.max(1, Math.min(minor, 9));
    }

    public String getMajorName() {
        return MAJOR_REALMS[majorIndex];
    }

    public int getMinorStage() {
        return minorStage;
    }

    public int getMajorIndex() {
        return majorIndex;
    }

    public String getFullRealmTitle() {
        String stageChinese = getChineseNumber(minorStage);
        if (minorStage == 9) {
            return getMajorName() + "九阶 (大圆满)";
        }
        return getMajorName() + stageChinese + "阶";
    }

    public static String getChineseNumber(int n) {
        switch (n) {
            case 1: return "一";
            case 2: return "二";
            case 3: return "三";
            case 4: return "四";
            case 5: return "五";
            case 6: return "六";
            case 7: return "七";
            case 8: return "八";
            case 9: return "九";
            default: return String.valueOf(n);
        }
    }

    /**
     * 计算当前阶段晋升所需修为经验
     */
    public int getExpNeededForNextStage() {
        // 基础系数：主境界指数上涨 * 当前小阶位倍数
        int base = 200 * (int)Math.pow(2.5, majorIndex);
        return base * minorStage;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    /**
     * 增加修为并自动检查是否升阶
     * @return 升级事件描述（若升阶返回详细文字，否则返回空字符串）
     */
    public String addCultivationExp(int amount) {
        this.currentExp += amount;
        StringBuilder sb = new StringBuilder();

        while (true) {
            int needed = getExpNeededForNextStage();
            if (this.currentExp >= needed) {
                if (minorStage < 9) {
                    this.currentExp -= needed;
                    minorStage++;
                    sb.append("【破阶突破！】道基夯实，晋升至：").append(getFullRealmTitle()).append("！\n");
                } else {
                    // 已达当前境界九阶大圆满，经验不再消耗，等待渡劫大突破
                    sb.append("【真灵圆满】已臻至 ").append(getMajorName()).append("九阶大圆满！请引动九霄天劫渡劫晋级！\n");
                    break;
                }
            } else {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 能否渡劫晋升下一个大境界
     */
    public boolean canTriggerTribulation() {
        return minorStage == 9 && currentExp >= getExpNeededForNextStage();
    }

    /**
     * 渡劫成功，晋级下一个大境界一阶
     */
    public boolean advanceMajorRealm() {
        if (minorStage == 9 && majorIndex < MAJOR_REALMS.length - 1) {
            currentExp = 0;
            majorIndex++;
            minorStage = 1;
            return true;
        }
        return false;
    }

    // 属性加成倍率
    public double getStatMultiplier() {
        // 主境界系数 + 小境界加成
        return 1.0 + (majorIndex * 0.8) + ((minorStage - 1) * 0.12);
    }
}
