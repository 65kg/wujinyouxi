package io.github.some_example_name.weapon;

import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.enemy.Enemy;

/**
 * 手枪：高伤害单体武器，命中敌人时附带击退效果。
 * <p>
 * 特性：将敌人朝投射物飞行方向的反方向击退 60 像素，
 * 可打断敌人的近身节奏，创造输出空间。
 */
public class Pistol extends Weapon {

    private static final float KNOCKBACK_DISTANCE = 60f;

    public Pistol() {
        super("手枪", "自带击退效果",
              new Color(0.35f, 0.65f, 1.0f, 1f),
              15, 250, 0.5f, 0, 0, 0);
    }

    @Override
    public void onHit(Enemy enemy, float projectileX, float projectileY) {
        float dx = enemy.getX() - projectileX;
        float dy = enemy.getY() - projectileY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            enemy.knockback(dx / dist, dy / dist, KNOCKBACK_DISTANCE);
        }
    }
}
