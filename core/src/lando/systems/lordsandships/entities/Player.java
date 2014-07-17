package lando.systems.lordsandships.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utilities;

/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Player extends Entity {

	Animation walkLeft;
	Animation walkRight;
	Animation walkUp;
	Animation walkDown;
	Animation currentAnim;
	Animation punchAnim;
	TextureRegion currentKeyFrame;

	float animTimer = 0f;
	boolean punching = false;

	private static final float SHOOT_COOLDOWN = 0.2f;
	float shootCooldown = SHOOT_COOLDOWN;
	boolean shooting = false;

	private static final int MAX_BULLETS = 100;
	// TODO : make pools instead of arrays
	Array<Bullet> bullets;
	Array<Bullet> bulletsToRemove;


	public Player(Texture texture, float x, float y, float w, float h, float animRate) {
		super(new TextureRegion(texture), x, y, w, h);

		final int frameWidth = 32;
		final int frameHeight = 48;
		final int sheetCol = 0;
		final int sheetRow = 0;
		final int xTiles = 4;
		final int yTiles = 4;

		TextureRegion[][] keyframes = Utilities.splitAndGet(
				Assets.playertex,
				frameWidth, frameHeight,
				sheetCol, sheetRow,
				xTiles, yTiles);

		walkDown  = new Animation(animRate, keyframes[0]);
		walkLeft  = new Animation(animRate, keyframes[1]);
		walkRight = new Animation(animRate, keyframes[2]);
		walkUp    = new Animation(animRate, keyframes[3]);

		TextureRegion[][] keyframesTest = Utilities.splitAndGet(
				Assets.enemytex, 16, 18, 3, 0, 1, 4);

		punchAnim = new Animation(animRate / 2, keyframesTest[0][0], keyframesTest[1][0], keyframesTest[2][0], keyframesTest[3][0]);

		walkDown.setPlayMode(Animation.PlayMode.LOOP);
		walkLeft.setPlayMode(Animation.PlayMode.LOOP);
		walkRight.setPlayMode(Animation.PlayMode.LOOP);
		walkUp.setPlayMode(Animation.PlayMode.LOOP);
		punchAnim.setPlayMode(Animation.PlayMode.NORMAL);

		currentAnim = walkDown;
		currentKeyFrame = currentAnim.getKeyFrame(0);

		bullets = new Array<Bullet>(MAX_BULLETS);
		bulletsToRemove = new Array<Bullet>(MAX_BULLETS);
	}


	@Override
	public void update(float delta) {
		updateMovement(delta);
		updateAnimation(delta);
		updateBullets(delta);
	}

	private void updateAnimation(float delta) {
		animTimer += delta;

		// Update animation type and timer if appropriate
		if (punching) {
			if (currentAnim == punchAnim) {
				if (currentAnim.isAnimationFinished(animTimer)) {
					// TODO : reset to idle animation
					currentAnim = walkDown;
					punching = false;
					animTimer = 0f;
				}
			} else {
				currentAnim = punchAnim;
				animTimer = 0f;
			}
		} else {
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
		}

		// Set current keyframe to draw with
		currentKeyFrame = currentAnim.getKeyFrame(animTimer);
	}

	private void updateMovement(float delta) {
		// TODO : convert to static
		final float max_vel_x = 100;
		final float max_vel_y = 100;
		final float drag = 0.95f;

		// Cap velocity
		if      (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
		else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
		if      (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
		else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

		// Move the player
		boundingBox.x += velocity.x * delta;
		boundingBox.y += velocity.y * delta;

		// Slow down and clamp velocity
		velocity.x *= drag;
		velocity.y *= drag;
		if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
		if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;
	}

	private void updateBullets(float delta) {
		// Update shooting state
		if (shooting) {
			if ((shootCooldown -= delta) < 0f) {
				shooting = false;
			}
		}

		// Update bullets
		bulletsToRemove.clear();
		for (Bullet bullet : bullets) {
			if (bullet.isAlive()) bullet.update(delta);
			else                  bulletsToRemove.add(bullet);
		}
		bullets.removeAll(bulletsToRemove, true);
	}

	@Override
	public void render(SpriteBatch batch) {
		batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y, 16, 24);

		for (Bullet bullet : bullets) {
			bullet.render(batch);
		}
	}

	public void punch() { punching = true; }

	private Vector3 pos = new Vector3();
	private Vector3 mouse = new Vector3();
	private Vector3 vel = new Vector3();

	public void shoot(Camera camera) {
		if (shooting) return;

		if ((bullets.size - 1) < MAX_BULLETS) {
			pos.set(boundingBox.x + boundingBox.width / 2.f, boundingBox.y + boundingBox.height / 2.f, 0);
			mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(mouse);

			vel.set(mouse).sub(pos).nor().scl(Bullet.BULLET_SPEED);

			shooting = true;
			shootCooldown = SHOOT_COOLDOWN;
			bullets.add(new Bullet(pos.x, pos.y, vel.x, vel.y));
		}
	}
}
