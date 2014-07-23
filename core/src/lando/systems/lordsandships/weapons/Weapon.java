package lando.systems.lordsandships.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public abstract class Weapon {

	private String type;

	protected String name;
	protected int damage;
	protected int condition;
	protected float angle;
	protected boolean attacking;
	// TODO : cooldown

	public Weapon(Builder builder) {
		this.type      = builder.type;
		this.name      = builder.name;
		this.damage    = builder.damage;
		this.condition = builder.condition;
		this.angle     = builder.angle;
		this.attacking = builder.attacking;
	}

	public abstract void attack(Vector2 direction, LordsAndShips game);

	public abstract void render(SpriteBatch batch, float originX, float originY);

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public boolean isAttacking() {
		return attacking;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}


	/**
	 * Weapon builder
	 */
	public static class Builder {

		private String type      = "default";
		private String name      = "Generic Weapon";
		private int    damage    = 5;
		private int    condition = 100;
		private float  angle     = 0;
		private boolean attacking = false;

		public Builder() {}

		public Builder(String type) {
			this.type = type;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder damage(int damage) {
			this.damage = damage;
			return this;
		}

		public Builder condition(int condition) {
			this.condition = condition;
			return this;
		}

		public Builder angle(float angle) {
			this.angle = angle;
			return this;
		}

		public Builder attacking(boolean attacking) {
			this.attacking = attacking;
			return this;
		}
	}
}
