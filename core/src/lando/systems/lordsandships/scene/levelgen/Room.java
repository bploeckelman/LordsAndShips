package lando.systems.lordsandships.scene.levelgen;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.utils.graph.Vertex;

/**
 * A Room created during level generation
 *
 * Brian Ploeckelman created on 7/14/2014.
 */
public class Room extends Vertex
{
    public Rectangle rect = new Rectangle();
    public Vector2 center = new Vector2();
    public Vector2 vel    = new Vector2();

    public boolean isSelected;

    // TODO : add other contents once level layout is done

    public Room(float x, float y, float w, float h) {
        super();
        rect.set(x, y, w, h);
        rect.getCenter(center);
        vel.set(0, 0);
        isSelected = false;
    }

    public Room(Rectangle bounds) {
        super();
        rect.set(bounds);
        rect.getCenter(center);
        isSelected = false;
    }
}
