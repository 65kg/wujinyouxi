package io.github.some_example_name.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.some_example_name.Upgrade;
import io.github.some_example_name.character.Player;

import java.util.ArrayList;

/**
 * 升级系统，负责生成升级选项、渲染升级面板、处理玩家选择。
 * <p>
 * 职责：
 * - 玩家升级时随机生成 3 个升级选项
 * - 游戏暂停时渲染升级面板覆盖层
 * - 检测玩家点击并应用选中的升级效果
 * <p>
 * 与玩家状态解耦：只调用 player 的 setter 方法修改属性，不直接操作内部字段。
 */
public class UpgradeSystem {

    private final Player player;
    private boolean isLevelingUp;
    private final ArrayList<Upgrade> upgradeOptions;

    // 升级面板布局参数（可根据屏幕分辨率动态调整）
    private static final float BTN_WIDTH = 220;
    private static final float BTN_HEIGHT = 120;
    private static final float BTN_SPACING = 30;

    public UpgradeSystem(Player player) {
        this.player = player;
        this.isLevelingUp = false;
        this.upgradeOptions = new ArrayList<>();
    }

    /**
     * 进入升级状态，随机生成 3 个不重复的升级选项。
     * 由 Main 在检测到玩家升级时调用。
     */
    public void generateOptions() {
        isLevelingUp = true;
        upgradeOptions.clear();

        Upgrade.Type[] types = Upgrade.Type.values();
        ArrayList<Upgrade.Type> available = new ArrayList<>();
        for (Upgrade.Type type : types) {
            available.add(type);
        }

        // 随机打乱后取前 3 个
        java.util.Collections.shuffle(available);
        for (int i = 0; i < Math.min(3, available.size()); i++) {
            upgradeOptions.add(new Upgrade(available.get(i)));
        }
    }

    /**
     * 处理升级面板的点击输入。
     * 每帧由 Main 在升级状态下调用。
     *
     * @return 如果玩家成功选择了某个选项，返回 true
     */
    public boolean handleInput() {
        if (!Gdx.input.justTouched()) return false;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalW = BTN_WIDTH * 3 + BTN_SPACING * 2;
        float startX = (screenW - totalW) / 2;
        float btnY = screenH / 2 - BTN_HEIGHT / 2;

        for (int i = 0; i < upgradeOptions.size(); i++) {
            float btnX = startX + i * (BTN_WIDTH + BTN_SPACING);
            if (mouseX >= btnX && mouseX <= btnX + BTN_WIDTH &&
                mouseY >= btnY && mouseY <= btnY + BTN_HEIGHT) {

                // 应用选中的升级效果到玩家
                upgradeOptions.get(i).apply(player);
                isLevelingUp = false;
                upgradeOptions.clear();
                return true;
            }
        }
        return false;
    }

    /**
     * 渲染升级面板。
     * 包含半透明遮罩、彩色按钮背景、黑色文字。
     *
     * @param batch         SpriteBatch（已设置 HUD 投影矩阵）
     * @param shapeRenderer ShapeRenderer（已设置 HUD 投影矩阵）
     * @param font          中文字体
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        if (!isLevelingUp) return;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // ====== 半透明黑色遮罩 ======
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, screenW, screenH);

        // ====== 绘制 3 个选项按钮 ======
        float totalW = BTN_WIDTH * 3 + BTN_SPACING * 2;
        float startX = (screenW - totalW) / 2;
        float btnY = screenH / 2 - BTN_HEIGHT / 2;

        for (int i = 0; i < upgradeOptions.size(); i++) {
            Upgrade upgrade = upgradeOptions.get(i);
            float btnX = startX + i * (BTN_WIDTH + BTN_SPACING);

            // 按钮背景（使用升级类型对应的颜色）
            shapeRenderer.setColor(upgrade.getColor());
            shapeRenderer.rect(btnX, btnY, BTN_WIDTH, BTN_HEIGHT);

            // 按钮白色边框
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(btnX, btnY, BTN_WIDTH, 2);
            shapeRenderer.rect(btnX, btnY + BTN_HEIGHT - 2, BTN_WIDTH, 2);
            shapeRenderer.rect(btnX, btnY, 2, BTN_HEIGHT);
            shapeRenderer.rect(btnX + BTN_WIDTH - 2, btnY, 2, BTN_HEIGHT);
        }
        shapeRenderer.end();

        // ====== 绘制文字（黑色） ======
        batch.begin();
        font.setColor(Color.BLACK);
        for (int i = 0; i < upgradeOptions.size(); i++) {
            Upgrade upgrade = upgradeOptions.get(i);
            float btnX = startX + i * (BTN_WIDTH + BTN_SPACING);
            float btnY2 = screenH / 2 - BTN_HEIGHT / 2;

            font.draw(batch, upgrade.getName(), btnX + 10, btnY2 + BTN_HEIGHT - 20);
            font.draw(batch, upgrade.getDescription(), btnX + 10, btnY2 + BTN_HEIGHT - 50);
        }
        font.draw(batch, "LEVEL UP! Choose an upgrade:", screenW / 2 - 100, screenH / 2 + BTN_HEIGHT / 2 + 40);
        batch.end();
    }

    // ==================== 状态查询 ====================

    public boolean isLevelingUp() {
        return isLevelingUp;
    }

    public void setLevelingUp(boolean levelingUp) {
        isLevelingUp = levelingUp;
    }
}
