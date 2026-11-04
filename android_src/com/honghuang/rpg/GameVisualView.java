package com.honghuang.rpg;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public class GameVisualView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path = new Path();

    // 核心特效系统
    private final ParticleSystem particleSys = new ParticleSystem();
    private final MeshVFX meshVFX = new MeshVFX();
    private final FrameAnimationController frameAnim = new FrameAnimationController();

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
    private int actionFrame = 0;
    private float projectileX = -1f;
    private float screenShakeX = 0f;
    private float screenShakeY = 0f;
    private float globalTime = 0f; // 驱动待机呼吸与网格旋转

    // 当前战斗中玩家角色的主题样式 (SWORD, FOX, TITAN)
    private String activeHeroTheme = "SWORD";

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

    public void setActiveHeroTheme(String theme) {
        this.activeHeroTheme = theme;
        invalidate();
    }

    public void setMonsterInfo(MonsterEntity.MonsterTier tier, String name) {
        this.monsterTier = tier;
        this.monsterName = name;
        invalidate();
    }

    /**
     * 触发复合角色动作、面片网格、序列帧与粒子流喷射
     */
    public void triggerCombatSkillAnimation(String actionType, String effectText) {
        this.currentAction = actionType;
        this.actionFrame = 28;
        this.projectileX = 0.32f;
        this.combatEffectText = effectText;
        this.effectTimer = 42;

        int w = getWidth() > 0 ? getWidth() : 1920;
        int h = getHeight() > 0 ? getHeight() : 1080;
        float px = w * 0.28f;
        float py = h * 0.60f;
        float ex = w * 0.72f;
        float ey = h * 0.50f;

        // 依据角色技能发射定制粒子束与序列帧
        if ("SLASH".equals(actionType)) {
            // 普攻：发射 24 颗向右飞驰的剑气流光粒子
            int pColor = "FOX".equals(activeHeroTheme) ? Color.parseColor("#E84118") :
                         "TITAN".equals(activeHeroTheme) ? Color.parseColor("#E67E22") :
                         Color.parseColor("#2ECC71");
            particleSys.emitDirected(px, py, ex, ey, 24, pColor, 7.0f, 22);

        } else if ("CAST".equals(actionType)) {
            // 施法：触发九天雷霆 6 帧连环破裂序列帧 + 目标受击粒子爆破
            frameAnim.play(FrameAnimationController.AnimType.THUNDER_CRACK, ex, ey, Color.parseColor("#F1C40F"));
            particleSys.emit(ex, ey, 36, Color.parseColor("#F39C12"), 8.0f, 9.0f, 25, true);

        } else if ("ULTIMATE".equals(actionType)) {
            // 大招：红莲绽放 8 帧序列帧 + 60 颗全屏扩散神剑金煞粒子
            frameAnim.play(FrameAnimationController.AnimType.LOTUS_BURST, ex, ey, Color.parseColor("#9B59B6"));
            particleSys.emit(ex, ey, 60, Color.parseColor("#9B59B6"), 12.0f, 10.0f, 32, true);
        }

        // 触发受击震屏
        if ("ULTIMATE".equals(actionType) || "HIT".equals(actionType)) {
            this.screenShakeX = (float)((Math.random() - 0.5) * 22.0);
            this.screenShakeY = (float)((Math.random() - 0.5) * 22.0);
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

        globalTime += 0.05f;

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

        // 绘制序列帧特效层
        frameAnim.draw(canvas);

        // 绘制粒子系统层
        particleSys.draw(canvas);

        canvas.restore();

        // 推进粒子、序列帧与动作状态
        particleSys.update();
        frameAnim.update();

        if (actionFrame > 0) {
            actionFrame--;
            if (projectileX >= 0) {
                projectileX += 0.032f;
                // 弹道飞行时沿途喷吐光尘粒子
                float curProjX = w * projectileX;
                float curProjY = h * 0.52f;
                particleSys.emit(curProjX, curProjY, 2, Color.WHITE, 1.5f, 4.0f, 10, true);

                if (projectileX > 0.72f) {
                    projectileX = -1f;
                    // 弹道击中敌人瞬间播放爆破序列帧！
                    frameAnim.play(FrameAnimationController.AnimType.HIT_EXPLODE, w * 0.72f, h * 0.50f, Color.parseColor("#F1C40F"));
                    particleSys.emit(w * 0.72f, h * 0.50f, 20, Color.parseColor("#E74C3C"), 6.0f, 6.0f, 18, true);
                }
            }
            if (actionFrame == 0) {
                currentAction = "IDLE";
                screenShakeX = 0f;
                screenShakeY = 0f;
            }
            invalidate();
        } else if (particleSys.getActiveCount() > 0 || frameAnim.isPlaying()) {
            invalidate();
        }

        if (effectTimer > 0) {
            effectTimer--;
            invalidate();
        }
    }

    // ==========================================
    // 1. 横屏选角立绘
    // ==========================================
    private void drawCharacterSelect(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0A0F17"));
        canvas.drawRect(0, 0, w, h, paint);

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

        // 八卦天地结界网格面片
        int auraColor = selectedCharIndex == 0 ? Color.parseColor("#2ECC71") :
                        selectedCharIndex == 1 ? Color.parseColor("#E84118") :
                        Color.parseColor("#F1C40F");
        meshVFX.drawArrayGridMesh(canvas, cx, cy - 20 * scale, 130 * scale, globalTime * 20.0f, auraColor, Color.parseColor("#2C3E50"));

        if (selectedCharIndex == 0) {
            drawSwordImmortal(canvas, cx, cy, scale);
        } else if (selectedCharIndex == 1) {
            drawFoxFairy(canvas, cx, cy, scale);
        } else {
            drawTitanWarrior(canvas, cx, cy, scale);
        }

        paint.setStyle(Paint.Style.FILL);
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
    // 2. 横屏青绿大地图
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
    // 3. 粒子/面片/序列帧动作复合战场
    // ==========================================
    private void drawCombatField(Canvas canvas, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#0D1520"));
        canvas.drawRect(0, 0, w, h, paint);

        float scale = Math.min(w, h) * 0.003f;

        // 地面八卦旋转面片网格 (Quad Array Mesh)
        meshVFX.drawArrayGridMesh(canvas, w * 0.5f, h * 0.55f, Math.min(w, h) * 0.42f, globalTime * 15.0f, Color.parseColor("#2ECC71"), Color.parseColor("#152434"));

        // 角色骨骼动作（待机轻微呼吸律动 + 普攻突刺冲锋）
        float px = w * 0.28f;
        float py = h * 0.60f + (float)Math.sin(globalTime * 3.0f) * 4.0f; // 呼吸悬浮

        if ("SLASH".equals(currentAction)) {
            if ("TITAN".equals(activeHeroTheme)) {
                py += (actionFrame > 12 ? (25 - actionFrame) * 3.5f : actionFrame * 3.5f);
            } else if ("FOX".equals(activeHeroTheme)) {
                px -= 12 * scale;
            } else {
                px += (actionFrame > 12 ? (25 - actionFrame) * 4.5f : actionFrame * 4.5f);
            }
        } else if ("CAST".equals(currentAction)) {
            py -= 16 * scale;
        }

        // 绘制分面片骨骼角色
        if ("FOX".equals(activeHeroTheme)) {
            meshVFX.drawBodySegment(canvas, px, py + 15 * scale, 36 * scale, 60 * scale, 0, Color.parseColor("#C44569"));
            paint.setColor(Color.parseColor("#FF5E7E"));
            canvas.drawCircle(px, py - 30 * scale, 16 * scale, paint);
        } else if ("TITAN".equals(activeHeroTheme)) {
            meshVFX.drawBodySegment(canvas, px, py + 15 * scale, 56 * scale, 64 * scale, 0, Color.parseColor("#7F8C8D"));
            paint.setColor(Color.parseColor("#D35400"));
            canvas.drawCircle(px, py - 34 * scale, 22 * scale, paint);
        } else {
            meshVFX.drawBodySegment(canvas, px, py + 15 * scale, 44 * scale, 60 * scale, 0, Color.parseColor("#2C4E5E"));
            paint.setColor(Color.parseColor("#58D68D"));
            canvas.drawCircle(px, py - 32 * scale, 18 * scale, paint);
        }

        // 怪物立绘
        float mScale = scale * monsterTier.scale;
        float ex = w * 0.72f;
        float ey = h * 0.50f + (float)Math.cos(globalTime * 2.5f) * 3.0f;

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

        paint.setColor(Color.parseColor(monsterTier.colorHex));
        paint.setTextSize(14 * scale);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("【" + monsterTier.title + "】" + monsterName, ex, ey - 60 * mScale, paint);

        if (isEnemyBroken) {
            paint.setColor(Color.parseColor("#F39C12"));
            paint.setTextSize(22);
            canvas.drawText("【 破韧虚弱 · 伤害暴增50% 】", ex, ey - 85 * mScale, paint);
        }

        // 面片特效：绘制剑气/法术斩击 Ribbon 面片
        if (projectileX > 0) {
            float curProjX = w * projectileX;
            float curProjY = h * 0.52f;
            meshVFX.drawSlashRibbon(
                canvas,
                curProjX - 80 * scale, curProjY,
                curProjX + 30 * scale, curProjY,
                24 * scale,
                Color.parseColor("#2ECC71"), Color.WHITE,
                (float)(actionFrame) / 28.0f
            );
        }

        drawBar(canvas, 20, 20, w * 0.38f, 16, playerHpPercent, Color.parseColor("#2ECC71"), "我方真元气血");
        drawBar(canvas, w * 0.58f, 20, w * 0.38f, 16, enemyHpPercent, Color.parseColor(monsterTier.colorHex), monsterName + " 气血");
        drawBar(canvas, w * 0.58f, 44, w * 0.38f, 10, enemyTenacityPercent, Color.parseColor("#F39C12"), "定力韧性");

        if (effectTimer > 0 && !combatEffectText.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setTextSize(26);
            paint.setTextAlign(Paint.Align.CENTER);
            float alpha = (float)effectTimer / 42.0f;
            paint.setAlpha((int)(alpha * 255));
            canvas.drawText(combatEffectText, w * 0.5f, h * 0.35f - (42 - effectTimer), paint);
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
