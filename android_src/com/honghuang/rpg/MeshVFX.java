package com.honghuang.rpg;

import android.graphics.*;

/**
 * 2D 变形面片与变形网格系统 (Quad & Mesh VFX)
 * 支持剑道气浪面片、八卦结界流动网格、诛仙巨剑面片与角色分面片骨骼动作
 */
public class MeshVFX {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    /**
     * 绘制受法线扰动的飞剑/刀芒气浪面片 (Energy Slash Ribbon)
     */
    public void drawSlashRibbon(Canvas canvas, float startX, float startY, float endX, float endY, float width, int colorStart, int colorEnd, float progress) {
        float dx = endX - startX;
        float dy = endY - startY;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.001f) return;

        float nx = -dy / len * width * (1.0f - progress * 0.5f);
        float ny = dx / len * width * (1.0f - progress * 0.5f);

        // 构造四边形 Ribbon 面片
        path.reset();
        path.moveTo(startX - nx * 0.2f, startY - ny * 0.2f);
        path.lineTo(endX - nx, endY - ny);
        path.lineTo(endX + nx, endY + ny);
        path.lineTo(startX + nx * 0.2f, startY + ny * 0.2f);
        path.close();

        LinearGradient gradient = new LinearGradient(
            startX, startY, endX, endY,
            colorStart, colorEnd, Shader.TileMode.CLAMP
        );
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(gradient);
        paint.setAlpha((int)((1.0f - progress) * 220));
        canvas.drawPath(path, paint);
        paint.setShader(null);
    }

    /**
     * 绘制旋转八卦天地结界网格 (Aura Array Grid Mesh)
     */
    public void drawArrayGridMesh(Canvas canvas, float cx, float cy, float radius, float rotationDeg, int primaryColor, int ringColor) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(ringColor);
        paint.setAlpha(160);

        // 外圈与内圈同心圆网格
        canvas.drawCircle(cx, cy, radius, paint);
        canvas.drawCircle(cx, cy, radius * 0.65f, paint);
        canvas.drawCircle(cx, cy, radius * 0.35f, paint);

        // 旋转八向经纬网格线
        for (int i = 0; i < 8; i++) {
            double rad = Math.toRadians(rotationDeg + i * 45.0);
            float x1 = (float)(cx + Math.cos(rad) * radius * 0.35f);
            float y1 = (float)(cy + Math.sin(rad) * radius * 0.35f);
            float x2 = (float)(cx + Math.cos(rad) * radius);
            float y2 = (float)(cy + Math.sin(rad) * radius);
            paint.setColor(primaryColor);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

    /**
     * 绘制角色身体的独立分面片层 (用于呼吸和动作形变)
     */
    public void drawBodySegment(Canvas canvas, float cx, float cy, float w, float h, float angleDeg, int color) {
        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(angleDeg);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        RectF r = new RectF(-w * 0.5f, -h * 0.5f, w * 0.5f, h * 0.5f);
        canvas.drawRoundRect(r, 8, 8, paint);
        canvas.restore();
    }
}
