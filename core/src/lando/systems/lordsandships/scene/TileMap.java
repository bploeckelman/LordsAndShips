package lando.systems.lordsandships.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.utils.TimeUtils;
import lando.systems.lordsandships.utils.Assets;

/**
 * TileMap
 *
 *
 *
 * Brian Ploeckelman created on 5/31/2014.
 */
public class TileMap
{
	static final int BLOCK_TILES = 25;
	static final int NUM_LAYERS = 5;

	int layers[];
	int width, height;
	long startTime = TimeUtils.nanoTime();
	SpriteCache caches[];

	public TileMap(int width, int height) {
		this.width = width;
		this.height = height;

		generate();
	}

	public void generate() {
		layers = new int[NUM_LAYERS];
		caches = new SpriteCache[NUM_LAYERS];

		for (int i = 0; i < NUM_LAYERS; ++i) {
			caches[i] = new SpriteCache();
			SpriteCache cache = caches[i];
			cache.beginCache();
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					cache.add(Assets.tiles, x << 5, y << 5, 1 + Assets.rand.nextInt(5) * 33, 1 + Assets.rand.nextInt(5) * 33, 32, 32);
				}
			}
			layers[i] = cache.endCache();
		}

		SpriteCache spriteCache = new SpriteCache();
	}

	public void render(Camera camera) {
		for (int i = 0; i < NUM_LAYERS; i++) {
			SpriteCache cache = caches[i];
			cache.setProjectionMatrix(camera.combined);
			cache.begin();
			for (int j = 0; j < width * height; j += BLOCK_TILES) {
				cache.draw(layers[i], j, BLOCK_TILES);
			}
			cache.end();
		}

		if (TimeUtils.nanoTime() - startTime >= 1000000000) {
			System.out.println("fps( " + Gdx.graphics.getFramesPerSecond() + " )");
			startTime = TimeUtils.nanoTime();
		}
	}

}
