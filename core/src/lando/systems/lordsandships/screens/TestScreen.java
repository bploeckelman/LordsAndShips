package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.*;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.level.Level;
import lando.systems.lordsandships.scene.ui.UserInterface;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.tweens.Vector3Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class TestScreen extends InputAdapter implements UpdatingScreen {

    final GameInstance game;

    // TODO : extract to UpdatingScreen?
    private Vector3 mouseWorldCoords  = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();

    Color              bgColor;
    OrthographicCamera camera;
    OrthographicCamera uiCamera;

    UserInterface ui;
    Level level;

    public TestScreen(GameInstance game) {
        this.game = game;
        create();
    }

    public void create() {
        bgColor = new Color(0.43f,0.43f,0.43f,1);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width, Constants.win_half_height, 0);
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        ui = new UserInterface(game);

        level = new Level();
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        level.update(delta);

        if (!transition) {
            // TODO : limit zoom level to min room dimension
            // Constrain camera to the occupied room's bounds
            float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
            float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
            float hw = effectiveViewportWidth / 2f;
            float hh = effectiveViewportHeight / 2f;

            Rectangle bounds = level.getOccupiedRoomBounds();
            if (camera.position.x - hw < bounds.x)
                camera.position.x = bounds.x + hw;
            if (camera.position.x + hw > bounds.x + bounds.width)
                camera.position.x = bounds.x + bounds.width - hw;

            if (camera.position.y - hh < bounds.y)
                camera.position.y = bounds.y + hh;
            if (camera.position.y + hh > bounds.y + bounds.height)
                camera.position.y = bounds.y + bounds.height - hh;
        }

        camera.update();
        uiCamera.update();
        ui.update(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final SpriteBatch batch = Assets.batch;
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        {
            batch.setColor(1, 1, 1, roomAlpha.floatValue());
            level.render(batch, camera);
        }
        batch.end();

//        level.renderDebug(camera);

        ui.render(batch, uiCamera);
    }

    // -------------------------------------------------------------------------
    // InputAdapter Overrides
    // -------------------------------------------------------------------------

    MutableFloat roomAlpha = new MutableFloat(1);
    boolean transition = false;

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
//            game.setScreen(Constants.player_select_screen);
            GameInstance.exit();
        } else if (keycode == Input.Keys.SPACE) {
            if  (transition) return true;
            else transition = true;

            final Rectangle targetBounds = level.getNextRoomBounds();
            Timeline.createParallel()
                    .push(Tween.to(roomAlpha, -1, 1)
                               .target(0.0f)
                               .ease(Circ.OUT)
                               .setCallback(new TweenCallback() {
                                   @Override
                                   public void onEvent(int type, BaseTween<?> source) {
                                       level.nextRoom();
                                   }
                               }))
                    .push(Tween.to(bgColor, ColorAccessor.RGB, 1)
                               .target(0,0,0)
                               .ease(Circ.OUT))
                    .push(Tween.to(camera.position, Vector3Accessor.XY, 1)
                               .target(targetBounds.x + targetBounds.width / 2f,
                                       targetBounds.y + targetBounds.height / 2f)
                               .ease(Expo.INOUT)
                               .delay(0.5f))
                    .push(Tween.to(roomAlpha, -1, 0.5f)
                               .target(1)
                               .ease(Circ.IN)
                               .delay(1)
                               .setCallback(new TweenCallback() {
                                   @Override
                                   public void onEvent(int type, BaseTween<?> source) {
                                       transition = false;
                                   }
                               }))
                    .push(Tween.to(bgColor, ColorAccessor.RGB, 1)
                               .target(0.43f, 0.43f, 0.43f)
                               .delay(1))
                    .start(GameInstance.tweens);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        ui.resize(width, height);
    }

    @Override
    public void show() {
        enableInput();
    }

    @Override
    public void hide() {
        disableInput();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        ui.dispose();
    }


    // -------------------------------------------------------------------------
    // Implementation Methods
    // -------------------------------------------------------------------------

    private void enableInput() {
        OrthoCamController camController = new OrthoCamController(camera);
        camController.camera_zoom.setValue(0.5f);
        camController.scrolled(0);

        GameInstance.input.reset();
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        inputMux.addProcessor(camController);
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    private void disableInput() {
        GameInstance.input.reset();
        Gdx.input.setInputProcessor(GameInstance.input);
    }

    // TODO : extract to UpdatingScreen?
    private void updateMouseVectors(Camera camera) {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenCoords.set(mx, my, 0);
        mouseWorldCoords.set(mx, my, 0);
        camera.unproject(mouseWorldCoords);
    }

}
