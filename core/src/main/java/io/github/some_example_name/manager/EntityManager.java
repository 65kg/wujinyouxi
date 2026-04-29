package io.github.some_example_name.manager;

import io.github.some_example_name.DamageNumber;
import io.github.some_example_name.ExpOrb;
import io.github.some_example_name.Projectile;
import io.github.some_example_name.enemy.Enemy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 实体管理器，统一管理游戏中所有动态实体（敌人、投射物、经验球）。
 * <p>
 * 职责：提供增删查、批量清理的统一接口，解耦各系统对实体集合的直接操作。
 * 未来扩展新实体类型（如道具、陷阱等）时，在此类中添加对应列表即可。
 */
public class EntityManager {

    private final ArrayList<Enemy> enemies;
    private final ArrayList<Projectile> projectiles;
    private final ArrayList<ExpOrb> expOrbs;
    private final ArrayList<DamageNumber> damageNumbers;

    public EntityManager() {
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.expOrbs = new ArrayList<>();
        this.damageNumbers = new ArrayList<>();
    }

    // ==================== 敌人管理 ====================

    /**
     * 添加一个敌人到世界中。
     */
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    /**
     * 获取所有敌人（包含已死亡的）。
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * 清理死亡敌人，并通过回调通知调用方处理掉落等逻辑。
     *
     * @param callback 敌人死亡时的回调
     */
    public void cleanupDeadEnemies(EnemyDropCallback callback) {
        Iterator<Enemy> iter = enemies.iterator();
        while (iter.hasNext()) {
            Enemy enemy = iter.next();
            if (!enemy.isAlive()) {
                callback.onEnemyDied(enemy);
                iter.remove();
            }
        }
    }

    /**
     * 清空所有敌人。
     */
    public void clearAllEnemies() {
        enemies.clear();
    }

    // ==================== 投射物管理 ====================

    /**
     * 添加一个投射物到世界中。
     */
    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    /**
     * 获取所有投射物（包含已失效的）。
     */
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /**
     * 移除所有已失效（超出边界或命中耗尽）的投射物。
     */
    public void cleanupInactiveProjectiles() {
        projectiles.removeIf(p -> !p.isActive());
    }

    // ==================== 经验球管理 ====================

    /**
     * 添加一个经验球到世界中。
     */
    public void addExpOrb(ExpOrb orb) {
        expOrbs.add(orb);
    }

    /**
     * 获取所有经验球（包含已被吸收的）。
     */
    public List<ExpOrb> getExpOrbs() {
        return expOrbs;
    }

    /**
     * 移除所有已被吸收的经验球。
     */
    public void cleanupInactiveExpOrbs() {
        expOrbs.removeIf(o -> !o.isActive());
    }

    // ==================== 伤害数字管理 ====================

    /**
     * 添加一个伤害数字到世界中。
     */
    public void addDamageNumber(DamageNumber damageNumber) {
        damageNumbers.add(damageNumber);
    }

    /**
     * 获取所有伤害数字（包含已消失的）。
     */
    public List<DamageNumber> getDamageNumbers() {
        return damageNumbers;
    }

    /**
     * 移除所有已消失的伤害数字。
     */
    public void cleanupInactiveDamageNumbers() {
        damageNumbers.removeIf(d -> !d.isActive());
    }

    // ==================== 回调接口 ====================

    /**
     * 敌人死亡回调接口，用于解耦死亡处理逻辑（如掉落经验球）。
     */
    public interface EnemyDropCallback {
        /**
         * 当敌人死亡时调用。
         *
         * @param enemy 死亡的敌人实例
         */
        void onEnemyDied(Enemy enemy);
    }
}
