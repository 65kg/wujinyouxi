package io.github.some_example_name.map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * 地图生成器，负责生成游戏世界的大地图纹理。
 * <p>
 * 当前生成带随机草斑、石头、花朵、枯木的暗色草地，作为 3000x3000 地图的视觉基底。
 * 使用固定随机种子保证每次生成的地图纹理一致。
 * <p>
 * 未来可扩展：添加道路、河流、建筑区域等地形元素。
 */
public class MapGenerator {

    /**
     * 生成草地纹理。
     *
     * @param width  地图宽度（像素）
     * @param height 地图高度（像素）
     * @return 生成的草地 Texture，需要在 dispose 时释放
     */
    public static Texture generateGrassTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        java.util.Random rand = new java.util.Random(12345);

        // ====== 底色 ======
        // 深暗草绿色作为基底
        pixmap.setColor(0.08f, 0.20f, 0.04f, 1f);
        pixmap.fill();

        // ====== 随机草斑 ======
        // 大量不同深浅的绿色色块，形成草地纹理的自然变化
        for (int i = 0; i < 8000; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int w = 8 + rand.nextInt(24);
            int h = 8 + rand.nextInt(24);
            float shade = rand.nextFloat();
            if (shade < 0.33f) {
                pixmap.setColor(0.10f, 0.25f, 0.05f, 1f);
            } else if (shade < 0.66f) {
                pixmap.setColor(0.12f, 0.28f, 0.06f, 1f);
            } else {
                pixmap.setColor(0.06f, 0.18f, 0.03f, 1f);
            }
            pixmap.fillRectangle(x, y, w, h);
        }

        // ====== 浅色草点 ======
        // 小簇亮色像素模拟阳光下的草尖反光
        for (int i = 0; i < 3000; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            pixmap.setColor(0.14f, 0.32f, 0.08f, 1f);
            pixmap.drawPixel(x, y);
            if (x + 1 < width) pixmap.drawPixel(x + 1, y);
            if (y + 1 < height) pixmap.drawPixel(x, y + 1);
        }

        // ====== 石头装饰 ======
        // 深灰色小方块，带浅色高光，模拟散落的石块
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(width - 12);
            int y = rand.nextInt(height - 10);
            int w = 6 + rand.nextInt(8);
            int h = 4 + rand.nextInt(6);
            // 外轮廓
            pixmap.setColor(0.18f, 0.18f, 0.20f, 1f);
            pixmap.fillRectangle(x, y, w, h);
            // 高光
            pixmap.setColor(0.22f, 0.22f, 0.24f, 1f);
            pixmap.fillRectangle(x + 1, y + 1, w - 2, h - 2);
        }

        // ====== 花朵装饰 ======
        // 2x2 像素的小方块，随机红色或黄色，点缀草地
        for (int i = 0; i < 800; i++) {
            int x = rand.nextInt(width - 2);
            int y = rand.nextInt(height - 2);
            if (rand.nextBoolean()) {
                pixmap.setColor(0.45f, 0.12f, 0.10f, 1f); // 红色花
            } else {
                pixmap.setColor(0.40f, 0.35f, 0.08f, 1f); // 黄色花
            }
            pixmap.drawPixel(x, y);
            pixmap.drawPixel(x + 1, y);
            pixmap.drawPixel(x, y + 1);
            pixmap.drawPixel(x + 1, y + 1);
        }

        // ====== 枯木/树桩装饰 ======
        // 棕色矩形，模拟倒下的树干或树桩
        for (int i = 0; i < 60; i++) {
            int x = rand.nextInt(width - 20);
            int y = rand.nextInt(height - 16);
            pixmap.setColor(0.20f, 0.14f, 0.08f, 1f);
            pixmap.fillRectangle(x, y, 12 + rand.nextInt(10), 8 + rand.nextInt(8));
            pixmap.setColor(0.25f, 0.18f, 0.10f, 1f);
            pixmap.fillRectangle(x + 2, y + 2, 4, 4);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
