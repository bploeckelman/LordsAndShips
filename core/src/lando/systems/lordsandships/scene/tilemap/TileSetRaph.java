package lando.systems.lordsandships.scene.tilemap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/4/2015.
 */
public class TileSetRaph extends TileSet {

    public static final int TILE_SIZE = 32;

    public TileSetRaph() {
        super();

        Texture tile_floor          = new Texture("raph-tiles/floor_skull.png");
        Texture tile_block_big      = new Texture("raph-tiles/block_big.png");
        Texture tile_corner_inside  = new Texture("raph-tiles/corner_inside.png");
        Texture tile_corner_outside = new Texture("raph-tiles/corner_outside.png");
        Texture tile_wall_h         = new Texture("raph-tiles/wall_h.png");
        Texture tile_wall_v         = new Texture("raph-tiles/wall_v.png");
        Texture tile_wall_slant     = new Texture("raph-tiles/wall_slant.png");

        TextureRegion[][] textures = TextureRegion.split(Assets.oryxWorld,
                                                         TILE_SIZE, TILE_SIZE);
        int rows = textures.length;
        int cols = textures[0].length;

        TextureRegion blank = new TextureRegion(textures[rows - 1][cols - 1]);

        TextureRegion floor        = new TextureRegion(tile_floor);
        TextureRegion wall_horiz_n = new TextureRegion(tile_wall_h);
        TextureRegion wall_horiz_s = new TextureRegion(tile_wall_h);
        TextureRegion wall_vert_e  = new TextureRegion(tile_wall_v);
        TextureRegion wall_vert_w  = new TextureRegion(tile_wall_v);

        wall_horiz_n.flip(false, false);
        wall_horiz_s.flip(false, true);
        wall_vert_e.flip(true, false);
        wall_vert_w.flip(false, false);

        TextureRegion corner_inner_nw = new TextureRegion(tile_corner_inside);
        TextureRegion corner_inner_ne = new TextureRegion(tile_corner_inside);
        TextureRegion corner_inner_sw = new TextureRegion(tile_corner_inside);
        TextureRegion corner_inner_se = new TextureRegion(tile_corner_inside);

        corner_inner_nw.flip(false, false);
        corner_inner_ne.flip(true,  false);
        corner_inner_sw.flip(false, true);
        corner_inner_se.flip(true,  true);

        TextureRegion corner_outer_nw = new TextureRegion(tile_corner_outside);
        TextureRegion corner_outer_ne = new TextureRegion(tile_corner_outside);
        TextureRegion corner_outer_sw = new TextureRegion(tile_corner_outside);
        TextureRegion corner_outer_se = new TextureRegion(tile_corner_outside);

        corner_outer_nw.flip(false, false);
        corner_outer_ne.flip(true,  false);
        corner_outer_sw.flip(false, true);
        corner_outer_se.flip(true,  true);

        tiles.put(TileType.BLANK, blank);
        tiles.put(TileType.FLOOR, floor);

        tiles.put(TileType.WALL_HORIZ_N, wall_horiz_n);
        tiles.put(TileType.WALL_HORIZ_S, wall_horiz_s);

        tiles.put(TileType.WALL_VERT_E, wall_vert_e);
        tiles.put(TileType.WALL_VERT_W, wall_vert_w);

        tiles.put(TileType.CORNER_OUTER_NW, corner_outer_nw);
        tiles.put(TileType.CORNER_OUTER_NE, corner_outer_ne);
        tiles.put(TileType.CORNER_OUTER_SW, corner_outer_sw);
        tiles.put(TileType.CORNER_OUTER_SE, corner_outer_se);

        tiles.put(TileType.CORNER_INNER_NW, corner_inner_nw);
        tiles.put(TileType.CORNER_INNER_NE, corner_inner_ne);
        tiles.put(TileType.CORNER_INNER_SW, corner_inner_sw);
        tiles.put(TileType.CORNER_INNER_SE, corner_inner_se);
    }

}
