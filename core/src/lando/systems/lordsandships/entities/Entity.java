package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Brian Ploeckelman created on 6/15/2014.
 */
public class Entity {
	public TextureRegion texture;
	public Vector2 velocity;
	public Rectangle boundingBox;

	public Entity(TextureRegion texture, float x, float y, float w, float h) {
		this.texture = texture;
		this.velocity = new Vector2();
		this.boundingBox = new Rectangle(x,y,w,h);
	}

	public void update(float delta) {
		final float max_vel_x = 100;
		final float max_vel_y = 100;
		final float drag = 0.95f;

		     if (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
		else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
		     if (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
		else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

		boundingBox.x += velocity.x * delta;
		boundingBox.y += velocity.y * delta;

		velocity.x *= drag;
		velocity.y *= drag;
	}

	public void render(SpriteBatch batch) {
		batch.draw(texture, boundingBox.x, boundingBox.y);
	}

	public int getGridX() { return (int) (boundingBox.x / 16); }
	public int getGridY() { return (int) (boundingBox.y / 16); }
	public Vector3 getPosition() { return new Vector3(boundingBox.x, boundingBox.y, 0); }
}
