package lando.systems.lordsandships.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/30/2015.
 */
public class SlimeSmall extends Enemy {

    final float max_vel_x = 64;
    final float max_vel_y = 64;

    boolean       moving = false;
    float         waitTime;
    float         moveTime;
    float         stateTime;
    Animation     anim;

    public SlimeSmall(Texture texture, float x, float y, float w, float h, float animRate) {
        super(texture, x, y, w, h, animRate);

        final String name = "slime_small";
        final int num_keyframes = 6;
        final TextureRegion[] keyframes = new TextureRegion[num_keyframes];
        for (int i = 0; i < num_keyframes; ++i) {
            keyframes[i] = Assets.collection.findRegion(name, i+1);
        }

        anim = new Animation(animRate, keyframes);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        keyframe = anim.getKeyFrame(0);

        boundingBox.set(x, y, keyframes[0].getRegionWidth(), keyframes[0].getRegionHeight());
    }

    @Override
    public void update(float delta) {
        if (waitTime == 0f) {
            waitTime = (float) Math.random() * 1.5f;
        }

        if (!moving) {
            moveTime += delta;
            if (moveTime >= waitTime) {
                moveTime -= waitTime;
                waitTime = 0f;
                moving = true;
            }
        }

        // Update animation
        stateTime += delta;
        keyframe = anim.getKeyFrame(stateTime);

        if (moving) {
            // Update movement
            switch ((int) (Math.random() * 4.f)) {
                case 0:
                    velocity.x = (Assets.rand.nextBoolean() ? -1f : 1f) * max_vel_x;
                    velocity.y = 0f;
                    break;
                case 1:
                    velocity.x = 0f;
                    velocity.y = (Assets.rand.nextBoolean() ? -1f : 1f) * max_vel_y;
                    break;
                case 2:
                    velocity.x = (Assets.rand.nextBoolean() ? -1f : 1f) * max_vel_x;
                    velocity.y = (Assets.rand.nextBoolean() ? -1f : 1f) * max_vel_y;
                    break;
                case 3:
                    velocity.x = 0f;
                    velocity.y = 0f;
            }
            moving = false;
        }

        if      (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
        else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
        if      (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
        else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

        // Apply movement
        // TODO: remove some of this redundancy
        boundingBox.x += velocity.x * delta;
        boundingBox.y += velocity.y * delta;

        position.set(boundingBox.x + boundingBox.width / 2f,
                     boundingBox.y + boundingBox.height / 2f);

        collisionBounds.set(boundingBox.x + boundingBox.width / 2f,
                            boundingBox.y + boundingBox.height / 2f,
                            (boundingBox.width + boundingBox.height) / 4f - 1.5f);

        healthbar.value = health;
        healthbar.bounds.set(boundingBox.x, boundingBox.y - 8, boundingBox.width, 6);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(Assets.shadow, boundingBox.x, boundingBox.y - 2, boundingBox.width, Assets.shadow.getRegionHeight());
        batch.setColor(color);
        batch.draw(keyframe, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        batch.setColor(1, 1, 1, 1);
        healthbar.render(batch);
    }


}
