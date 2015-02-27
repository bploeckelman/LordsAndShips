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

    private final Vector2 weaponIconPos  = new Vector2(30, 30);
    private final Vector2 weaponIconSize = new Vector2(64, 64);


    public Arsenal() {
        weaponIcons = new TextureRegion[Weapon.NUM_WEAPON_TYPES];
        weaponIcons[0] = Assets.atlas.findRegion("sword");
        weaponIcons[1] = Assets.atlas.findRegion("gun");
        weaponIcons[2] = Assets.atlas.findRegion("sword");

        currentWeaponIcon = 0;
    }

    // -------------------------------------------------------------------------
    // Public Interface
    // -------------------------------------------------------------------------

    public void render(SpriteBatch batch, Camera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(weaponIcons[currentWeaponIcon],
                   weaponIconPos.x,  weaponIconPos.y,
                   weaponIconSize.x, weaponIconSize.y);
        batch.end();
    }

    public void updateCurrentWeapon(Player player) {
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)
         && !(player.getCurrentWeapon() instanceof Sword)) {
             player.setWeapon(Weapon.TYPE_SWORD);
             Timeline.createSequence()
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        Assets.sword_slice1.play(0.1f);
                                        currentWeaponIcon = 0;
                                    }
                                }))
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                     .start(GameInstance.tweens);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)
         && !(player.getCurrentWeapon() instanceof Handgun)) {
             player.setWeapon(Weapon.TYPE_HANDGUN);
             Timeline.createSequence()
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                             .target(-weaponIconSize.y)
                             .ease(Cubic.OUT)
                             .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                       Assets.gunshot_reload.play(0.4f);
                                       currentWeaponIcon = 1;
                                    }
                             }))
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                             .target(30)
                             .ease(Bounce.OUT))
                     .start(GameInstance.tweens);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)
         && !(player.getCurrentWeapon() instanceof Spear)) {
             player.setWeapon(Weapon.TYPE_SPEAR);
             Timeline.createSequence()
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        Assets.sword_slice1.play(0.1f);
                                        currentWeaponIcon = 2;
                                    }
                                }))
                     .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                     .start(GameInstance.tweens);
        }
    }

}
