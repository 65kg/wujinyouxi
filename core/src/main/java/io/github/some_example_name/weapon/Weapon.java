package io.github.some_example_name.weapon;

import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.enemy.Enemy;

/**
 * 武器抽象基类，定义所有武器的通用属性和行为。
 * <p>
 * 设计目的：
 * <ul>
 *   <li>每种武器单独一个文件，继承此类</li>
 *   <li>基础属性（伤害、范围、冷却等）由基类统一维护</li>
 *   <li>特殊效果（击退、弹射、额外攻击等）通过重写 {@link #onHit} 实现</li>
 * </ul>
 * <p>
 * 扩展示例：新增"雷电法杖"时，只需新建一个类继承 Weapon，重写 onHit 添加麻痹效果即可。
 */
public abstract class Weapon {

    protected final String name;
    protected final String description;
    protected final Color color;
    protected final int damage;
    protected final float range;
    protected final float cooldown;
    protected final int pierce;
    protected final int extraTargets;
    protected final int bounceCount;

    public Weapon(String name, String description, Color color,
                  int damage, float range, float cooldown,
                  int pierce, int extraTargets, int bounceCount) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.pierce = pierce;
        this.extraTargets = extraTargets;
        this.bounceCount = bounceCount;
    }

    /**
     * 命中特效钩子。子类可重写此方法实现武器独有效果（如击退、流血、爆炸等）。
     *
     * @param enemy        被命中的敌人
     * @param projectileX  投射物当前 X 坐标
     * @param projectileY  投射物当前 Y 坐标
     */
    public void onHit(Enemy enemy, float projectileX, float projectileY) {
        // 默认无特效，子类按需重写
    }

    // ==================== Getter ====================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public int getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public float getCooldown() {
        return cooldown;
    }

    public int getPierce() {
        return pierce;
    }

    public int getExtraTargets() {
        return extraTargets;
    }

    public int getBounceCount() {
        return bounceCount;
    }
}
