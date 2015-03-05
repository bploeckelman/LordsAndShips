package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.scene.OrthoCamController;
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

    private Drawable[][] mapTiles;

    boolean isPainting = false;

    final float map_tile_size  = 32;
    final int   map_tiles_wide = 200;
    final int   map_tiles_high = 125;

    public MapEditorScreen(GameInstance game) {
        super();

        this.game = game;

        Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("images/cursor2" +
                                                            ".png"));
        Gdx.input.setCursorImage(cursorPixmap, 8, 8);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width,
                         Constants.win_half_height,
                         0);
        camera.update();

        backgroundColor = new Color(0.9f, 0.9f, 0.9f, 1);

        ui = new MapEditorUI();

        Texture blankTex = new Texture("images/tile-floor1.png");
        TextureRegion blankReg = new TextureRegion(blankTex);
        TextureRegionDrawable blank = new TextureRegionDrawable(blankReg);
        mapTiles = new Drawable[map_tiles_high][map_tiles_wide];
        for (int y = 0; y < map_tiles_high; ++y) {
            for (int x = 0; x < map_tiles_wide; ++x) {
                mapTiles[y][x] = blank;
            }
        }

        enableInput();
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();
        camera.update();
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
        batch.begin();
        for (int y = 0; y < map_tiles_high; ++y) {
            for (int x = 0; x < map_tiles_wide; ++x) {
                mapTiles[y][x].draw(batch,
                                    x * map_tile_size,
                                    y * map_tile_size,
                                    map_tile_size,
                                    map_tile_size);
            }
        }
        batch.end();

        final ShapeRenderer shapes = Assets.shapes;
        shapes.setColor(0, 0, 0, 1);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int y = 0; y < map_tiles_high; ++y) {
            float py = map_tile_size * y;
            shapes.line(0, py, map_tile_size * map_tiles_wide, py);
        }
        for (int x = 0; x < map_tiles_wide; ++x) {
            float px = map_tile_size * x;
            shapes.line(px, 0, px, map_tile_size * map_tiles_high);
        }
        shapes.end();

        ui.render(batch, camera);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            game.setScreen(Constants.player_select_screen);
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            int ix = (int) (mouseWorldCoords.x / map_tile_size);
            int iy = (int) (mouseWorldCoords.y / map_tile_size);

            Image selectedTile = ui.getSelectedTile();
            if (selectedTile != null
             && ix >= 0 && ix < map_tiles_wide
             && iy >= 0 && iy < map_tiles_high) {
                isPainting = true;
                mapTiles[iy][ix] = selectedTile.getDrawable();
            }
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            isPainting = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Image selectedTile = ui.getSelectedTile();
        if (isPainting && selectedTile != null) {
            int ix = (int) (mouseWorldCoords.x / map_tile_size);
            int iy = (int) (mouseWorldCoords.y / map_tile_size);

            if (ix >= 0 && ix < map_tiles_wide
             && iy >= 0 && iy < map_tiles_high) {
                mapTiles[iy][ix] = selectedTile.getDrawable();
            }
        }
        return false;
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

    private void updateMouseVectors() {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenCoords.set(mx, my, 0);
        mouseWorldCoords.set(mx, my, 0);
        camera.unproject(mouseWorldCoords);
    }

}
