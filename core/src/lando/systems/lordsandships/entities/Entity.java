package lando.systems.lordsandships.entities;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quint;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 6/15/2014.
 */
public abstract class Entity {

	public Vector2 position;
	public Vector2 velocity;
	public Rectangle boundingBox;
	public TextureRegion texture;
	public Circle collisionBounds;
	public Color color;

	// TODO : extract to attributes class
	public int health = 100;
	public boolean alive = true;

	public Entity(TextureRegion texture, float x, float y, float w, float h) {
		this.texture = texture;
		this.position = new Vector2(x + w/2f,y + h/2f);
		this.velocity = new Vector2();
		this.boundingBox = new Rectangle(x,y,w,h);
		this.collisionBounds = new Circle();
		this.color = new Color(1,1,1,1);
	}

	public abstract void update(float delta);

	public void render(SpriteBatch batch) {
		batch.setColor(color);
		batch.draw(texture, boundingBox.x, boundingBox.y);
		batch.setColor(1,1,1,1);
	}

	public int getGridMinX() { return (int) (boundingBox.x / 16); }
	public int getGridMinY() { return (int) (boundingBox.y / 16); }
	public int getGridMaxX() { return (int) ((boundingBox.x + boundingBox.width ) / 16); }
	public int getGridMaxY() { return (int) ((boundingBox.y + boundingBox.height) / 16); }

	public boolean isAlive() { return alive; }
	public Vector2 getPosition() { return position; }

	static final Vector2 temp = new Vector2();
	static final float entity_shake_scale = 2f;
	public void takeDamage(int amount, Vector2 dir, LordsAndShips game) {
		health -= amount;
		if (health <= 0) {
			health = 0;
			alive = false;
		}
		temp.x = dir.x + MathUtils.random() * entity_shake_scale;
		temp.y = dir.y + MathUtils.random() * entity_shake_scale;
		boundingBox.x += temp.x;
		boundingBox.y += temp.y;

		color.set(1, 0, 0, 1);
		Tween.to(color, ColorAccessor.RGB, 0.2f)
				.target(1, 1, 1)
				.ease(Quint.IN)
				.start(game.tween);
	}

	private Vector2 centerPos = new Vector2();
	public Vector2 getCenterPos() {
		centerPos.set(
				boundingBox.x + boundingBox.width / 2f,
				boundingBox.y + boundingBox.height / 2f);
		return centerPos;
	}

	private Vector2 dir = new Vector2();
	public Vector2 getDirection(float worldx, float worldy) {
		getCenterPos();
		dir.set(worldx, worldy).sub(centerPos).nor();
		return dir;
	}

	public Circle getCollisionBounds() { return collisionBounds; }

}
