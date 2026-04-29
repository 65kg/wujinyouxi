package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.character.Player;
import io.github.some_example_name.character.Shinobi;
import io.github.some_example_name.enemy.Enemy;
import io.github.some_example_name.DamageNumber;
import io.github.some_example_name.manager.EntityManager;
import io.github.some_example_name.map.MapGenerator;
import io.github.some_example_name.renderer.GameRenderer;
import io.github.some_example_name.system.CombatSystem;
import io.github.some_example_name.system.ExpSystem;
import io.github.some_example_name.system.SpawnSystem;
import io.github.some_example_name.system.UpgradeSystem;
import io.github.some_example_name.weapon.Bow;
import io.github.some_example_name.weapon.Pistol;
import io.github.some_example_name.weapon.Staff;
import io.github.some_example_name.weapon.Weapon;

/**
 * 游戏主入口类，负责初始化所有子系统并进行调度。
 * <p>
 * 设计原则：
 * - 本类<strong>只负责协调</strong>，不实现具体游戏逻辑
 * - 所有逻辑已拆分到各 System 中，通过组合方式使用
 * - 更新顺序和渲染顺序由本类控制，确保数据一致性
 * <p>
 * 子系统职责：
 * <ul>
 *   <li>{@link EntityManager} - 管理所有动态实体的增删查</li>
 *   <li>{@link CombatSystem} - 自动攻击、碰撞检测、伤害结算</li>
 *   <li>{@link SpawnSystem} - 敌人生成逻辑</li>
 *   <li>{@link ExpSystem} - 经验球更新、吸收、升级触发</li>
 *   <li>{@link UpgradeSystem} - 升级选项生成、面板渲染、输入处理</li>
 *   <li>{@link GameRenderer} - 世界渲染 + HUD 渲染</li>
 *   <li>{@link MapGenerator} - 地图纹理生成（静态工具类）</li>
 * </ul>
 */
public class Main extends ApplicationAdapter {

    // ==================== 渲染资源 ====================
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camera;

    // ==================== 游戏世界 ====================
    /** 地图宽度（像素） */
    private static final float MAP_WIDTH = 3000;
    /** 地图高度（像素） */
    private static final float MAP_HEIGHT = 3000;
    private Texture grassTexture;
    private Player player;

    // ==================== 子系统 ====================
    private EntityManager entityManager;
    private CombatSystem combatSystem;
    private SpawnSystem spawnSystem;
    private ExpSystem expSystem;
    private UpgradeSystem upgradeSystem;
    private GameRenderer gameRenderer;

    // ==================== 游戏状态 ====================
    /**
     * 游戏全局状态。
     * - MENU: 主菜单，显示开始/强化/图鉴按钮
     * - PLAYING: 游戏进行中
     */
    private enum GameState {
        MENU, WEAPON_SELECT, LOADING, PLAYING
    }

    private GameState currentState;

    // ==================== 加载界面配置 ====================
    private float loadingTimer;
    private static final float LOADING_DURATION = 1.5f;

    // ==================== 死亡菜单配置 ====================
    private boolean showDeathMenu;
    private static final String[] DEATH_MENU_BUTTONS = {"重新开始", "返回主菜单"};
    private static final float DEATH_BTN_WIDTH = 260;
    private static final float DEATH_BTN_HEIGHT = 65;
    private static final float DEATH_BTN_SPACING = 25;

    // ==================== 武器选择界面配置 ====================
    private static final float WEAPON_CARD_WIDTH = 220;
    private static final float WEAPON_CARD_HEIGHT = 320;
    private static final float WEAPON_CARD_SPACING = 40;
    private final Weapon[] WEAPONS = {new Pistol(), new Staff(), new Bow()};

    // ==================== 主菜单配置 ====================
    private static final String[] MENU_BUTTONS = {"开始", "强化", "图鉴"};
    private static final float MENU_BTN_WIDTH = 300;
    private static final float MENU_BTN_HEIGHT = 75;
    private static final float MENU_BTN_SPACING = 30;
    private static final String VERSION_TEXT = "v1.0.0";
    private static final String AUTHOR_TEXT = "作者：无限叙事组";

    /** 按钮颜色：开始=蓝，强化=橙，图鉴=青 */
    private static final Color[] BTN_COLORS = {
        new Color(0.35f, 0.65f, 1.0f, 1f),
        new Color(1.0f, 0.55f, 0.15f, 1f),
        new Color(0.15f, 0.85f, 0.85f, 1f)
    };

    /** 背景闪烁星星 */
    private Star[] stars;

    /** 星星数据 */
    private static class Star {
        float x, y, size, phase, speed;
        Star(float x, float y, float size, float phase, float speed) {
            this.x = x; this.y = y; this.size = size;
            this.phase = phase; this.speed = speed;
        }
    }

    @Override
    public void create() {
        // ------ 初始化渲染资源 ------
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        initFont();

        // ------ 初始化摄像机 ------
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // ------ 生成地图 ------
        grassTexture = MapGenerator.generateGrassTexture((int) MAP_WIDTH, (int) MAP_HEIGHT);

        // ------ 初始化玩家（默认使用忍者角色）------
        player = new Shinobi(MAP_WIDTH / 2 - 32, MAP_HEIGHT / 2 - 32);

        // ------ 初始化子系统 ------
        // 注意初始化顺序：EntityManager 必须先创建，其他系统依赖它
        entityManager = new EntityManager();
        combatSystem = new CombatSystem(entityManager, player);
        spawnSystem = new SpawnSystem(entityManager, camera);
        expSystem = new ExpSystem(entityManager, player);
        upgradeSystem = new UpgradeSystem(player);
        gameRenderer = new GameRenderer(batch, shapeRenderer, camera, font, grassTexture, MAP_WIDTH, MAP_HEIGHT);

        // ------ 初始状态：主菜单 ------
        currentState = GameState.MENU;
        showDeathMenu = false;

        // ------ 初始化菜单星星 ------
        stars = new Star[70];
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star(
                rand.nextFloat() * Gdx.graphics.getWidth(),
                rand.nextFloat() * Gdx.graphics.getHeight(),
                1 + rand.nextFloat() * 2.5f,
                rand.nextFloat() * (float) Math.PI * 2,
                1 + rand.nextFloat() * 3
            );
        }
    }

    /**
     * 加载中文字体（黑体），支持游戏内中文显示。
     * <p>
     * 使用 FreeTypeFontGenerator 从系统字体文件动态生成 BitmapFont，
     * 相比默认字体可支持任意中文文本。
     */
    private void initFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.absolute("C:/Windows/Fonts/simhei.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        // 预生成所有 CJK 统一表意文字（约 2 万字），以后任何中文都能直接显示
        parameter.characters = buildFullCharSet();
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    /**
     * 构建完整的字符集，包含 ASCII + 所有常用中文汉字。
     * <p>
     * Unicode 范围 0x4E00-0x9FA5 覆盖了简体、繁体中文的几乎所有常用字，
     * 共约 20902 个字符。生成耗时约 3-8 秒，但只需执行一次。
     */
    private String buildFullCharSet() {
        StringBuilder sb = new StringBuilder();
        sb.append(FreeTypeFontGenerator.DEFAULT_CHARS);
        // CJK 统一表意文字（基本区）：覆盖 99.9% 的中文使用场景
        for (int i = 0x4E00; i <= 0x9FA5; i++) {
            sb.append((char) i);
        }
        return sb.toString();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        if (currentState == GameState.MENU) {
            // 主菜单状态：渲染菜单 + 处理菜单输入
            ScreenUtils.clear(0.02f, 0.02f, 0.04f, 1f);
            handleMenuInput();
            renderMenu();
        } else if (currentState == GameState.WEAPON_SELECT) {
            // 武器选择状态
            ScreenUtils.clear(0.02f, 0.02f, 0.04f, 1f);
            handleWeaponSelectInput();
            renderWeaponSelect();
        } else if (currentState == GameState.LOADING) {
            // 加载状态：黑屏 + 加载文字
            ScreenUtils.clear(0, 0, 0, 1f);
            update(deltaTime);
            renderLoadingScreen();
        } else {
            // 游戏状态：清屏 + 更新逻辑 + 渲染游戏画面
            ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);
            update(deltaTime);
            gameRenderer.render(entityManager, player, upgradeSystem);
            // 玩家死亡时叠加死亡菜单
            if (player.isDead() && showDeathMenu) {
                renderDeathMenu();
            }
        }
    }

    /**
     * 游戏主更新逻辑，按顺序调用各子系统。
     * <p>
     * 更新顺序很重要：
     * <ol>
     *   <li>玩家移动（输入响应）</li>
     *   <li>摄像机跟随（依赖玩家新位置）</li>
     *   <li>战斗系统（攻击 + 碰撞检测）</li>
     *   <li>敌人移动（AI 追击）</li>
     *   <li>投射物移动</li>
     *   <li>边界清理（投射物出界）</li>
     *   <li>经验系统（经验球 magnet + 吸收 + 升级判定）</li>
     *   <li>死亡清理 + 掉落（触发经验球生成）</li>
     *   <li>生成系统（补充新敌人）</li>
     * </ol>
     */
    private void update(float deltaTime) {
        // 加载状态：倒计时，结束后进入游戏
        if (currentState == GameState.LOADING) {
            loadingTimer += deltaTime;
            if (loadingTimer >= LOADING_DURATION) {
                currentState = GameState.PLAYING;
            }
            return;
        }

        // 玩家死亡：播放死亡动画，显示死亡菜单
        if (player.isDead()) {
            player.update(deltaTime);
            if (!showDeathMenu) {
                showDeathMenu = true;
            }
            if (showDeathMenu) {
                handleDeathMenuInput();
            }
            return;
        }

        // 升级状态下游戏暂停，只处理升级面板输入
        if (upgradeSystem.isLevelingUp()) {
            upgradeSystem.handleInput();
            return;
        }

        // ---- 0.5 生命回复 ----
        if (player.getHp() < player.getMaxHp() && player.getHpRegen() > 0) {
            player.setHp(Math.min(player.getMaxHp(),
                player.getHp() + (int) (player.getHpRegen() * deltaTime)));
        }

        // ---- 1. 玩家移动 ----
        player.update(deltaTime);
        player.clampPosition(0, 0, MAP_WIDTH - player.getWidth(), MAP_HEIGHT - player.getHeight());

        // ---- 2. 摄像机跟随 ----
        camera.position.set(player.getCenterX(), player.getCenterY(), 0);
        camera.update();

        // ---- 3. 战斗系统 ----
        // 处理攻击冷却、自动攻击、投射物与敌人碰撞、敌人与玩家碰撞
        combatSystem.update(deltaTime);
        combatSystem.handleCollisions();
        combatSystem.handleEnemyPlayerCollisions();

        // ---- 4. 更新敌人 ----
        float slowRange = player.getSlowAuraRange();
        float slowFactor = slowRange > 0 ? 0.8f : 1.0f;
        for (Enemy enemy : entityManager.getEnemies()) {
            if (enemy.isAlive()) {
                // 减速光环：范围内的敌人移速降低
                float actualSpeed = enemy.getSpeed();
                if (slowRange > 0) {
                    float dx = enemy.getX() - player.getCenterX();
                    float dy = enemy.getY() - player.getCenterY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < slowRange) {
                        actualSpeed *= slowFactor;
                    }
                }
                enemy.update(deltaTime, player.getCenterX(), player.getCenterY(), actualSpeed);
            }
        }

        // ---- 4.5 敌人之间碰撞推开 ----
        resolveEnemyCollisions();

        // ---- 5. 更新投射物位置 ----
        for (Projectile projectile : entityManager.getProjectiles()) {
            if (projectile.isActive()) {
                projectile.update(deltaTime);
            }
        }

        // ---- 6. 投射物超出地图边界销毁 ----
        for (Projectile projectile : entityManager.getProjectiles()) {
            if (projectile.isActive()) {
                if (projectile.getX() < -100 || projectile.getX() > MAP_WIDTH + 100 ||
                    projectile.getY() < -100 || projectile.getY() > MAP_HEIGHT + 100) {
                    projectile.deactivate();
                }
            }
        }
        entityManager.cleanupInactiveProjectiles();

        // ---- 7. 经验系统 ----
        // 更新经验球位置、检测吸收、触发升级
        boolean leveledUp = expSystem.update(deltaTime);
        if (leveledUp) {
            upgradeSystem.generateOptions();
        }

        // ---- 8. 清理死亡敌人并掉落经验 ----
        // 使用回调模式：死亡时通过 ExpSystem 生成经验球
        entityManager.cleanupDeadEnemies(enemy -> {
            expSystem.onEnemyDied(enemy);
            // 击杀回血
            if (player.getKillHeal() > 0) {
                player.setHp(Math.min(player.getMaxHp(), player.getHp() + player.getKillHeal()));
            }
            // 死亡爆炸
            if (player.getDeathExplosionDamage() > 0) {
                for (Enemy e : entityManager.getEnemies()) {
                    if (e.isAlive() && e != enemy) {
                        float dx = e.getX() - enemy.getX();
                        float dy = e.getY() - enemy.getY();
                        float d = (float) Math.sqrt(dx * dx + dy * dy);
                        if (d < 80) {
                            e.takeDamage(player.getDeathExplosionDamage());
                        }
                    }
                }
            }
        });

        // ---- 8.5 更新伤害数字 ----
        for (DamageNumber dmg : entityManager.getDamageNumbers()) {
            if (dmg.isActive()) dmg.update(deltaTime);
        }
        entityManager.cleanupInactiveDamageNumbers();

        // ---- 9. 生成新敌人 ----
        spawnSystem.update(deltaTime);
    }

    // ==================== 主菜单渲染与输入 ====================

    /**
     * 渲染主菜单界面。
     * <p>
     * 视觉效果参考：
     * - 纯黑背景 + 70颗闪烁星星
     * - 四个角落齿轮装饰
     * - 三个圆角矩形按钮（蓝/橙/青），带外发光
     * - 右上角版本号和作者
     */
    private void renderMenu() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float time = System.currentTimeMillis() / 1000f;

        // ====== 闪烁星星背景 ======
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Star star : stars) {
            float alpha = (float) (Math.sin(time * star.speed + star.phase) * 0.5 + 0.5) * 0.8f;
            shapeRenderer.setColor(0.5f + alpha * 0.3f, 0.7f + alpha * 0.2f, 1.0f, alpha);
            shapeRenderer.circle(star.x, star.y, star.size);
        }
        shapeRenderer.end();

        // ====== 齿轮装饰（四个角） ======
        drawGear(60, 60, 55, 8, new Color(0.12f, 0.15f, 0.20f, 1f));
        drawGear(screenW - 60, 60, 45, 6, new Color(0.10f, 0.13f, 0.18f, 1f));
        drawGear(100, screenH - 80, 35, 5, new Color(0.10f, 0.13f, 0.18f, 1f));
        drawGear(screenW - 100, screenH - 80, 40, 7, new Color(0.12f, 0.15f, 0.20f, 1f));

        // ====== 计算按钮位置 ======
        float totalHeight = MENU_BUTTONS.length * MENU_BTN_HEIGHT
            + (MENU_BUTTONS.length - 1) * MENU_BTN_SPACING;
        float startY = (screenH + totalHeight) / 2 - MENU_BTN_HEIGHT;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < MENU_BUTTONS.length; i++) {
            float btnX = (screenW - MENU_BTN_WIDTH) / 2;
            float btnY = startY - i * (MENU_BTN_HEIGHT + MENU_BTN_SPACING);
            Color baseColor = BTN_COLORS[i];

            // 外发光（半透明放大矩形）
            shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.12f);
            drawRoundedRect(btnX - 10, btnY - 10, MENU_BTN_WIDTH + 20, MENU_BTN_HEIGHT + 20, 14);
            shapeRenderer.setColor(baseColor.r, baseColor.g, baseColor.b, 0.06f);
            drawRoundedRect(btnX - 20, btnY - 20, MENU_BTN_WIDTH + 40, MENU_BTN_HEIGHT + 40, 20);

            // 按钮主体（稍暗的底色）
            shapeRenderer.setColor(baseColor.r * 0.5f, baseColor.g * 0.5f, baseColor.b * 0.5f, 1f);
            drawRoundedRect(btnX, btnY, MENU_BTN_WIDTH, MENU_BTN_HEIGHT, 10);

            // 按钮上半部分高光（更亮）
            shapeRenderer.setColor(baseColor.r * 0.75f, baseColor.g * 0.75f, baseColor.b * 0.75f, 1f);
            drawRoundedRectTop(btnX, btnY + MENU_BTN_HEIGHT * 0.45f, MENU_BTN_WIDTH, MENU_BTN_HEIGHT * 0.55f, 10);
        }

        shapeRenderer.end();

        // ====== 绘制按钮文字（放大2.2倍） ======
        batch.begin();
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        for (int i = 0; i < MENU_BUTTONS.length; i++) {
            float btnX = (screenW - MENU_BTN_WIDTH) / 2;
            float btnY = startY - i * (MENU_BTN_HEIGHT + MENU_BTN_SPACING);
            float textWidth = getTextWidth(MENU_BUTTONS[i]) * 2.2f;
            float textX = btnX + (MENU_BTN_WIDTH - textWidth) / 2;
            float textY = btnY + MENU_BTN_HEIGHT / 2 + 14;
            font.draw(batch, MENU_BUTTONS[i], textX, textY);
        }
        font.getData().setScale(1f);

        // 右上角版本号和作者（正常大小）
        font.setColor(0.85f, 0.85f, 0.85f, 1f);
        font.draw(batch, VERSION_TEXT, screenW - 150, screenH - 20);
        font.draw(batch, AUTHOR_TEXT, screenW - 150, screenH - 45);
        batch.end();
    }

    /**
     * 处理主菜单的点击输入。
     */
    private void handleMenuInput() {
        if (!Gdx.input.justTouched()) return;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalHeight = MENU_BUTTONS.length * MENU_BTN_HEIGHT
            + (MENU_BUTTONS.length - 1) * MENU_BTN_SPACING;
        float startY = (screenH + totalHeight) / 2 - MENU_BTN_HEIGHT;

        for (int i = 0; i < MENU_BUTTONS.length; i++) {
            float btnX = (screenW - MENU_BTN_WIDTH) / 2;
            float btnY = startY - i * (MENU_BTN_HEIGHT + MENU_BTN_SPACING);

            if (mouseX >= btnX && mouseX <= btnX + MENU_BTN_WIDTH &&
                mouseY >= btnY && mouseY <= btnY + MENU_BTN_HEIGHT) {

                switch (i) {
                    case 0: // 开始 -> 进入武器选择界面
                        currentState = GameState.WEAPON_SELECT;
                        break;
                    case 1: // 强化（预留）
                        // TODO: 打开强化界面
                        break;
                    case 2: // 图鉴（预留）
                        // TODO: 打开图鉴界面
                        break;
                }
                return;
            }
        }
    }

    // ==================== 菜单绘制辅助方法 ====================

    /**
     * 绘制圆角矩形（完整）。
     *
     * @param x 左上角 X
     * @param y 左上角 Y
     * @param w 宽度
     * @param h 高度
     * @param r 圆角半径
     */
    private void drawRoundedRect(float x, float y, float w, float h, float r) {
        shapeRenderer.circle(x + r, y + r, r);
        shapeRenderer.circle(x + w - r, y + r, r);
        shapeRenderer.circle(x + r, y + h - r, r);
        shapeRenderer.circle(x + w - r, y + h - r, r);
        shapeRenderer.rect(x + r, y, w - 2 * r, h);
        shapeRenderer.rect(x, y + r, w, h - 2 * r);
    }

    /**
     * 绘制圆角矩形（仅上半部分圆角）。
     * 用于按钮的高光层。
     */
    private void drawRoundedRectTop(float x, float y, float w, float h, float r) {
        shapeRenderer.circle(x + r, y + h - r, r);
        shapeRenderer.circle(x + w - r, y + h - r, r);
        shapeRenderer.rect(x + r, y, w - 2 * r, h);
        shapeRenderer.rect(x, y + r, w, h - r);
    }

    /**
     * 绘制简化齿轮装饰。
     *
     * @param cx     中心 X
     * @param cy     中心 Y
     * @param radius 外半径
     * @param teeth  齿数
     * @param color  颜色
     */
    private void drawGear(float cx, float cy, float radius, int teeth, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // 外圆
        shapeRenderer.setColor(color);
        shapeRenderer.circle(cx, cy, radius);
        // 内圆镂空
        shapeRenderer.setColor(0.02f, 0.02f, 0.04f, 1f);
        shapeRenderer.circle(cx, cy, radius * 0.5f);
        shapeRenderer.end();

        // 齿
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        for (int i = 0; i < teeth; i++) {
            float angle = (float) (i * Math.PI * 2 / teeth);
            float x = cx + (float) Math.cos(angle) * radius * 0.82f;
            float y = cy + (float) Math.sin(angle) * radius * 0.82f;
            shapeRenderer.rect(x - 3.5f, y - 3.5f, 7, 7);
        }
        shapeRenderer.end();

        // 中心轴
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f, 1f);
        shapeRenderer.circle(cx, cy, radius * 0.2f);
        shapeRenderer.end();
    }

    /**
     * 粗略计算文字宽度，用于按钮文字居中。
     */
    private float getTextWidth(String text) {
        return text.length() * 16f;
    }

    /**
     * 处理敌人之间的碰撞，防止怪物模型重叠。
     * <p>
     * 算法：遍历所有敌人对，检测距离是否小于半径之和。
     * 如果重叠，将两个敌人沿连线方向各推开一半重叠距离。
     */
    private void resolveEnemyCollisions() {
        java.util.List<Enemy> enemies = entityManager.getEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy a = enemies.get(i);
            if (!a.isAlive()) continue;

            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy b = enemies.get(j);
                if (!b.isAlive()) continue;

                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float minDist = a.getRadius() + b.getRadius();

                if (dist < minDist && dist > 0) {
                    float overlap = minDist - dist;
                    float pushX = (dx / dist) * overlap * 0.5f;
                    float pushY = (dy / dist) * overlap * 0.5f;
                    a.push(-pushX, -pushY);
                    b.push(pushX, pushY);
                }
            }
        }
    }

    // ==================== 武器选择界面渲染与输入 ====================

    /**
     * 渲染武器选择界面。
     * <p>
     * 三个卡片水平排列，每个卡片显示武器名称、属性、描述和颜色主题。
     */
    private void renderWeaponSelect() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // 标题
        batch.begin();
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        String title = "选择你的武器";
        float titleWidth = getTextWidth(title) * 2.5f;
        font.draw(batch, title, (screenW - titleWidth) / 2, screenH - 60);
        font.getData().setScale(1f);
        batch.end();

        // 计算卡片起始位置（水平居中）
        float totalWidth = WEAPONS.length * WEAPON_CARD_WIDTH
            + (WEAPONS.length - 1) * WEAPON_CARD_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float cardY = (screenH - WEAPON_CARD_HEIGHT) / 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < WEAPONS.length; i++) {
            Weapon w = WEAPONS[i];
            float cardX = startX + i * (WEAPON_CARD_WIDTH + WEAPON_CARD_SPACING);
            Color base = w.getColor();

            // 卡片外发光
            shapeRenderer.setColor(base.r, base.g, base.b, 0.1f);
            drawRoundedRect(cardX - 8, cardY - 8, WEAPON_CARD_WIDTH + 16, WEAPON_CARD_HEIGHT + 16, 16);

            // 卡片背景（深色）
            shapeRenderer.setColor(0.06f, 0.06f, 0.1f, 1f);
            drawRoundedRect(cardX, cardY, WEAPON_CARD_WIDTH, WEAPON_CARD_HEIGHT, 12);

            // 顶部色条
            shapeRenderer.setColor(base);
            drawRoundedRectTop(cardX, cardY + WEAPON_CARD_HEIGHT - 50, WEAPON_CARD_WIDTH, 50, 12);
        }

        shapeRenderer.end();

        // 绘制文字
        batch.begin();
        for (int i = 0; i < WEAPONS.length; i++) {
            Weapon w = WEAPONS[i];
            float cardX = startX + i * (WEAPON_CARD_WIDTH + WEAPON_CARD_SPACING);

            // 武器名称（卡片顶部，大字）
            font.getData().setScale(1.8f);
            font.setColor(Color.WHITE);
            float nameWidth = getTextWidth(w.getName()) * 1.8f;
            font.draw(batch, w.getName(), cardX + (WEAPON_CARD_WIDTH - nameWidth) / 2, cardY + WEAPON_CARD_HEIGHT - 18);
            font.getData().setScale(1f);

            // 属性列表
            font.setColor(0.8f, 0.8f, 0.8f, 1f);
            float attrY = cardY + WEAPON_CARD_HEIGHT - 80;
            float attrX = cardX + 20;
            font.draw(batch, "伤害: " + w.getDamage(), attrX, attrY);
            font.draw(batch, "射程: " + (int) w.getRange(), attrX, attrY - 25);
            font.draw(batch, "冷却: " + w.getCooldown() + "s", attrX, attrY - 50);

            // 描述（卡片底部）
            font.setColor(w.getColor());
            float descWidth = getTextWidth(w.getDescription());
            font.draw(batch, w.getDescription(), cardX + (WEAPON_CARD_WIDTH - descWidth) / 2, cardY + 40);
        }
        batch.end();
    }

    /**
     * 处理武器选择界面的点击输入。
     * 点击任意武器卡片后，装备该武器并进入加载界面。
     */
    private void handleWeaponSelectInput() {
        if (!Gdx.input.justTouched()) return;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalWidth = WEAPONS.length * WEAPON_CARD_WIDTH
            + (WEAPONS.length - 1) * WEAPON_CARD_SPACING;
        float startX = (screenW - totalWidth) / 2;
        float cardY = (screenH - WEAPON_CARD_HEIGHT) / 2;

        for (int i = 0; i < WEAPONS.length; i++) {
            float cardX = startX + i * (WEAPON_CARD_WIDTH + WEAPON_CARD_SPACING);
            if (mouseX >= cardX && mouseX <= cardX + WEAPON_CARD_WIDTH &&
                mouseY >= cardY && mouseY <= cardY + WEAPON_CARD_HEIGHT) {

                // 装备选中的武器
                player.setWeapon(WEAPONS[i]);
                // 进入加载界面
                currentState = GameState.LOADING;
                loadingTimer = 0;
                return;
            }
        }
    }

    /**
     * 渲染加载界面。
     * 纯黑背景 + 居中 "加载中..." 文字。
     */
    private void renderLoadingScreen() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        batch.begin();
        font.setColor(Color.WHITE);

        String text = "加载中...";
        float textWidth = getTextWidth(text);
        font.draw(batch, text, (screenW - textWidth) / 2, screenH / 2 + 8);

        batch.end();
    }

    // ==================== 死亡菜单渲染与输入 ====================

    /**
     * 渲染死亡菜单。
     * <p>
     * 半透明黑色遮罩 + "你阵亡了" 标题 + 两个按钮。
     */
    private void renderDeathMenu() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // 半透明遮罩
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, screenW, screenH);
        shapeRenderer.end();

        // 计算按钮位置
        float totalHeight = DEATH_MENU_BUTTONS.length * DEATH_BTN_HEIGHT
            + (DEATH_MENU_BUTTONS.length - 1) * DEATH_BTN_SPACING;
        float startY = (screenH + totalHeight) / 2 - DEATH_BTN_HEIGHT;

        // 绘制按钮背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < DEATH_MENU_BUTTONS.length; i++) {
            float btnX = (screenW - DEATH_BTN_WIDTH) / 2;
            float btnY = startY - i * (DEATH_BTN_HEIGHT + DEATH_BTN_SPACING);
            Color baseColor = i == 0
                ? new Color(0.35f, 0.65f, 1.0f, 1f)   // 重新开始 = 蓝色
                : new Color(0.85f, 0.35f, 0.35f, 1f); // 返回主菜单 = 红色
            shapeRenderer.setColor(baseColor.r * 0.6f, baseColor.g * 0.6f, baseColor.b * 0.6f, 1f);
            drawRoundedRect(btnX, btnY, DEATH_BTN_WIDTH, DEATH_BTN_HEIGHT, 10);
        }
        shapeRenderer.end();

        // 绘制标题和按钮文字
        batch.begin();
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        String title = "你阵亡了";
        float titleWidth = getTextWidth(title) * 2.5f;
        font.draw(batch, title, (screenW - titleWidth) / 2, startY + DEATH_BTN_HEIGHT + 60);
        font.getData().setScale(1.5f);
        for (int i = 0; i < DEATH_MENU_BUTTONS.length; i++) {
            float btnX = (screenW - DEATH_BTN_WIDTH) / 2;
            float btnY = startY - i * (DEATH_BTN_HEIGHT + DEATH_BTN_SPACING);
            float textWidth = getTextWidth(DEATH_MENU_BUTTONS[i]) * 1.5f;
            float textX = btnX + (DEATH_BTN_WIDTH - textWidth) / 2;
            float textY = btnY + DEATH_BTN_HEIGHT / 2 + 10;
            font.draw(batch, DEATH_MENU_BUTTONS[i], textX, textY);
        }
        font.getData().setScale(1f);
        batch.end();
    }

    /**
     * 处理死亡菜单的点击输入。
     */
    private void handleDeathMenuInput() {
        if (!Gdx.input.justTouched()) return;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float totalHeight = DEATH_MENU_BUTTONS.length * DEATH_BTN_HEIGHT
            + (DEATH_MENU_BUTTONS.length - 1) * DEATH_BTN_SPACING;
        float startY = (screenH + totalHeight) / 2 - DEATH_BTN_HEIGHT;

        for (int i = 0; i < DEATH_MENU_BUTTONS.length; i++) {
            float btnX = (screenW - DEATH_BTN_WIDTH) / 2;
            float btnY = startY - i * (DEATH_BTN_HEIGHT + DEATH_BTN_SPACING);

            if (mouseX >= btnX && mouseX <= btnX + DEATH_BTN_WIDTH &&
                mouseY >= btnY && mouseY <= btnY + DEATH_BTN_HEIGHT) {

                if (i == 0) {
                    // 重新开始：重置游戏并直接进入游戏
                    resetGame();
                    currentState = GameState.PLAYING;
                } else {
                    // 返回主菜单：重置游戏并回到菜单
                    resetGame();
                    currentState = GameState.MENU;
                }
                return;
            }
        }
    }

    /**
     * 重置游戏状态。
     * <p>
     * 保留当前武器选择，重置玩家位置、血量、等级、经验，
     * 清空所有敌人、投射物、经验球和伤害数字。
     */
    private void resetGame() {
        // 保留当前武器
        Weapon currentWeapon = player.getWeapon();

        // 释放旧玩家资源
        player.dispose();

        // 重建玩家
        player = new Shinobi(MAP_WIDTH / 2 - 32, MAP_HEIGHT / 2 - 32);
        if (currentWeapon != null) {
            player.setWeapon(currentWeapon);
        }

        // 重建实体管理器（清空所有敌人和投射物）
        entityManager = new EntityManager();

        // 重建各子系统
        combatSystem = new CombatSystem(entityManager, player);
        spawnSystem = new SpawnSystem(entityManager, camera);
        expSystem = new ExpSystem(entityManager, player);
        upgradeSystem = new UpgradeSystem(player);

        // 重置摄像机到玩家位置
        camera.position.set(player.getCenterX(), player.getCenterY(), 0);
        camera.update();

        // 关闭死亡菜单
        showDeathMenu = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        grassTexture.dispose();
        player.dispose();
    }
}
