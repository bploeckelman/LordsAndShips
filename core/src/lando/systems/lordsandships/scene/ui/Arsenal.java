package lando.systems.lordsandships.scene.ui;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Cubic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.weapons.*;

/**
 * Brian Ploeckelman created on 1/3/2015.
 */
public class Arsenal {

    private int currentWeaponIcon;
    private TextureRegion[] weaponIcons;

    private final float icon_height_active   = -10;
    private final float icon_height_inactive = -30;
    private final float icon_left_margin     = 32;

    private final Vector2[] weaponIconsPos;
    private final Vector2 weaponIconSize;


    public Arsenal() {
        weaponIcons = new TextureRegion[Weapon.NUM_WEAPON_TYPES];
        weaponIcons[Weapon.TYPE_BOW]     = Assets.collection.findRegion("weaponcards", 1);
        weaponIcons[Weapon.TYPE_SPEAR]   = Assets.collection.findRegion("weapons", 1);
        weaponIcons[Weapon.TYPE_AXE]     = Assets.collection.findRegion("weapons", 3);
        weaponIcons[Weapon.TYPE_SWORD]   = Assets.collection.findRegion("weaponcards", 2);
        weaponIcons[Weapon.TYPE_HANDGUN] = Assets.collection.findRegion("weaponcards", 3);
        currentWeaponIcon = 0;

        weaponIconSize = new Vector2(32, 48);
        weaponIconsPos = new Vector2[Weapon.NUM_WEAPON_TYPES];
        for (int i = 0; i < weaponIconsPos.length; ++i) {
            float x = icon_left_margin + i * weaponIconSize.x;
            float y = (currentWeaponIcon == i) ? icon_height_active : icon_height_inactive;
            weaponIconsPos[i] = new Vector2(x, y);
        }
    }

    // -------------------------------------------------------------------------
    // Public Interface
    // -------------------------------------------------------------------------

    public void render(SpriteBatch batch, Camera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int i = 0; i < weaponIcons.length; ++i) {
            batch.draw(weaponIcons[i], weaponIconsPos[i].x, weaponIconsPos[i].y, weaponIconSize.x, weaponIconSize.y);
        }
        batch.end();
    }

    public void updateCurrentWeapon(Player player) {
        final Weapon weapon = player.getCurrentWeapon();

        if      (Gdx.input.isKeyPressed(Input.Keys.NUM_1) && !(weapon instanceof Bow)) {
            setCurrentWeaponIcon(Weapon.TYPE_BOW, player);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2) && !(weapon instanceof Spear)) {
            setCurrentWeaponIcon(Weapon.TYPE_SPEAR, player);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3) && !(weapon instanceof Axe)) {
            setCurrentWeaponIcon(Weapon.TYPE_AXE, player);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_4) && !(weapon instanceof Sword)) {
            setCurrentWeaponIcon(Weapon.TYPE_SWORD, player);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_5) && !(weapon instanceof Handgun)) {
            setCurrentWeaponIcon(Weapon.TYPE_HANDGUN, player);
        }
    }

    public void setCurrentWeaponIcon(final int weaponType, Player player) {
        if (weaponType < 0 || weaponType >= Weapon.NUM_WEAPON_TYPES) {
            throw new IllegalArgumentException("Unable to change weapon, unknown weapon type: " + weaponType);
        }

        int prevWeaponType = currentWeaponIcon;
        player.setWeapon(weaponType);

        Timeline.createSequence()
                .push(Tween.to(weaponIconsPos[prevWeaponType], Vector2Accessor.Y, 0.2f)
                           .target(icon_height_inactive)
                           .ease(Cubic.OUT)
                           .setCallback(new TweenCallback() {
                               @Override
                               public void onEvent(int type, BaseTween<?> source) {
                                   switch (weaponType) {
                                       case Weapon.TYPE_BOW     : Assets.bow_shot1.play(1.0f); break;
                                       case Weapon.TYPE_SPEAR   : Assets.spear_stab1.play(0.04f); break;
                                       case Weapon.TYPE_AXE     : Assets.axe_swing1.play(0.04f); break;
                                       case Weapon.TYPE_SWORD   : Assets.sword_slice1.play(0.04f); break;
                                       case Weapon.TYPE_HANDGUN : Assets.gunshot_reload.play(0.1f); break;
                                   }
                                   currentWeaponIcon = weaponType;
                               }
                           }))
                .push(Tween.to(weaponIconsPos[weaponType], Vector2Accessor.Y, 0.5f)
                           .target(icon_height_active)
                           .ease(Bounce.OUT))
                .start(GameInstance.tweens);
    }

}
