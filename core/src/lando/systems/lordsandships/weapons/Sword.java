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
import com.badlogic.gdx.math.*;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public class Sword extends Weapon {

    public static final String sword_type     = "Sword";
    public static final float  sword_duration = 0.2f;
    public static final float  sword_cooldown = 0.3f;

    public float accum;

    float attackCooldown = 0;
    public boolean canAttack = true;

    /**
     * Constructor
     *
     * @param builder The Weapon.Builder to create this object with
     */
    public Sword(Builder builder) {
        super(builder);
        setType(sword_type);

        int num_frames = 12;
        TextureRegion keyframes[] = new TextureRegion[num_frames];
        for (int i = 0; i < num_frames; ++i) {
            keyframes[i] = Assets.raphAtlas.findRegion("sSword", i);
        }
        animation = new Animation(sword_duration / num_frames, keyframes);
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
        if (attacking || !canAttack) return;
        attacking = true;
        attackCooldown = sword_cooldown;
        canAttack = false;

        color.a = 1;
        direction.set(dir);
        angle = MathUtils.radiansToDegrees * (float) Math.atan2(direction.y, direction.x);

        Assets.sword_slice1.play(0.1f);

        Tween.to(color, ColorAccessor.A, sword_duration)
                .target(0)
                .ease(Cubic.INOUT)
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
        if (!attacking) return;

        // Size and half size
        float w = animation.getKeyFrames()[0].getRegionWidth();
        float h = animation.getKeyFrames()[0].getRegionHeight();
        float hw = w / 2f;
        float hh = h / 2f;

        // Offset and position
        float ox = direction.x * hw * 0.3f;
        float oy = direction.y * hh * 0.3f;
        float px = originX - hw + ox;
        float py = originY - hh + oy;

        // Scale
        float sx = 0.8f;
        float sy = 0.8f;

        float box = direction.x * hw * 0.5f;
        float boy = direction.y * hh * 0.5f;
        bounds.set(originX + box, originY + boy, (w * sx + h * sy) / 4.75f);

        batch.setColor(color);
        batch.draw(animation.getKeyFrame(accum += Gdx.graphics.getDeltaTime()), px, py, hw, hh, w, h, sx, sy, angle);
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
        attackCooldown -= delta;
        if (attackCooldown < 0f) attackCooldown = 0f;
        if (attacking && attackCooldown == 0f) {
            attacking = false;
        }
    }

    @Override
    public boolean collides(Circle otherBounds) {
        return attacking && (attackCooldown > (sword_cooldown - 0.1f)) && Intersector.overlaps(bounds, otherBounds);
    }
}
