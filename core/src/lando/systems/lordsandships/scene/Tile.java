package lando.systems.lordsandships.scene;

/**
 * Brian Ploeckelman created on 10/28/2014.
 */
public class Tile {

    public static final int TILE_SIZE = 16; // pixels

    public String texture; // TODO : change to enum for lookup
    private int x, y;

    public Tile(String texture, int x, int y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public int getGridX() {
        return x;
    }

    public int getGridY() {
        return y;
    }

    public float getWorldMinX() {
        return x * TILE_SIZE;
    }

    public float getWorldMinY() {
        return y * TILE_SIZE;
    }

    public float getWorldMaxX() {
        return (x + 1) * TILE_SIZE;
    }

    public float getWorldMaxY() {
        return (y + 1) * TILE_SIZE;
    }

}
