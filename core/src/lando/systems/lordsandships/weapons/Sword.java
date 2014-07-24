package lando.systems.lordsandships.weapons;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public class Sword extends Weapon {

	public static final String sword_type = "Sword";
	public static final float slash_duration = 0.25f;

	public Color color;
	// TODO : make animation and move to Weapon superclass
	public TextureRegion texture;


	/**
	 * Constructor
	 *
	 * @param builder The Weapon.Builder to create this object with
	 */
	public Sword(Builder builder) {
		super(builder);
		setType(sword_type);

		texture = Assets.atlas.findRegion("slash");
		color = new Color(1,1,1,0);
		direction = new Vector2();

		float w = texture.getRegionWidth();
		float h = texture.getRegionHeight();
		bounds.set(0, 0, (w + h) / 4);
	}

	/**
	 * Attack in the specified direction
	 *
	 * @param direction The direction in which to use the weapon
	 * @param game The game instance... TODO : create singleton GameInstance and remove this
	 */
	@Override
	public void attack(Vector2 origin, Vector2 direction, LordsAndShips game) {
		if (attacking) return;
		attacking = true;

		this.angle = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);
		this.color.a = 1;
		this.direction.set(direction);

		Assets.sword_slice1.play();

		Tween.to(color, ColorAccessor.A, slash_duration)
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

	/**
	 * Render the weapon effect
	 *
	 * @param batch The SpriteBatch to draw with
	 * @param originX The origin point to draw from, x coordinate
	 * @param originY The origin point to draw from, y coordinate
	 */
	@Override
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
		float sy = 0.75f;

		bounds.set(originX + ox, originY + oy, (w * sx + h * sy) / 4);

		batch.setColor(color);
		batch.draw(texture, px, py, hw, hh, w, h, sx, sy, angle);
		batch.setColor(Color.WHITE);

//		if (attacking) {
//			batch.end();
//			Assets.shapes.setColor(Color.RED);
//			Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
//			Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
//			Assets.shapes.end();
//
//			Assets.shapes.setColor(Color.MAGENTA);
//			Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
//			Assets.shapes.circle(originX + ox, originY + oy, 1.1f);
//			Assets.shapes.end();
//			batch.begin();
//		}
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public boolean collides(Circle otherBounds) {
		return attacking && Intersector.overlaps(bounds, otherBounds);
	}
}
