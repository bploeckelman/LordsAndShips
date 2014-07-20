package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utils;

/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Enemy extends Entity {
	Animation walkLeft;
	Animation walkRight;
	Animation walkUp;
	Animation walkDown;
	Animation currentAnim;
	TextureRegion currentKeyFrame;

	float animTimer = 0f;

	public Enemy(Texture texture, float x, float y, float w, float h, float animRate) {
		super(new TextureRegion(texture), x, y, w, h);

		final int frameWidth = 16;
		final int frameHeight = 18;
		final int sheetCol = 0;
		final int sheetRow = 0;
		final int xTiles = 3;
		final int yTiles = 4;

		TextureRegion[][] keyframes = Utils.splitAndGet(
				Assets.enemytex,
				frameWidth, frameHeight,
				sheetCol, sheetRow,
				xTiles, yTiles);

		walkUp    = new Animation(animRate, keyframes[0]);
		walkRight = new Animation(animRate, keyframes[1]);
		walkDown  = new Animation(animRate, keyframes[2]);
		walkLeft  = new Animation(animRate, keyframes[3]);

		walkDown.setPlayMode(Animation.PlayMode.LOOP);
		walkLeft.setPlayMode(Animation.PlayMode.LOOP);
		walkRight.setPlayMode(Animation.PlayMode.LOOP);
		walkUp.setPlayMode(Animation.PlayMode.LOOP);

		currentAnim = walkDown;
		currentKeyFrame = currentAnim.getKeyFrame(0);
	}

	float timer = 3f;
	@Override
	public void update(float delta) {
		final float max_vel_x = 50;
		final float max_vel_y = 50;
		final float drag = 0.995f;

		if ((timer += delta) > 3f) {
			if (Assets.rand.nextBoolean()) {
				velocity.x = ((float) Math.random() * 2f - 1f) * max_vel_x;
				velocity.y = 0f;
			} else {
				velocity.x = 0f;
				velocity.y = ((float) Math.random() * 2f - 1f) * max_vel_y;
			}
			timer = 0f;
		}

		     if (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
		else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
		     if (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
		else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

		// Update animation type and timer if appropriate
		animTimer += delta;
		if (velocity.x == 0 && velocity.y == 0) {
			// Reset animation when not moving
			animTimer = 0f;
		} else {
			// Switch up/down animation
			if (velocity.y > 0 && velocity.x == 0 && currentAnim != walkUp) {
				currentAnim = walkUp;
				animTimer = 0f;
			} else if (velocity.y < 0 && velocity.x == 0 && currentAnim != walkDown) {
				currentAnim = walkDown;
				animTimer = 0f;
			}

			// Switch left/right animation
			if (velocity.x > 0 && currentAnim != walkRight) {
				currentAnim = walkRight;
				animTimer = 0f;
			} else if (velocity.x < 0 && currentAnim != walkLeft) {
				currentAnim = walkLeft;
				animTimer = 0f;
			}
		}

		// Set current keyframe to draw with
		currentKeyFrame = currentAnim.getKeyFrame(animTimer);

		// Move the player
		boundingBox.x += velocity.x * delta;
		boundingBox.y += velocity.y * delta;
		position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);

		// Slow down and clamp velocity
		velocity.x *= drag;
		velocity.y *= drag;
		if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
		if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;
	}

	@Override
	public void render(SpriteBatch batch) {
		batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y, 16, 18);
	}
}
