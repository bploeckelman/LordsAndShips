package lando.systems.lordsandships.entities;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Quint;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.tweens.ColorAccessor;

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
    public Healthbar healthbar;

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
        this.healthbar = new Healthbar();
        this.healthbar.maxValue = health;
        this.healthbar.value    = health;
        this.healthbar.bounds = new Rectangle(x, y - 6, w, 6);
    }

    public abstract void update(float delta);

    public void render(SpriteBatch batch) {
        batch.setColor(color);
        batch.draw(texture, boundingBox.x, boundingBox.y);
        batch.setColor(1,1,1,1);
    }

    public int getGridMinX() { return (int) (boundingBox.x / Tile.TILE_SIZE); }
    public int getGridMinY() { return (int) (boundingBox.y / Tile.TILE_SIZE); }
    public int getGridMaxX() { return (int) ((boundingBox.x + boundingBox.width ) / Tile.TILE_SIZE); }
    public int getGridMaxY() { return (int) ((boundingBox.y + boundingBox.height) / Tile.TILE_SIZE); }

    public boolean isAlive() { return alive; }
    public Vector2 getPosition() { return position; }

    static final Vector2 temp = new Vector2();
    static final float entity_recoil = 2f;
    public boolean takeDamage(int amount, Vector2 dir) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
        temp.x = dir.x * entity_recoil;
        temp.y = dir.y * entity_recoil;
        boundingBox.x += temp.x;
        boundingBox.y += temp.y;
        position.set(boundingBox.x, boundingBox.y);

        color.set(1, 1, 0, 1);
        Tween.to(color, ColorAccessor.RGB, 0.2f)
                .target(1, 1, 1)
                .ease(Quint.IN)
                .start(GameInstance.tweens);

        return alive;
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

    public Circle getCollisionBounds() {
        return collisionBounds;
    }

}
