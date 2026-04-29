package io.github.some_example_name.enemy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.ExpOrb;

/**
 * 敌人抽象基类，定义所有敌人的通用属性和行为。
 * <p>
 * 设计目的：
 * <ul>
 *   <li>每种敌人单独一个文件，继承此类</li>
 *   <li>通用逻辑（追击、血量、死亡判定）由基类统一维护</li>
 *   <li>外观通过 {@link #render} 由子类实现</li>
 * </ul>
 * <p>
 * 扩展示例：新增"精英怪"时，新建类继承 Enemy，
 * 重写 {@link #render} 绘制更大体型和不同颜色即可。
 */
public abstract class Enemy {

    protected float x;
    protected float y;
    protected float speed;
    protected float radius;
    protected int hp;
    protected int maxHp;
    protected boolean alive;
    protected int damage;              // 触碰玩家时造成的伤害
    protected float damageCooldown;    // 对玩家造成伤害的冷却计时器（秒）
    protected static final float DAMAGE_COOLDOWN_DURATION = 0.5f; // 每只怪物触碰冷却 0.5 秒
    protected int baseCoinValue;       // 死亡时掉落的基础金币数量

    public Enemy(float x, float y) {
        this.x = x;          // X 坐标
        this.y = y;          // Y 坐标
        this.alive = true;   // 是否存活
        this.damage = 10;    // 默认触碰伤害
        this.damageCooldown = 0; // 初始无冷却
        this.baseCoinValue = 1; // 默认掉落 1 金币
    }

    /**
     * 更新敌人位置：朝玩家方向移动。
     */
    public void update(float deltaTime, float playerCenterX, float playerCenterY) {
        update(deltaTime, playerCenterX, playerCenterY, speed);
    }

    /**
     * 更新敌人位置：朝玩家方向移动（可指定实际移速，用于减速光环等效果）。
     */
    public void update(float deltaTime, float playerCenterX, float playerCenterY, float actualSpeed) {
        // 递减对玩家的伤害冷却
        if (damageCooldown > 0) {
            damageCooldown -= deltaTime;
            if (damageCooldown < 0) damageCooldown = 0;
        }

        float dx = playerCenterX - x;
        float dy = playerCenterY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            x += (dx / distance) * actualSpeed * deltaTime;
            y += (dy / distance) * actualSpeed * deltaTime;
        }
    }

    /**
     * 受到伤害。
     */
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    /**
     * 击退效果：将敌人沿指定方向推动一段距离。
     */
    public void knockback(float dirX, float dirY, float distance) {
        x += dirX * distance;
        y += dirY * distance;
    }

    /**
     * 平移敌人位置，用于碰撞推开。
     *
     * @param dx X 方向位移
     * @param dy Y 方向位移
     */
    public void push(float dx, float dy) {
        x += dx;
        y += dy;
    }

    /**
     * 死亡时掉落经验球。子类可重写以改变掉落数值。
     */
    public ExpOrb dropExp() {
        return new ExpOrb(x, y, 10);
    }

    // ==================== 抽象方法 ====================

    /**
     * 渲染敌人外观。子类必须实现。
     */
    public abstract void render(ShapeRenderer shapeRenderer);

    // ==================== Getter ====================

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isAlive() {
        return alive;
    }

    public float getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public float getDamageCooldown() {
        return damageCooldown;
    }

    public void setDamageCooldown(float damageCooldown) {
        this.damageCooldown = damageCooldown;
    }

    public void resetDamageCooldown() {
        this.damageCooldown = DAMAGE_COOLDOWN_DURATION;
    }

    public int getBaseCoinValue() {
        return baseCoinValue;
    }

    public void setBaseCoinValue(int baseCoinValue) {
        this.baseCoinValue = baseCoinValue;
    }
}
