package io.github.some_example_name.enemy;

/**
 * 骷髅：最弱的小怪，速度快但血量极低。
 */
public class Skeleton extends SpriteEnemy {

    public Skeleton(float x, float y) {
        super(x, y);
        this.speed = 100;   // 移动速度（较快）
        this.maxHp = 20;    // 最大血量（极低）
        this.hp = maxHp;    // 当前血量
        this.damage = 5;    // 触碰伤害（最低）
        this.radius = 14f;  // 碰撞半径（较小）
    }

    @Override
    protected void loadAnimations() {
        // 128x128 每帧
        moveFrames = splitSheet("monster/Skeleton/Walk.png", 8);
        deadFrames = splitSheet("monster/Skeleton/Dead.png", 3);
    }
}
