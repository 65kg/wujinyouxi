package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * 金币实体，敌人死亡时掉落，玩家靠近后自动拾取。
 * <p>
 * 行为类似 ExpOrb：在吸引范围内向玩家移动，靠近后被吸收。
 */
public class Coin {
    private float x;
    private float y;
    private int value;          // 金币面值
    private boolean active;
    private float magnetSpeed;
    private static final float ABSORB_DIST = 20;

    // 共享纹理（所有金币实例共用）
    private static Texture icon;
    private static boolean textureLoaded = false;
    private static final float DRAW_SIZE = 36;

    public Coin(float x, float y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.active = true;
        this.magnetSpeed = 700;  // 与经验球相同的吸引速度
        loadTexture();
    }

    private static synchronized void loadTexture() {
        if (textureLoaded) return;
        icon = new Texture("common/Coin_A.png");
        textureLoaded = true;
    }

    public void update(float deltaTime, float playerX, float playerY, float magnetRange) {
        if (!active) return;

        float dx = playerX - x;
        float dy = playerY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 在吸引范围内则向玩家移动
        if (distance < magnetRange && distance > 0) {
            x += (dx / distance) * magnetSpeed * deltaTime;
            y += (dy / distance) * magnetSpeed * deltaTime;
        }

        // 被玩家吸收
        if (distance < ABSORB_DIST) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(icon, x - DRAW_SIZE / 2, y - DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
    }

    public boolean isActive() {
        return active;
    }

    public int getValue() {
        return value;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public static void dispose() {
        if (icon != null) {
            icon.dispose();
            icon = null;
            textureLoaded = false;
        }
    }
}
