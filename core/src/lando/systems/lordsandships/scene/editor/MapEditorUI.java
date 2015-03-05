package lando.systems.lordsandships.scene.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Brian Ploeckelman created on 3/2/2015.
 */
public class MapEditorUI {

    Skin  skin;
    Stage stage;

    TilePicker tilePicker;

    class EditorConfig {

        public float picker_table_pad = 10;
        public float picker_height = 100;
        public float picker_width  = 400;
        public float cell_height = picker_height - 30;

    }


    public MapEditorUI() {
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        EditorConfig config = new EditorConfig();
        config.picker_width = stage.getWidth();
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
//        stage.setDebugUnderMouse(true);
    }

    // -------------------------------------------------------------------------

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        tilePicker.resize(width, height);
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
        tilePicker = new TilePicker("Tile Picker", skin, config);
        stage.addActor(tilePicker);
    }

}
