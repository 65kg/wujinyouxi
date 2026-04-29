package io.github.some_example_name.enemy;

/**
 * 兽人萨满：法师型怪物，中等血量和速度。
 */
public class OrcShaman extends SpriteEnemy {

    public OrcShaman(float x, float y) {
        super(x, y);
        this.speed = 65;    // 移动速度（偏慢）
        this.maxHp = 60;    // 最大血量（中等偏高）
        this.hp = maxHp;    // 当前血量
        this.damage = 10;   // 触碰伤害（中等）
        this.radius = 18f;  // 碰撞半径（中等）
    }

    @Override
    protected void loadAnimations() {
        // 96x96 每帧
        moveFrames = splitSheet("monster/Orc_Shaman/Walk.png", 7);
        deadFrames = splitSheet("monster/Orc_Shaman/Dead.png", 5);
    }
}
