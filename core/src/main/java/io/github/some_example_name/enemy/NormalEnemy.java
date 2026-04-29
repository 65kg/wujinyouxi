package io.github.some_example_name.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 普通敌人：使用 monster1 素材包的外星怪物。
 * <p>
 * 包含移动动画（Fly）和死亡动画（Dying），
 * 所有实例共享同一套纹理以节省内存。
 */
public class NormalEnemy extends Enemy {

    // ==================== 静态共享纹理 ====================
    private static Texture[] flyTextures;
    private static Texture[] dyingTextures;
    private static boolean texturesLoaded = false;
    private static SpriteBatch sharedBatch;

    // ==================== 动画状态 ====================
    private int currentFrame;
    private float animationTimer;
    private boolean isDying;
    private float dyingTimer;
    private static final float FRAME_DURATION = 0.04f;

    public NormalEnemy(float x, float y) {
        super(x, y);
        this.speed = 80;          // 移动速度
        this.maxHp = 30;          // 最大血量
        this.hp = maxHp;          // 当前血量
        this.damage = 8;          // 触碰伤害（较低）
        this.isDying = false;     // 是否正在死亡
        this.currentFrame = 0;    // 当前动画帧
        this.animationTimer = 0;  // 动画计时器
        this.dyingTimer = 0;      // 死亡动画计时器
        loadSharedTextures();
        this.radius = 30f;        // 碰撞半径（绘制尺寸 60x60）
    }

    /**
     * 加载共享纹理（所有 NormalEnemy 实例共用）。
     */
    private static synchronized void loadSharedTextures() {
        if (texturesLoaded) return;

        flyTextures = new Texture[18];
        for (int i = 0; i < 18; i++) {
            String num = String.format("%03d", i);
            flyTextures[i] = new Texture("monster/monster1/Fly/0_Monster_Fly_" + num + ".png");
        }

        dyingTextures = new Texture[18];
        for (int i = 0; i < 18; i++) {
            String num = String.format("%03d", i);
            dyingTextures[i] = new Texture("monster/monster1/Dying/0_Monster_Dying_" + num + ".png");
        }

        sharedBatch = new SpriteBatch();
        texturesLoaded = true;
    }

    @Override
    public void update(float deltaTime, float playerCenterX, float playerCenterY) {
        update(deltaTime, playerCenterX, playerCenterY, speed);
    }

    @Override
    public void update(float deltaTime, float playerCenterX, float playerCenterY, float actualSpeed) {
        // 死亡动画播放期间不移动
        if (isDying) {
            dyingTimer += deltaTime;
            currentFrame = (int) (dyingTimer / FRAME_DURATION);
            if (currentFrame >= dyingTextures.length) {
                currentFrame = dyingTextures.length - 1;
                alive = false; // 动画播完才真正标记死亡
            }
            return;
        }

        super.update(deltaTime, playerCenterX, playerCenterY, actualSpeed);

        // 移动动画循环
        animationTimer += deltaTime;
        if (animationTimer >= FRAME_DURATION) {
            animationTimer = 0;
            currentFrame = (currentFrame + 1) % flyTextures.length;
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            isDying = true;
            currentFrame = 0;
            dyingTimer = 0;
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        Texture currentTexture = isDying ? dyingTextures[currentFrame] : flyTextures[currentFrame];

        // 使用 SpriteBatch 绘制怪物图片
        sharedBatch.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
        sharedBatch.begin();
        sharedBatch.draw(currentTexture, x - radius, y - radius, radius * 2, radius * 2);
        sharedBatch.end();

        // 绘制血条（只在非死亡状态显示）
        if (!isDying) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(x - radius, y + radius + 4, radius * 2, 4);
            float hpPercent = (float) hp / maxHp;
            shapeRenderer.setColor(hpPercent > 0.5f ? Color.GREEN : hpPercent > 0.25f ? Color.YELLOW : Color.RED);
            shapeRenderer.rect(x - radius, y + radius + 4, radius * 2 * hpPercent, 4);
            shapeRenderer.end();
        }
    }
}
