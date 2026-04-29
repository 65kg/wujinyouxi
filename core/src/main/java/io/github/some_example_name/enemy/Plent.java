package io.github.some_example_name.enemy;

/**
 * 毒液植物：移动缓慢但血量极厚。
 */
public class Plent extends SpriteEnemy {

    public Plent(float x, float y) {
        super(x, y);
        this.speed = 50;    // 移动速度（极慢）
        this.maxHp = 100;   // 最大血量（极高）
        this.hp = maxHp;    // 当前血量
        this.damage = 15;   // 触碰伤害（高）
        this.baseCoinValue = 3; // 掉落金币
        this.radius = 18f;  // 碰撞半径（中等）
    }

    @Override
    protected void loadAnimations() {
        // 128x128 每帧
        moveFrames = splitSheet("monster/Plent/Walk.png", 9);
        deadFrames = splitSheet("monster/Plent/Dead.png", 2);
    }
}
