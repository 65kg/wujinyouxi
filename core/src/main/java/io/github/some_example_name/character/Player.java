package io.github.some_example_name.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.weapon.Pistol;
import io.github.some_example_name.weapon.Weapon;

/**
 * 玩家抽象基类，定义所有玩家角色的通用属性和行为。
 * <p>
 * 设计目的：
 * <ul>
 *   <li>每个角色单独一个文件，继承此类</li>
 *   <li>通用逻辑（移动、血量、经验、武器）由基类统一维护</li>
 *   <li>外观和动画通过 {@link #render} 和 {@link #updateAnimation} 由子类实现</li>
 * </ul>
 * <p>
 * 扩展示例：新增"魔法师"角色时，新建类继承 Player，
 * 重写 {@link #render} 绘制法师外观即可。
 */
public abstract class Player {

    // ==================== 位置与体型 ====================
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float speed;

    // ==================== 战斗属性 ====================
    protected int maxHp;
    protected int hp;
    protected float attackRange;
    protected float attackCooldown;
    protected float attackTimer;
    protected int attackDamage;
    protected int pierceCount;
    protected int extraTargets;

    // ==================== 暴击属性 ====================
    protected float critRate;
    protected float critDamage;

    // ==================== 生存属性 ====================
    protected float hpRegen;
    protected float damageReduction;
    protected int killHeal;
    protected float lifeSteal;
    protected int deathExplosionDamage;
    protected float slowAuraRange;
    protected float thornsRatio;
    protected int reviveCount;

    // ==================== 特殊机制 ====================
    protected int splitCount;
    protected float knockbackForce;

    // ==================== 经验与等级 ====================
    protected int exp;
    protected int maxExp;
    protected int level;
    protected float expMultiplier;
    protected float magnetRange;

    // ==================== 武器 ====================
    protected Weapon weapon;

    // ==================== 状态 ====================
    protected boolean isMoving;
    protected boolean isAttacking;
    protected boolean facingLeft;
    protected boolean isDead;

    public Player(float startX, float startY) {
        this.x = startX;                       // X 坐标
        this.y = startY;                       // Y 坐标
        this.speed = 200;                      // 移动速度
        this.width = 64;                       // 角色宽度
        this.height = 64;                      // 角色高度
        this.maxHp = 100;                      // 最大血量
        this.hp = maxHp;                       // 当前血量
        this.attackRange = 250;                // 攻击范围
        this.attackCooldown = 0.5f;            // 攻击冷却时间
        this.attackTimer = 0;                  // 当前攻击计时器
        this.attackDamage = 10;                // 攻击伤害
        this.exp = 0;                          // 当前经验值
        this.maxExp = 50;                      // 升级所需经验
        this.level = 1;                        // 当前等级
        this.pierceCount = 0;                  // 穿透次数
        this.extraTargets = 0;                 // 额外攻击目标数
        this.critRate = 0;                     // 暴击率
        this.critDamage = 2.0f;                // 暴击伤害倍数
        this.hpRegen = 0;                      // 每秒生命回复
        this.damageReduction = 0;              // 伤害减免比例
        this.killHeal = 0;                     // 击杀回血数值
        this.lifeSteal = 0;                    // 吸血比例
        this.deathExplosionDamage = 0;         // 死亡爆炸伤害
        this.slowAuraRange = 0;                // 减速光环范围
        this.thornsRatio = 0;                  // 反伤比例
        this.reviveCount = 0;                  // 复活次数
        this.splitCount = 0;                   // 子弹分裂次数
        this.knockbackForce = 1.0f;            // 击退力度系数
        this.expMultiplier = 1.0f;             // 经验获取倍率
        this.magnetRange = 150f;               // 经验球吸引范围
        this.isMoving = false;                 // 是否正在移动
        this.isAttacking = false;              // 是否正在攻击
        this.facingLeft = false;               // 是否朝向左方
        this.weapon = new Pistol();            // 默认武器：手枪
    }

    /**
     * 更新玩家状态。
     * 处理移动输入 + 调用子类动画更新。
     */
    public void update(float deltaTime) {
        // 死亡状态下只播放动画，不接受输入
        if (isDead) {
            updateAnimation(deltaTime);
            return;
        }

        float moveDistance = speed * deltaTime;
        isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            y += moveDistance;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            y -= moveDistance;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= moveDistance;
            isMoving = true;
            facingLeft = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += moveDistance;
            isMoving = true;
            facingLeft = false;
        }

        updateAnimation(deltaTime);
    }

    /**
     * 动画更新钩子。子类重写此方法实现角色特有的动画逻辑。
     *
     * @param deltaTime 帧间隔时间
     */
    protected void updateAnimation(float deltaTime) {
        // 默认空实现，子类按需重写
    }

    /**
     * 触发攻击动画。
     */
    public void startAttack() {
        isAttacking = true;
    }

    /**
     * 切换武器，同时更新玩家攻击属性。
     */
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
        this.attackDamage = weapon.getDamage();
        this.attackRange = weapon.getRange();
        this.attackCooldown = weapon.getCooldown();
        this.pierceCount = weapon.getPierce();
        this.extraTargets = weapon.getExtraTargets();
    }

    public Weapon getWeapon() {
        return weapon;
    }

    // ==================== 位置约束 ====================

    public void clampPosition(float minX, float minY, float maxX, float maxY) {
        if (x < minX) x = minX;
        if (y < minY) y = minY;
        if (x > maxX) x = maxX;
        if (y > maxY) y = maxY;
    }

    // ==================== 伤害与生存 ====================

    public void takeDamage(int damage) {
        if (isDead) return;
        // 应用减伤
        int actualDamage = Math.max(1, (int) (damage * (1f - damageReduction)));
        hp -= actualDamage;
        if (hp <= 0) {
            // 复活判定
            if (reviveCount > 0) {
                reviveCount--;
                hp = (int) (maxHp * 0.3f);
                isDead = false;
            } else {
                hp = 0;
                isDead = true;
            }
        }
    }

    public boolean isAlive() {
        return hp > 0 && !isDead;
    }

    public boolean isDead() {
        return isDead;
    }

    // ==================== 经验 ====================

    public boolean addExp(int value) {
        exp += value;
        if (exp >= maxExp) {
            exp -= maxExp;
            level++;
            maxExp = (int) (maxExp * 1.5f);
            return true;
        }
        return false;
    }

    // ==================== 抽象方法 ====================

    /**
     * 渲染玩家角色。子类必须实现此方法绘制角色外观。
     */
    public abstract void render(SpriteBatch batch);

    /**
     * 释放角色占用的资源（纹理等）。子类必须实现。
     */
    public abstract void dispose();

    // ==================== Getter / Setter ====================

    public float getCenterX() {
        return x + width / 2;
    }

    public float getCenterY() {
        return y + height / 2;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public float getAttackTimer() {
        return attackTimer;
    }

    public void setAttackTimer(float attackTimer) {
        this.attackTimer = attackTimer;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public int getExp() {
        return exp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public int getLevel() {
        return level;
    }

    public int getPierceCount() {
        return pierceCount;
    }

    public void setPierceCount(int pierceCount) {
        this.pierceCount = pierceCount;
    }

    public int getExtraTargets() {
        return extraTargets;
    }

    public void setExtraTargets(int extraTargets) {
        this.extraTargets = extraTargets;
    }

    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // ==================== 新增属性 Getter / Setter ====================

    public float getCritRate() { return critRate; }
    public void setCritRate(float critRate) { this.critRate = critRate; }

    public float getCritDamage() { return critDamage; }
    public void setCritDamage(float critDamage) { this.critDamage = critDamage; }

    public float getHpRegen() { return hpRegen; }
    public void setHpRegen(float hpRegen) { this.hpRegen = hpRegen; }

    public float getDamageReduction() { return damageReduction; }
    public void setDamageReduction(float damageReduction) { this.damageReduction = damageReduction; }

    public int getKillHeal() { return killHeal; }
    public void setKillHeal(int killHeal) { this.killHeal = killHeal; }

    public float getLifeSteal() { return lifeSteal; }
    public void setLifeSteal(float lifeSteal) { this.lifeSteal = lifeSteal; }

    public int getDeathExplosionDamage() { return deathExplosionDamage; }
    public void setDeathExplosionDamage(int deathExplosionDamage) { this.deathExplosionDamage = deathExplosionDamage; }

    public float getSlowAuraRange() { return slowAuraRange; }
    public void setSlowAuraRange(float slowAuraRange) { this.slowAuraRange = slowAuraRange; }

    public float getThornsRatio() { return thornsRatio; }
    public void setThornsRatio(float thornsRatio) { this.thornsRatio = thornsRatio; }

    public int getReviveCount() { return reviveCount; }
    public void setReviveCount(int reviveCount) { this.reviveCount = reviveCount; }

    public int getSplitCount() { return splitCount; }
    public void setSplitCount(int splitCount) { this.splitCount = splitCount; }

    public float getKnockbackForce() { return knockbackForce; }
    public void setKnockbackForce(float knockbackForce) { this.knockbackForce = knockbackForce; }

    public float getExpMultiplier() { return expMultiplier; }
    public void setExpMultiplier(float expMultiplier) { this.expMultiplier = expMultiplier; }

    public float getMagnetRange() { return magnetRange; }
    public void setMagnetRange(float magnetRange) { this.magnetRange = magnetRange; }
}
