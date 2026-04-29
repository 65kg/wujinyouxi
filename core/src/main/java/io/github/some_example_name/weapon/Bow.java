package io.github.some_example_name.weapon;

import com.badlogic.gdx.graphics.Color;

/**
 * 弓箭：远距离武器，自带 1 个额外攻击目标。
 * <p>
 * 特性：每次攻击可同时射向 2 个敌人（基础 1 + 额外 1），
 * 射程最远，适合放风筝战术。
 */
public class Bow extends Weapon {

    public Bow() {
        super("弓箭", "自带一次额外攻击",
              new Color(0.25f, 0.85f, 0.45f, 1f),
              12, 350, 0.7f, 0, 1, 0);
    }
}
