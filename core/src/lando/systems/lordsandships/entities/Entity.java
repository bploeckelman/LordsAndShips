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

	public Vector2 position;
	public Vector2 velocity;
	public Rectangle boundingBox;
	public TextureRegion texture;

	// TODO : extract to attributes class
	public int health = 100;
	public boolean alive = true;

	public Entity(TextureRegion texture, float x, float y, float w, float h) {
		this.texture = texture;
		this.position = new Vector2(x + w/2f,y + h/2f);
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

	public Vector2 getPosition() { return position; }

	public void takeDamage(int amount) {
		health -= amount;
		if (health <= 0) {
			health = 0;
			alive = false;
		}
	}

	public boolean isAlive() { return alive; }
}
