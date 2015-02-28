package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Circ;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.World;
import lando.systems.lordsandships.scene.particles.ExplosionEmitter;
import lando.systems.lordsandships.scene.ui.UserInterface;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * GameScreen
 *
 * The main game screen
 *
 * Brian Ploeckelman created on 5/28/2014.
 */
public class GameScreen extends InputAdapter implements UpdatingScreen {
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

        world = new World(camera);

        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(camController);
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    public void create(PlayerSelectScreen.PlayerType playerType) {
        world.initializePlayer(playerType);
        ui.getArsenal().setCurrentWeaponIcon(playerType.value(), world.getPlayer());
        regenerateLevel();
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();

        world.update(delta);

        // TODO (brian): make a utility function to lerp a camera to a vec2 so we can drop the extra vec3 player pos
        playerPosition.set(world.getPlayer().getPosition().x, world.getPlayer().getPosition().y, 0);
        camera.position.lerp(playerPosition, 4*delta);

        ui.update(delta);
        ui.getArsenal().updateCurrentWeapon(world.getPlayer());

        camera.zoom = camController.camera_zoom.floatValue();
        camera.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (camController.debugRender) {
            world.debugRender(Assets.shapes, camera);
        }

        world.render(Assets.batch, camera);

        ui.render(Assets.batch, uiCamera);
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
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(camController);
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    @Override
    public void hide() {
        GameInstance.input.reset();
        Gdx.input.setInputProcessor(GameInstance.input);
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

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            game.setScreen(Constants.player_select_screen);
        }
        return true;
    }
}
