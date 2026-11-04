package com.honghuang.rpg;

import android.graphics.*;

/**
 * 序列帧动画渲染引擎 (Flip-book Frame Animation Engine)
 * 支持多阶段爆裂受击帧、红莲业火燃烧帧、雷电破裂序列帧
 */
public class FrameAnimationController {
    public enum AnimType {
        HIT_EXPLODE,      // 受击火花多帧爆裂
        LOTUS_BURST,      // 红莲业火 8 帧连续绽放
        THUNDER_CRACK,    // 灭神雷霆 6 帧破裂
        SWORD_SLASH_ARC   // 剑意圆弧斩击序列帧
    }

    private AnimType currentType;
    private float x, y;
    private int currentFrame = 0;
    private int totalFrames = 8;
    private boolean isPlaying = false;
    private int color = Color.YELLOW;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void play(AnimType type, float x, float y, int color) {
        this.currentType = type;
        this.x = x;
        this.y = y;
        this.color = color;
        this.currentFrame = 0;
        this.totalFrames = (type == AnimType.THUNDER_CRACK) ? 6 : 8;
        this.isPlaying = true;
    }

    public boolean update() {
        if (!isPlaying) return false;
        currentFrame++;
        if (currentFrame >= totalFrames) {
            isPlaying = false;
            return false;
        }
        return true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void draw(Canvas canvas) {
        if (!isPlaying) return;

        float progress = (float)currentFrame / (float)totalFrames;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        if (currentType == AnimType.HIT_EXPLODE) {
            // 受击爆裂多帧扩散：同心外扩冲击波 + 放射刺
            paint.setStrokeWidth(6.0f * (1.0f - progress));
            float r = 20.0f + progress * 65.0f;
            paint.setAlpha((int)((1.0f - progress) * 255));
            canvas.drawCircle(x, y, r, paint);

            // 8 向飞溅刺
            paint.setStrokeWidth(3.0f);
            for (int i = 0; i < 8; i++) {
                double rad = Math.toRadians(i * 45.0 + currentFrame * 12.0);
                float x1 = (float)(x + Math.cos(rad) * (r * 0.4f));
                float y1 = (float)(y + Math.sin(rad) * (r * 0.4f));
                float x2 = (float)(x + Math.cos(rad) * r);
                float y2 = (float)(y + Math.sin(rad) * r);
                canvas.drawLine(x1, y1, x2, y2, paint);
            }
        } else if (currentType == AnimType.LOTUS_BURST) {
            // 红莲业火多帧绽放
            paint.setStyle(Paint.Style.FILL);
            int alpha = (int)((1.0f - progress) * 220);
            paint.setAlpha(alpha);

            float r = 25.0f + progress * 55.0f;
            for (int i = 0; i < 6; i++) {
                double rad = Math.toRadians(i * 60.0 + progress * 40.0);
                float px = (float)(x + Math.cos(rad) * r);
                float py = (float)(y + Math.sin(rad) * r);
                paint.setColor(color);
                canvas.drawCircle(px, py, 14.0f * (1.0f - progress * 0.6f), paint);
            }
        } else if (currentType == AnimType.THUNDER_CRACK) {
            // 雷电连环劈裂多帧
            paint.setStrokeWidth(5.0f * (1.0f - progress));
            paint.setColor(Color.parseColor("#F1C40F"));
            paint.setAlpha((int)((1.0f - progress) * 255));

            float prevX = x;
            float prevY = y - 160.0f;
            for (int i = 1; i <= 5; i++) {
                float segY = y - 160.0f + (i * 32.0f);
                float offset = (float)((Math.sin(i * 3.0 + currentFrame) * 25.0f));
                float nextX = x + offset;
                canvas.drawLine(prevX, prevY, nextX, segY, paint);
                prevX = nextX;
                prevY = segY;
            }
        }
    }
}
