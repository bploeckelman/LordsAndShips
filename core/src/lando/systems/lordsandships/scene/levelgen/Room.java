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
	public Rectangle rect;
	public Vector2 center;
	public Vector2 vel;

	public boolean isSelected;

	// TODO : add other contents once level layout is done

	public Room(float x, float y, float w, float h) {
		super();

		center = new Vector2();

		rect = new Rectangle(x,y,w,h);
		rect.getCenter(center);

		vel= new Vector2();

		isSelected = false;
	}
}
