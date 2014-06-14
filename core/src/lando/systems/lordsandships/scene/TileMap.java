package lando.systems.lordsandships.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Graph;
import lando.systems.lordsandships.scene.LevelGenerator.*;

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
	static final int TILE_SIZE = 16; // pixels
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
		tiles.put("grate",           Assets.atlas.findRegion("grate"));

		tile_textures = Collections.unmodifiableMap(tiles);
		tile_textures_keys = new ArrayList<String>(tile_textures.keySet());
	}

	int layers[];
	int width, height;
	SpriteCache caches[];

	Graph<Room> roomGraph;
	List<Room> rooms;

	public TileMap(int width, int height) {
		this.width = width;
		this.height = height;

		generate();
	}

	public TileMap(Graph<Room> roomGraph, List<Room> rooms) {
		this.roomGraph = roomGraph;
		this.rooms = rooms;

		generateFromGraph();
	}

	public void generateFromGraph() {
		layers = new int[NUM_LAYERS];
		caches = new SpriteCache[NUM_LAYERS];

		// Generate sprite caches for tile layers
		for (int i = 0; i < NUM_LAYERS; ++i) {
			caches[i] = new SpriteCache(5460, true);
			SpriteCache cache = caches[i];
			cache.beginCache();

			for (Room room : rooms) {
				generateRoomTiles(room, cache);
			}
			generateCorridorTiles(cache);

			layers[i] = cache.endCache();
		}
	}

	public void generateRoomTiles(Room room, SpriteCache cache) {
		int worldx0 = (int) room.rect.x;
		int worldy0 = (int) room.rect.y;
		int worldx1 = (int)(room.rect.x + room.rect.width) - 1;
		int worldy1 = (int)(room.rect.y + room.rect.height) - 1;

		// Internal tiles
		for (int y = worldy0 + 1; y < worldy1; ++y) {
			for (int x = worldx0 + 1; x < worldx1; ++x) {
				cache.add(tile_textures.get("grate"), x << 4, y << 4, TILE_SIZE, TILE_SIZE);
			}
		}

		// Corner tiles
		cache.add(tile_textures.get("tile-wall-sw"), worldx0 << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-wall-nw"), worldx0 << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-wall-ne"), worldx1 << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-wall-se"), worldx1 << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);

		// Edge tiles
		for (int y = worldy0 + 1; y < worldy1; ++y) {
			cache.add(tile_textures.get("tile-wall-vert"), worldx0 << 4, y << 4, TILE_SIZE, TILE_SIZE);
			cache.add(tile_textures.get("tile-wall-vert"), worldx1 << 4, y << 4, TILE_SIZE, TILE_SIZE);
		}
		for (int x = worldx0 + 1; x < worldx1; ++x) {
			cache.add(tile_textures.get("tile-wall-horiz"), x << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);
			cache.add(tile_textures.get("tile-wall-horiz"), x << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		}
	}

	public void generateCorridorTiles(SpriteCache cache) {
		Set<Edge> completedEdges = new HashSet<Edge>();
		Edge edge;
		int xStart, xEnd;
		int yStart, yEnd;

		for (Room u : LevelGenerator.mst.vertices()) {
			Iterable<Room> neighbors = LevelGenerator.mst.adjacentTo(u);
			if (neighbors == null) continue;

			// For each edge
			for (Room v : neighbors) {
				edge = new Edge(u, v);
				// If a corridor has already been generated for this edge, skip it
				if (completedEdges.contains(edge)) {
					continue;
				}

				// Determine direction of corridor:
				if (u.center.x <= v.center.x) {
					xStart = (int) Math.floor(u.center.x);
					xEnd   = (int) Math.floor(v.center.x) + 1;
					int y  = (int) Math.floor(u.center.y);
					// u is to the left of v
					for (int x = xStart; x <= xEnd; ++x) {
//						cache.add(tile_textures.get("tile-block"), x << 4, (y-2) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y-1) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y-0) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y+1) << 4, TILE_SIZE, TILE_SIZE);
//						cache.add(tile_textures.get("tile-block"), x << 4, (y+2) << 4, TILE_SIZE, TILE_SIZE);
					}
				} else {
					xStart = (int) Math.floor(u.center.x);
					xEnd   = (int) Math.floor(v.center.x) - 1;
					int y  = (int) Math.floor(u.center.y);
					// u is to the right of v
					for (int x = xStart; x >= xEnd; --x) {
//						cache.add(tile_textures.get("tile-block"), x << 4, (y-2) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y-1) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y-0) << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      x << 4, (y+1) << 4, TILE_SIZE, TILE_SIZE);
//						cache.add(tile_textures.get("tile-block"), x << 4, (y+2) << 4, TILE_SIZE, TILE_SIZE);
					}
				}
				if (u.center.y <= v.center.y) {
					yStart = (int) Math.floor(u.center.y);
					yEnd   = (int) Math.floor(v.center.y);
					int x  = (int) Math.floor(v.center.x);
					// u is above v
					for (int y = yStart; y <= yEnd; ++y) {
//						cache.add(tile_textures.get("tile-block"), (x-2) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x-1) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x-0) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x+1) << 4, y << 4, TILE_SIZE, TILE_SIZE);
//						cache.add(tile_textures.get("tile-block"), (x+2) << 4, y << 4, TILE_SIZE, TILE_SIZE);
					}
				} else {
					yStart = (int) Math.floor(u.center.y);
					yEnd   = (int) Math.floor(v.center.y);
					int x  = (int) Math.floor(v.center.x);
					// u is below v
					for (int y = yStart; y >= yEnd; --y) {
//						cache.add(tile_textures.get("tile-block"), (x-2) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x-1) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x-0) << 4, y << 4, TILE_SIZE, TILE_SIZE);
						cache.add(tile_textures.get("grate"),      (x+1) << 4, y << 4, TILE_SIZE, TILE_SIZE);
//						cache.add(tile_textures.get("tile-block"), (x+2) << 4, y << 4, TILE_SIZE, TILE_SIZE);
					}
				}

				// Add edge to completed list so its reverse isn't also processed
				completedEdges.add(edge);
			}
		}
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
					worldx = x << 4; // x * 2^4 => x * 16 => tile coord to world coord
					worldy = y << 4; // y * 2^4 => y * 16 => tile coord to world coord
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
			cache.draw(layers[i]);
			cache.end();
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
