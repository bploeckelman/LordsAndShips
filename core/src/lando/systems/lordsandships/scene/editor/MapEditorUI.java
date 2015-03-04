package lando.systems.lordsandships.scene.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/2/2015.
 */
public class MapEditorUI {

    Skin  skin;
    Stage stage;

    class EditorConfig {
        public static final float default_tile_size = 24;
        public static final float default_tile_zoom = 4/3;
        public static final float default_tile_pad  = 6;

        public float tile_size = default_tile_size;
        public float tile_zoom = default_tile_zoom;
        public float tile_pad  = default_tile_pad;
        public float picker_width = 200; //4 * (tile_size + tile_pad) * tile_zoom;
    }


    public MapEditorUI() {
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        EditorConfig config = new EditorConfig();
        initializeWidgets(config);
    }

    // -------------------------------------------------------------------------

    public void update(float delta) {
        stage.act(delta);
    }

    public void render(SpriteBatch batch, Camera camera) {
        Gdx.gl20.glViewport(0,
                            0,
                            (int) camera.viewportWidth,
                            (int) camera.viewportHeight);
        stage.draw();
        stage.setDebugUnderMouse(true);
    }

    // -------------------------------------------------------------------------

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    // -------------------------------------------------------------------------

    public Stage getStage() {
        return stage;
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private void initializeWidgets(EditorConfig config) {
        TextureRegion[][] tileRegions = TextureRegion.split(Assets.oryxWorld, 16, 16);

        float cell_width = config.tile_size * config.tile_zoom + config.tile_pad;
        int   cells_per_row = (int) Math.floor(config.picker_width / cell_width);

        int   cell  = 0;
        Image image = null;

        Table tileTable = new Table(skin);
        tileTable.debug();
        for (int y = 0; y < tileRegions.length; ++y) {
            for (int x = 0; x < tileRegions[0].length; ++x) {
                image = new Image(tileRegions[y][x]);
                image.setSize(config.tile_size, config.tile_size);
                image.setScale(config.tile_zoom);

                tileTable.add(image).fill().expand().pad(config.tile_pad);

                if (++cell == cells_per_row) {
                    cell = 0;
                    tileTable.row();
                }
            }
        }
        ScrollPane pickerScroll = new ScrollPane(tileTable);
        pickerScroll.setFillParent(true);
        pickerScroll.setScrollbarsOnTop(true);
        pickerScroll.setScrollingDisabled(true, false);

        Window picker = new Window("Tile Picker", skin);
        picker.setResizable(true);
        picker.setSize(config.picker_width, stage.getHeight());
        picker.add(pickerScroll).fill().expand();

        stage.addActor(picker);
    }

}
