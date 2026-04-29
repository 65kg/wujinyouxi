package io.github.some_example_name.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 基于精灵图的敌人抽象基类。
 * <p>
 * 子类只需在 {@link #loadAnimations} 中配置路径和帧数，
 * 通用逻辑（动画切换、渲染、死亡处理）由本类统一维护。
 */
public abstract class SpriteEnemy extends Enemy {

    protected TextureRegion[] moveFrames;
    protected TextureRegion[] deadFrames;

    protected int currentFrame;
    protected float animationTimer;
    protected boolean isDying;
    protected float dyingTimer;
    protected static final float FRAME_DURATION = 0.08f;

    private static SpriteBatch sharedBatch;

    public SpriteEnemy(float x, float y) {
        super(x, y);
        this.currentFrame = 0;       // 当前动画帧
        this.animationTimer = 0;     // 动画计时器
        this.isDying = false;        // 是否正在死亡
        this.dyingTimer = 0;         // 死亡动画计时器
        loadAnimations();
        if (sharedBatch == null) sharedBatch = new SpriteBatch();
    }

    /**
     * 子类实现：加载精灵图并切分帧。
     */
    protected abstract void loadAnimations();

    /**
     * 辅助方法：将水平排列的精灵图切分为 TextureRegion 数组。
     *
     * @param path       图片路径
     * @param frameCount 帧数
     * @return 切分后的帧数组
     */
    protected TextureRegion[] splitSheet(String path, int frameCount) {
        Texture sheet = new Texture(path);
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(sheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    }

    @Override
    public void update(float deltaTime, float playerCenterX, float playerCenterY, float actualSpeed) {
        // 死亡动画播放期间不移动
        if (isDying) {
            dyingTimer += deltaTime;
            currentFrame = (int) (dyingTimer / FRAME_DURATION);
            if (currentFrame >= deadFrames.length) {
                currentFrame = deadFrames.length - 1;
                alive = false;
            }
            return;
        }

        super.update(deltaTime, playerCenterX, playerCenterY, actualSpeed);

        // 移动动画循环
        animationTimer += deltaTime;
        if (animationTimer >= FRAME_DURATION) {
            animationTimer = 0;
            currentFrame = (currentFrame + 1) % moveFrames.length;
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
        TextureRegion currentRegion = isDying ? deadFrames[currentFrame] : moveFrames[currentFrame];
        float drawSize = radius * 2;

        sharedBatch.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
        sharedBatch.begin();
        sharedBatch.draw(currentRegion, x - radius, y - radius, drawSize, drawSize);
        sharedBatch.end();

        // 绘制血条（只在非死亡状态显示）
        if (!isDying) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(x - radius, y + radius + 4, drawSize, 4);
            float hpPercent = (float) hp / maxHp;
            shapeRenderer.setColor(hpPercent > 0.5f ? Color.GREEN : hpPercent > 0.25f ? Color.YELLOW : Color.RED);
            shapeRenderer.rect(x - radius, y + radius + 4, drawSize * hpPercent, 4);
            shapeRenderer.end();
        }
    }
}
