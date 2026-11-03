package com.honghuang.rpg;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public class GameVisualView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path = new Path();

    // 视图模式: "SELECT" (选角), "MAP" (大地图), "COMBAT" (战斗对阵)
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
            this.effectTimer = 40; // 动画帧时长
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

    // 1. 绘制东方美学开局角色形象大图
    private void drawCharacterSelect(Canvas canvas, int w, int h) {
        // 玄黑水墨背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0C1118"));
        canvas.drawRect(0, 0, w, h, paint);

        // 青绿远山背景晕染 (千里江山意境)
        paint.setColor(Color.parseColor("#15323A"));
        path.reset();
        path.moveTo(0, h * 0.75f);
        path.lineTo(w * 0.3f, h * 0.45f);
        path.lineTo(w * 0.7f, h * 0.55f);
        path.lineTo(w, h * 0.40f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);

        // 角色法相神光轮 (根据选中角色不同色泽)
        int auraColor = selectedCharIndex == 0 ? Color.parseColor("#36D1B2") : // 青莲青绿
                        selectedCharIndex == 1 ? Color.parseColor("#FF5E7E") : // 狐仙朱雀红
                        Color.parseColor("#E6A23C");                           // 战尊玄金
        paint.setColor(auraColor);
        paint.setAlpha(60);
        canvas.drawCircle(w * 0.5f, h * 0.36f, w * 0.35f, paint);

        // 绘制立绘躯干与法象
        paint.setAlpha(255);
        if (selectedCharIndex == 0) {
            // 太微 · 青莲剑仙
            drawSwordImmortal(canvas, w, h);
        } else if (selectedCharIndex == 1) {
            // 望舒 · 青丘天狐
            drawFoxFairy(canvas, w, h);
        } else {
            // 断岳 · 荒古战尊
            drawTitanWarrior(canvas, w, h);
        }
    }

    private void drawSwordImmortal(Canvas canvas, int w, int h) {
        float cx = w * 0.5f;
        float cy = h * 0.38f;

        // 背后悬浮飞剑
        paint.setColor(Color.parseColor("#A8E6CF"));
        paint.setStrokeWidth(6);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - 50, cy - 80, cx - 70, cy + 80, paint);
        canvas.drawLine(cx + 50, cy - 80, cx + 70, cy + 80, paint);

        // 飘逸道袍法相
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2C4E5E"));
        path.reset();
        path.moveTo(cx, cy - 60);
        path.lineTo(cx - 70, cy + 90);
        path.lineTo(cx + 70, cy + 90);
        path.close();
        canvas.drawPath(path, paint);

        // 面容与发冠
        paint.setColor(Color.parseColor("#F5E6CA"));
        canvas.drawCircle(cx, cy - 70, 28, paint);
        paint.setColor(Color.parseColor("#1B262C"));
        canvas.drawCircle(cx, cy - 85, 14, paint); // 发髻

        // 胸前青莲印记
        paint.setColor(Color.parseColor("#4ECCA3"));
        canvas.drawCircle(cx, cy - 20, 10, paint);
    }

    private void drawFoxFairy(Canvas canvas, int w, int h) {
        float cx = w * 0.5f;
        float cy = h * 0.38f;

        // 背后九尾灵弧
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FF6B81"));
        paint.setAlpha(120);
        for (int i = -3; i <= 3; i++) {
            path.reset();
            path.moveTo(cx, cy + 30);
            path.quadTo(cx + i * 35, cy - 40, cx + i * 45, cy - 100);
            path.quadTo(cx + i * 20, cy - 20, cx, cy + 30);
            canvas.drawPath(path, paint);
        }

        // 朱红流仙裙
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#C44569"));
        path.reset();
        path.moveTo(cx, cy - 50);
        path.lineTo(cx - 55, cy + 90);
        path.lineTo(cx + 55, cy + 90);
        path.close();
        canvas.drawPath(path, paint);

        // 面容与朱砂
        paint.setColor(Color.parseColor("#FFF0E1"));
        canvas.drawCircle(cx, cy - 65, 26, paint);
        paint.setColor(Color.parseColor("#EE5253"));
        canvas.drawCircle(cx, cy - 68, 5, paint); // 额头朱砂印
    }

    private void drawTitanWarrior(Canvas canvas, int w, int h) {
        float cx = w * 0.5f;
        float cy = h * 0.38f;

        // 巨大开山辟地神斧
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#8395A7"));
        canvas.drawRect(cx + 40, cy - 110, cx + 55, cy + 90, paint);
        paint.setColor(Color.parseColor("#C8D6E5"));
        path.reset();
        path.moveTo(cx + 55, cy - 90);
        path.lineTo(cx + 105, cy - 60);
        path.lineTo(cx + 55, cy - 30);
        path.close();
        canvas.drawPath(path, paint);

        // 蛮荒战甲重躯
        paint.setColor(Color.parseColor("#576574"));
        canvas.drawRect(cx - 65, cy - 45, cx + 65, cy + 90, paint);

        // 头颅与兽角金冠
        paint.setColor(Color.parseColor("#E0A96D"));
        canvas.drawCircle(cx, cy - 65, 32, paint);
        paint.setColor(Color.parseColor("#F1C40F"));
        canvas.drawLine(cx - 20, cy - 80, cx - 35, cy - 115, paint);
        canvas.drawLine(cx + 20, cy - 80, cx + 35, cy - 115, paint);
    }

    // 2. 绘制青绿山水大荒节点大地图
    private void drawOrientalMap(Canvas canvas, int w, int h) {
        // 底层水墨江山图
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#08121E"));
        canvas.drawRect(0, 0, w, h, paint);

        // 四海汪洋微光
        paint.setColor(Color.parseColor("#0C243B"));
        canvas.drawRect(0, h * 0.6f, w, h, paint);

        // 绘制群峰山势 (工笔青绿)
        paint.setColor(Color.parseColor("#1B4D3E"));
        path.reset();
        path.moveTo(0, h * 0.5f);
        path.lineTo(w * 0.25f, h * 0.25f);
        path.lineTo(w * 0.5f, h * 0.45f);
        path.lineTo(w * 0.75f, h * 0.20f);
        path.lineTo(w, h * 0.55f);
        path.lineTo(w, h * 0.7f);
        path.lineTo(0, h * 0.7f);
        path.close();
        canvas.drawPath(path, paint);

        // 绘制四大探索秘境节点
        float[][] nodePos = {
            { w * 0.25f, h * 0.35f }, // 终南山
            { w * 0.75f, h * 0.30f }, // 不周山
            { w * 0.35f, h * 0.65f }, // 东海
            { w * 0.80f, h * 0.70f }  // 幽冥血海
        };

        // 连线灵脉
        paint.setColor(Color.parseColor("#34495E"));
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(nodePos[0][0], nodePos[0][1], nodePos[1][0], nodePos[1][1], paint);
        canvas.drawLine(nodePos[0][0], nodePos[0][1], nodePos[2][0], nodePos[2][1], paint);
        canvas.drawLine(nodePos[2][0], nodePos[2][1], nodePos[3][0], nodePos[3][1], paint);

        // 节点图标
        for (int i = 0; i < 4; i++) {
            boolean isCur = (i == selectedMapIndex);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isCur ? Color.parseColor("#F1C40F") : Color.parseColor("#2ECC71"));
            canvas.drawCircle(nodePos[i][0], nodePos[i][1], isCur ? 16 : 10, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(2);
            canvas.drawCircle(nodePos[i][0], nodePos[i][1], isCur ? 22 : 14, paint);
        }
    }

    // 3. 绘制图形化战斗对阵战场
    private void drawCombatField(Canvas canvas, int w, int h) {
        // 战场背景 (雷云劫象/阵法地面)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#101924"));
        canvas.drawRect(0, 0, w, h, paint);

        // 地面乾坤八卦微光
        paint.setColor(Color.parseColor("#1D2B3A"));
        canvas.drawCircle(w * 0.5f, h * 0.65f, w * 0.45f, paint);

        // 我方英雄立影 (左侧)
        float px = w * 0.25f;
        float py = h * 0.55f;
        paint.setColor(Color.parseColor("#2ECC71"));
        paint.setAlpha(50);
        canvas.drawCircle(px, py, 45, paint);
        paint.setAlpha(255);
        paint.setColor(Color.parseColor("#58D68D"));
        canvas.drawCircle(px, py - 35, 18, paint);
        canvas.drawRect(px - 22, py - 15, px + 22, py + 35, paint);

        // 敌方异兽首领立像 (右侧)
        float ex = w * 0.75f;
        float ey = h * 0.45f;
        int bossColor = isEnemyBroken ? Color.parseColor("#95A5A6") : Color.parseColor("#E74C3C");
        paint.setColor(bossColor);
        paint.setAlpha(60);
        canvas.drawCircle(ex, ey, 60, paint);
        paint.setAlpha(255);
        paint.setColor(bossColor);
        canvas.drawCircle(ex, ey - 40, 24, paint);
        canvas.drawRect(ex - 35, ey - 15, ex + 35, ey + 45, paint);

        // 破韧虚弱特效标记
        if (isEnemyBroken) {
            paint.setColor(Color.parseColor("#F39C12"));
            paint.setTextSize(26);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("【 破 韧 虚 弱 · BREAK 】", ex, ey - 75, paint);
        }

        // 血条 UI (玩家)
        drawBar(canvas, 30, 25, w * 0.4f, 16, playerHpPercent, Color.parseColor("#2ECC71"), "我方气血");

        // 血条与韧性条 UI (敌人)
        drawBar(canvas, w * 0.55f, 25, w * 0.4f, 16, enemyHpPercent, Color.parseColor("#E74C3C"), "首领凶煞气血");
        drawBar(canvas, w * 0.55f, 50, w * 0.4f, 10, enemyTenacityPercent, Color.parseColor("#F39C12"), "定力韧性");

        // 飘字伤害或法术特效
        if (effectTimer > 0 && !combatEffectText.isEmpty()) {
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            float alpha = (float)effectTimer / 40.0f;
            paint.setAlpha((int)(alpha * 255));
            canvas.drawText(combatEffectText, w * 0.5f, h * 0.35f - (40 - effectTimer), paint);
            paint.setAlpha(255);
        }
    }

    private void drawBar(Canvas canvas, float x, float y, float bw, float bh, float percent, int color, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2C3E50"));
        canvas.drawRect(x, y, x + bw, y + bh, paint);

        paint.setColor(color);
        canvas.drawRect(x, y, x + (bw * percent), y + bh, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#7F8C8D"));
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + bw, y + bh, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, x, y - 5, paint);
    }
}
