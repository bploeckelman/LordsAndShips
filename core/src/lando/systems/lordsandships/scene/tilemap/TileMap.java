package lando.systems.lordsandships.scene.tilemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.scene.levelgen.Room;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.List;

/**
 * TileMap
 *
 * Brian Ploeckelman created on 5/31/2014.
 */
public class TileMap {

    Tile[][] tiles = null;
    TileSet tileSet;
    Animation spawnTile;

    public int width, height;
    public int spawnX, spawnY;
    public boolean hasTiles = false;


    public TileMap() {
        this.width = 0;
        this.height = 0;
        this.tiles = new Tile[height][width];
        this.spawnTile = new Animation(0.06f,
                                       Assets.shadow);
        this.spawnTile.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
//        this.tileSet = new TileSetOryx();
        this.tileSet = new TileSetRaph();
    }

    public void generateFromRoomGraph(Graph<Room> roomGraph) {
        tiles = (new TileMapGenerator()).generateTilesFromGraph(roomGraph);
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
        int width  = tiles[0].length; //getMapWidthInTiles();
        int height = tiles.length; //getMapHeightInTiles();
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

    public Vector2 getRandomFloorTile() {
//        final int max_iters = 500;
//        int iters = 0;
        while (true) {
            int x = Assets.rand.nextInt(tiles[0].length - 5) + 5;
            int y = Assets.rand.nextInt(tiles.length - 5) + 5;
            if (tiles[y][x].type == TileType.FLOOR) {
                return new Vector2(x, y);
            }
//            if (++iters >= max_iters) {
//                break;
//            }
        }
//        return new Vector2();
    }

}
