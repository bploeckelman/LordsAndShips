package lando.systems.lordsandships.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import lando.systems.lordsandships.entities.Entity;
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
public class TileMap {

    private static final int delay_ms_tiles = 1;
    private static final int delay_ms_rooms = 1;
    private static final int delay_ms_corners = 8;
    private static final int delay_ms_walls = 2;

    Tile[][] tiles = null;
    TileSet tileSet;
    Animation spawnTile;
    Graph<Room> roomGraph;

    public int width, height;
    public int spawnX, spawnY;
    public boolean hasTiles = false;


    public TileMap() {
        this.roomGraph = null;
        this.width = 0;
        this.height = 0;
        this.tiles = new Tile[height][width];
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
        this.tileSet = new TileSetDefault();
    }

    public void generateTilesFromGraph(Graph<Room> roomGraph) {
        this.roomGraph = roomGraph;
        this.width = getMapWidthInTiles();
        this.height = getMapHeightInTiles();

        tiles = new Tile[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tiles[y][x] = new Tile(TileType.BLANK, x, y);
            }
        }
        hasTiles = true;

        for (Room room : roomGraph.vertices()) {
            generateRoomTiles(room);
            try { Thread.sleep(delay_ms_rooms); } catch (Exception e) {}
        }

        generateCorridorTiles();

        generateWallTiles();
    }

    public void generateRoomTiles(Room room) {
        int worldx0 = (int) room.rect.x;
        int worldy0 = (int) room.rect.y;
        int worldx1 = (int)(room.rect.x + room.rect.width)  - 1;
        int worldy1 = (int)(room.rect.y + room.rect.height) - 1;

        // Internal tiles
        for (int y = worldy0 + 1; y < worldy1; ++y) {
            for (int x = worldx0 + 1; x < worldx1; ++x) {
                tiles[y][x].type = TileType.FLOOR;
                try { Thread.sleep(delay_ms_tiles); } catch (Exception e) {}
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

        for (Room u : roomGraph.vertices()) {
            Iterable<Room> neighbors = roomGraph.adjacentTo(u);
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
                        tiles[y-1][x].type = TileType.FLOOR;
                        tiles[y-0][x].type = TileType.FLOOR;
                        tiles[y+1][x].type = TileType.FLOOR;
                    }
                } else {
                    xStart = (int) Math.floor(u.center.x);
                    xEnd   = (int) Math.floor(v.center.x) - 1;
                    int y  = (int) Math.floor(u.center.y);
                    // u is to the right of v
                    for (int x = xStart; x >= xEnd; --x) {
                        tiles[y-1][x].type = TileType.FLOOR;
                        tiles[y-0][x].type = TileType.FLOOR;
                        tiles[y+1][x].type = TileType.FLOOR;
                    }
                }
                if (u.center.y <= v.center.y) {
                    yStart = (int) Math.floor(u.center.y);
                    yEnd   = (int) Math.floor(v.center.y);
                    int x  = (int) Math.floor(v.center.x);
                    // u is above v
                    for (int y = yStart; y <= yEnd; ++y) {
                        tiles[y][x-1].type = TileType.FLOOR;
                        tiles[y][x-0].type = TileType.FLOOR;
                        tiles[y][x+1].type = TileType.FLOOR;
                    }
                } else {
                    yStart = (int) Math.floor(u.center.y);
                    yEnd   = (int) Math.floor(v.center.y);
                    int x  = (int) Math.floor(v.center.x);
                    // u is below v
                    for (int y = yStart; y >= yEnd; --y) {
                        tiles[y][x-1].type = TileType.FLOOR;
                        tiles[y][x-0].type = TileType.FLOOR;
                        tiles[y][x+1].type = TileType.FLOOR;
                    }
                }
                try { Thread.sleep(delay_ms_tiles); } catch (Exception e) {}

                // Add edge to completed list so its reverse isn't also processed
                completedEdges.add(edge);
            }
        }
    }

    public void generateWallTiles() {
        try { Thread.sleep(5); } catch (Exception e) {}
        addCornerTiles();
        try { Thread.sleep(5); } catch (Exception e) {}
        addWallTiles();
    }

    private void addWallTiles() {
        // Add non-corner wall tiles
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (tiles[y][x].type == TileType.FLOOR) {
                    // Clamp neighbor indices to map boundaries
                    int xl = (x - 1 < 0) ? x : x - 1;
                    int yd = (y - 1 < 0) ? y : y - 1;
                    int xr = (x + 1 >= width)  ? x : x + 1;
                    int yu = (y + 1 >= height) ? y : y + 1;

                    // Check edge neighbors
                    if (tiles[yu][x].type == TileType.BLANK) {
                        tiles[yu][x].type = TileType.WALL_HORIZ_N;
                        try { Thread.sleep(delay_ms_walls); } catch (Exception e) {}
                    }
                    if (tiles[yd][x].type == TileType.BLANK) {
                        tiles[yd][x].type = TileType.WALL_HORIZ_S;
                        try { Thread.sleep(delay_ms_walls); } catch (Exception e) {}
                    }
                    if (tiles[y][xl].type == TileType.BLANK) {
                        tiles[y][xl].type = TileType.WALL_VERT_E;
                        try { Thread.sleep(delay_ms_walls); } catch (Exception e) {}
                    }
                    if (tiles[y][xr].type == TileType.BLANK) {
                        tiles[y][xr].type = TileType.WALL_VERT_W;
                        try { Thread.sleep(delay_ms_walls); } catch (Exception e) {}
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

                if (tiles[y][x].type == TileType.FLOOR) {
                    addInnerCornerTiles(x, y, xl, yd, xr, yu);
                    addOuterCornerTiles(x, y, xl, yd, xr, yu);
                }
            }
        }
    }

    private boolean isCorner(TileType type) {
        return (type == TileType.CORNER_OUTER_NE || type == TileType.CORNER_OUTER_NW
             || type == TileType.CORNER_OUTER_SE || type == TileType.CORNER_OUTER_SW
             || type == TileType.CORNER_INNER_NE || type == TileType.CORNER_INNER_NW
             || type == TileType.CORNER_INNER_SE || type == TileType.CORNER_INNER_SW);
    }

    private void addOuterCornerTiles(int x, int y, int xl, int yd, int xr, int yu) {
        if ((tiles[y ][xl].type == TileType.BLANK || isCorner(tiles[y ][xl].type))
         &&  tiles[yu][xl].type == TileType.BLANK
         && (tiles[yu][x ].type == TileType.BLANK || isCorner(tiles[yu][x ].type))) {
            tiles[yu][xl].type = TileType.CORNER_OUTER_NW;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if ((tiles[yu][x ].type == TileType.BLANK || isCorner(tiles[yu][x ].type))
         &&  tiles[yu][xr].type == TileType.BLANK
         && (tiles[y ][xr].type == TileType.BLANK || isCorner(tiles[y ][xr].type))) {
            tiles[yu][xr].type = TileType.CORNER_OUTER_NE;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if ((tiles[y ][xr].type == TileType.BLANK || isCorner(tiles[y ][xr].type))
         &&  tiles[yd][xr].type == TileType.BLANK
         && (tiles[yd][x ].type == TileType.BLANK || isCorner(tiles[yd][x ].type))) {
            tiles[yd][xr].type = TileType.CORNER_OUTER_SE;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if ((tiles[yd][x ].type == TileType.BLANK || isCorner(tiles[yd][x ].type))
         &&  tiles[yd][xl].type == TileType.BLANK
         && (tiles[y ][xl].type == TileType.BLANK || isCorner(tiles[y ][xl].type))) {
            tiles[yd][xl].type = TileType.CORNER_OUTER_SW;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
    }

    private void addInnerCornerTiles(int x, int y, int xl, int yd, int xr, int yu) {
        if (tiles[y ][xl].type != TileType.BLANK && !isCorner(tiles[y ][xl].type)
         && tiles[yu][xl].type == TileType.BLANK
         && tiles[yu][x ].type != TileType.BLANK && !isCorner(tiles[yu][x ].type)) {
            tiles[yu][xl].type = TileType.CORNER_INNER_NE;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if (tiles[yu][x ].type != TileType.BLANK && !isCorner(tiles[yu][x ].type)
         && tiles[yu][xr].type == TileType.BLANK
         && tiles[y ][xr].type != TileType.BLANK && !isCorner(tiles[y ][xr].type)) {
            tiles[yu][xr].type = TileType.CORNER_INNER_NW;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if (tiles[y ][xr].type != TileType.BLANK && !isCorner(tiles[y ][xr].type)
         && tiles[yd][xr].type == TileType.BLANK
         && tiles[yd][x ].type != TileType.BLANK && !isCorner(tiles[yd][x ].type)) {
            tiles[yd][xr].type = TileType.CORNER_INNER_SW;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
        if (tiles[yd][x ].type != TileType.BLANK && !isCorner(tiles[yd][x ].type)
         && tiles[yd][xl].type == TileType.BLANK
         && tiles[y ][xl].type != TileType.BLANK && !isCorner(tiles[y ][xl].type)) {
            tiles[yd][xl].type = TileType.CORNER_INNER_SE;
            try { Thread.sleep(delay_ms_corners); } catch (Exception e) {}
        }
    }

    public int getMapWidthInTiles() {
        int width = 0;
        if (roomGraph.vertices() == null) {
            return width;
        }

        for (Room room : roomGraph.vertices()) {
            int x = (int) (room.rect.x + room.rect.width);
            if (width < x) width = x;
        }
        return width;
    }

    public int getMapHeightInTiles() {
        int height = 0;
        if (roomGraph.vertices() == null) {
            return height;
        }

        for (Room room : roomGraph.vertices()) {
            int y = (int) (room.rect.y + room.rect.height);
            if (height < y) height = y;
        }
        return height;
    }

    public boolean isBlocking(int x, int y) {
        if ((x < 0 || x > width)
         || (y < 0 || y > height)) {
            return true;
        }

        TileType type = tiles[y][x].type;
        return !(type == TileType.FLOOR || type == TileType.BLANK);
    }

    float accum = 0f;
    public void render(Camera camera) {
        int width = getMapWidthInTiles();
        int height = getMapHeightInTiles();
        Tile tile;

        Assets.batch.begin();
        Assets.batch.enableBlending();
        Assets.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Assets.batch.setProjectionMatrix(camera.combined);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tile = tiles[y][x];
                Assets.batch.draw(tileSet.getTexture(tile.type),
                        tile.getWorldMinX(), tile.getWorldMinY(),
                        Tile.TILE_SIZE, Tile.TILE_SIZE);
            }
        }

        Assets.batch.draw(spawnTile.getKeyFrame(accum += Gdx.graphics.getDeltaTime()), spawnX * 16, spawnY * 16, 16, 16);
        Assets.batch.end();
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

}
