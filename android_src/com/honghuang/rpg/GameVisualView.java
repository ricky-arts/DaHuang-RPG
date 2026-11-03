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

    // 战斗动态
    private float playerHpPercent = 1.0f;
    private float enemyHpPercent = 1.0f;
    private float enemyTenacityPercent = 1.0f;
    private boolean isEnemyBroken = false;
    private String combatEffectText = "";
    private int effectTimer = 0;

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

    public void updateCombatState(float pHp, float eHp, float eTenacity, boolean broken, String effect) {
        this.playerHpPercent = Math.max(0f, Math.min(1f, pHp));
        this.enemyHpPercent = Math.max(0f, Math.min(1f, eHp));
        this.enemyTenacityPercent = Math.max(0f, Math.min(1f, eTenacity));
        this.isEnemyBroken = broken;
        if (effect != null && !effect.isEmpty()) {
            this.combatEffectText = effect;
            this.effectTimer = 45;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if ("SELECT".equals(mode)) {
            drawCharacterSelect(canvas, w, h);
        } else if ("MAP".equals(mode)) {
            drawOrientalMap(canvas, w, h);
        } else if ("COMBAT".equals(mode)) {
            drawCombatField(canvas, w, h);
        }

        if (effectTimer > 0) {
            effectTimer--;
            invalidate();
        }
    }

    // ==========================================
    // 1. 横屏角色立绘与法象展示
    // ==========================================
    private void drawCharacterSelect(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0A0F17"));
        canvas.drawRect(0, 0, w, h, paint);

        // 远山水墨青绿渐变
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
        float scale = Math.min(w, h) * 0.003f; // 动态自适应缩放因子

        // 神光神轮
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

        // 底部水墨祥云台
        paint.setColor(Color.parseColor("#1B2A38"));
        canvas.drawRoundRect(cx - 140 * scale, cy + 115 * scale, cx + 140 * scale, cy + 135 * scale, 10, 10, paint);
    }

    private void drawSwordImmortal(Canvas canvas, float cx, float cy, float s) {
        // 背后悬浮双飞剑
        paint.setColor(Color.parseColor("#A8E6CF"));
        paint.setStrokeWidth(4 * s);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 50 * s, cy - 90 * s, cx - 65 * s, cy + 70 * s, paint);
        canvas.drawLine(cx + 50 * s, cy - 90 * s, cx + 65 * s, cy + 70 * s, paint);

        // 飘逸道袍法相
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#245266"));
        path.reset();
        path.moveTo(cx, cy - 50 * s);
        path.lineTo(cx - 70 * s, cy + 110 * s);
        path.lineTo(cx + 70 * s, cy + 110 * s);
        path.close();
        canvas.drawPath(path, paint);

        // 面容与发髻
        paint.setColor(Color.parseColor("#FBE8D3"));
        canvas.drawCircle(cx, cy - 65 * s, 26 * s, paint);
        paint.setColor(Color.parseColor("#192A56"));
        canvas.drawCircle(cx, cy - 82 * s, 14 * s, paint);

        // 护体青莲印
        paint.setColor(Color.parseColor("#2ECC71"));
        canvas.drawCircle(cx, cy - 15 * s, 10 * s, paint);
    }

    private void drawFoxFairy(Canvas canvas, float cx, float cy, float s) {
        // 背后九尾灵弧
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

        // 朱红流仙长裙
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#C23616"));
        path.reset();
        path.moveTo(cx, cy - 45 * s);
        path.lineTo(cx - 60 * s, cy + 110 * s);
        path.lineTo(cx + 60 * s, cy + 110 * s);
        path.close();
        canvas.drawPath(path, paint);

        // 绝美容颜与额间朱砂印
        paint.setColor(Color.parseColor("#FFF0E1"));
        canvas.drawCircle(cx, cy - 62 * s, 24 * s, paint);
        paint.setColor(Color.parseColor("#FF3838"));
        canvas.drawCircle(cx, cy - 66 * s, 5 * s, paint);
    }

    private void drawTitanWarrior(Canvas canvas, float cx, float cy, float s) {
        // 巨大神斧
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

        // 玄金重甲蛮躯
        paint.setColor(Color.parseColor("#2F3640"));
        canvas.drawRect(cx - 68 * s, cy - 40 * s, cx + 68 * s, cy + 110 * s, paint);

        // 战尊头颅与兽骨战角
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

        // 汪洋与地脉水系
        paint.setColor(Color.parseColor("#0D263D"));
        canvas.drawRect(0, h * 0.65f, w, h, paint);

        // 青绿山峦起伏
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

        // 四大秘境节点坐标 (适配横屏布局)
        float[][] nodes = {
            { w * 0.20f, h * 0.40f }, // 终南山
            { w * 0.48f, h * 0.30f }, // 不周山
            { w * 0.32f, h * 0.72f }, // 东海古龙墟
            { w * 0.80f, h * 0.68f }  // 幽冥血海
        };

        // 灵脉连线
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
    // 3. 横屏图形化对决战场
    // ==========================================
    private void drawCombatField(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0D1520"));
        canvas.drawRect(0, 0, w, h, paint);

        // 中央八卦战阵法盘
        paint.setColor(Color.parseColor("#152434"));
        canvas.drawCircle(w * 0.5f, h * 0.55f, Math.min(w, h) * 0.42f, paint);

        float scale = Math.min(w, h) * 0.003f;

        // 我方英雄立象 (左侧)
        float px = w * 0.28f;
        float py = h * 0.60f;
        paint.setColor(Color.parseColor("#2ECC71"));
        paint.setAlpha(60);
        canvas.drawCircle(px, py, 45 * scale, paint);
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#58D68D"));
        canvas.drawCircle(px, py - 32 * scale, 18 * scale, paint);
        canvas.drawRect(px - 22 * scale, py - 12 * scale, px + 22 * scale, py + 38 * scale, paint);

        // 敌方异兽立相 (右侧)
        float ex = w * 0.72f;
        float ey = h * 0.50f;
        int bossColor = isEnemyBroken ? Color.parseColor("#95A5A6") : Color.parseColor("#E74C3C");
        paint.setColor(bossColor);
        paint.setAlpha(60);
        canvas.drawCircle(ex, ey, 55 * scale, paint);
        paint.setAlpha(255);
        paint.setColor(bossColor);
        canvas.drawCircle(ex, ey - 36 * scale, 22 * scale, paint);
        canvas.drawRect(ex - 32 * scale, ey - 12 * scale, ex + 32 * scale, ey + 42 * scale, paint);

        // 破韧虚弱标记
        if (isEnemyBroken) {
            paint.setColor(Color.parseColor("#F39C12"));
            paint.setTextSize(24);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("【 破韧虚弱 · 伤害增幅60% 】", ex, ey - 65 * scale, paint);
        }

        // 横屏状态条：左上角我方血条
        drawBar(canvas, 20, 20, w * 0.38f, 16, playerHpPercent, Color.parseColor("#2ECC71"), "我方真元气血");

        // 右上角首领血条与定力韧性条
        drawBar(canvas, w * 0.58f, 20, w * 0.38f, 16, enemyHpPercent, Color.parseColor("#E74C3C"), "异兽凶煞气血");
        drawBar(canvas, w * 0.58f, 44, w * 0.38f, 10, enemyTenacityPercent, Color.parseColor("#F39C12"), "定力韧性");

        // 飘字伤害或特效
        if (effectTimer > 0 && !combatEffectText.isEmpty()) {
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setTextSize(28);
            paint.setTextAlign(Paint.Align.CENTER);
            float alpha = (float)effectTimer / 45.0f;
            paint.setAlpha((int)(alpha * 255));
            canvas.drawText(combatEffectText, w * 0.5f, h * 0.35f - (45 - effectTimer), paint);
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
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, x, y - 4, paint);
    }
}
