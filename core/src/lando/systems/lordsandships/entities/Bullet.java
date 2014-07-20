package lando.systems.lordsandships.entities;

import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/16/2014.
 */
public class Bullet extends Entity {

	private static final float LIFETIME = 2f;
	public static final float BULLET_SPEED = 3.5f;

	private float age;
	private boolean alive;

	public int damageAmount;

	public Bullet(float x, float y, float vx, float vy) {
		super(Assets.atlas.findRegion("bullet"), x, y, 4, 4);
		velocity.set(vx ,vy);
		alive = true;
		age = 0f;
		damageAmount = 25;
	}

	@Override
	public void update(float delta) {
		boundingBox.x += velocity.x;
		boundingBox.y += velocity.y;
		position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);

		// TODO : check bullets for collisiona gainst world

		if ((age += delta) >= LIFETIME) {
			kill();
		}
	}

	public boolean isAlive() { return alive; }

	public void kill() {
		age = LIFETIME;
		alive = false;
	}
}
