package com.honghuang.rpg;

/**
 * 棋子实时动态对弈实体 (包含位置、血量、蓝量、攻击冷却、平滑位移)
 */
public class LiveChampionEntity {
    public ChessChampion baseData;
    public boolean isPlayerTeam; // true: 我方, false: 敌方

    // 棋盘网格逻辑坐标与屏幕像素平滑坐标
    public float gridX; // 0.0 ~ 7.0 (对应 8 列)
    public float gridY; // 0.0 ~ 3.0 (对应 4 行)
    public float targetGridX;
    public float targetGridY;

    // 实时动态数值
    public int curHp;
    public int maxHp;
    public int curMana;
    public int maxMana;

    // 攻速计时器 (帧为单位，按 30FPS 计算)
    public float attackCooldownTicks = 0f;
    public boolean isAlive = true;
    public LiveChampionEntity currentTarget = null;

    // 动作与施法状态
    public int castAnimationTimer = 0; // 技能前摇帧数
    public boolean isCastingSkill = false;

    public LiveChampionEntity(ChessChampion baseData, int initR, int initC, boolean isPlayer) {
        this.baseData = baseData;
        this.isPlayerTeam = isPlayer;
        this.gridX = initC;
        this.gridY = initR;
        this.targetGridX = initC;
        this.targetGridY = initR;

        this.maxHp = baseData.getMaxHp();
        this.curHp = this.maxHp;
        this.maxMana = baseData.maxMana;
        this.curMana = baseData.startingMana;
    }

    /**
     * 计算两次攻击之间所需帧数 (30FPS / 攻速)
     */
    public float getFramesPerAttack() {
        double spd = baseData.getAttackSpeed();
        return (float)(30.0 / spd);
    }

    /**
     * 检测目标是否在攻击射程内 (曼哈顿切比雪夫距离)
     */
    public boolean isTargetInRange(LiveChampionEntity target) {
        if (target == null || !target.isAlive) return false;
        float dx = Math.abs(this.gridX - target.gridX);
        float dy = Math.abs(this.gridY - target.gridY);
        float dist = Math.max(dx, dy); // 战棋格切比雪夫距离
        return dist <= (float)baseData.attackRange + 0.15f;
    }

    /**
     * 平滑移动逼近目标 (轻量化步进，严控计算开销)
     */
    public void stepTowardsTarget(LiveChampionEntity target) {
        if (target == null) return;
        float dx = target.gridX - this.gridX;
        float dy = target.gridY - this.gridY;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        if (dist <= 0.05f) return;

        // 移速：每帧移动 0.05 个格子
        float step = 0.06f;
        this.gridX += (dx / dist) * step;
        this.gridY += (dy / dist) * step;
    }
}
