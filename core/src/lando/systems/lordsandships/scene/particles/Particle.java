package lando.systems.lordsandships.scene.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Brian Ploeckelman created on 7/20/2014.
 */
public class Particle {

	Vector2 position;
	Vector2 velocity;
	float age;
	float scale;

	public Particle() {
		position = new Vector2();
		velocity = new Vector2();
		age = 1;
		scale = 1;
	}

	public void init(Vector2 position, Vector2 velocity, float lifetime, float scale) {
		this.position.set(position);
		this.velocity.set(velocity);
		this.age = lifetime;
		this.scale = scale;
	}

}
