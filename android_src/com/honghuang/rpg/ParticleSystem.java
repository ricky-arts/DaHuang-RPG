package com.honghuang.rpg;

import android.graphics.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * 粒子实体：具备初速度、加速度、角速度、生命周期、颜色衰减与尺寸缩放
 */
class Particle {
    public float x, y;
    public float vx, vy;
    public float ax, ay;
    public float size;
    public float rotation;
    public float rotationSpeed;
    public int life;
    public int maxLife;
    public int color;
    public boolean additive;

    public Particle(float x, float y, float vx, float vy, float ax, float ay, float size, int maxLife, int color, boolean additive) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ax = ax;
        this.ay = ay;
        this.size = size;
        this.life = maxLife;
        this.maxLife = maxLife;
        this.color = color;
        this.additive = additive;
        this.rotation = (float)(Math.random() * 360.0);
        this.rotationSpeed = (float)((Math.random() - 0.5) * 12.0);
    }

    public boolean update() {
        vx += ax;
        vy += ay;
        x += vx;
        y += vy;
        rotation += rotationSpeed;
        life--;
        return life > 0;
    }
}

/**
 * 专业游戏级粒子特效发射器 (ParticleSystem)
 * 支持剑气光尘、灭神雷火、红莲业火飘散、受击金煞碎石
 */
public class ParticleSystem {
    private final ArrayList<Particle> particles = new ArrayList<>(256);
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng = new Random();

    public void emit(float x, float y, int count, int color, float speed, float size, int lifeSpan, boolean additive) {
        for (int i = 0; i < count; i++) {
            double angle = rng.nextDouble() * Math.PI * 2.0;
            float spd = (float)(speed * (0.6 + rng.nextDouble() * 0.8));
            float vx = (float)(Math.cos(angle) * spd);
            float vy = (float)(Math.sin(angle) * spd);
            float sz = (float)(size * (0.7 + rng.nextDouble() * 0.6));
            particles.add(new Particle(x, y, vx, vy, 0, 0.05f, sz, lifeSpan, color, additive));
        }
    }

    public void emitDirected(float x, float y, float targetX, float targetY, int count, int color, float size, int lifeSpan) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        if (dist <= 0.001f) dist = 1f;
        float ndx = dx / dist;
        float ndy = dy / dist;

        for (int i = 0; i < count; i++) {
            float spd = (float)(14.0 + rng.nextDouble() * 6.0);
            float perp = (float)((rng.nextDouble() - 0.5) * 4.0);
            float vx = ndx * spd - ndy * perp;
            float vy = ndy * spd + ndx * perp;
            particles.add(new Particle(x, y, vx, vy, 0, 0, size, lifeSpan, color, true));
        }
    }

    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            if (!it.next().update()) {
                it.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : particles) {
            float progress = (float)p.life / (float)p.maxLife;
            int alpha = (int)(progress * 255);
            paint.setColor(p.color);
            paint.setAlpha(alpha);

            float curSize = p.size * (0.5f + progress * 0.5f);
            canvas.drawCircle(p.x, p.y, curSize, paint);
        }
    }

    public int getActiveCount() {
        return particles.size();
    }

    public void clear() {
        particles.clear();
    }
}
