package lando.systems.lordsandships.weapons;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public class Spear extends Weapon {

    public static final String sword_type = "Spear";
    public static final float spear_duration = 0.5f;

    public float accum;


    /**
     * Constructor
     *
     * @param builder The Weapon.Builder to create this object with
     */
    public Spear(Builder builder) {
        super(builder);
        setType(sword_type);

        animation = new Animation(spear_duration / 16f,
                Assets.raphAtlas.findRegion("sSpear", 0),
                Assets.raphAtlas.findRegion("sSpear", 1),
                Assets.raphAtlas.findRegion("sSpear", 2),
                Assets.raphAtlas.findRegion("sSpear", 3),
                Assets.raphAtlas.findRegion("sSpear", 4),
                Assets.raphAtlas.findRegion("sSpear", 5),
                Assets.raphAtlas.findRegion("sSpear", 6),
                Assets.raphAtlas.findRegion("sSpear", 7),
                Assets.raphAtlas.findRegion("sSpear", 8),
                Assets.raphAtlas.findRegion("sSpear", 9),
                Assets.raphAtlas.findRegion("sSpear", 10),
                Assets.raphAtlas.findRegion("sSpear", 11),
                Assets.raphAtlas.findRegion("sSpear", 12),
                Assets.raphAtlas.findRegion("sSpear", 13),
                Assets.raphAtlas.findRegion("sSpear", 14),
                Assets.raphAtlas.findRegion("sSpear", 15));
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        direction = new Vector2();
        color.a = 0;

        float w = animation.getKeyFrames()[0].getRegionWidth();
        float h = animation.getKeyFrames()[0].getRegionHeight();
        bounds.set(0, 0, (w + h) / 4);
    }

    /**
     * Attack in the specified direction
     *
     * @param dir The direction in which to use the weapon
     */
    @Override
    public void attack(Vector2 origin, Vector2 dir) {
        if (attacking) return;
        attacking = true;

        color.a = 1;
        direction.set(dir);
        angle = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);

        Assets.sword_slice1.play(0.1f);

        Tween.to(color, ColorAccessor.A, spear_duration)
                .target(0)
                .ease(Cubic.INOUT)
                .setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        attacking = false;
                    }
                })
                .start(GameInstance.tweens);

        accum = 0f;
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
        float w = animation.getKeyFrames()[0].getRegionWidth();
        float h = animation.getKeyFrames()[0].getRegionHeight();
        float hw = w / 2f;
        float hh = h / 2f;

        // Offset and position
        float ox = direction.x * hw * 0.9f;
        float oy = direction.y * hh * 0.9f;
        float px = originX - hw + ox;
        float py = originY - hh + oy;

        // Scale
        float sx = 0.75f;
        float sy = 0.55f;

        bounds.set(originX + ox, originY + oy, (w * sx + h * sy) / 4.75f);

        batch.setColor(color);
        batch.draw(animation.getKeyFrame(accum += Gdx.graphics.getDeltaTime()), px, py, hw, hh, w, h, sx, sy, angle);
        batch.setColor(Color.WHITE);

//        if (attacking) {
//            batch.end();
//            Assets.shapes.setColor(Color.RED);
//            Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
//            Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
//            Assets.shapes.end();
//
//            Assets.shapes.setColor(Color.MAGENTA);
//            Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
//            Assets.shapes.circle(originX + ox, originY + oy, 1.1f);
//            Assets.shapes.end();
//            batch.begin();
//        }
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public boolean collides(Circle otherBounds) {
        return attacking && Intersector.overlaps(bounds, otherBounds);
    }
}
