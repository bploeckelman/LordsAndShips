package lando.systems.lordsandships.scene.level.objects;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;

/**
 * Brian Ploeckelman created on 4/11/2015.
 */
public class Light extends GameObject {

    private static final float default_size = 200f;

    Vector2       position;
    Vector2       size;
    Color         color;
    TextureRegion customTexture;
    Animation     customAnimation;
    float         animTimer;
    boolean       enabled;
    boolean       transitioning;
    MutableFloat  alpha;

    // TODO : add pulse, color change characteristics

    public Light() {
        position        = new Vector2();
        size            = new Vector2(default_size, default_size);
        color           = new Color(1, 1, 1, 1);
        customTexture   = null;
        enabled         = false;
        alpha           = new MutableFloat(1f);
        customAnimation = null;
        animTimer       = 0f;
    }

    public void update(float delta) {
        animTimer += delta;
    }

    public TextureRegion getCurrentFrame() {
        if (customTexture != null) return customTexture;
        if (customAnimation != null) {
            return customAnimation.getKeyFrame(animTimer);
        }
        return null;
    }

    public void fadeOut(float duration) {
        transitioning = true;
        Tween.to(alpha, -1, duration)
                .target(0f)
                .setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        transitioning = false;
                        enabled = false;
                    }
                })
                .start(GameInstance.tweens);
    }

    public void fadeIn(float duration) {
        enabled = true;
        transitioning = true;
        Tween.to(alpha, -1, duration)
             .target(1f)
             .setCallback(new TweenCallback() {
                 @Override
                 public void onEvent(int type, BaseTween<?> source) {
                     transitioning = false;
                 }
             })
             .start(GameInstance.tweens);
    }

    public void enable()  { enabled = true;  }
    public void disable() { enabled = false; }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setSize(float w, float h) {
        size.set(w, h);
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public boolean isTransitioning() { return transitioning; }

    public boolean isEnabled() { return enabled; }

    public TextureRegion getCustomTexture() { return customTexture; }

    public Vector2 getPosition() { return position; }

    public Vector2 getSize() { return size; }

    public MutableFloat getAlpha() { return alpha; }

    public Animation getCustomAnimation() { return customAnimation; }

    public void setCustomAnimation(Animation animation) { customAnimation = animation; }

}
