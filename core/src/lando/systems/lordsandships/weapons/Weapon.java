package lando.systems.lordsandships.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;

/**
 * Brian Ploeckelman created on 7/22/2014.
 */
public abstract class Weapon {

    public static final int TYPE_BOW         = 0;
    public static final int TYPE_SPEAR       = 1;
    public static final int TYPE_AXE         = 2;
    public static final int TYPE_SWORD       = 3;
    public static final int TYPE_HANDGUN     = 4;
    public static final int NUM_WEAPON_TYPES = 5;

    private String type;

    protected String    name;
    protected int       damage;
    protected int       condition;
    protected float     angle;
    protected boolean   attacking;
    protected Color     color;
    protected Circle    bounds;
    protected Vector2   direction;
    protected Animation animation;
    protected boolean   debug;
    // TODO : cooldown

    public Weapon(Builder builder) {
        this.type      = builder.type;
        this.name      = builder.name;
        this.damage    = builder.damage;
        this.condition = builder.condition;
        this.angle     = builder.angle;
        this.attacking = builder.attacking;
        this.color     = builder.color;
        this.bounds    = builder.bounds;
        this.direction = builder.direction;
        this.animation = builder.animation;
        this.debug     = false;
    }


    public abstract void attack(Vector2 origin, Vector2 direction);

    public abstract void render(SpriteBatch batch, float originX, float originY);

    public abstract void update(float delta);

    public abstract boolean collides(Circle otherBounds);


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Circle getBounds() {
        return bounds;
    }

    public void setBounds(Circle bounds) {
        this.bounds = bounds;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public void setDirection(Vector2 direction) {
        this.direction = direction;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void showHitBounds(boolean showWeaponHitBounds) {
        debug = showWeaponHitBounds;
    }


    /**
     * Weapon builder
     */
    public static class Builder {

        private String type      = "default";
        private String name      = "Generic Weapon";
        private int    damage    = 5;
        private int    condition = 100;
        private float  angle     = 0;
        private boolean attacking = false;
        private Color   color     = new Color(1,1,1,1);
        private Circle  bounds    = new Circle();
        private Vector2 direction = new Vector2();
        private Animation animation = new Animation(0);

        public Builder() {}

        public Builder(String type) {
            this.type = type;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder damage(int damage) {
            this.damage = damage;
            return this;
        }

        public Builder condition(int condition) {
            this.condition = condition;
            return this;
        }

        public Builder angle(float angle) {
            this.angle = angle;
            return this;
        }

        public Builder attacking(boolean attacking) {
            this.attacking = attacking;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder bounds(Circle bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder direction(Vector2 direction) {
            this.direction = direction;
            return this;
        }

        public Builder animation(Animation animation) {
            this.animation = animation;
            return this;
        }
    }
}
