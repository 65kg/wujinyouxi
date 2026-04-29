package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ExpOrb {
    private float x;
    private float y;
    private float radius;
    private int expValue;
    private boolean active;
    private float magnetSpeed;

    // 共享纹理
    private static Texture icon;
    private static boolean textureLoaded = false;
    private static final float DRAW_SIZE = 32;

    public ExpOrb(float x, float y, int expValue) {
        this.x = x;                              // X 坐标
        this.y = y;                              // Y 坐标
        this.expValue = expValue;                // 经验值
        this.radius = expValue >= 20 ? 10 : 6;   // 碰撞半径（大经验球更大）
        this.active = true;                      // 是否活跃
        this.magnetSpeed = 700;                  // 被吸引时的移动速度
        loadTexture();
    }

    private static synchronized void loadTexture() {
        if (textureLoaded) return;
        icon = new Texture("common/Bonus_Icon.png");
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
        if (distance < 20) {
            active = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;
        // 保留 ShapeRenderer 备用渲染
        shapeRenderer.setColor(new com.badlogic.gdx.graphics.Color(0.3f, 0.8f, 1.0f, 0.4f));
        shapeRenderer.circle(x, y, radius + 3);
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.CYAN);
        shapeRenderer.circle(x, y, radius);
    }

    /**
     * 使用图标纹理渲染经验球。
     */
    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(icon, x - DRAW_SIZE / 2, y - DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE);
    }

    public boolean isActive() {
        return active;
    }

    public int getExpValue() {
        return expValue;
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
