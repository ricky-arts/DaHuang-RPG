package com.honghuang.rpg;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * 8x4 战棋网格棋盘渲染、棋子立绘模型、面片阵法与自动法术粒子演播
 */
public class ChessBoardView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ParticleSystem particleSys = new ParticleSystem();
    private final MeshVFX meshVFX = new MeshVFX();
    private final FrameAnimationController frameAnim = new FrameAnimationController();

    private ChessEngine engine;

    // 选中状态
    public int selectedBenchIdx = -1;
    public int selectedBoardR = -1;
    public int selectedBoardC = -1;
    public int selectedEquipIdx = -1;

    // 战斗仿真状态 (0: 准备/布阵阶段, 1: 战斗回合阶段)
    public int battlePhase = 0;
    private int combatTimer = 0;
    private float globalTime = 0f;

    public interface OnTileClickListener {
        void onBenchClicked(int idx);
        void onBoardTileClicked(int r, int c);
    }
    private OnTileClickListener clickListener;

    public ChessBoardView(Context context) {
        super(context);
    }

    public void setEngine(ChessEngine engine) {
        this.engine = engine;
        invalidate();
    }

    public void setOnTileClickListener(OnTileClickListener l) {
        this.clickListener = l;
    }

    public void triggerCombatTurn() {
        this.battlePhase = 1;
        this.combatTimer = 60; // 60 帧战斗动画
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || engine == null) return;

        globalTime += 0.05f;

        // 1. 绘制东方青绿水墨深色棋盘底衬
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#081018"));
        canvas.drawRect(0, 0, w, h, paint);

        // 2. 绘制 8x4 战斗主棋盘网格 (占画布上半部)
        float boardTop = 20.0f;
        float boardBottom = h * 0.72f;
        float boardH = boardBottom - boardTop;
        float tileW = (w - 30.0f) / 8.0f;
        float tileH = boardH / 4.0f;

        // 敌我分界线光效 (楚河汉界)
        paint.setColor(Color.parseColor("#34495E"));
        paint.setStrokeWidth(3);
        canvas.drawLine(15, boardTop + tileH * 2, w - 15, boardTop + tileH * 2, paint);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                float tx = 15 + c * tileW;
                float ty = boardTop + r * tileH;
                boolean isSelected = (r == selectedBoardR && c == selectedBoardC);

                // 交替青绿与墨石玉格
                boolean isEven = (r + c) % 2 == 0;
                int tileColor = r < 2 ? (isEven ? Color.parseColor("#15202B") : Color.parseColor("#101A24"))  // 敌方半场
                                      : (isEven ? Color.parseColor("#122A26") : Color.parseColor("#0D211E")); // 我方半场

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(tileColor);
                canvas.drawRect(tx + 2, ty + 2, tx + tileW - 2, ty + tileH - 2, paint);

                // 选中高亮光圈
                if (isSelected) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);
                    paint.setColor(Color.parseColor("#F1C40F"));
                    canvas.drawRect(tx + 2, ty + 2, tx + tileW - 2, ty + tileH - 2, paint);
                }

                // 绘制敌方棋子
                if (r < 2 && engine.enemyBoard[r][c] != null) {
                    drawChampionPawn(canvas, engine.enemyBoard[r][c], tx + tileW * 0.5f, ty + tileH * 0.5f, tileW * 0.38f, false);
                }

                // 绘制我方上阵棋子
                if (r >= 2 && engine.board[r][c] != null) {
                    drawChampionPawn(canvas, engine.board[r][c], tx + tileW * 0.5f, ty + tileH * 0.5f, tileW * 0.38f, true);
                }
            }
        }

        // 3. 绘制我方备战席 (8 格)
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
                drawChampionPawn(canvas, engine.bench[i], bx + benchTileW * 0.5f, benchTop + benchTileH * 0.55f, benchTileW * 0.36f, true);
            }
        }

        // 4. 驱动战斗动画、法术粒子与序列帧
        if (battlePhase == 1 && combatTimer > 0) {
            combatTimer--;
            // 棋盘法术交织粒子演播
            if (combatTimer % 8 == 0) {
                float sx = 15 + (float)(Math.random() * (w - 30));
                float sy = boardTop + tileH * 3;
                float tx = 15 + (float)(Math.random() * (w - 30));
                float ty = boardTop + tileH * 1;
                particleSys.emitDirected(sx, sy, tx, ty, 12, Color.parseColor("#2ECC71"), 6.0f, 15);
                frameAnim.play(FrameAnimationController.AnimType.HIT_EXPLODE, tx, ty, Color.parseColor("#F1C40F"));
            }
            if (combatTimer == 0) {
                battlePhase = 0; // 回归准备阶段
            }
            invalidate();
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
     * 绘制棋盘上的独立棋子法相 (包含星级、品质光圈与装备小图标)
     */
    private void drawChampionPawn(Canvas canvas, ChessChampion c, float cx, float cy, float radius, boolean isPlayer) {
        paint.setStyle(Paint.Style.FILL);

        // 星级专属光环 (1星绿/2星蓝/3星金闪闪)
        int starColor = (c.star == 1) ? Color.parseColor("#2ECC71") :
                        (c.star == 2) ? Color.parseColor("#3498DB") :
                        Color.parseColor("#F1C40F");

        paint.setColor(starColor);
        paint.setAlpha(60);
        canvas.drawCircle(cx, cy, radius * 1.25f, paint);

        // 3星大能旋转网格法相
        if (c.star == 3) {
            meshVFX.drawArrayGridMesh(canvas, cx, cy, radius * 1.3f, globalTime * 30.0f, Color.parseColor("#F1C40F"), Color.WHITE);
        }

        // 棋子底台
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isPlayer ? Color.parseColor("#2C3E50") : Color.parseColor("#800000"));
        paint.setAlpha(255);
        canvas.drawCircle(cx, cy, radius, paint);

        // 棋子职业边框
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(starColor);
        canvas.drawCircle(cx, cy, radius, paint);

        // 棋子名字 (如: 杨戬)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(radius * 0.58f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.name.substring(0, Math.min(2, c.name.length())), cx, cy + radius * 0.22f, paint);

        // 星级五角星标记 (★ / ★★ / ★★★)
        String stars = (c.star == 1) ? "★" : (c.star == 2 ? "★★" : "★★★");
        paint.setColor(Color.parseColor("#F1C40F"));
        paint.setTextSize(radius * 0.42f);
        canvas.drawText(stars, cx, cy - radius * 0.65f, paint);

        // 装备小图标标记 (槽位 1~3)
        if (!c.equipments.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            for (int k = 0; k < c.equipments.size(); k++) {
                paint.setColor(c.equipments.get(k).type.isCombined ? Color.parseColor("#9B59B6") : Color.parseColor("#F39C12"));
                canvas.drawCircle(cx - radius * 0.4f + k * 8.0f, cy + radius * 0.85f, 3.5f, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int w = getWidth();
            int h = getHeight();

            // 点击备战席
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

            // 点击棋盘
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
