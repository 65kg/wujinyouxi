package io.github.some_example_name.enemy;

/**
 * 兽人战士：标准近战怪，属性均衡。
 */
public class OrcWarrior extends SpriteEnemy {

    public OrcWarrior(float x, float y) {
        super(x, y);
        this.speed = 70;    // 移动速度（中等）
        this.maxHp = 50;    // 最大血量（中等）
        this.hp = maxHp;    // 当前血量
        this.damage = 12;   // 触碰伤害（中等偏高）
        this.baseCoinValue = 2; // 掉落金币
        this.radius = 18f;  // 碰撞半径（中等）
    }

    @Override
    protected void loadAnimations() {
        // 96x96 每帧
        moveFrames = splitSheet("monster/Orc_Warrior/Walk.png", 7);
        deadFrames = splitSheet("monster/Orc_Warrior/Dead.png", 4);
    }
}
