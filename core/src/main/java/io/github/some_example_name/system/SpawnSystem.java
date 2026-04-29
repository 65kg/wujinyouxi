package io.github.some_example_name.system;

import com.badlogic.gdx.graphics.OrthographicCamera;
import io.github.some_example_name.enemy.*;
import io.github.some_example_name.manager.EntityManager;

/**
 * 生成系统，负责控制敌人的生成逻辑。
 * <p>
 * 当前实现：基于摄像机的视野外生成，每隔固定时间生成一个普通敌人。
 * <p>
 * 未来可扩展：
 * - 波次系统（Wave System）：随时间增加生成频率和敌人强度
 * - 多类型生成：根据难度权重选择不同 EnemyType
 * - 特殊事件生成：Boss 出现、精英怪潮等
 */
public class SpawnSystem {

    private final EntityManager entityManager;
    private final OrthographicCamera camera;
    private float spawnTimer;
    private float spawnInterval;

    /** 游戏已进行时间（秒），用于控制怪物解锁阶段 */
    private float gameTime;

    /**
     * 构造函数。
     *
     * @param entityManager 实体管理器，用于将新敌人加入世界
     * @param camera        摄像机，用于计算视野边界（敌人在视野外生成）
     */
    public SpawnSystem(EntityManager entityManager, OrthographicCamera camera) {
        this.entityManager = entityManager;
        this.camera = camera;
        this.spawnTimer = 0;
        this.spawnInterval = 0.5f;
        this.gameTime = 0;
    }

    /**
     * 更新生成计时，到达间隔则生成敌人。
     * 每帧由 Main 统一调度调用。
     *
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        gameTime += deltaTime;
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            spawnEnemy();
        }
    }

    /**
     * 在摄像机视野外随机位置生成一个敌人。
     * <p>
     * 算法：随机选择屏幕四条边中的一条，在对应边外侧生成。
     * 这样敌人会从玩家视野外自然进入，增强游戏沉浸感。
     */
    private void spawnEnemy() {
        float camLeft = camera.position.x - camera.viewportWidth / 2;
        float camRight = camera.position.x + camera.viewportWidth / 2;
        float camBottom = camera.position.y - camera.viewportHeight / 2;
        float camTop = camera.position.y + camera.viewportHeight / 2;

        // 随机选择一条边（0=上, 1=右, 2=下, 3=左）
        int side = (int) (Math.random() * 4);
        float ex, ey;

        switch (side) {
            case 0: // 上边
                ex = camLeft + (float) (Math.random() * camera.viewportWidth);
                ey = camTop + 50;
                break;
            case 1: // 右边
                ex = camRight + 50;
                ey = camBottom + (float) (Math.random() * camera.viewportHeight);
                break;
            case 2: // 下边
                ex = camLeft + (float) (Math.random() * camera.viewportWidth);
                ey = camBottom - 50;
                break;
            default: // 左边
                ex = camLeft - 50;
                ey = camBottom + (float) (Math.random() * camera.viewportHeight);
                break;
        }

        // 随机选择怪物类型（等概率）
        Enemy enemy = createRandomEnemy(ex, ey);
        entityManager.addEnemy(enemy);
    }

    /**
     * 根据当前游戏时间，从已解锁的怪物池中随机创建敌人。
     * <p>
     * 解锁阶段：
     * <ul>
     *   <li>0-30s：低血量怪（NormalEnemy、Skeleton、FireSpirit）</li>
     *   <li>30-60s：加入 OrcWarrior</li>
     *   <li>60-90s：加入 OrcShaman</li>
     *   <li>90-120s：加入 Plent</li>
     *   <li>120s+：加入 OrcBerserk，全部解锁</li>
     * </ul>
     */
    private Enemy createRandomEnemy(float x, float y) {
        int phase;
        if (gameTime < 30) {
            phase = 0;
        } else if (gameTime < 60) {
            phase = 1;
        } else if (gameTime < 90) {
            phase = 2;
        } else if (gameTime < 120) {
            phase = 3;
        } else {
            phase = 4;
        }

        double roll = Math.random();
        switch (phase) {
            case 0: // 初期：只有低血量怪
                if (roll < 0.40) return new NormalEnemy(x, y);
                if (roll < 0.70) return new Skeleton(x, y);
                return new FireSpirit(x, y);
            case 1: // 前期：加入 OrcWarrior
                if (roll < 0.30) return new NormalEnemy(x, y);
                if (roll < 0.50) return new Skeleton(x, y);
                if (roll < 0.70) return new FireSpirit(x, y);
                return new OrcWarrior(x, y);
            case 2: // 中期：加入 OrcShaman
                if (roll < 0.20) return new NormalEnemy(x, y);
                if (roll < 0.35) return new Skeleton(x, y);
                if (roll < 0.50) return new FireSpirit(x, y);
                if (roll < 0.70) return new OrcWarrior(x, y);
                return new OrcShaman(x, y);
            case 3: // 后期：加入 Plent
                if (roll < 0.15) return new NormalEnemy(x, y);
                if (roll < 0.25) return new Skeleton(x, y);
                if (roll < 0.35) return new FireSpirit(x, y);
                if (roll < 0.55) return new OrcWarrior(x, y);
                if (roll < 0.75) return new OrcShaman(x, y);
                return new Plent(x, y);
            default: // 末期：全部解锁，含 OrcBerserk
                if (roll < 0.12) return new NormalEnemy(x, y);
                if (roll < 0.22) return new Skeleton(x, y);
                if (roll < 0.32) return new FireSpirit(x, y);
                if (roll < 0.48) return new OrcWarrior(x, y);
                if (roll < 0.64) return new OrcShaman(x, y);
                if (roll < 0.82) return new Plent(x, y);
                return new OrcBerserk(x, y);
        }
    }

    // ==================== 配置接口 ====================

    /**
     * 设置生成间隔。
     *
     * @param interval 间隔时间（秒）
     */
    public void setSpawnInterval(float interval) {
        this.spawnInterval = interval;
    }

    /**
     * 获取当前生成间隔。
     */
    public float getSpawnInterval() {
        return spawnInterval;
    }
}
