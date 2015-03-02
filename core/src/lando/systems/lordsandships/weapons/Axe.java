package lando.systems.lordsandships.weapons;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 2/27/2015.
 */
public class Axe extends Weapon {

    public static final String axe_type     = "Axe";
    public static final float  axe_duration = 0.5f;

    public float accum;


    /**
     * Constructor
     *
     * @param builder The Weapon.Builder to create this object with
     */
    public Axe(Builder builder) {
        super(builder);
        setType(axe_type);

        int num_frames = 24;
        TextureRegion keyframes[] = new TextureRegion[num_frames];
        for (int i = 0; i < num_frames; ++i) {
            keyframes[i] = Assets.raphAtlas.findRegion("sAxe", i);
        }
        animation = new Animation(axe_duration / num_frames, keyframes);
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
//        direction.set(dir);
//        angle = MathUtils.radiansToDegrees *
//                (float) Math.atan2(direction.y, direction.x);

        Assets.axe_swing1.play(0.1f);

        Tween.to(color, ColorAccessor.A, axe_duration)
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
     * @param batch   The SpriteBatch to draw with
     * @param originX The origin point to draw from, x coordinate
     * @param originY The origin point to draw from, y coordinate
     */
    @Override
    public void render(SpriteBatch batch, float originX, float originY) {
        if (!attacking) return;

        accum += Gdx.graphics.getDeltaTime();
        TextureRegion keyframe = animation.getKeyFrame(accum);
        // Size and half size
        float w = keyframe.getRegionWidth();
        float h = keyframe.getRegionHeight();
        float hw = w / 2f;
        float hh = h / 2f;

        bounds.set(originX, originY, hw);

        batch.setColor(color);
        batch.draw(keyframe, bounds.x - hw, bounds.y - hh, w, h);
        batch.setColor(Color.WHITE);

        if (debug) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,
                               GL20.GL_ONE_MINUS_SRC_ALPHA);
            Assets.shapes.setColor(1, 1, 0, 1);
            Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
            Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
            Assets.shapes.end();

            Assets.shapes.setColor(1, 0, 1, 0.2f);
            Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
            Assets.shapes.circle(bounds.x, bounds.y, bounds.radius);
            Assets.shapes.end();
            batch.begin();
        }
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public boolean collides(Circle otherBounds) {
        return attacking && Intersector.overlaps(bounds, otherBounds);
    }
}
