package io.github.some_example_name.character;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 忍者角色：当前游戏的默认玩家角色。
 * <p>
 * 外观使用 Shinobi 素材包的行走和攻击动画，
 * 继承 {@link Player} 基类，只负责动画加载、更新和渲染。
 */
public class Shinobi extends Player {

    private Texture idleSheet;
    private TextureRegion[] idleFrames;
    private Texture runSheet;
    private TextureRegion[] runFrames;
    private Texture attackSheet;
    private TextureRegion[] attackFrames;
    private Texture deadSheet;
    private TextureRegion[] deadFrames;
    private int currentFrame;
    private float animationTimer;
    private float attackAnimationTimer;
    private float deadAnimationTimer;
    private static final float ATTACK_ANIM_DURATION = 0.35f;
    private static final float DEAD_ANIM_DURATION = 0.6f;

    public Shinobi(float startX, float startY) {
        super(startX, startY);
        initAnimations();  // 初始化动画资源
    }

    /**
     * 加载忍者的站立、奔跑和攻击动画纹理。
     */
    private void initAnimations() {
        // 站立动画（Idle）
        idleSheet = new Texture("Shinobi/Idle.png");
        int idleFrameCount = 6;
        int idleFrameWidth = idleSheet.getWidth() / idleFrameCount;
        int idleFrameHeight = idleSheet.getHeight();
        idleFrames = new TextureRegion[idleFrameCount];
        for (int i = 0; i < idleFrameCount; i++) {
            idleFrames[i] = new TextureRegion(idleSheet, i * idleFrameWidth, 0, idleFrameWidth, idleFrameHeight);
        }

        // 奔跑动画（Run）
        runSheet = new Texture("Shinobi/Run.png");
        int runFrameCount = 8;
        int runFrameWidth = runSheet.getWidth() / runFrameCount;
        int runFrameHeight = runSheet.getHeight();
        runFrames = new TextureRegion[runFrameCount];
        for (int i = 0; i < runFrameCount; i++) {
            runFrames[i] = new TextureRegion(runSheet, i * runFrameWidth, 0, runFrameWidth, runFrameHeight);
        }

        // 攻击动画（Attack_1）
        attackSheet = new Texture("Shinobi/Attack_1.png");
        int attackFrameCount = 6;
        int attackFrameWidth = attackSheet.getWidth() / attackFrameCount;
        int attackFrameHeight = attackSheet.getHeight();
        attackFrames = new TextureRegion[attackFrameCount];
        for (int i = 0; i < attackFrameCount; i++) {
            attackFrames[i] = new TextureRegion(attackSheet, i * attackFrameWidth, 0, attackFrameWidth, attackFrameHeight);
        }

        // 死亡动画（Dead）
        deadSheet = new Texture("Shinobi/Dead.png");
        int deadFrameCount = 4;
        int deadFrameWidth = deadSheet.getWidth() / deadFrameCount;
        int deadFrameHeight = deadSheet.getHeight();
        deadFrames = new TextureRegion[deadFrameCount];
        for (int i = 0; i < deadFrameCount; i++) {
            deadFrames[i] = new TextureRegion(deadSheet, i * deadFrameWidth, 0, deadFrameWidth, deadFrameHeight);
        }

        currentFrame = 0;
        animationTimer = 0;
        attackAnimationTimer = 0;
        deadAnimationTimer = 0;
    }

    @Override
    protected void updateAnimation(float deltaTime) {
        // 死亡动画优先，播放一次后停在最后一帧
        if (isDead) {
            deadAnimationTimer += deltaTime;
            currentFrame = (int) ((deadAnimationTimer / DEAD_ANIM_DURATION) * deadFrames.length);
            if (currentFrame >= deadFrames.length) {
                currentFrame = deadFrames.length - 1;
            }
            return;
        }

        // 攻击动画
        if (isAttacking) {
            attackAnimationTimer += deltaTime;
            if (attackAnimationTimer >= ATTACK_ANIM_DURATION) {
                isAttacking = false;
                attackAnimationTimer = 0;
                currentFrame = 0;
            } else {
                currentFrame = (int) ((attackAnimationTimer / ATTACK_ANIM_DURATION) * attackFrames.length);
                if (currentFrame >= attackFrames.length) currentFrame = attackFrames.length - 1;
            }
        } else if (isMoving) {
            // 奔跑动画
            animationTimer += deltaTime;
            if (animationTimer >= 0.08f) {
                animationTimer = 0;
                currentFrame = (currentFrame + 1) % runFrames.length;
            }
        } else {
            // 站立动画
            animationTimer += deltaTime;
            if (animationTimer >= 0.15f) {
                animationTimer = 0;
                currentFrame = (currentFrame + 1) % idleFrames.length;
            }
        }
    }

    @Override
    public void startAttack() {
        if (!isAttacking) {
            isAttacking = true;
            attackAnimationTimer = 0;
            currentFrame = 0;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion[] currentFrames;
        if (isDead) {
            currentFrames = deadFrames;
        } else if (isAttacking) {
            currentFrames = attackFrames;
        } else if (isMoving) {
            currentFrames = runFrames;
        } else {
            currentFrames = idleFrames;
        }
        // 安全边界：切换动画时防止越界
        if (currentFrame >= currentFrames.length) {
            currentFrame = currentFrames.length - 1;
        }
        if (facingLeft) {
            batch.draw(currentFrames[currentFrame], x + width, y, 0, 0, width, height, -1, 1, 0);
        } else {
            batch.draw(currentFrames[currentFrame], x, y, width, height);
        }
    }

    @Override
    public void dispose() {
        idleSheet.dispose();
        runSheet.dispose();
        attackSheet.dispose();
        deadSheet.dispose();
    }
}
