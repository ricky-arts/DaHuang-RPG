package com.honghuang.rpg;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * 战棋网格棋盘、实体平滑位移、血条/蓝条渲染、面片阵法与自动法术粒子演播
 */
public class ChessBoardView extends View implements ChessBattleSimulator.BattleEventListener {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ParticleSystem particleSys = new ParticleSystem();
    private final MeshVFX meshVFX = new MeshVFX();
    private final FrameAnimationController frameAnim = new FrameAnimationController();

    private ChessEngine engine;
    private final ChessBattleSimulator simulator = new ChessBattleSimulator();

    // 选中状态
    public int selectedBenchIdx = -1;
    public int selectedBoardR = -1;
    public int selectedBoardC = -1;

    // 战斗仿真状态 (0: 准备/布阵阶段, 1: 真实对局移动与攻砍阶段)
    public int battlePhase = 0;
    private int combatDurationTicks = 0;
    private float globalTime = 0f;

    public interface OnTileClickListener {
        void onBenchClicked(int idx);
        void onBoardTileClicked(int r, int c);
        void onCombatFinished(boolean isVictory);
    }
    private OnTileClickListener clickListener;

    public ChessBoardView(Context context) {
        super(context);
        simulator.listener = this;
    }

    public void setEngine(ChessEngine engine) {
        this.engine = engine;
        invalidate();
    }

    public void setOnTileClickListener(OnTileClickListener l) {
        this.clickListener = l;
    }

    public void startLiveBattle() {
        if (engine == null) return;
        simulator.initializeBattle(engine.board, engine.enemyBoard);
        this.battlePhase = 1;
        this.combatDurationTicks = 0;
        invalidate();
    }

    @Override
    public void onAttackHit(LiveChampionEntity attacker, LiveChampionEntity target, int damage, boolean isCrit) {
        int w = getWidth();
        int h = getHeight();
        float tileW = (w - 30.0f) / 8.0f;
        float tileH = (h * 0.72f - 20.0f) / 4.0f;

        float tx = 15 + target.gridX * tileW + tileW * 0.5f;
        float ty = 20 + target.gridY * tileH + tileH * 0.5f;

        // 命中火花与序列帧爆破
        particleSys.emit(tx, ty, isCrit ? 18 : 8, isCrit ? Color.parseColor("#F1C40F") : Color.WHITE, 4.0f, 4.0f, 12, true);
        if (isCrit) {
            frameAnim.play(FrameAnimationController.AnimType.HIT_EXPLODE, tx, ty, Color.parseColor("#E74C3C"));
        }
    }

    @Override
    public void onSkillCast(LiveChampionEntity caster, String skillName) {
        int w = getWidth();
        int h = getHeight();
        float tileW = (w - 30.0f) / 8.0f;
        float tileH = (h * 0.72f - 20.0f) / 4.0f;

        float cx = 15 + caster.gridX * tileW + tileW * 0.5f;
        float cy = 20 + caster.gridY * tileH + tileH * 0.5f;

        // 满蓝神威施法特效 (通天教主剑雨 / 赵公明定海神珠 / 哪吒真火)
        if (skillName.contains("诛仙")) {
            frameAnim.play(FrameAnimationController.AnimType.LOTUS_BURST, w * 0.5f, h * 0.36f, Color.parseColor("#9B59B6"));
            particleSys.emit(w * 0.5f, h * 0.36f, 40, Color.parseColor("#9B59B6"), 8.0f, 8.0f, 25, true);
        } else if (skillName.contains("定海")) {
            frameAnim.play(FrameAnimationController.AnimType.HIT_EXPLODE, cx, cy, Color.parseColor("#3498DB"));
            particleSys.emit(cx, cy, 30, Color.parseColor("#2980B9"), 6.0f, 7.0f, 20, true);
        } else {
            frameAnim.play(FrameAnimationController.AnimType.THUNDER_CRACK, cx, cy, Color.parseColor("#F1C40F"));
            particleSys.emit(cx, cy, 25, Color.parseColor("#F39C12"), 6.0f, 6.0f, 18, true);
        }
    }

    @Override
    public void onUnitDied(LiveChampionEntity unit) {
        int w = getWidth();
        int h = getHeight();
        float tileW = (w - 30.0f) / 8.0f;
        float tileH = (h * 0.72f - 20.0f) / 4.0f;
        float tx = 15 + unit.gridX * tileW + tileW * 0.5f;
        float ty = 20 + unit.gridY * tileH + tileH * 0.5f;
        particleSys.emit(tx, ty, 20, Color.GRAY, 3.0f, 5.0f, 16, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || engine == null) return;

        globalTime += 0.05f;

        // 1. 底衬
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#081018"));
        canvas.drawRect(0, 0, w, h, paint);

        // 2. 8x4 战棋网格
        float boardTop = 20.0f;
        float boardBottom = h * 0.72f;
        float boardH = boardBottom - boardTop;
        float tileW = (w - 30.0f) / 8.0f;
        float tileH = boardH / 4.0f;

        paint.setColor(Color.parseColor("#34495E"));
        paint.setStrokeWidth(3);
        canvas.drawLine(15, boardTop + tileH * 2, w - 15, boardTop + tileH * 2, paint);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                float tx = 15 + c * tileW;
                float ty = boardTop + r * tileH;
                boolean isSelected = (r == selectedBoardR && c == selectedBoardC);

                boolean isEven = (r + c) % 2 == 0;
                int tileColor = r < 2 ? (isEven ? Color.parseColor("#15202B") : Color.parseColor("#101A24"))
                                      : (isEven ? Color.parseColor("#122A26") : Color.parseColor("#0D211E"));

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(tileColor);
                canvas.drawRect(tx + 2, ty + 2, tx + tileW - 2, ty + tileH - 2, paint);

                if (isSelected && battlePhase == 0) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(Color.parseColor("#F1C40F"));
                    canvas.drawRect(tx + 2, ty + 2, tx + tileW - 2, ty + tileH - 2, paint);
                }

                // 准备阶段绘制静态站位棋子
                if (battlePhase == 0) {
                    if (r < 2 && engine.enemyBoard[r][c] != null) {
                        drawStaticChampionPawn(canvas, engine.enemyBoard[r][c], tx + tileW * 0.5f, ty + tileH * 0.5f, tileW * 0.36f, false);
                    }
                    if (r >= 2 && engine.board[r][c] != null) {
                        drawStaticChampionPawn(canvas, engine.board[r][c], tx + tileW * 0.5f, ty + tileH * 0.5f, tileW * 0.36f, true);
                    }
                }
            }
        }

        // 3. 战斗阶段：绘制实时平滑位移走砍的真实实体 (带动态血条与蓝条)
        if (battlePhase == 1) {
            // 推进一次物理心跳步进
            simulator.updateTick();
            combatDurationTicks++;

            // 绘制敌方活动棋子
            for (LiveChampionEntity e : simulator.enemyUnits) {
                if (e.isAlive) {
                    float px = 15 + e.gridX * tileW + tileW * 0.5f;
                    float py = boardTop + e.gridY * tileH + tileH * 0.5f;
                    drawLiveChampionPawn(canvas, e, px, py, tileW * 0.36f, false);
                }
            }

            // 绘制我方活动棋子
            for (LiveChampionEntity u : simulator.playerUnits) {
                if (u.isAlive) {
                    float px = 15 + u.gridX * tileW + tileW * 0.5f;
                    float py = boardTop + u.gridY * tileH + tileH * 0.5f;
                    drawLiveChampionPawn(canvas, u, px, py, tileW * 0.36f, true);
                }
            }

            // 检查回合对弈是否结束
            if (simulator.isBattleFinished || combatDurationTicks > 30 * 20) {
                battlePhase = 0;
                if (clickListener != null) {
                    clickListener.onCombatFinished(simulator.isPlayerVictory);
                }
            }
            invalidate(); // 保持 30~60 FPS 持续渲染
        }

        // 4. 绘制我方备战席 (8 格)
        float benchTop = h * 0.76f;
        float benchTileW = (w - 20.0f) / 8.0f;
        float benchTileH = h * 0.20f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#485460"));
        paint.setStrokeWidth(2);
        canvas.drawRect(10, benchTop, w - 10, benchTop + benchTileH, paint);

        for (int i = 0; i < 8; i++) {
            float bx = 10 + i * benchTileW;
            boolean isSelected = (i == selectedBenchIdx);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isSelected ? Color.parseColor("#2C3E50") : Color.parseColor("#1E272E"));
            canvas.drawRect(bx + 2, benchTop + 2, bx + benchTileW - 2, benchTop + benchTileH - 2, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#718093"));
            paint.setTextSize(11);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText((i + 1) + "", bx + benchTileW * 0.5f, benchTop + 14, paint);

            if (engine.bench[i] != null) {
                drawStaticChampionPawn(canvas, engine.bench[i], bx + benchTileW * 0.5f, benchTop + benchTileH * 0.55f, benchTileW * 0.36f, true);
            }
        }

        frameAnim.draw(canvas);
        particleSys.draw(canvas);
        particleSys.update();
        frameAnim.update();

        if (particleSys.getActiveCount() > 0 || frameAnim.isPlaying()) {
            invalidate();
        }
    }

    /**
     * 准备阶段静态棋子
     */
    private void drawStaticChampionPawn(Canvas canvas, ChessChampion c, float cx, float cy, float radius, boolean isPlayer) {
        drawPawnCore(canvas, c.name, c.star, c.equipments.size(), cx, cy, radius, isPlayer, 1.0f, 0.0f);
    }

    /**
     * 战斗阶段实时动态棋子 (带血条与法力蓝条)
     */
    private void drawLiveChampionPawn(Canvas canvas, LiveChampionEntity live, float cx, float cy, float radius, boolean isPlayer) {
        float hpRate = (float)live.curHp / (float)live.maxHp;
        float manaRate = (float)live.curMana / (float)live.maxMana;
        drawPawnCore(canvas, live.baseData.name, live.baseData.star, live.baseData.equipments.size(), cx, cy, radius, isPlayer, hpRate, manaRate);
    }

    private void drawPawnCore(Canvas canvas, String name, int star, int eqCount, float cx, float cy, float radius, boolean isPlayer, float hpRate, float manaRate) {
        paint.setStyle(Paint.Style.FILL);

        int starColor = (star == 1) ? Color.parseColor("#2ECC71") :
                        (star == 2) ? Color.parseColor("#3498DB") :
                        Color.parseColor("#F1C40F");

        paint.setColor(starColor);
        paint.setAlpha(55);
        canvas.drawCircle(cx, cy, radius * 1.25f, paint);

        if (star == 3) {
            meshVFX.drawArrayGridMesh(canvas, cx, cy, radius * 1.3f, globalTime * 30.0f, Color.parseColor("#F1C40F"), Color.WHITE);
        }

        // 棋子底台
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isPlayer ? Color.parseColor("#2C3E50") : Color.parseColor("#800000"));
        paint.setAlpha(255);
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(starColor);
        canvas.drawCircle(cx, cy, radius, paint);

        // 名字
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(radius * 0.58f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name.substring(0, Math.min(2, name.length())), cx, cy + radius * 0.22f, paint);

        // 星级 (★ / ★★ / ★★★)
        String stars = (star == 1) ? "★" : (star == 2 ? "★★" : "★★★");
        paint.setColor(Color.parseColor("#F1C40F"));
        paint.setTextSize(radius * 0.42f);
        canvas.drawText(stars, cx, cy - radius * 0.65f, paint);

        // 血条 (绿色/红色)
        float barW = radius * 1.8f;
        float barH = 5.0f;
        float barY = cy - radius - 14.0f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        canvas.drawRect(cx - barW * 0.5f, barY, cx + barW * 0.5f, barY + barH, paint);

        paint.setColor(isPlayer ? Color.parseColor("#2ECC71") : Color.parseColor("#E74C3C"));
        canvas.drawRect(cx - barW * 0.5f, barY, cx - barW * 0.5f + (barW * hpRate), barY + barH, paint);

        // 蓝条 (天蓝色)
        float manaY = barY + barH + 1.0f;
        paint.setColor(Color.BLACK);
        canvas.drawRect(cx - barW * 0.5f, manaY, cx + barW * 0.5f, manaY + 3.0f, paint);
        paint.setColor(Color.parseColor("#3498DB"));
        canvas.drawRect(cx - barW * 0.5f, manaY, cx - barW * 0.5f + (barW * manaRate), manaY + 3.0f, paint);

        // 装备提示点
        if (eqCount > 0) {
            paint.setStyle(Paint.Style.FILL);
            for (int k = 0; k < eqCount; k++) {
                paint.setColor(Color.parseColor("#9B59B6"));
                canvas.drawCircle(cx - radius * 0.4f + k * 8.0f, cy + radius * 0.85f, 3.5f, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            if (battlePhase == 1) return true; // 战斗进行中禁止移动棋子

            float x = event.getX();
            float y = event.getY();
            int w = getWidth();
            int h = getHeight();

            float benchTop = h * 0.76f;
            float benchTileW = (w - 20.0f) / 8.0f;
            if (y >= benchTop) {
                int idx = (int)((x - 10) / benchTileW);
                if (idx >= 0 && idx < 8 && clickListener != null) {
                    clickListener.onBenchClicked(idx);
                    invalidate();
                    return true;
                }
            }

            float boardTop = 20.0f;
            float boardBottom = h * 0.72f;
            float tileW = (w - 30.0f) / 8.0f;
            float tileH = (boardBottom - boardTop) / 4.0f;
            if (y >= boardTop && y <= boardBottom) {
                int r = (int)((y - boardTop) / tileH);
                int c = (int)((x - 15) / tileW);
                if (r >= 0 && r < 4 && c >= 0 && c < 8 && clickListener != null) {
                    clickListener.onBoardTileClicked(r, c);
                    invalidate();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
