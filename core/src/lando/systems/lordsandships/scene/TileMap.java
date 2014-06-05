package lando.systems.lordsandships.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import lando.systems.lordsandships.utils.Assets;

import java.util.*;

/**
 * TileMap
 *
 *
 *
 * Brian Ploeckelman created on 5/31/2014.
 */
public class TileMap implements Disposable
{
	static final int BLOCK_TILES = 25;
	static final int TILE_SIZE = 32; // pixels
	static final int NUM_LAYERS = 1;

	private static final Map<String, TextureRegion> tile_textures;
	private static final List<String> tile_textures_keys;
	static {
		Map<String, TextureRegion> tiles = new HashMap<String, TextureRegion>();
		tiles.put("tile-blank",      Assets.atlas.findRegion("tile-blank"));
		tiles.put("tile-block",      Assets.atlas.findRegion("tile-block"));
		tiles.put("tile-floor1",     Assets.atlas.findRegion("tile-floor1"));
		tiles.put("tile-wall-horiz", Assets.atlas.findRegion("tile-wall-horiz"));
		tiles.put("tile-wall-vert",  Assets.atlas.findRegion("tile-wall-vert"));
		tiles.put("tile-wall-nw",    Assets.atlas.findRegion("tile-wall-nw"));
		tiles.put("tile-wall-ne",    Assets.atlas.findRegion("tile-wall-ne"));
		tiles.put("tile-wall-se",    Assets.atlas.findRegion("tile-wall-se"));
		tiles.put("tile-wall-sw",    Assets.atlas.findRegion("tile-wall-sw"));

		tile_textures = Collections.unmodifiableMap(tiles);
		tile_textures_keys = new ArrayList<String>(tile_textures.keySet());
	}

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

		float worldx = 0;
		float worldy = 0;
		for (int i = 0; i < NUM_LAYERS; ++i) {
			caches[i] = new SpriteCache();
			SpriteCache cache = caches[i];
			cache.beginCache();
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					worldx = x << 5; // x * 2^5 => x * 32 => tile coord to world coord
					worldy = y << 5; // y * 2^5 => y * 32 => tile coord to world coord
					cache.add(getRandomTileTexture(), worldx, worldy, TILE_SIZE, TILE_SIZE);
				}
			}
			layers[i] = cache.endCache();
		}
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

	@Override
	public void dispose() {
		for (int i = 0; i < NUM_LAYERS; ++i) {
			caches[i].dispose();
		}
	}

	private TextureRegion getRandomTileTexture() {
		return tile_textures.get(
			tile_textures_keys.get(Assets.rand.nextInt(tile_textures_keys.size()))
		);
	}

}
