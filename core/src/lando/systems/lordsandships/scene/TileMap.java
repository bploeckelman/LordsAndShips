package lando.systems.lordsandships.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.scene.levelgen.LevelGenerator;
import lando.systems.lordsandships.scene.levelgen.Room;
import lando.systems.lordsandships.scene.levelgen.RoomEdge;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.graph.Edge;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.*;

/**
 * TileMap
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
		tiles.put("tile-blank",       Assets.atlas.findRegion("tile-blank"));
		tiles.put("tile-block",       Assets.atlas.findRegion("tile-block"));
		tiles.put("tile-box",         Assets.atlas.findRegion("purple_floor_tile1"));//tile-box"));
		tiles.put("tile-floor1",      Assets.atlas.findRegion("purple_floor_tile1"));//tile-floor1"));
		tiles.put("tile-wall-horiz",  Assets.atlas.findRegion("tile-wall-horiz"));
		tiles.put("tile-wall-vert",   Assets.atlas.findRegion("tile-wall-vert"));
		tiles.put("tile-wall-nw",     Assets.atlas.findRegion("tile-wall-nw"));
		tiles.put("tile-wall-ne",     Assets.atlas.findRegion("tile-wall-ne"));
		tiles.put("tile-wall-se",     Assets.atlas.findRegion("tile-wall-se"));
		tiles.put("tile-wall-sw",     Assets.atlas.findRegion("tile-wall-sw"));
		tiles.put("tile-brick-horiz", Assets.atlas.findRegion("wall-vertical-n"));//purple_floor_tile1"));//tile-brick-horiz"));
		tiles.put("tile-brick-vert",  Assets.atlas.findRegion("wall-horizontal-e"));//purple_floor_tile1"));//tile-brick-vert"));
		tiles.put("tile-brick-nw",    Assets.atlas.findRegion("inner-corner-nw"));//purple_floor_tile1"));//"tile-brick-nw"));
		tiles.put("tile-brick-ne",    Assets.atlas.findRegion("inner-corner-ne"));//purple_floor_tile1"));//"tile-brick-ne"));
		tiles.put("tile-brick-se",    Assets.atlas.findRegion("inner-corner-se"));//purple_floor_tile1"));//"tile-brick-se"));
		tiles.put("tile-brick-sw",    Assets.atlas.findRegion("inner-corner-sw"));//purple_floor_tile1"));//tile-brick-sw"));
		tiles.put("grate",            Assets.atlas.findRegion("purple_bricks1"));//tile-floor5"));

		tile_textures = Collections.unmodifiableMap(tiles);
		tile_textures_keys = new ArrayList<String>(tile_textures.keySet());
	}

	public class Tile
	{
		String texture;
		int x,y;

		public Tile(String texture, int x, int y) {
			this.texture = texture;
			this.x = x;
			this.y = y;
		}

		public int getGridX() { return x; }
		public int getGridY() { return y; }
		public float getWorldMinX() { return x * TILE_SIZE; }
		public float getWorldMinY() { return y * TILE_SIZE; }
		public float getWorldMaxX() { return (x + 1) * TILE_SIZE; }
		public float getWorldMaxY() { return (y + 1) * TILE_SIZE; }

		public void render() {
			Assets.batch.draw(tile_textures.get(texture),
					getWorldMinX(), getWorldMinY(), TILE_SIZE, TILE_SIZE);
		}
	}

	Tile[][] tiles = null;
	Animation spawnTile;

	int layers[];
	int width, height;
	public int spawnX, spawnY;
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

		this.width = getMapWidthInTiles();
		this.height = getMapHeightInTiles();

		this.spawnTile = new Animation(0.06f,
				Assets.atlas.findRegion("spawn1"),
				Assets.atlas.findRegion("spawn2"),
				Assets.atlas.findRegion("spawn3"),
				Assets.atlas.findRegion("spawn4"),
				Assets.atlas.findRegion("spawn5"),
				Assets.atlas.findRegion("spawn6"),
				Assets.atlas.findRegion("spawn7"),
				Assets.atlas.findRegion("spawn8"));
		this.spawnTile.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

//		generateCacheFromGraph();
		generateTilesFromGraph();
	}

	public void generateTilesFromGraph() {
		tiles = new Tile[height][width];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				tiles[y][x] = new Tile("tile-blank", x, y);
			}
		}

		for (Room room : rooms) {
			generateRoomTiles(room);
		}

		generateCorridorTiles();

		generateWallTiles();

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (tiles[y][x].texture.equals("tile-blank")) {
					tiles[y][x].texture = "tile-box";
				}
			}
		}
	}

	public void generateRoomTiles(Room room) {
		int worldx0 = (int) room.rect.x;
		int worldy0 = (int) room.rect.y;
		int worldx1 = (int)(room.rect.x + room.rect.width)  - 1;
		int worldy1 = (int)(room.rect.y + room.rect.height) - 1;

		// Internal tiles
		for (int y = worldy0 + 1; y < worldy1; ++y) {
			for (int x = worldx0 + 1; x < worldx1; ++x) {
				tiles[y][x].texture = "grate";
			}
		}

		if (spawnX == 0 && spawnY == 0) {
			spawnX = worldx0 + ((worldx1 - worldx0) / 2);
			spawnY = worldy0 + ((worldy1 - worldy0) / 2);
		}
	}

	public void generateCorridorTiles() {
		Set<Edge> completedEdges = new HashSet<Edge>();
		RoomEdge edge;
		int xStart, xEnd;
		int yStart, yEnd;

		for (Room u : LevelGenerator.mst.vertices()) {
			Iterable<Room> neighbors = LevelGenerator.mst.adjacentTo(u);
			if (neighbors == null) continue;

			// For each edge
			for (Room v : neighbors) {
				edge = new RoomEdge(u, v);
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
						tiles[y-1][x].texture = "grate";
						tiles[y-0][x].texture = "grate";
						tiles[y+1][x].texture = "grate";
					}
				} else {
					xStart = (int) Math.floor(u.center.x);
					xEnd   = (int) Math.floor(v.center.x) - 1;
					int y  = (int) Math.floor(u.center.y);
					// u is to the right of v
					for (int x = xStart; x >= xEnd; --x) {
						tiles[y-1][x].texture = "grate";
						tiles[y-0][x].texture = "grate";
						tiles[y+1][x].texture = "grate";
					}
				}
				if (u.center.y <= v.center.y) {
					yStart = (int) Math.floor(u.center.y);
					yEnd   = (int) Math.floor(v.center.y);
					int x  = (int) Math.floor(v.center.x);
					// u is above v
					for (int y = yStart; y <= yEnd; ++y) {
						tiles[y][x-1].texture = "grate";
						tiles[y][x-0].texture = "grate";
						tiles[y][x+1].texture = "grate";
					}
				} else {
					yStart = (int) Math.floor(u.center.y);
					yEnd   = (int) Math.floor(v.center.y);
					int x  = (int) Math.floor(v.center.x);
					// u is below v
					for (int y = yStart; y >= yEnd; --y) {
						tiles[y][x-1].texture = "grate";
						tiles[y][x-0].texture = "grate";
						tiles[y][x+1].texture = "grate";
					}
				}

				// Add edge to completed list so its reverse isn't also processed
				completedEdges.add(edge);
			}
		}
	}

	public void generateWallTiles() {
		addCornerTiles();
		addWallTiles();
	}

	private void addWallTiles() {
		// Add non-corner wall tiles
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (tiles[y][x].texture.equals("grate")) {
					// Clamp neighbor indices to map boundaries
					int xl = (x - 1 < 0) ? x : x - 1;
					int yd = (y - 1 < 0) ? y : y - 1;
					int xr = (x + 1 >= width)  ? x : x + 1;
					int yu = (y + 1 >= height) ? y : y + 1;

					// Check edge neighbors
					if (tiles[yu][x].texture.equals("tile-blank")) {
						tiles[yu][x].texture = "tile-brick-horiz";
					}
					if (tiles[yd][x].texture.equals("tile-blank")) {
						tiles[yd][x].texture = "tile-brick-horiz";
					}
					if (tiles[y][xl].texture.equals("tile-blank")) {
						tiles[y][xl].texture = "tile-brick-vert";
					}
					if (tiles[y][xr].texture.equals("tile-blank")) {
						tiles[y][xr].texture = "tile-brick-vert";
					}
				}
			}
		}
	}

	private void addCornerTiles() {
		// Add corner wall tiles
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				// Clamp neighbor indices to map boundaries
				int xl = (x - 1 < 0) ? x : x - 1;
				int yd = (y - 1 < 0) ? y : y - 1;
				int xr = (x + 1 >= width) ? x : x + 1;
				int yu = (y + 1 >= height) ? y : y + 1;

				if (tiles[y][x].texture.equals("grate")) {
					addInnerCornerTiles(x, y, xl, yd, xr, yu);
					addOuterCornerTiles(x, y, xl, yd, xr, yu);
				}
			}
		}
	}

	private void addOuterCornerTiles(int x, int y, int xl, int yd, int xr, int yu) {
		if ((tiles[y ][xl].texture.equals("tile-blank") || tiles[y ][xl].texture.equals("tile-box"))
		 &&  tiles[yu][xl].texture.equals("tile-blank")
		 && (tiles[yu][x ].texture.equals("tile-blank") || tiles[yu][x ].texture.equals("tile-box"))) {
			tiles[yu][xl].texture = "tile-brick-nw";
		}
		if ((tiles[yu][x ].texture.equals("tile-blank") || tiles[yu][x ].texture.equals("tile-box"))
		 &&  tiles[yu][xr].texture.equals("tile-blank")
		 && (tiles[y ][xr].texture.equals("tile-blank") || tiles[y ][xr].texture.equals("tile-box"))) {
			tiles[yu][xr].texture = "tile-brick-ne";
		}
		if ((tiles[y ][xr].texture.equals("tile-blank") || tiles[y ][xr].texture.equals("tile-box"))
		 &&  tiles[yd][xr].texture.equals("tile-blank")
		 && (tiles[yd][x ].texture.equals("tile-blank") || tiles[yd][x ].texture.equals("tile-box"))) {
			tiles[yd][xr].texture = "tile-brick-se";
		}
		if ((tiles[yd][x ].texture.equals("tile-blank") || tiles[yd][x ].texture.equals("tile-box"))
		 &&  tiles[yd][xl].texture.equals("tile-blank")
		 && (tiles[y ][xl].texture.equals("tile-blank") || tiles[y ][xl].texture.equals("tile-box"))) {
			tiles[yd][xl].texture = "tile-brick-sw";
		}
	}

	private void addInnerCornerTiles(int x, int y, int xl, int yd, int xr, int yu) {
		if (!tiles[y ][xl].texture.equals("tile-blank") && !tiles[y ][xl].texture.equals("tile-box")
		 &&  tiles[yu][xl].texture.equals("tile-blank")
		 && !tiles[yu][x ].texture.equals("tile-blank") && !tiles[yu][x ].texture.equals("tile-box")) {
			tiles[yu][xl].texture = "tile-box";
		}
		if (!tiles[yu][x ].texture.equals("tile-blank") && !tiles[yu][x ].texture.equals("tile-box")
		 &&  tiles[yu][xr].texture.equals("tile-blank")
		 && !tiles[y ][xr].texture.equals("tile-blank") && !tiles[y ][xr].texture.equals("tile-box")) {
			tiles[yu][xr].texture = "tile-box";
		}
		if (!tiles[y ][xr].texture.equals("tile-blank") && !tiles[y ][xr].texture.equals("tile-box")
		 &&  tiles[yd][xr].texture.equals("tile-blank")
		 && !tiles[yd][x ].texture.equals("tile-blank") && !tiles[yd][x ].texture.equals("tile-box")) {
			tiles[yd][xr].texture = "tile-box";
		}
		if (!tiles[yd][x ].texture.equals("tile-blank") && !tiles[yd][x ].texture.equals("tile-box")
		 &&  tiles[yd][xl].texture.equals("tile-blank")
		 && !tiles[y ][xl].texture.equals("tile-blank") && !tiles[y ][xl].texture.equals("tile-box")) {
			tiles[yd][xl].texture = "tile-box";
		}
	}

	public int getMapWidthInTiles() {
		int width = 0;
		if (rooms == null) {
			return width;
		}

		for (Room room : rooms) {
			int x = (int) (room.rect.x + room.rect.width);
			if (width < x) width = x;
		}
		return width;
	}

	public int getMapHeightInTiles() {
		int height = 0;
		if (rooms == null) {
			return height;
		}

		for (Room room : rooms) {
			int y = (int) (room.rect.y + room.rect.height);
			if (height < y) height = y;
		}
		return height;
	}

	public void generateCacheFromGraph() {
		layers = new int[NUM_LAYERS];
		caches = new SpriteCache[NUM_LAYERS];

		// Generate sprite caches for tile layers
		for (int i = 0; i < NUM_LAYERS; ++i) {
			caches[i] = new SpriteCache(5460, true);
			SpriteCache cache = caches[i];
			cache.beginCache();

			for (Room room : rooms) {
				generateRoomCacheTiles(room, cache);
			}
			generateCorridorCacheTiles(cache);

			layers[i] = cache.endCache();
		}
	}

	public void generateRoomCacheTiles(Room room, SpriteCache cache) {
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
		cache.add(tile_textures.get("tile-brick-sw"), worldx0 << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-brick-nw"), worldx0 << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-brick-ne"), worldx1 << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		cache.add(tile_textures.get("tile-brick-se"), worldx1 << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);

		// Edge tiles
		for (int y = worldy0 + 1; y < worldy1; ++y) {
			cache.add(tile_textures.get("tile-brick-vert"), worldx0 << 4, y << 4, TILE_SIZE, TILE_SIZE);
			cache.add(tile_textures.get("tile-brick-vert"), worldx1 << 4, y << 4, TILE_SIZE, TILE_SIZE);
		}
		for (int x = worldx0 + 1; x < worldx1; ++x) {
			cache.add(tile_textures.get("tile-brick-horiz"), x << 4, worldy0 << 4, TILE_SIZE, TILE_SIZE);
			cache.add(tile_textures.get("tile-brick-horiz"), x << 4, worldy1 << 4, TILE_SIZE, TILE_SIZE);
		}
	}

	public void generateCorridorCacheTiles(SpriteCache cache) {
		Set<Edge> completedEdges = new HashSet<Edge>();
		Edge edge;
		int xStart, xEnd;
		int yStart, yEnd;

		for (Room u : LevelGenerator.mst.vertices()) {
			Iterable<Room> neighbors = LevelGenerator.mst.adjacentTo(u);
			if (neighbors == null) continue;

			// For each edge
			for (Room v : neighbors) {
				edge = new RoomEdge(u, v);
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

	public boolean isBlocking(int x, int y) {
		if ((x < 0 || x > width)
		 || (y < 0 || y > height)) {
			return true;
		}

		 if (tiles[y][x].texture.equals("grate") || tiles[y][x].texture.equals("tile-blank")) {
			return false;
		} else {
			return true;
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

	float accum = 0f;
	public void render(Camera camera) {
//		for (int i = 0; i < NUM_LAYERS; i++) {
//			SpriteCache cache = caches[i];
//			cache.setProjectionMatrix(camera.combined);
//			cache.begin();
//			cache.draw(layers[i]);
//			cache.end();
//		}
		int width = getMapWidthInTiles();
		int height = getMapHeightInTiles();

		Assets.batch.begin();
		Assets.batch.enableBlending();
		Assets.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Assets.batch.setProjectionMatrix(camera.combined);
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				tiles[y][x].render();
			}
		}

		Assets.batch.draw(spawnTile.getKeyFrame(accum += Gdx.graphics.getDeltaTime()), spawnX * 16, spawnY * 16, 16, 16);
		Assets.batch.end();
	}

	@Override
	public void dispose() {
		if (caches == null) return;

		for (int i = 0; i < NUM_LAYERS; ++i) {
			if (caches[i] != null) {
				caches[i].dispose();
			}
		}
	}

	public void getCollisionTiles(Entity entity, List<Tile> collisionTiles) {
		int entityMinX = entity.getGridMinX();
		int entityMinY = entity.getGridMinY();
		int entityMaxX = entity.getGridMaxX();
		int entityMaxY = entity.getGridMaxY();

		collisionTiles.clear();
		for (int y = entityMinY; y <= entityMaxY; ++y) {
			for (int x = entityMinX; x <= entityMaxX; ++x) {
				collisionTiles.add(tiles[y][x]);
			}
		}
	}

	private TextureRegion getRandomTileTexture() {
		return tile_textures.get(
			tile_textures_keys.get(Assets.rand.nextInt(tile_textures_keys.size()))
		);
	}

}
