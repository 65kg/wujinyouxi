package io.github.some_example_name.weapon;

import com.badlogic.gdx.graphics.Color;

/**
 * 法杖：中等射程的魔法武器，投射物自带 1 次穿透（弹射）。
 * <p>
 * 特性：命中第一个敌人后不会消失，继续飞行寻找下一个目标。
 * 适合应对密集敌群，一次发射可造成连锁伤害。
 */
public class Staff extends Weapon {

    public Staff() {
        super("法杖", "自带一次弹射",
              new Color(0.75f, 0.35f, 1.0f, 1f),
              8, 300, 0.6f, 1, 0, 1);
    }
}
