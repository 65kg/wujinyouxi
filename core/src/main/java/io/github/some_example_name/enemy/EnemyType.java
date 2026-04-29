package io.github.some_example_name.enemy;

/**
 * 敌人类型配置，定义不同种类敌人的基础属性模板。
 * <p>
 * 设计目的：将敌人的数值配置与逻辑代码分离，方便后期调整平衡性
 * 或添加新种类（如精英怪、Boss）时只需扩展此枚举。
 * <p>
 * 当前 Enemy 类暂未使用此配置（保持向后兼容），
 * 后续重构 Enemy 构造函数时可直接引用此模板。
 */
public enum EnemyType {

    /**
     * 普通小怪：标准血量、标准速度、基础经验
     */
    NORMAL(30, 80, 16, 10),

    /**
     * 快速怪：低血量、高速度、较高经验
     */
    FAST(20, 140, 14, 15),

    /**
     * 坦克怪：高血量、低速度、高经验
     */
    TANK(80, 50, 20, 25);

    private final int maxHp;      // 最大生命值
    private final int speed;      // 移动速度（像素/秒）
    private final int radius;     // 碰撞半径（像素）
    private final int expValue;   // 死亡掉落经验值

    EnemyType(int maxHp, int speed, int radius, int expValue) {
        this.maxHp = maxHp;
        this.speed = speed;
        this.radius = radius;
        this.expValue = expValue;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRadius() {
        return radius;
    }

    public int getExpValue() {
        return expValue;
    }
}
