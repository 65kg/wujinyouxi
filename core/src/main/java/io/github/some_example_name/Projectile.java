package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.enemy.Enemy;

import java.util.ArrayList;

public class Projectile {
    private float x;
    private float y;
    private float speed;
    private float radius;
    private int damage;
    private float dirX;
    private float dirY;
    private boolean active;
    private int pierceCount;
    private int bounceCount;
    private ArrayList<Enemy> hitEnemies;

    public Projectile(float x, float y, float targetX, float targetY, int damage, int pierceCount) {
        this.x = x;                         // X 坐标
        this.y = y;                         // Y 坐标
        this.speed = 400;                   // 飞行速度
        this.radius = 6;                    // 碰撞半径
        this.damage = damage;               // 伤害值
        this.pierceCount = pierceCount;     // 穿透次数
        this.active = true;                 // 是否活跃
        this.hitEnemies = new ArrayList<>();// 已命中敌人列表

        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            this.dirX = dx / distance;      // X 方向单位向量
            this.dirY = dy / distance;      // Y 方向单位向量
        } else {
            this.dirX = 0;
            this.dirY = 0;
        }
    }

    public void update(float deltaTime) {
        x += dirX * speed * deltaTime;
        y += dirY * speed * deltaTime;
    }

    public boolean checkHit(Enemy enemy) {
        if (hitEnemies.contains(enemy)) return false;

        float dx = x - enemy.getX();
        float dy = y - enemy.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < (radius + enemy.getRadius());
    }

    public void onHit(Enemy enemy) {
        hitEnemies.add(enemy);
        pierceCount--;
        if (pierceCount < 0) {
            active = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.valueOf("#F1C40F"));
        shapeRenderer.circle(x, y, radius);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public int getDamage() {
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getBounceCount() {
        return bounceCount;
    }

    public void setBounceCount(int bounceCount) {
        this.bounceCount = bounceCount;
    }

    /**
     * 检查该投射物是否已经命中过指定敌人。
     */
    public boolean hasHit(Enemy enemy) {
        return hitEnemies.contains(enemy);
    }

    /**
     * 改变投射物飞行方向，朝向新的目标。
     *
     * @param targetX 新目标 X 坐标
     * @param targetY 新目标 Y 坐标
     */
    public void redirectTo(float targetX, float targetY) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            this.dirX = dx / dist;
            this.dirY = dy / dist;
        }
    }
}
