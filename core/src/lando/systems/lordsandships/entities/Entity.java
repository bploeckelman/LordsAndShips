package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utilities;

/**
 * Brian Ploeckelman created on 6/15/2014.
 */
public class Entity {
	public TextureRegion texture;
	public Vector2 velocity;
	public Rectangle boundingBox;

	public Animation walkLeft;
	public Animation walkRight;
	public Animation walkUp;
	public Animation walkDown;
	public Animation currentAnim;
	public TextureRegion currentKeyFrame;

	public float animTimer = 0f;

	public Entity(TextureRegion texture, float x, float y, float w, float h) {
		this.texture = texture;
		this.velocity = new Vector2();
		this.boundingBox = new Rectangle(x,y,w,h);

		TextureRegion[][] keyframes = Utilities.splitAndGet(Assets.playertex, 16, 20, 0, 0, 4, 4);
		walkDown  = new Animation(0.1f, keyframes[0]);
		walkLeft  = new Animation(0.1f, keyframes[1]);
		walkRight = new Animation(0.1f, keyframes[2]);
		walkUp    = new Animation(0.1f, keyframes[3]);

		walkDown.setPlayMode(Animation.PlayMode.LOOP);
		walkLeft.setPlayMode(Animation.PlayMode.LOOP);
		walkRight.setPlayMode(Animation.PlayMode.LOOP);
		walkUp.setPlayMode(Animation.PlayMode.LOOP);

		currentAnim = walkDown;
		currentKeyFrame = currentAnim.getKeyFrame(0);
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
		if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
		if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;

		// Update animation type and timer if appropriate
		animTimer += delta;
		if (velocity.x == 0 && velocity.y == 0) {
			animTimer = 0f;
		}
		else if (velocity.x > 0 && currentAnim != walkRight) {
			currentAnim = walkRight;
			animTimer = 0f;
		}
		else if (velocity.x < 0 && currentAnim != walkLeft) {
			currentAnim = walkLeft;
			animTimer = 0f;
		}
		else if (velocity.y > 0 && currentAnim != walkUp) {
			currentAnim = walkUp;
			animTimer = 0f;
		}
		else if (velocity.y < 0 && currentAnim != walkDown) {
			currentAnim = walkDown;
			animTimer = 0f;
		}
		currentKeyFrame = currentAnim.getKeyFrame(animTimer);
	}

	public void render(SpriteBatch batch) {
		batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y);
	}

	public int getGridMinX() { return (int) (boundingBox.x / 16); }
	public int getGridMinY() { return (int) (boundingBox.y / 16); }
	public int getGridMaxX() { return (int) ((boundingBox.x + boundingBox.width ) / 16); }
	public int getGridMaxY() { return (int) ((boundingBox.y + boundingBox.height) / 16); }
	public Vector3 getPosition() { return new Vector3(boundingBox.x, boundingBox.y, 0); }
}
