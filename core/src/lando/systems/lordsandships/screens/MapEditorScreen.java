package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.editor.MapEditorUI;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * Brian Ploeckelman created on 3/2/2015.
 */
public class MapEditorScreen extends InputAdapter implements UpdatingScreen {

    private final GameInstance game;

    private Vector3 mouseWorldCoords  = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();

    private OrthographicCamera camera;
    private Color              backgroundColor;

    private MapEditorUI ui;

    public MapEditorScreen(GameInstance game) {
        super();

        this.game = game;

        Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("images/cursor2.png"));
        Gdx.input.setCursorImage(cursorPixmap, 8, 8);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width, Constants.win_half_height, 0);
        camera.update();

        backgroundColor = new Color(1,1,1,1);

        ui = new MapEditorUI();

        enableInput();
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();
        ui.update(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glClearColor(backgroundColor.r,
                            backgroundColor.g,
                            backgroundColor.b,
                            backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final SpriteBatch batch = Assets.batch;
        batch.setProjectionMatrix(camera.combined);

        ui.render(batch, camera);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            game.setScreen(Constants.player_select_screen);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.set(0, 0, 0);
        camera.translate(width / 2, height / 2, 0);
        camera.update();
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
    // Implementation Details
    // -------------------------------------------------------------------------

    private void enableInput() {
        GameInstance.input.reset();
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    private void disableInput() {
        GameInstance.input.reset();
        Gdx.input.setInputProcessor(GameInstance.input);
    }

    private void updateMouseVectors() {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenCoords.set(mx, my, 0);
        mouseWorldCoords.set(mx, my, 0);
        camera.unproject(mouseWorldCoords);
    }

}
