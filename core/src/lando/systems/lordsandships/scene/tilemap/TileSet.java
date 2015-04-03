package lando.systems.lordsandships.scene.tilemap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

/**
 * Brian Ploeckelman created on 10/28/2014.
 */
public abstract class TileSet {

    protected Map<TileType, TextureRegion> tiles;

    public TileSet() {
        tiles = new HashMap<TileType, TextureRegion>();
    }

    public TextureRegion getTexture(TileType type) {
        return tiles.get(type);
    }

    public boolean isCorner(TileType type) {
        return (isInnerCorner(type) || isOuterCorner(type));
    }

    public boolean isInnerCorner(TileType type) {
        return ((type == TileType.CORNER_INNER_NE)
             || (type == TileType.CORNER_INNER_NW)
             || (type == TileType.CORNER_INNER_SE)
             || (type == TileType.CORNER_INNER_SW));
    }

    public boolean isOuterCorner(TileType type) {
        return ((type == TileType.CORNER_OUTER_NE)
             || (type == TileType.CORNER_OUTER_NW)
             || (type == TileType.CORNER_OUTER_SE)
             || (type == TileType.CORNER_OUTER_SW));
    }

    public boolean isWall(TileType type) {
        return ((type == TileType.WALL_HORIZ_N)
             || (type == TileType.WALL_HORIZ_S)
             || (type == TileType.WALL_VERT_E)
             || (type == TileType.WALL_VERT_W)
             || (isCorner(type)));
    }

    public boolean isFloor(TileType type) {
        return type == TileType.FLOOR;
    }

}
