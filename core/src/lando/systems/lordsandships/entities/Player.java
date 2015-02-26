package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.screens.PlayerSelectScreen;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Utils;
import lando.systems.lordsandships.weapons.Handgun;
import lando.systems.lordsandships.weapons.Spear;
import lando.systems.lordsandships.weapons.Sword;
import lando.systems.lordsandships.weapons.Weapon;

/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Player extends Entity {

    Animation walkLeft;
    Animation walkRight;
    Animation walkUp;
    Animation walkDown;
    Animation currentAnim;
    Animation punchAnim;
    TextureRegion currentKeyFrame;


    float animTimer = 0f;
    boolean punching = false;

    Weapon currentWeapon;
    Array<Weapon> weapons;

    public Player(PlayerSelectScreen.PlayerType type, float x, float y, float w, float h, float animRate) {
        super(new TextureRegion(), x, y, w, h);

        TextureRegion[] up    = new TextureRegion[4];
        TextureRegion[] down  = new TextureRegion[4];
        TextureRegion[] right = new TextureRegion[4];
        for (int i = 0; i < 4; ++i) {
            up[i]    = Assets.raphAtlas.findRegion("sHero" + type.name() + "Up", i);
            down[i]  = Assets.raphAtlas.findRegion("sHero" + type.name() + "Down", i);
            right[i] = Assets.raphAtlas.findRegion("sHero" + type.name() + "Side", i);
        }
        walkDown  = new Animation(animRate, down);
        walkLeft  = new Animation(animRate, right);
        walkRight = new Animation(animRate, right);
        walkUp    = new Animation(animRate, up);

        walkDown.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkUp.setPlayMode(Animation.PlayMode.LOOP);

        currentAnim = walkDown;
        currentKeyFrame = currentAnim.getKeyFrame(0);

        weapons = new Array<Weapon>();
        weapons.add(new Sword(new Weapon.Builder().damage(15)));
        weapons.add(new Handgun(new Weapon.Builder().damage(50)));
        weapons.add(new Spear(new Weapon.Builder().damage(100)));
        currentWeapon = weapons.get(2);
    }
    public Player(Texture texture, float x, float y, float w, float h, float animRate) {
        super(new TextureRegion(texture), x, y, w, h);

        TextureRegion[] up    = new TextureRegion[4];
        TextureRegion[] down  = new TextureRegion[4];
        TextureRegion[] right = new TextureRegion[4];
        for (int i = 0; i < 4; ++i) {
            up[i]    = Assets.raphAtlas.findRegion("sHeroHornsUp", i);
            down[i]  = Assets.raphAtlas.findRegion("sHeroHornsDown", i);
            right[i] = Assets.raphAtlas.findRegion("sHeroHornsSide", i);
        }
        walkDown  = new Animation(animRate, down);
        walkLeft  = new Animation(animRate, right);
        walkRight = new Animation(animRate, right);
        walkUp    = new Animation(animRate, up);

        TextureRegion[][] keyframesTest = Utils.splitAndGet(
                Assets.enemytex, 16, 18, 3, 0, 1, 4);

        punchAnim = new Animation(animRate / 2, keyframesTest[0][0], keyframesTest[1][0], keyframesTest[2][0], keyframesTest[3][0]);

        walkDown.setPlayMode(Animation.PlayMode.LOOP);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP);
        walkRight.setPlayMode(Animation.PlayMode.LOOP);
        walkUp.setPlayMode(Animation.PlayMode.LOOP);
        punchAnim.setPlayMode(Animation.PlayMode.NORMAL);

        currentAnim = walkDown;
        currentKeyFrame = currentAnim.getKeyFrame(0);

        weapons = new Array<Weapon>();
        weapons.add(new Sword(new Weapon.Builder().damage(15)));
        weapons.add(new Handgun(new Weapon.Builder().damage(50)));
        weapons.add(new Spear(new Weapon.Builder().damage(100)));
        currentWeapon = weapons.get(2);
    }


    @Override
    public void update(float delta) {
        updateMovement(delta);
        updateAnimation(delta);

        if (currentWeapon instanceof Handgun) {
            Handgun gun = (Handgun) currentWeapon;
            gun.update(delta);
        }
    }

    Vector2 dir = new Vector2();
    private void updateAnimation(float delta) {
        animTimer += delta;

        if (velocity.x == 0 && velocity.y == 0) {
            animTimer = 0f;
        }

        // Set walk direction based on movement direction
//        if (velocity.y == 0) {
//            if (currentAnim != walkRight && velocity.x > 0) {
//                currentAnim  = walkRight;
//                animTimer = 0f;
//            } else if (currentAnim != walkLeft && velocity.x < 0) {
//                currentAnim = walkLeft;
//                animTimer = 0f;
//            }
//        } else if (velocity.x == 0) {
//            if (currentAnim != walkUp && velocity.y > 0) {
//                currentAnim  = walkUp;
//                animTimer = 0f;
//            }
//            else if (currentAnim != walkDown && velocity.y < 0) {
//                currentAnim = walkDown;
//                animTimer = 0f;
//            }
//        }

        // Set walk direction based on relative mouse orientation
        dir.set(GameInstance.mousePlayerDirection);
        if (dir.x != 0 && dir.y != 0) {
            float deg =
                    MathUtils.radiansToDegrees * MathUtils.atan2(dir.y, dir.x);
            if (currentAnim != walkRight &&
                ((deg >= -45 && deg <= 0) || (deg >= 0 && deg <= 45))) {
                currentAnim = walkRight;
                animTimer = 0f;
            } else if (currentAnim != walkLeft &&
                       ((deg >= -180 && deg <= (-180 + 45)) ||
                        (deg >= (180 - 45) && deg <= 180))) {
                currentAnim = walkLeft;
                animTimer = 0f;
            } else if (currentAnim != walkUp &&
                       (deg >= 45 && deg <= (180 - 45))) {
                currentAnim = walkUp;
                animTimer = 0f;
            } else if (currentAnim != walkDown &&
                       (deg >= (-180 + 45) && deg <= -45)) {
                currentAnim = walkDown;
                animTimer = 0f;
            }
        }

        // Set current keyframe to draw with, flipped if needed
        TextureRegion keyframe = currentAnim.getKeyFrame(animTimer);
        if ((currentAnim == walkLeft  && !keyframe.isFlipX())
         || (currentAnim == walkRight &&  keyframe.isFlipX())) {
            keyframe.flip(true, false);
        }
        currentKeyFrame = keyframe;
    }

    private void updateMovement(float delta) {
        // TODO : convert to static
        final float max_vel_x = 88;
        final float max_vel_y = 88;
        final float drag = 0.95f;

        // Cap velocity
        if      (velocity.x >  max_vel_x) velocity.x =  max_vel_x;
        else if (velocity.x < -max_vel_x) velocity.x = -max_vel_x;
        if      (velocity.y >  max_vel_y) velocity.y =  max_vel_y;
        else if (velocity.y < -max_vel_y) velocity.y = -max_vel_y;

        // Move the player
        boundingBox.x += velocity.x * delta;
        boundingBox.y += velocity.y * delta;
        position.set(boundingBox.x + boundingBox.width / 2f, boundingBox.y + boundingBox.height / 2f);
        collisionBounds.set(position, (boundingBox.width + boundingBox.height) / 2f);

        // Slow down and clamp velocity
        velocity.x *= drag;
        velocity.y *= drag;
        if (Math.abs(velocity.x) < 0.11f) velocity.x = 0;
        if (Math.abs(velocity.y) < 0.11f) velocity.y = 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(Assets.atlas.findRegion("shadow"), boundingBox.x, boundingBox.y - 1, 16, 16);
        batch.draw(currentKeyFrame, boundingBox.x, boundingBox.y, 16, 16);//24);

        currentWeapon.render(batch, getCenterPos().x, getCenterPos().y);

        // TODO : replace me
        if (currentWeapon instanceof Handgun) {
            Handgun gun = (Handgun) currentWeapon;
            for (Bullet bullet : gun.bullets) {
                bullet.render(batch);
            }
        }
    }

    public void attack(Vector2 direction) {
        currentWeapon.attack(position, direction);
    }

    // TODO : remove to weapon subclass?
    public void punch() {
        punching = true;
    }

    // TODO : this isn't really a great solution...
    Array<Bullet> bullets = new Array<Bullet>();
    public Array<Bullet> getBullets() {
        bullets.clear();
        for (Weapon weapon : weapons) {
            if (weapon instanceof Handgun) {
                bullets.addAll(((Handgun) weapon).bullets);
            }
        }
        return bullets;
    }

    public Weapon getCurrentWeapon() { return currentWeapon; }

    public void setWeapon(int type) {
        if (type >= 0 && type < Weapon.NUM_WEAPON_TYPES && type < weapons.size) {
            currentWeapon = weapons.get(type);
        }
    }
}
