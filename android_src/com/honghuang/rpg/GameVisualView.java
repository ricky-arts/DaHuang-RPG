package com.honghuang.rpg;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public class GameVisualView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path = new Path();

    // 模式: "SELECT", "MAP", "COMBAT"
    private String mode = "SELECT";
    private int selectedCharIndex = 0;
    private int selectedMapIndex = 0;

    // 战斗动态状态
    private float playerHpPercent = 1.0f;
    private float enemyHpPercent = 1.0f;
    private float enemyTenacityPercent = 1.0f;
    private boolean isEnemyBroken = false;
    private String combatEffectText = "";
    private int effectTimer = 0;

    // 动作与技能特效状态机
    private String currentAction = "IDLE"; // "IDLE", "SLASH", "CAST", "ULTIMATE", "HIT"
    private int actionFrame = 0; // 动作帧计时器 (0~30)
    private float projectileX = -1f; // 技能弹道飞掠位置
    private float screenShakeX = 0f; // 震屏偏移
    private float screenShakeY = 0f;

    // 怪物梯度展示
    private MonsterEntity.MonsterTier monsterTier = MonsterEntity.MonsterTier.NORMAL;
    private String monsterName = "妖精";

    public GameVisualView(Context context) {
        super(context);
    }

    public void setMode(String m) {
        this.mode = m;
        invalidate();
    }

    public void setSelectedChar(int idx) {
        this.selectedCharIndex = idx;
        invalidate();
    }

    public void setSelectedMap(int idx) {
        this.selectedMapIndex = idx;
        invalidate();
    }

    public void setMonsterInfo(MonsterEntity.MonsterTier tier, String name) {
        this.monsterTier = tier;
        this.monsterName = name;
        invalidate();
    }

    /**
     * 触发角色动作与技能绚丽弹道特效
     */
    public void triggerCombatSkillAnimation(String actionType, String effectText) {
        this.currentAction = actionType;
        this.actionFrame = 25; // 25 帧动画生命周期
        this.projectileX = 0.32f; // 从主角位置起始飞行
        this.combatEffectText = effectText;
        this.effectTimer = 40;

        if ("ULTIMATE".equals(actionType) || "HIT".equals(actionType)) {
            this.screenShakeX = (float)((Math.random() - 0.5) * 16.0);
            this.screenShakeY = (float)((Math.random() - 0.5) * 16.0);
        } else {
            this.screenShakeX = 0f;
            this.screenShakeY = 0f;
        }
        invalidate();
    }

    public void updateCombatState(float pHp, float eHp, float eTenacity, boolean broken) {
        this.playerHpPercent = Math.max(0f, Math.min(1f, pHp));
        this.enemyHpPercent = Math.max(0f, Math.min(1f, eHp));
        this.enemyTenacityPercent = Math.max(0f, Math.min(1f, eTenacity));
        this.isEnemyBroken = broken;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 震屏矩阵变换
        canvas.save();
        if (actionFrame > 15 && (screenShakeX != 0f || screenShakeY != 0f)) {
            canvas.translate(screenShakeX, screenShakeY);
        }

        if ("SELECT".equals(mode)) {
            drawCharacterSelect(canvas, w, h);
        } else if ("MAP".equals(mode)) {
            drawOrientalMap(canvas, w, h);
        } else if ("COMBAT".equals(mode)) {
            drawCombatField(canvas, w, h);
        }

        canvas.restore();

        // 驱动帧动画与特效推进
        if (actionFrame > 0) {
            actionFrame--;
            if (projectileX >= 0) {
                projectileX += 0.025f; // 弹道以每帧 2.5% 屏幕宽度向右侧飞掠
                if (projectileX > 0.72f) {
                    projectileX = -1f; // 击中敌人
                }
            }
            if (actionFrame == 0) {
                currentAction = "IDLE";
                screenShakeX = 0f;
                screenShakeY = 0f;
            }
            invalidate();
        }

        if (effectTimer > 0) {
            effectTimer--;
            invalidate();
        }
    }

    // ==========================================
    // 1. 横屏角色形象立绘与法象
    // ==========================================
    private void drawCharacterSelect(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0A0F17"));
        canvas.drawRect(0, 0, w, h, paint);

        // 青绿山水晕染背景
        paint.setColor(Color.parseColor("#132A32"));
        path.reset();
        path.moveTo(0, h * 0.85f);
        path.lineTo(w * 0.35f, h * 0.35f);
        path.lineTo(w * 0.70f, h * 0.50f);
        path.lineTo(w, h * 0.25f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);

        float cx = w * 0.5f;
        float cy = h * 0.52f;
        float scale = Math.min(w, h) * 0.003f;

        // 神轮
        int auraColor = selectedCharIndex == 0 ? Color.parseColor("#2ECC71") :
                        selectedCharIndex == 1 ? Color.parseColor("#E84118") :
                        Color.parseColor("#F1C40F");
        paint.setColor(auraColor);
        paint.setAlpha(45);
        canvas.drawCircle(cx, cy - 20 * scale, 130 * scale, paint);

        paint.setAlpha(255);
        if (selectedCharIndex == 0) {
            drawSwordImmortal(canvas, cx, cy, scale);
        } else if (selectedCharIndex == 1) {
            drawFoxFairy(canvas, cx, cy, scale);
        } else {
            drawTitanWarrior(canvas, cx, cy, scale);
        }

        paint.setColor(Color.parseColor("#1B2A38"));
        canvas.drawRoundRect(cx - 140 * scale, cy + 115 * scale, cx + 140 * scale, cy + 135 * scale, 10, 10, paint);
    }

    private void drawSwordImmortal(Canvas canvas, float cx, float cy, float s) {
        paint.setColor(Color.parseColor("#A8E6CF"));
        paint.setStrokeWidth(4 * s);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 50 * s, cy - 90 * s, cx - 65 * s, cy + 70 * s, paint);
        canvas.drawLine(cx + 50 * s, cy - 90 * s, cx + 65 * s, cy + 70 * s, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#245266"));
        path.reset();
        path.moveTo(cx, cy - 50 * s);
        path.lineTo(cx - 70 * s, cy + 110 * s);
        path.lineTo(cx + 70 * s, cy + 110 * s);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.parseColor("#FBE8D3"));
        canvas.drawCircle(cx, cy - 65 * s, 26 * s, paint);
        paint.setColor(Color.parseColor("#192A56"));
        canvas.drawCircle(cx, cy - 82 * s, 14 * s, paint);

        paint.setColor(Color.parseColor("#2ECC71"));
        canvas.drawCircle(cx, cy - 15 * s, 10 * s, paint);
    }

    private void drawFoxFairy(Canvas canvas, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#EA2027"));
        paint.setAlpha(110);
        for (int i = -3; i <= 3; i++) {
            path.reset();
            path.moveTo(cx, cy + 30 * s);
            path.quadTo(cx + i * 35 * s, cy - 40 * s, cx + i * 42 * s, cy - 110 * s);
            path.quadTo(cx + i * 18 * s, cy - 10 * s, cx, cy + 30 * s);
            canvas.drawPath(path, paint);
        }

        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#C23616"));
        path.reset();
        path.moveTo(cx, cy - 45 * s);
        path.lineTo(cx - 60 * s, cy + 110 * s);
        path.lineTo(cx + 60 * s, cy + 110 * s);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.parseColor("#FFF0E1"));
        canvas.drawCircle(cx, cy - 62 * s, 24 * s, paint);
        paint.setColor(Color.parseColor("#FF3838"));
        canvas.drawCircle(cx, cy - 66 * s, 5 * s, paint);
    }

    private void drawTitanWarrior(Canvas canvas, float cx, float cy, float s) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#718093"));
        canvas.drawRect(cx + 45 * s, cy - 120 * s, cx + 58 * s, cy + 100 * s, paint);
        paint.setColor(Color.parseColor("#DCDDE1"));
        path.reset();
        path.moveTo(cx + 58 * s, cy - 105 * s);
        path.lineTo(cx + 115 * s, cy - 70 * s);
        path.lineTo(cx + 58 * s, cy - 35 * s);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.parseColor("#2F3640"));
        canvas.drawRect(cx - 68 * s, cy - 40 * s, cx + 68 * s, cy + 110 * s, paint);

        paint.setColor(Color.parseColor("#E1B12C"));
        canvas.drawCircle(cx, cy - 62 * s, 30 * s, paint);
        paint.setColor(Color.parseColor("#FBC531"));
        canvas.drawLine(cx - 20 * s, cy - 75 * s, cx - 35 * s, cy - 110 * s, paint);
        canvas.drawLine(cx + 20 * s, cy - 75 * s, cx + 35 * s, cy - 110 * s, paint);
    }

    // ==========================================
    // 2. 横屏青绿大地图展示
    // ==========================================
    private void drawOrientalMap(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#07111D"));
        canvas.drawRect(0, 0, w, h, paint);

        paint.setColor(Color.parseColor("#0D263D"));
        canvas.drawRect(0, h * 0.65f, w, h, paint);

        paint.setColor(Color.parseColor("#1B4D3E"));
        path.reset();
        path.moveTo(0, h * 0.6f);
        path.lineTo(w * 0.22f, h * 0.25f);
        path.lineTo(w * 0.45f, h * 0.45f);
        path.lineTo(w * 0.72f, h * 0.18f);
        path.lineTo(w, h * 0.50f);
        path.lineTo(w, h * 0.75f);
        path.lineTo(0, h * 0.75f);
        path.close();
        canvas.drawPath(path, paint);

        float[][] nodes = {
            { w * 0.20f, h * 0.40f },
            { w * 0.48f, h * 0.30f },
            { w * 0.32f, h * 0.72f },
            { w * 0.80f, h * 0.68f }
        };

        paint.setColor(Color.parseColor("#34495E"));
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(nodes[0][0], nodes[0][1], nodes[1][0], nodes[1][1], paint);
        canvas.drawLine(nodes[0][0], nodes[0][1], nodes[2][0], nodes[2][1], paint);
        canvas.drawLine(nodes[2][0], nodes[2][1], nodes[3][0], nodes[3][1], paint);

        for (int i = 0; i < 4; i++) {
            boolean isCur = (i == selectedMapIndex);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isCur ? Color.parseColor("#F1C40F") : Color.parseColor("#2ECC71"));
            canvas.drawCircle(nodes[i][0], nodes[i][1], isCur ? 18 : 11, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(2);
            canvas.drawCircle(nodes[i][0], nodes[i][1], isCur ? 25 : 15, paint);
        }
    }

    // ==========================================
    // 3. 动态对战、角色突刺/施法动作与全屏技能弹道
    // ==========================================
    private void drawCombatField(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0D1520"));
        canvas.drawRect(0, 0, w, h, paint);

        // 八卦阵法地表
        paint.setColor(Color.parseColor("#152434"));
        canvas.drawCircle(w * 0.5f, h * 0.55f, Math.min(w, h) * 0.42f, paint);

        float scale = Math.min(w, h) * 0.003f;

        // 计算动作偏移（突刺向前位移）
        float px = w * 0.28f;
        float py = h * 0.60f;
        if ("SLASH".equals(currentAction)) {
            px += (actionFrame > 12 ? (25 - actionFrame) * 4.0f : actionFrame * 4.0f); // 突刺冲锋动画
        } else if ("CAST".equals(currentAction)) {
            py -= 15 * scale; // 悬空施法结印
        }

        // 绘制角色
        paint.setColor(Color.parseColor("#2ECC71"));
        paint.setAlpha(60);
        canvas.drawCircle(px, py, 45 * scale, paint);
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#58D68D"));
        canvas.drawCircle(px, py - 32 * scale, 18 * scale, paint);
        canvas.drawRect(px - 22 * scale, py - 12 * scale, px + 22 * scale, py + 38 * scale, paint);

        // 绘制怪物（根据怪物品质等级调整体型比例与光环）
        float mScale = scale * monsterTier.scale;
        float ex = w * 0.72f;
        float ey = h * 0.50f;

        // 精英怪银色雷环，首领怪金色火环
        if (monsterTier == MonsterEntity.MonsterTier.ELITE) {
            paint.setColor(Color.parseColor("#3498DB"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3 * scale);
            canvas.drawCircle(ex, ey, 62 * mScale, paint);
        } else if (monsterTier == MonsterEntity.MonsterTier.BOSS) {
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5 * scale);
            canvas.drawCircle(ex, ey, 72 * mScale, paint);
        }

        int bossColor = isEnemyBroken ? Color.parseColor("#95A5A6") : Color.parseColor(monsterTier.colorHex);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bossColor);
        paint.setAlpha(60);
        canvas.drawCircle(ex, ey, 55 * mScale, paint);
        paint.setAlpha(255);
        paint.setColor(bossColor);
        canvas.drawCircle(ex, ey - 36 * mScale, 22 * mScale, paint);
        canvas.drawRect(ex - 32 * mScale, ey - 12 * mScale, ex + 32 * mScale, ey + 42 * mScale, paint);

        // 怪物头顶等级标签
        paint.setColor(Color.parseColor(monsterTier.colorHex));
        paint.setTextSize(14 * scale);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("【" + monsterTier.title + "】" + monsterName, ex, ey - 60 * mScale, paint);

        // 破韧虚弱状态
        if (isEnemyBroken) {
            paint.setColor(Color.parseColor("#F39C12"));
            paint.setTextSize(22);
            canvas.drawText("【 破韧虚弱 · 伤害暴增50% 】", ex, ey - 85 * mScale, paint);
        }

        // 弹道特效 1: 【普攻·流光飞剑弹道】
        if ("SLASH".equals(currentAction) && projectileX > 0) {
            float projPx = w * projectileX;
            float projPy = h * 0.52f;
            paint.setColor(Color.parseColor("#2ECC71"));
            paint.setStrokeWidth(6 * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(projPx - 40 * scale, projPy, projPx + 40 * scale, projPy, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(projPx + 40 * scale, projPy, 8 * scale, paint); // 剑尖高光
        }

        // 弹道特效 2: 【五行·灭神印】九霄雷霆轰顶
        if ("CAST".equals(currentAction) && actionFrame > 5) {
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setStrokeWidth(8 * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(ex, 0, ex, ey + 20, paint); // 从天而降金色雷柱
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#E67E22"));
            canvas.drawCircle(ex, ey + 20, 40 * scale, paint); // 轰炸爆破光圈
        }

        // 弹道特效 3: 【本命·诛仙大阵】暗屏与四剑天降
        if ("ULTIMATE".equals(currentAction)) {
            // 背景暗化
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setAlpha(120);
            canvas.drawRect(0, 0, w, h, paint);
            paint.setAlpha(255);

            // 四柄诛仙暗金神剑从虚空插向敌方
            paint.setColor(Color.parseColor("#9B59B6"));
            paint.setStrokeWidth(6 * scale);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(ex - 60, ey - 180, ex - 20, ey, paint);
            canvas.drawLine(ex + 60, ey - 180, ex + 20, ey, paint);
            canvas.drawLine(ex, ey - 200, ex, ey - 10, paint);
        }

        // 血条绘制
        drawBar(canvas, 20, 20, w * 0.38f, 16, playerHpPercent, Color.parseColor("#2ECC71"), "我方真元气血");
        drawBar(canvas, w * 0.58f, 20, w * 0.38f, 16, enemyHpPercent, Color.parseColor(monsterTier.colorHex), monsterName + " 气血");
        drawBar(canvas, w * 0.58f, 44, w * 0.38f, 10, enemyTenacityPercent, Color.parseColor("#F39C12"), "定力韧性");

        // 飘字伤害
        if (effectTimer > 0 && !combatEffectText.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setTextSize(26);
            paint.setTextAlign(Paint.Align.CENTER);
            float alpha = (float)effectTimer / 40.0f;
            paint.setAlpha((int)(alpha * 255));
            canvas.drawText(combatEffectText, w * 0.5f, h * 0.35f - (40 - effectTimer), paint);
            paint.setAlpha(255);
        }
    }

    private void drawBar(Canvas canvas, float x, float y, float bw, float bh, float percent, int color, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#1C2A38"));
        canvas.drawRect(x, y, x + bw, y + bh, paint);

        paint.setColor(color);
        canvas.drawRect(x, y, x + (bw * percent), y + bh, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#576574"));
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + bw, y + bh, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(15);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, x, y - 4, paint);
    }
}
