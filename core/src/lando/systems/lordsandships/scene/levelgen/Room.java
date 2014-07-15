package lando.systems.lordsandships.scene.levelgen;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * A Room created during level generation
 *
 * Brian Ploeckelman created on 7/14/2014.
 */
public class Room
{
	private static int nextId = 0;

	public int id;

	public Rectangle rect;
	public Vector2 center;
	public Vector2 vel;

	public boolean isSelected;

	// TODO : add other contents once level layout is done

	public Room(float x, float y, float w, float h) {
		id = nextId++;

		center = new Vector2();

		rect = new Rectangle(x,y,w,h);
		rect.getCenter(center);

		vel= new Vector2();

		isSelected = false;
	}
}
