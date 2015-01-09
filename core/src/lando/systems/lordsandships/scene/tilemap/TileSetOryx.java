package lando.systems.lordsandships.scene.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 12/15/2014.
 */
public class TileSetOryx extends TileSet {

    public static final int TILE_SIZE = 24;

    public TileSetOryx() {
        super();

        TextureRegion[][] textures = TextureRegion.split(Assets.oryxWorld,
                                                         TILE_SIZE, TILE_SIZE);

        final int rows = textures.length;
        final int cols = textures[0].length;

        tiles.put(TileType.BLANK, textures[rows - 1][18]);
        tiles.put(TileType.FLOOR, textures[0][0]);

        tiles.put(TileType.WALL_HORIZ_N, textures[0][8]);
        tiles.put(TileType.WALL_HORIZ_S, textures[0][8]);

        tiles.put(TileType.WALL_VERT_E, textures[0][11]);
        tiles.put(TileType.WALL_VERT_W, textures[0][11]);

        tiles.put(TileType.CORNER_OUTER_NW, textures[0][13]);
        tiles.put(TileType.CORNER_OUTER_NE, textures[0][14]);
        tiles.put(TileType.CORNER_OUTER_SW, textures[0][15]);
        tiles.put(TileType.CORNER_OUTER_SE, textures[0][16]);

        tiles.put(TileType.CORNER_INNER_NW, textures[0][15]);
        tiles.put(TileType.CORNER_INNER_NE, textures[0][16]);
        tiles.put(TileType.CORNER_INNER_SE, textures[0][14]);
        tiles.put(TileType.CORNER_INNER_SW, textures[0][13]);
    }

}
