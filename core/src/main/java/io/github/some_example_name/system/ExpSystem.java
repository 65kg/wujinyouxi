package io.github.some_example_name.system;

import io.github.some_example_name.ExpOrb;
import io.github.some_example_name.character.Player;
import io.github.some_example_name.enemy.Enemy;
import io.github.some_example_name.manager.EntityManager;

/**
 * 经验系统，负责经验球的更新、吸收判定、以及升级触发。
 * <p>
 * 职责：
 * - 每帧更新所有活跃经验球的位置（向玩家 magnet 移动）
 * - 检测经验球是否被玩家吸收
 * - 吸收后为玩家增加经验，判断是否升级
 * <p>
 * 未来可扩展：经验球大小分级（小/中/大），对应不同经验值。
 */
public class ExpSystem {

    private final EntityManager entityManager;
    private final Player player;

    public ExpSystem(EntityManager entityManager, Player player) {
        this.entityManager = entityManager;
        this.player = player;
    }

    /**
     * 更新所有经验球的状态。
     * 每帧由 Main 统一调度调用。
     *
     * @param deltaTime 帧间隔时间（秒）
     * @return 如果玩家在本次更新中升级，返回 true
     */
    public boolean update(float deltaTime) {
        boolean leveledUp = false;

        for (ExpOrb orb : entityManager.getExpOrbs()) {
            if (!orb.isActive()) continue;

            // 更新经验球位置（向玩家 magnet 移动）
            orb.update(deltaTime, player.getCenterX(), player.getCenterY(), player.getMagnetRange());

            // 被玩家吸收后增加经验（应用经验倍率）
            if (!orb.isActive()) {
                int expValue = (int) (orb.getExpValue() * player.getExpMultiplier());
                boolean up = player.addExp(expValue);
                if (up) leveledUp = true;
            }
        }

        // 清理已被吸收的经验球
        entityManager.cleanupInactiveExpOrbs();
        return leveledUp;
    }

    /**
     * 处理敌人死亡后的经验球掉落。
     * 由 EntityManager 的死亡回调触发。
     *
     * @param enemy 死亡的敌人
     */
    public void onEnemyDied(Enemy enemy) {
        entityManager.addExpOrb(enemy.dropExp());
    }
}
