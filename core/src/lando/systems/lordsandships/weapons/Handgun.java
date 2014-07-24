package lando.systems.lordsandships.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/23/2014.
 */
public class Handgun extends Weapon {

	public static final String handgun_type = "Handgun";
	public static final int max_bullets = 100;
	public static final float attack_cooldown = 0.3f;

	public Color color;
	// TODO : make animation and move to Weapon superclass
	public TextureRegion texture;
	public Array<Bullet> bullets;
	public Array<Bullet> bulletsToRemove;

	public float attackCooldown = 0;

	public Handgun(Builder builder) {
		super(builder);
		setType(handgun_type);
		color = new Color(1,1,1,1);
		texture = Assets.atlas.findRegion("bullet");

		bullets = new Array<Bullet>(max_bullets);
		bulletsToRemove = new Array<Bullet>(max_bullets);
	}

	@Override
	public void attack(Vector2 origin, Vector2 direction, LordsAndShips game) {
		if (attacking) return;

		this.angle = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);
		this.color.a = 1;
		this.direction.set(direction);

		if ((bullets.size - 1) < max_bullets) {
			bullets.add(new Bullet(origin.x, origin.y,
					direction.x * Bullet.BULLET_SPEED,
					direction.y * Bullet.BULLET_SPEED));

			attacking = true;
			attackCooldown = attack_cooldown;
			Assets.gunshot_shot.play(0.1f);
		}
	}

	@Override
	public void render(SpriteBatch batch, float originX, float originY) {
		// TODO : render gun attacked to player
	}

	@Override
	public void update(float delta) {
		// Update bullets
		bulletsToRemove.clear();
		for (Bullet bullet : bullets) {
			if (bullet.isAlive()) bullet.update(delta);
			else                  bulletsToRemove.add(bullet);
		}
		bullets.removeAll(bulletsToRemove, true);

		if (attacking) {
			if ((attackCooldown -= delta) < 0f) {
				attacking = false;
			} else {
				return;
			}
		}
	}

	@Override
	public boolean collides(Circle otherBounds) {
		return false;
	}
}
