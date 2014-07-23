package lando.systems.lordsandships.weapons;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public class Weapon {

	private String type;
	private String name;
	private int damage;
	private int condition;

	public Weapon(Builder builder) {
		this.type = builder.type;
		this.name = builder.name;
		this.damage = builder.damage;
		this.condition = builder.condition;

		// TODO : removeme
		texture = Assets.atlas.findRegion("slash");
	}

	// TODO : move to Sword subclass
	public float slashRotation = 0;
	public boolean attacking = false;
	public Color slashColor = new Color(1,1,1,0);
	public float slash_duration = 0.25f;
	public Vector2 direction = new Vector2();
	// TODO : slash cooldown
	// TODO : slash anim
	public TextureRegion texture; // TODO : make animation

	/**
	 * TODO : make abstract, and add implementations in subclasses
	 * @param direction The direction in which to use the weapon
	 * @param game The game instance... TODO : create singleton GameInstance and remove this
	 */
	public void attack(Vector2 direction, LordsAndShips game) {
		slashRotation = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);
		attacking = true;
		slashColor.a = 1;
		this.direction.set(direction);

		Assets.sword_slice1.play();

		Tween.to(slashColor, ColorAccessor.A, slash_duration)
				.target(0)
				.ease(Cubic.INOUT)
				.setCallback(new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						attacking = false;
					}
				})
				.start(game.tween);

		// TODO : update animation?
	}

	// Draw a melee weapon slash arc TODO : move to subclass
	public void render(SpriteBatch batch, float originX, float originY) {
		// Size and half size
		float w = texture.getRegionWidth();
		float h = texture.getRegionHeight();
		float hw = w / 2f;
		float hh = h / 2f;

		// Offset and position
		float ox = direction.x * hw * 0.9f;
		float oy = direction.y * hh * 0.9f;
		float px = originX - hw + ox;
		float py = originY - hh + oy;

		// Scale
		float sx = 0.75f;
		float sy = 0.5f;

		batch.setColor(slashColor);
		batch.draw(texture, px, py, hw, hh, w, h, sx, sy, slashRotation);
		batch.setColor(Color.WHITE);
	}

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


	/**
	 * Weapon builder
	 */
	public static class Builder {

		private String type      = "default";
		private String name      = "Generic Weapon";
		private int    damage    = 5;
		private int    condition = 100;

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
	}
}
