package io.github.some_example_name.enemy;

/**
 * 兽人狂战士：精英怪，血厚且速度快。
 */
public class OrcBerserk extends SpriteEnemy {

    public OrcBerserk(float x, float y) {
        super(x, y);
        this.speed = 90;    // 移动速度（较快）
        this.maxHp = 80;    // 最大血量（高）
        this.hp = maxHp;    // 当前血量
        this.damage = 18;   // 触碰伤害（最高，精英怪）
        this.radius = 20f;  // 碰撞半径（较大）
    }

    @Override
    protected void loadAnimations() {
        // 96x96 每帧，狂战士用 Run 作为移动动画
        moveFrames = splitSheet("monster/Orc_Berserk/Run.png", 6);
        deadFrames = splitSheet("monster/Orc_Berserk/Dead.png", 4);
    }
}
