package io.github.some_example_name.system;

import io.github.some_example_name.DamageNumber;
import io.github.some_example_name.Projectile;
import io.github.some_example_name.character.Player;
import io.github.some_example_name.enemy.Enemy;
import io.github.some_example_name.manager.EntityManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗系统，负责处理自动攻击、投射物碰撞检测和伤害结算。
 * <p>
 * 职责分离：
 * - 本系统只关心"谁打谁"和"打中没"
 * - 不管理实体的生命周期（由 EntityManager 负责）
 * - 不处理经验掉落（由 ExpSystem 负责）
 * <p>
 * 未来扩展武器系统时，可将攻击逻辑委托给 {@link io.github.some_example_name.weapon.Weapon} 接口。
 */
public class CombatSystem {

    private final EntityManager entityManager;
    private final Player player;

    public CombatSystem(EntityManager entityManager, Player player) {
        this.entityManager = entityManager;
        this.player = player;
    }

    /**
     * 更新攻击冷却并尝试自动攻击。
     * 每帧调用一次，由 Main 统一调度。
     *
     * @param deltaTime 帧间隔时间（秒）
     */
    public void update(float deltaTime) {
        // 攻击冷却递减
        if (player.getAttackTimer() > 0) {
            player.setAttackTimer(player.getAttackTimer() - deltaTime);
        }

        // 冷却完毕则执行自动攻击
        if (player.getAttackTimer() <= 0) {
            performAutoAttack();
        }
    }

    /**
     * 执行自动攻击逻辑：
     * 1. 查找攻击范围内的存活敌人
     * 2. 按距离排序（优先攻击最近的）
     * 3. 根据"额外目标数"决定同时攻击几个敌人
     * 4. 向每个目标发射投射物
     */
    private void performAutoAttack() {
        List<Enemy> targetsInRange = findTargetsInRange();
        if (targetsInRange.isEmpty()) return;

        // 按距离升序排列，优先攻击最近的敌人
        targetsInRange.sort((a, b) -> {
            float da = distance(a.getX(), a.getY(), player.getCenterX(), player.getCenterY());
            float db = distance(b.getX(), b.getY(), player.getCenterX(), player.getCenterY());
            return Float.compare(da, db);
        });

        // 计算可同时攻击的目标数（基础1个 + 额外目标数）
        int maxTargets = player.getExtraTargets() + 1;
        int attackCount = Math.min(targetsInRange.size(), maxTargets);

        // 发射投射物
        int weaponBounce = player.getWeapon() != null ? player.getWeapon().getBounceCount() : 0;
        for (int i = 0; i < attackCount; i++) {
            Enemy target = targetsInRange.get(i);
            Projectile projectile = new Projectile(
                player.getCenterX(),
                player.getCenterY(),
                target.getX(),
                target.getY(),
                player.getAttackDamage(),
                player.getPierceCount()
            );
            projectile.setBounceCount(weaponBounce);
            entityManager.addProjectile(projectile);
        }

        // 重置冷却并触发攻击动画
        player.setAttackTimer(player.getAttackCooldown());
        player.startAttack();
    }

    /**
     * 查找玩家攻击范围内的所有存活敌人。
     *
     * @return 范围内的敌人列表（可能为空）
     */
    private List<Enemy> findTargetsInRange() {
        List<Enemy> result = new ArrayList<>();
        for (Enemy enemy : entityManager.getEnemies()) {
            if (!enemy.isAlive()) continue;

            float dist = distance(enemy.getX(), enemy.getY(), player.getCenterX(), player.getCenterY());
            if (dist < player.getAttackRange()) {
                result.add(enemy);
            }
        }
        return result;
    }

    /**
     * 检测并处理投射物与敌人的碰撞。
     * 遍历所有活跃投射物和存活敌人，检测命中后结算伤害。
     */
    public void handleCollisions() {
        for (Projectile projectile : entityManager.getProjectiles()) {
            if (!projectile.isActive()) continue;

            for (Enemy enemy : entityManager.getEnemies()) {
                if (!enemy.isAlive() || !projectile.isActive()) continue;

                if (projectile.checkHit(enemy)) {
                    // 暴击判定
                    int damage = projectile.getDamage();
                    boolean isCrit = Math.random() < player.getCritRate();
                    if (isCrit) {
                        damage = (int) (damage * player.getCritDamage());
                    }

                    enemy.takeDamage(damage);

                    // 吸血
                    if (player.getLifeSteal() > 0) {
                        int heal = (int) (damage * player.getLifeSteal());
                        player.setHp(Math.min(player.getMaxHp(), player.getHp() + heal));
                    }

                    // 生成伤害数字（暴击用橙色）
                    DamageNumber dmgNum = new DamageNumber(
                        enemy.getX(), enemy.getY() + enemy.getRadius(), damage
                    );
                    if (isCrit) dmgNum.setCritColor();
                    entityManager.addDamageNumber(dmgNum);

                    // 击退（基于玩家击退系数）
                    float kb = player.getKnockbackForce();
                    if (kb > 1.0f) {
                        float dx = enemy.getX() - player.getCenterX();
                        float dy = enemy.getY() - player.getCenterY();
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist > 0) {
                            enemy.knockback(dx / dist, dy / dist, 15 * (kb - 1.0f));
                        }
                    }

                    // 分裂：投射物失效时生成散射子弹
                    boolean wasActive = projectile.isActive();
                    projectile.onHit(enemy);
                    if (!projectile.isActive() && wasActive && player.getSplitCount() > 0) {
                        createSplitProjectiles(projectile, player.getSplitCount());
                    }

                    // 触发武器命中特效（击退、流血、爆炸等）
                    if (player.getWeapon() != null) {
                        player.getWeapon().onHit(enemy, projectile.getX(), projectile.getY());
                    }

                    // 弹射逻辑：命中后寻找下一个最近的目标并改变方向
                    if (projectile.getBounceCount() > 0 && projectile.isActive()) {
                        Enemy nextTarget = findNextBounceTarget(projectile, enemy);
                        if (nextTarget != null) {
                            projectile.redirectTo(nextTarget.getX(), nextTarget.getY());
                            projectile.setBounceCount(projectile.getBounceCount() - 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建分裂投射物：向随机方向散射。
     *
     * @param source 原投射物
     * @param count  分裂数量
     */
    private void createSplitProjectiles(Projectile source, int count) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.random() * Math.PI * 2);
            float tx = source.getX() + (float) Math.cos(angle) * 200;
            float ty = source.getY() + (float) Math.sin(angle) * 200;
            Projectile split = new Projectile(
                source.getX(), source.getY(), tx, ty,
                Math.max(1, source.getDamage() / 2), 0
            );
            entityManager.addProjectile(split);
        }
    }

    /**
     * 检测敌人与玩家的碰撞。
     * 每只怪物有独立的 0.5 秒触碰冷却，避免同一怪物每帧连续扣血。
     * 反伤无上限，可无限堆叠。
     */
    public void handleEnemyPlayerCollisions() {
        for (Enemy enemy : entityManager.getEnemies()) {
            if (!enemy.isAlive()) continue;

            float dx = enemy.getX() - player.getCenterX();
            float dy = enemy.getY() - player.getCenterY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // 敌人碰撞半径 + 玩家判定半径（约 20）
            if (dist < enemy.getRadius() + 20) {
                // 该怪物的伤害冷却已结束才能造成伤害
                if (enemy.getDamageCooldown() <= 0) {
                    int dmg = enemy.getDamage();
                    player.takeDamage(dmg);
                    enemy.resetDamageCooldown();
                    // 反伤：无上限，收到多少伤害按倍率返还
                    if (player.getThornsRatio() > 0) {
                        int reflect = (int) (dmg * player.getThornsRatio());
                        enemy.takeDamage(reflect);
                    }
                }
            }
        }
    }

    /**
     * 为弹射的投射物寻找下一个最近的目标。
     * <p>
     * 搜索条件：存活、未被当前投射物命中过、在 400 像素范围内。
     *
     * @param projectile   当前投射物
     * @param currentEnemy 刚刚命中的敌人（排除）
     * @return 找到的下一个目标，如果没有则返回 null
     */
    private Enemy findNextBounceTarget(Projectile projectile, Enemy currentEnemy) {
        Enemy closest = null;
        float closestDist = Float.MAX_VALUE;
        float bx = projectile.getX();
        float by = projectile.getY();

        for (Enemy e : entityManager.getEnemies()) {
            if (!e.isAlive() || e == currentEnemy) continue;
            if (projectile.hasHit(e)) continue;

            float dx = e.getX() - bx;
            float dy = e.getY() - by;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < closestDist && dist < 400) {
                closestDist = dist;
                closest = e;
            }
        }
        return closest;
    }

    /**
     * 计算两点之间的欧几里得距离。
     */
    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
