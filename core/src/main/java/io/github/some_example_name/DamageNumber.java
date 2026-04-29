package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * 伤害数字，命中敌人时在敌人位置弹出并向上飘动淡出。
 */
public class DamageNumber {

    private float x;
    private float y;
    private final String text;
    private float timer;
    private static final float DURATION = 0.8f;
    private static final float RISE_SPEED = 40f;
    private final Color color;

    public DamageNumber(float x, float y, int damage) {
        this.x = x;
        this.y = y;
        this.text = String.valueOf(damage);
        this.timer = 0;
        // 暴击/高伤害用橙色，普通伤害用白色
        this.color = damage >= 20 ? new Color(1f, 0.6f, 0.2f, 1f) : new Color(1f, 1f, 1f, 1f);
    }

    /**
     * 更新位置和透明度。
     *
     * @param deltaTime 帧间隔时间
     */
    public void update(float deltaTime) {
        timer += deltaTime;
        y += RISE_SPEED * deltaTime;
        // 透明度从 1 线性降到 0
        color.a = 1f - (timer / DURATION);
    }

    /**
     * 渲染伤害数字。
     *
     * @param batch 精灵批处理
     * @param font  位图字体
     */
    public void render(SpriteBatch batch, BitmapFont font) {
        font.setColor(color);
        // 居中绘制
        float width = text.length() * 10f;
        font.draw(batch, text, x - width / 2, y);
    }

    /**
     * 设置为暴击颜色（亮橙色）。
     */
    public void setCritColor() {
        this.color.set(1f, 0.4f, 0f, 1f);
    }

    /**
     * 是否还在显示期间。
     */
    public boolean isActive() {
        return timer < DURATION;
    }
}
