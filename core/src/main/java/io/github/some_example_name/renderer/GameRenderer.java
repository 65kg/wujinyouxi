package io.github.some_example_name.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.some_example_name.Coin;
import io.github.some_example_name.DamageNumber;
import io.github.some_example_name.ExpOrb;
import io.github.some_example_name.Projectile;
import io.github.some_example_name.character.Player;
import io.github.some_example_name.enemy.Enemy;
import io.github.some_example_name.manager.EntityManager;
import io.github.some_example_name.system.UpgradeSystem;

/**
 * 游戏渲染器，负责所有游戏画面的绘制。
 * <p>
 * 分为两层：
 * - 世界坐标层：地图、实体、玩家（跟随摄像机）
 * - HUD 坐标层：血条、经验条、文字、升级面板（固定屏幕位置）
 * <p>
 * 未来扩展：可拆分为 WorldRenderer 和 HUDRenderer 两个子类。
 */
public class GameRenderer {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera camera;
    private final Matrix4 hudMatrix;
    private final BitmapFont font;
    private final Texture grassTexture;
    private final float mapWidth;
    private final float mapHeight;

    /**
     * 构造函数。
     *
     * @param batch         精灵批处理
     * @param shapeRenderer 形状渲染器
     * @param camera        正交摄像机（世界坐标）
     * @param font          中文字体
     * @param grassTexture  草地背景纹理
     * @param mapWidth      地图宽度
     * @param mapHeight     地图高度
     */
    public GameRenderer(SpriteBatch batch, ShapeRenderer shapeRenderer,
                        OrthographicCamera camera, BitmapFont font,
                        Texture grassTexture, float mapWidth, float mapHeight) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
        this.font = font;
        this.grassTexture = grassTexture;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        // HUD 使用屏幕像素坐标系（左上角原点，Y 向下）
        this.hudMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * 渲染完整游戏画面。
     *
     * @param entityManager 实体管理器（提供要渲染的实体列表）
     * @param player        玩家实例
     * @param upgradeSystem 升级系统（负责渲染升级面板）
     */
    public void render(EntityManager entityManager, Player player, UpgradeSystem upgradeSystem,
                        boolean isPaused, boolean showStatsPanel) {
        // ========== 世界坐标绘制 ==========
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        renderWorldBackground();
        renderWorldEntities(entityManager);
        renderPlayer(player);

        // ========== HUD 坐标绘制 ==========
        batch.setProjectionMatrix(hudMatrix);
        shapeRenderer.setProjectionMatrix(hudMatrix);

        renderHUD(player, isPaused, showStatsPanel);
        upgradeSystem.render(batch, shapeRenderer, font);
    }

    /**
     * 渲染世界背景：草地纹理 + 地图边界。
     */
    private void renderWorldBackground() {
        // 草地纹理
        batch.begin();
        batch.draw(grassTexture, 0, 0, mapWidth, mapHeight);
        batch.end();

        // 地图边界（灰色粗线）
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(0, 0, mapWidth, 6);
        shapeRenderer.rect(0, mapHeight - 6, mapWidth, 6);
        shapeRenderer.rect(0, 0, 6, mapHeight);
        shapeRenderer.rect(mapWidth - 6, 0, 6, mapHeight);
        shapeRenderer.end();
    }

    /**
     * 渲染世界中的实体：敌人、投射物、经验球、金币。
     */
    private void renderWorldEntities(EntityManager entityManager) {
        // 敌人可能使用 SpriteBatch 绘制图片，各自独立管理渲染状态
        for (Enemy enemy : entityManager.getEnemies()) {
            if (enemy.isAlive()) enemy.render(shapeRenderer);
        }

        // 投射物统一使用 ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Projectile projectile : entityManager.getProjectiles()) {
            if (projectile.isActive()) projectile.render(shapeRenderer);
        }
        shapeRenderer.end();

        // 经验球和金币使用图标纹理（SpriteBatch）
        batch.begin();
        for (ExpOrb orb : entityManager.getExpOrbs()) {
            if (orb.isActive()) orb.render(batch);
        }
        for (Coin coin : entityManager.getCoins()) {
            if (coin.isActive()) coin.render(batch);
        }
        batch.end();

        // 伤害数字（使用 SpriteBatch）
        batch.begin();
        for (DamageNumber dmg : entityManager.getDamageNumbers()) {
            if (dmg.isActive()) dmg.render(batch, font);
        }
        batch.end();
    }

    /**
     * 渲染玩家角色。
     */
    private void renderPlayer(Player player) {
        batch.begin();
        player.render(batch);
        batch.end();
    }

    /**
     * 渲染 HUD：血条、经验条、等级和经验文字。
     */
    private void renderHUD(Player player, boolean isPaused, boolean showStatsPanel) {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // ========== 经验条：铺满屏幕最上方 ==========
        float expBarHeight = 14;
        float expBarY = screenH - expBarHeight;
        float expPercent = (float) player.getExp() / player.getMaxExp();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, expBarY, screenW, expBarHeight);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(0, expBarY, screenW * expPercent, expBarHeight);

        // ========== 血条：屏幕左上角，经验条下方 ==========
        float hpBarWidth = 180;
        float hpBarHeight = 14;
        float hpBarX = 10;
        float hpBarY = screenH - expBarHeight - 8 - hpBarHeight;
        float hpPercent = (float) player.getHp() / player.getMaxHp();

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
        shapeRenderer.setColor(hpPercent > 0.5f ? Color.GREEN : hpPercent > 0.25f ? Color.YELLOW : Color.RED);
        shapeRenderer.rect(hpBarX, hpBarY, hpBarWidth * hpPercent, hpBarHeight);

        // ========== 左下角按钮背景 ==========
        float btnSize = 40;
        float btnY = 20;
        float pauseX = 20;
        float statsX = 70;

        // 暂停按钮背景
        shapeRenderer.setColor(isPaused ? Color.GOLD : Color.DARK_GRAY);
        shapeRenderer.rect(pauseX, btnY, btnSize, btnSize);
        // 背包按钮背景
        shapeRenderer.setColor(showStatsPanel ? Color.GOLD : Color.DARK_GRAY);
        shapeRenderer.rect(statsX, btnY, btnSize, btnSize);
        shapeRenderer.end();

        // ========== 文字：等级、金币、按钮文字 ==========
        batch.begin();

        // 等级文字（左上角，血条下方）
        font.setColor(Color.WHITE);
        font.draw(batch, "Lv." + player.getLevel() + "  Exp: " + player.getExp() + "/" + player.getMaxExp(),
                  10, hpBarY - 8);

        // 金币数量（右上角，经验条下方）
        font.setColor(Color.GOLD);
        String coinText = "金币: " + player.getCurrentCoins();
        font.draw(batch, coinText, screenW - 140, screenH - expBarHeight - 8);

        // 按钮文字
        font.setColor(Color.WHITE);
        font.draw(batch, "暂", pauseX + 12, btnY + 30);
        font.draw(batch, "背", statsX + 12, btnY + 30);

        batch.end();
    }
}
