package lando.systems.lordsandships.scene.particles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 7/20/2014.
 */
public class ExplosionEmitter extends ParticleEmitter {

    public ExplosionEmitter() {
        super(Assets.atlas.findRegion("explosion"));

        damping = 1;
        delta_scale = -1;
        particle_lifetime = 2;
    }

    private void addParticle(Vector2 position, float scale) {
        super.addParticle(position, new Vector2(), particle_lifetime, scale);
    }

    private void addLaserExplosion(Vector2 position, Vector2 velocity) {
        Vector2 random = new Vector2();
        for (int i = 0; i < 10; ++i) {
            random.set(
                    MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
                    (float) (MathUtils.sin( MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));
            addParticle(position, velocity.scl(random), MathUtils.random(1, particle_lifetime), 1);
        }
    }

    public void addSmallExplosion(Vector2 position) {
        addParticle(position, 1);

        Vector2 random = new Vector2(
                MathUtils.cos((float) ((MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random()))),
                (float) (MathUtils.sin( MathUtils.random() * MathUtils.PI * 2f) * Math.sqrt(MathUtils.random())));

        for (int i = 1; i <= 2; ++i) {
            Vector2 vel = new Vector2().set(random).add(random);
//            Vector2 velp = new Vector2().set(vel).scl(i / 20.f * 10.f);
            Vector2 offset = new Vector2().set(random).scl(10);
            addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
        }

        for (int i = 1; i <= 1; ++i) {
            Vector2 vel = new Vector2(MathUtils.random() * 2 - 1, MathUtils.random() * 2 - 1).scl(10f);
            Vector2 offset = new Vector2().set(random).scl(3);
            addLaserExplosion(new Vector2(position.x + offset.x, position.y + offset.y), vel);
        }

        for (int i = 1; i <= 5; ++i) {
            Vector2 offset = new Vector2().set(random).scl(17);
            addParticle(new Vector2(position.x + offset.x, position.y + offset.y), new Vector2(), particle_lifetime, 1);
        }
    }

}
