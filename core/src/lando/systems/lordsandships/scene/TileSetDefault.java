package lando.systems.lordsandships.scene;

import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 10/28/2014.
 */
public class TileSetDefault extends TileSet {

    public TileSetDefault() {
        super();

        tiles.put(TileType.BLANK, Assets.atlas.findRegion("tile-blank"));
        tiles.put(TileType.FLOOR, Assets.atlas.findRegion("purple_bricks1"));

        tiles.put(TileType.WALL_HORIZ_N, Assets.atlas.findRegion("wall-horizontal-n"));
        tiles.put(TileType.WALL_HORIZ_S, Assets.atlas.findRegion("wall-horizontal-s"));

        tiles.put(TileType.WALL_VERT_E, Assets.atlas.findRegion("wall-vertical-e"));
        tiles.put(TileType.WALL_VERT_W, Assets.atlas.findRegion("wall-vertical-w"));

        tiles.put(TileType.CORNER_OUTER_NW, Assets.atlas.findRegion("outer-corner-nw"));
        tiles.put(TileType.CORNER_OUTER_NE, Assets.atlas.findRegion("outer-corner-ne"));
        tiles.put(TileType.CORNER_OUTER_SE, Assets.atlas.findRegion("outer-corner-se"));
        tiles.put(TileType.CORNER_OUTER_SW, Assets.atlas.findRegion("outer-corner-sw"));

        tiles.put(TileType.CORNER_INNER_NW, Assets.atlas.findRegion("inner-corner-nw"));
        tiles.put(TileType.CORNER_INNER_NE, Assets.atlas.findRegion("inner-corner-ne"));
        tiles.put(TileType.CORNER_INNER_SE, Assets.atlas.findRegion("inner-corner-se"));
        tiles.put(TileType.CORNER_INNER_SW, Assets.atlas.findRegion("inner-corner-sw"));
    }

}
