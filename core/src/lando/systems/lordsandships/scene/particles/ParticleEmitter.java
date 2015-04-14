package lando.systems.lordsandships.scene.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Brian Ploeckelman created on 7/20/2014.
 */
public class ParticleEmitter implements Disposable {

    protected final int max_particles = 500;

    protected float damping = 1;
    protected float delta_scale = 0;
    protected float particle_lifetime = 1;

    TextureRegion texture;
    Array<Particle> particles;

    private Vector2 size;
    private Pool<Particle> particlePool;
    private boolean done;

    public ParticleEmitter(TextureRegion textureRegion) {
        texture = textureRegion;
        size = new Vector2(texture.getRegionWidth(), texture.getRegionHeight());
        particles = new Array<Particle>(false, max_particles);
        particlePool = new Pool<Particle>() {
            @Override
            protected Particle newObject() {
                return new Particle();
            }
        };
    }

    public void init() {
        done = false;
    }

    public void render(SpriteBatch batch) {
        float delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

        Particle particle;

        // Count down to be able to remove particles without indexing problems
        boolean liveParticles = false;
        for (int i = particles.size - 1; i >= 0; --i) {
            particle = particles.get(i);

            update(particle, delta);

            if (particle.age == 0) {
                particles.removeIndex(i);
                particlePool.free(particle);
                continue;
            }
            liveParticles = true;

            render(particle, batch);
        }

        if (!liveParticles) {
            done = true;
        }

        batch.setColor(Color.WHITE);
    }

    public void addParticle(Vector2 position, Vector2 velocity, float lifetime, float scale) {
        if (particles.size > max_particles) return;

        Particle particle = particlePool.obtain();
        particle.init(position, velocity, lifetime, scale);
        particles.add(particle);
    }

    private void update(Particle particle, float delta) {
        particle.age -= delta;
        if (particle.age < 0) {
            particle.age = 0;
        } else {
            particle.position.add(particle.velocity.x * delta, particle.velocity.y * delta);
            particle.velocity.scl((float) Math.pow(damping, delta));
            particle.scale += delta_scale * delta;
            if (particle.scale < 0) particle.scale = 0;
        }
    }

    private void render(Particle particle, SpriteBatch batch) {
        batch.setColor(1, 1, 1, particle.age / particle_lifetime);
        batch.draw( texture,
                    particle.position.x, particle.position.y,
                    size.x / 2f, size.y / 2f,
                    size.x, size.y,
                    particle.scale, particle.scale,
                    360f *  2f * (1f - (particle.age / particle_lifetime)) );
    }

    @Override
    public void dispose() {
        particles.clear();
        particlePool.clear();
    }

    public boolean isDone() {
        return done;
    }
}
