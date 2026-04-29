package io.github.some_example_name.enemy;

/**
 * 火精灵：高速低血的飞行怪物。
 */
public class FireSpirit extends SpriteEnemy {

    public FireSpirit(float x, float y) {
        super(x, y);
        this.speed = 120;   // 移动速度（最快）
        this.maxHp = 25;    // 最大血量（低）
        this.hp = maxHp;    // 当前血量
        this.damage = 6;    // 触碰伤害（较低）
        this.baseCoinValue = 1; // 掉落金币
        this.radius = 14f;  // 碰撞半径（较小）
    }

    @Override
    protected void loadAnimations() {
        // 128x128 每帧，火精灵用 Run 作为移动动画
        moveFrames = splitSheet("monster/Fire_Spirit/Run.png", 7);
        deadFrames = splitSheet("monster/Fire_Spirit/Dead.png", 5);
    }
}
