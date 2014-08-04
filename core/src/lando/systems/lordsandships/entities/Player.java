package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utils;
import lando.systems.lordsandships.weapons.Handgun;
import lando.systems.lordsandships.weapons.Sword;
import lando.systems.lordsandships.weapons.Weapon;

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

	Weapon currentWeapon;
	Array<Weapon> weapons;

	public Player(Texture texture, float x, float y, float w, float h, float animRate) {
		super(new TextureRegion(texture), x, y, w, h);

		final int frameWidth = 32;
		final int frameHeight = 48;
		final int sheetCol = 0;
		final int sheetRow = 0;
		final int xTiles = 4;
		final int yTiles = 4;

		TextureRegion[][] keyframes = Utils.splitAndGet(
				Assets.playertex,
				frameWidth, frameHeight,
				sheetCol, sheetRow,
				xTiles, yTiles);

		walkDown  = new Animation(animRate, keyframes[0]);
		walkLeft  = new Animation(animRate, keyframes[1]);
		walkRight = new Animation(animRate, keyframes[2]);
		walkUp    = new Animation(animRate, keyframes[3]);

		TextureRegion[][] keyframesTest = Utils.splitAndGet(
				Assets.enemytex, 16, 18, 3, 0, 1, 4);

		punchAnim = new Animation(animRate / 2, keyframesTest[0][0], keyframesTest[1][0], keyframesTest[2][0], keyframesTest[3][0]);

		walkDown.setPlayMode(Animation.PlayMode.LOOP);
		walkLeft.setPlayMode(Animation.PlayMode.LOOP);
		walkRight.setPlayMode(Animation.PlayMode.LOOP);
		walkUp.setPlayMode(Animation.PlayMode.LOOP);
		punchAnim.setPlayMode(Animation.PlayMode.NORMAL);

		currentAnim = walkDown;
		currentKeyFrame = currentAnim.getKeyFrame(0);

		weapons = new Array<Weapon>();
		weapons.add(new Sword(new Weapon.Builder().damage(15)));
		weapons.add(new Handgun(new Weapon.Builder().damage(50)));
		currentWeapon = weapons.get(0);
	}


	@Override
	public void update(float delta) {
		updateMovement(delta);
		updateAnimation(delta);

		if (currentWeapon instanceof Handgun) {
			Handgun gun = (Handgun) currentWeapon;
			gun.update(delta);
		}
	}

	Vector2 dir = new Vector2();
	private void updateAnimation(float delta) {
		animTimer += delta;

		if (velocity.x == 0 && velocity.y == 0) {
			animTimer = 0f;
		}
		// Switch left/right animation based on mouse pos
		dir.set(GameInstance.mousePlayerDirection);
		if (currentAnim != walkRight && dir.x > 0) {
			currentAnim = walkRight;
			animTimer = 0f;
		} else if (currentAnim != walkLeft && dir.x < 0) {
			currentAnim = walkLeft;
			animTimer = 0f;
		}

		// Set current keyframe to draw with
		currentKeyFrame = currentAnim.getKeyFrame(animTimer);
	}

	private void updateMovement(float delta) {
		// TODO : convert to static
		final float max_vel_x = 128;
		final float max_vel_y = 128;
		final float drag = 0.95f;

		// Cap velocity
		if      (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
		else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
		if      (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
		else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

		// Move the player
		boundingBox.x += velocity.x * delta;
		boundingBox.y += velocity.y * delta;
		position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);
		collisionBounds.set(position, (boundingBox.width + boundingBox.height) / 2f);

		// Slow down and clamp velocity
		velocity.x *= drag;
		velocity.y *= drag;
		if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
		if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;
	}

	@Override
	public void render(SpriteBatch batch) {
		batch.draw(Assets.atlas.findRegion("shadow"), boundingBox.x, boundingBox.y - 1, 16, 16);
		batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y, 16, 24);

		currentWeapon.render(batch, getCenterPos().x, getCenterPos().y);

		// TODO : replace me
		if (currentWeapon instanceof Handgun) {
			Handgun gun = (Handgun) currentWeapon;
			for (Bullet bullet : gun.bullets) {
				bullet.render(batch);
			}
		}
	}

	public void attack(Vector2 direction) {
		currentWeapon.attack(position, direction);
	}

	// TODO : remove to weapon subclass?
	public void punch() {
		punching = true;
	}

	// TODO : this isn't really a great solution...
	Array<Bullet> bullets = new Array<Bullet>();
	public Array<Bullet> getBullets() {
		bullets.clear();
		for (Weapon weapon : weapons) {
			if (weapon instanceof Handgun) {
				bullets.addAll(((Handgun) weapon).bullets);
			}
		}
		return bullets;
	}

	public Weapon getCurrentWeapon() { return currentWeapon; }

	public void setWeapon(int type) {
		if (type >= 0 && type < Weapon.NUM_WEAPON_TYPES && type < weapons.size) {
			currentWeapon = weapons.get(type);
		}
	}
}
