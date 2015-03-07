package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.tilemap.TileSet;
import lando.systems.lordsandships.scene.tilemap.TileSetOryx;
import lando.systems.lordsandships.scene.tilemap.TileType;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class Room {

    Tile[][] tiles;
    TileSet  tileSet;
    Vector2  position;

    Array<Room> neighbors;

    public Room(int posx, int posy, int width, int height) {
        tiles = new Tile[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tiles[y][x] = new Tile(TileType.BLANK, x, y);
            }
        }
        tileSet = new TileSetOryx();
        position = new Vector2(posx, posy);
        neighbors = new Array<Room>(3);
    }

    public void update(float delta) {

    }

    public void render(SpriteBatch batch, Camera camera) {
        int width = tiles[0].length;
        int height = tiles.length;
        Tile tile;

        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tile = tiles[y][x];
                batch.draw(tileSet.getTexture(tile.type),
                           tile.getWorldMinX() + position.x,
                           tile.getWorldMinY() + position.y,
                           Tile.TILE_SIZE, Tile.TILE_SIZE);
            }
        }
        batch.end();
    }

    // -------------------------------------------------------------------------

    public void addNeighbor(Room neighbor, int x, int y) {
        if (neighbor == null
         || x < 0 || x >= tiles[0].length
         || y < 0 || y >= tiles.length) {
            throw new IllegalArgumentException("Unable to add neighbor to room");
        }

        neighbors.add(neighbor);
        // TODO (brian): set the tile at the specified location
        tiles[y][x].type = TileType.WALL_HORIZ_N;
    }

}
