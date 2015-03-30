package lando.systems.lordsandships.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utils;

/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Enemy extends Entity {
    Animation walkLeft;
    Animation walkRight;
    Animation currentAnim;
    TextureRegion currentKeyFrame;

    float animTimer = 0f;

    public Enemy(Texture texture, float x, float y, float w, float h, float animRate) {
        super(new TextureRegion(texture), x, y, w, h);
        TextureRegion[] keyframesRight = new TextureRegion[4];
        keyframesRight[0] = Assets.raphAtlas.findRegion("dragonFly", 0);
        keyframesRight[1] = Assets.raphAtlas.findRegion("dragonFly", 1);
        keyframesRight[2] = Assets.raphAtlas.findRegion("dragonFly", 2);
        keyframesRight[3] = Assets.raphAtlas.findRegion("dragonFly", 3);
        TextureRegion[] keyframesLeft = new TextureRegion[4];
        for (int i = 0; i < 4; ++i) {
            keyframesLeft[i] = new TextureRegion(keyframesRight[i]);
            keyframesLeft[i].flip(true, false);
        }
        boundingBox.set(x, y, keyframesRight[0].getRegionWidth(), keyframesRight[0].getRegionHeight());


        walkRight = new Animation(animRate, keyframesRight);
        walkLeft  = new Animation(animRate, keyframesLeft);

        walkLeft.setPlayMode(Animation.PlayMode.LOOP);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);

        currentAnim = walkRight;
        currentKeyFrame = currentAnim.getKeyFrame(0);
    }

    float timer = 3f;
    @Override
    public void update(float delta) {
        final float max_vel_x = 50;
        final float max_vel_y = 50;
        final float drag = 0.995f;

        if ((timer += delta) > Assets.rand.nextInt(5) + 2) {
            timer = 0f;
            switch (Assets.rand.nextInt(3)) {
                case 0:
                    velocity.x = ((float) Math.random() * 2f - 1f) * max_vel_x;
                    velocity.y = 0f;
                    break;
                case 1:
                    velocity.x = 0f;
                    velocity.y = ((float) Math.random() * 2f -1f) * max_vel_y;
                    break;
                case 2:
                    velocity.x = 0f;
                    velocity.y = 0f;
            }
        }

             if (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
        else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
             if (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
        else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

        // Slow down and clamp velocity
        velocity.x *= drag;
        velocity.y *= drag;
        if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
        if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;

        // Update animation type and timer if appropriate
        animTimer += delta;
        if (currentAnim != walkRight && velocity.x > 0) {
            currentAnim  = walkRight;
            animTimer = 0f;
        } else if (currentAnim != walkLeft && velocity.x < 0) {
            currentAnim = walkLeft;
            animTimer = 0f;
        }
        currentKeyFrame = currentAnim.getKeyFrame(animTimer);

        // Move the player
        boundingBox.x += velocity.x * delta;
        boundingBox.y += velocity.y * delta;
        position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);
        collisionBounds.set(boundingBox.x + boundingBox.width / 2f,
                            boundingBox.y + boundingBox.height / 2f,
                            (boundingBox.width + boundingBox.height) / 4f);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(Assets.shadow, boundingBox.x, boundingBox.y - 2);
        batch.setColor(color);
        batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        batch.setColor(1,1,1,1);
    }

    public void renderDebug() {
        Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
        Assets.shapes.setColor(Color.RED);
        Assets.shapes.circle(position.x, position.y, 1);
        Assets.shapes.setColor(Color.MAGENTA);
        Assets.shapes.circle(boundingBox.x, boundingBox.y, 2);
        Assets.shapes.end();

        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
        Assets.shapes.setColor(Color.YELLOW);
        Assets.shapes.circle(collisionBounds.x, collisionBounds.y, collisionBounds.radius);
        Assets.shapes.setColor(Color.ORANGE);
        Assets.renderRect(boundingBox);
        Assets.shapes.end();
    }

}
