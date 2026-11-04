package com.honghuang.rpg;

import java.util.ArrayList;
import java.util.List;

/**
 * 实时自走棋对弈模拟机 (Live Chess Battle Simulator)
 * 职责：
 * 1. 索敌 (Target Finding)：寻找距离自身欧氏距离最近的存活敌方棋子；
 * 2. 攻速计时器与移动走位 (Move / Attack Timer)；
 * 3. 普攻伤害与回蓝 (Auto Attack & Mana Gain)；
 * 4. 满蓝专属大神通释放 (Cast Divine Ultimate)；
 * 5. 扣减气血与阵亡裁决。
 */
public class ChessBattleSimulator {
    public final List<LiveChampionEntity> playerUnits = new ArrayList<>();
    public final List<LiveChampionEntity> enemyUnits = new ArrayList<>();
    public boolean isBattleFinished = false;
    public boolean isPlayerVictory = false;

    // 回调事件接口
    public interface BattleEventListener {
        void onAttackHit(LiveChampionEntity attacker, LiveChampionEntity target, int damage, boolean isCrit);
        void onSkillCast(LiveChampionEntity caster, String skillName);
        void onUnitDied(LiveChampionEntity unit);
    }
    public BattleEventListener listener;

    public void initializeBattle(ChessChampion[][] myBoard, ChessChampion[][] enemyBoard) {
        playerUnits.clear();
        enemyUnits.clear();
        isBattleFinished = false;
        isPlayerVictory = false;

        // 初始化我方棋子 (位于下方两行：行2、行3)
        for (int r = 2; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                if (myBoard[r][c] != null) {
                    playerUnits.add(new LiveChampionEntity(myBoard[r][c], r, c, true));
                }
            }
        }

        // 初始化敌方棋子 (位于上方两行：行0、行1)
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 8; c++) {
                if (enemyBoard[r][c] != null) {
                    enemyUnits.add(new LiveChampionEntity(enemyBoard[r][c], r, c, false));
                }
            }
        }
    }

    /**
     * 固定步长物理心跳 (30 FPS Fixed Tick)
     */
    public void updateTick() {
        if (isBattleFinished) return;

        // 1. 更新我方所有棋子
        for (LiveChampionEntity unit : playerUnits) {
            if (unit.isAlive) {
                updateUnitState(unit, enemyUnits);
            }
        }

        // 2. 更新敌方所有棋子
        for (LiveChampionEntity enemy : enemyUnits) {
            if (enemy.isAlive) {
                updateUnitState(enemy, playerUnits);
            }
        }

        // 3. 胜负裁定
        boolean anyPlayerAlive = false;
        for (LiveChampionEntity u : playerUnits) {
            if (u.isAlive) { anyPlayerAlive = true; break; }
        }
        boolean anyEnemyAlive = false;
        for (LiveChampionEntity u : enemyUnits) {
            if (u.isAlive) { anyEnemyAlive = true; break; }
        }

        if (!anyPlayerAlive || !anyEnemyAlive) {
            isBattleFinished = true;
            isPlayerVictory = anyPlayerAlive;
        }
    }

    private void updateUnitState(LiveChampionEntity unit, List<LiveChampionEntity> enemies) {
        // 目标检测与校验 (Target Detection)
        if (unit.currentTarget == null || !unit.currentTarget.isAlive) {
            unit.currentTarget = findNearestTarget(unit, enemies);
        }

        if (unit.currentTarget == null) return; // 全场无存活敌人

        // 技能施法前摇
        if (unit.isCastingSkill) {
            unit.castAnimationTimer--;
            if (unit.castAnimationTimer <= 0) {
                unit.isCastingSkill = false;
                executeSkillDamage(unit);
            }
            return;
        }

        // 判断是否在普攻射程内
        if (unit.isTargetInRange(unit.currentTarget)) {
            // 在攻击范围内：累积攻速计时器进行普攻
            unit.attackCooldownTicks += 1.0f;
            if (unit.attackCooldownTicks >= unit.getFramesPerAttack()) {
                unit.attackCooldownTicks = 0f;
                executeAutoAttack(unit, unit.currentTarget);
            }
        } else {
            // 不在射程内：平滑移动逼近目标 (走砍寻路)
            unit.stepTowardsTarget(unit.currentTarget);
        }
    }

    private LiveChampionEntity findNearestTarget(LiveChampionEntity unit, List<LiveChampionEntity> enemies) {
        LiveChampionEntity nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (LiveChampionEntity e : enemies) {
            if (e.isAlive) {
                float dx = e.gridX - unit.gridX;
                float dy = e.gridY - unit.gridY;
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = e;
                }
            }
        }
        return nearest;
    }

    private void executeAutoAttack(LiveChampionEntity attacker, LiveChampionEntity target) {
        int atk = attacker.baseData.getAttack();
        int def = target.baseData.getArmor();
        boolean isCrit = Math.random() < 0.15;

        int dmg = ChessCombatFormulas.calculateDamage(
            ChessCombatFormulas.DamageType.PHYSICAL,
            atk, def, 0, 100, isCrit
        );

        target.curHp = Math.max(0, target.curHp - dmg);

        // 普攻回蓝 (每次普攻加 10 点蓝，受击加 5 点蓝)
        attacker.curMana = Math.min(attacker.maxMana, attacker.curMana + 10);
        target.curMana = Math.min(target.maxMana, target.curMana + 5);

        if (listener != null) {
            listener.onAttackHit(attacker, target, dmg, isCrit);
        }

        // 检查满蓝释放大招！
        if (attacker.curMana >= attacker.maxMana) {
            triggerSkillCast(attacker);
        }

        if (target.curHp <= 0) {
            target.isAlive = false;
            target.currentTarget = null;
            if (listener != null) {
                listener.onUnitDied(target);
            }
        }
    }

    private void triggerSkillCast(LiveChampionEntity caster) {
        caster.curMana = 0; // 清空蓝量
        caster.isCastingSkill = true;
        caster.castAnimationTimer = 12; // 0.4 秒前摇动画

        if (listener != null) {
            listener.onSkillCast(caster, caster.baseData.skillName);
        }
    }

    private void executeSkillDamage(LiveChampionEntity caster) {
        List<LiveChampionEntity> opponents = caster.isPlayerTeam ? enemyUnits : playerUnits;
        int baseSkillDmg = caster.baseData.star * 260 + caster.baseData.cost * 120;
        int ap = caster.baseData.getApPercent();

        // 范围/目标释放
        for (LiveChampionEntity opp : opponents) {
            if (opp.isAlive) {
                int mr = opp.baseData.getMagicResist();
                int dmg = ChessCombatFormulas.calculateDamage(
                    ChessCombatFormulas.DamageType.MAGIC,
                    baseSkillDmg, 0, mr, ap, false
                );
                opp.curHp = Math.max(0, opp.curHp - dmg);
                if (opp.curHp <= 0) {
                    opp.isAlive = false;
                    opp.currentTarget = null;
                    if (listener != null) {
                        listener.onUnitDied(opp);
                    }
                }
            }
        }
    }
}
