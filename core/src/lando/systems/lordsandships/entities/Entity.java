package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Brian Ploeckelman created on 6/15/2014.
 */
public abstract class Entity {
	public TextureRegion texture;
	public Vector2 velocity;
	public Rectangle boundingBox;

	public Entity(TextureRegion texture, float x, float y, float w, float h) {
		this.texture = texture;
		this.velocity = new Vector2();
		this.boundingBox = new Rectangle(x,y,w,h);
	}

	public abstract void update(float delta);

	public void render(SpriteBatch batch) {
		batch.draw(texture, boundingBox.x, boundingBox.y);
	}

	public int getGridMinX() { return (int) (boundingBox.x / 16); }
	public int getGridMinY() { return (int) (boundingBox.y / 16); }
	public int getGridMaxX() { return (int) ((boundingBox.x + boundingBox.width ) / 16); }
	public int getGridMaxY() { return (int) ((boundingBox.y + boundingBox.height) / 16); }
	public Vector3 getPosition() { return new Vector3(boundingBox.x, boundingBox.y, 0); }
}
