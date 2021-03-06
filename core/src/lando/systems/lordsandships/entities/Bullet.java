package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/16/2014.
 */
public class Bullet extends Entity {

    private static final float LIFETIME = 2f;
    public static final float BULLET_SPEED = 7f;

    private float age;
    private boolean alive;

    public int damageAmount;

    public Bullet(float x, float y, float vx, float vy) {
        super(Assets.atlas.findRegion("bullet"), x, y,
              Assets.atlas.findRegion("bullet").getRegionWidth(),
              Assets.atlas.findRegion("bullet").getRegionHeight());
        velocity.set(vx ,vy);
        alive = true;
        age = 0f;
        damageAmount = 50;
    }

    @Override
    public void update(float delta) {
        boundingBox.x += velocity.x;
        boundingBox.y += velocity.y;
        position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);

        // TODO : check bullets for collisiona gainst world

        if ((age += delta) >= LIFETIME) {
            kill();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setColor(color);
        batch.draw(texture,
                   boundingBox.x,
                   boundingBox.y,
                   boundingBox.width / 2f,
                   boundingBox.height / 2f,
                   texture.getRegionWidth(),
                   texture.getRegionHeight(),
                   1, 1,
                   velocity.angle());
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isAlive() { return alive; }

    public void kill() {
        age = LIFETIME;
        alive = false;
    }
}
