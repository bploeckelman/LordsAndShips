package lando.systems.lordsandships.scene.tilemap;

import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 12/15/2014.
 */
public class TileSetOryx extends TileSet {

    public TileSetOryx() {
        super();

        tiles.put(TileType.BLANK, Assets.world.findRegion("oryx_16bit_scifi_world_1103"));
        tiles.put(TileType.FLOOR, Assets.world.findRegion("oryx_16bit_scifi_world_01"));

        tiles.put(TileType.WALL_HORIZ_N, Assets.world.findRegion("oryx_16bit_scifi_world_206"));
        tiles.put(TileType.WALL_HORIZ_S, Assets.world.findRegion("oryx_16bit_scifi_world_206"));

        tiles.put(TileType.WALL_VERT_E, Assets.world.findRegion("oryx_16bit_scifi_world_209"));
        tiles.put(TileType.WALL_VERT_W, Assets.world.findRegion("oryx_16bit_scifi_world_209"));

        tiles.put(TileType.CORNER_OUTER_NW, Assets.world.findRegion("oryx_16bit_scifi_world_211"));
        tiles.put(TileType.CORNER_OUTER_NE, Assets.world.findRegion("oryx_16bit_scifi_world_212"));
        tiles.put(TileType.CORNER_OUTER_SW, Assets.world.findRegion("oryx_16bit_scifi_world_213"));
        tiles.put(TileType.CORNER_OUTER_SE, Assets.world.findRegion("oryx_16bit_scifi_world_214"));

        tiles.put(TileType.CORNER_INNER_NW, Assets.world.findRegion("oryx_16bit_scifi_world_213"));
        tiles.put(TileType.CORNER_INNER_NE, Assets.world.findRegion("oryx_16bit_scifi_world_214"));
        tiles.put(TileType.CORNER_INNER_SE, Assets.world.findRegion("oryx_16bit_scifi_world_212"));
        tiles.put(TileType.CORNER_INNER_SW, Assets.world.findRegion("oryx_16bit_scifi_world_211"));
    }

}
