package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.World;
import lando.systems.lordsandships.scene.particles.ExplosionEmitter;
import lando.systems.lordsandships.scene.ui.UserInterface;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;
import lando.systems.lordsandships.weapons.Handgun;
import lando.systems.lordsandships.weapons.Sword;
import lando.systems.lordsandships.weapons.Weapon;

/**
 * GameScreen
 *
 * The main game screen
 *
 * Brian Ploeckelman created on 5/28/2014.
 */
public class GameScreen implements UpdatingScreen {
    private final GameInstance game;

    private UserInterface ui;
    private World world;

    // TODO (brian): move camera stuff out to View class
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private OrthoCamController camController;

    private Vector3 playerPosition    = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();
    private Vector3 mouseWorldCoords  = new Vector3();

    private TextureRegion weaponIcon;
    private Vector2 weaponIconPos = new Vector2(30, 30);
    private Vector2 weaponIconSize = new Vector2(64, 64);

    private ExplosionEmitter explosionEmitter = new ExplosionEmitter();

    public GameScreen(GameInstance game) {
        super();

        this.game = game;

        Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("images/cursor.png"));
        Gdx.input.setCursorImage(cursorPixmap, 8, 8);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0,0,0);
        camera.update();

        ui = new UserInterface(game);
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        camController = new OrthoCamController(camera);

        world = new World();
        regenerateLevel();

        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(camController);
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        Gdx.input.setInputProcessor(inputMux);

        // TODO (brian): handle the weapon inventory separately, find each region once and cache them internally
        if (world.getPlayer().getCurrentWeapon() instanceof Sword) {
            weaponIcon = Assets.atlas.findRegion("sword");
        } else if (world.getPlayer().getCurrentWeapon() instanceof  Handgun) {
            weaponIcon = Assets.atlas.findRegion("gun");
        }
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();
        updateCurrentWeapon();

        world.update(delta);

        // TODO (brian): make a utility function to lerp a camera to a vec2 so we can drop the extra vec3 player pos
        playerPosition.set(world.getPlayer().getPosition().x, world.getPlayer().getPosition().y, 0);
        camera.position.lerp(playerPosition, 4*delta);

        ui.update(delta);

        camera.zoom = camController.camera_zoom.floatValue();
        camera.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0.17969f, 0.20313f, 0.21094f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (camController.debugRender) {
            world.debugRender(Assets.shapes, camera);
        }

        world.render(Assets.batch, camera);

        uiRender();
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.set(world.getPlayer().boundingBox.x, world.getPlayer().boundingBox.y, 0);
        camera.update();

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }

    @Override
    public void show() {
        GameInstance.input.reset();
    }

    @Override
    public void hide() {
        GameInstance.input.reset();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        ui.dispose();
        explosionEmitter.dispose();
    }

    // -------------------------------------------------------------------------
    // Delegating Methods
    // ------------------
    // NOTE: mainly used for console commands, find a cleaner way to handle them
    // -------------------------------------------------------------------------

    public void regenerateLevel() {
        camController.camera_zoom.setValue(camera.zoom);

        world.regenerateLevel();

        Tween.to(camController.camera_zoom, 0, 1.75f)
                .target(0.3f)
                .ease(Circ.OUT)
                .start(GameInstance.tweens);
    }

    // -------------------------------------------------------------------------
    // Implementation Details
    // -------------------------------------------------------------------------

    private void updateMouseVectors() {
        mouseScreenCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        mouseWorldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldCoords);
        GameInstance.mousePlayerDirection.set(
                mouseWorldCoords.x - world.getPlayer().getCenterPos().x,
                mouseWorldCoords.y - world.getPlayer().getCenterPos().y);
    }


    private void updateCurrentWeapon() {
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            if (world.getPlayer().getCurrentWeapon() instanceof Handgun) {
                world.getPlayer().setWeapon(Weapon.TYPE_SWORD);
                Timeline.createSequence()
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        weaponIcon = Assets.atlas.findRegion("sword");
                                        Assets.sword_slice1.play(0.1f);
                                    }
                                }))
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                        .start(GameInstance.tweens);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            if (world.getPlayer().getCurrentWeapon() instanceof Sword) {
                world.getPlayer().setWeapon(Weapon.TYPE_HANDGUN);
                Timeline.createSequence()
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        weaponIcon = Assets.atlas.findRegion("gun");
                                        Assets.gunshot_reload.play(0.4f);
                                    }
                                }))
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                        .start(GameInstance.tweens);
            }
        }
    }

    // TODO (brian): move into ui object
    private void uiRender() {
        Gdx.gl20.glViewport(0, 0, (int) uiCamera.viewportWidth, (int) uiCamera.viewportHeight);

        Assets.batch.setProjectionMatrix(uiCamera.combined);
        Assets.batch.begin();
        Assets.batch.draw(weaponIcon, weaponIconPos.x, weaponIconPos.y, weaponIconSize.x, weaponIconSize.y);
        Assets.batch.end();

        ui.draw();
    }

}
