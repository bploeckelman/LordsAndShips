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

}
