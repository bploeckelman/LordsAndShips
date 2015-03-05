package lando.systems.lordsandships.scene.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Brian Ploeckelman created on 3/2/2015.
 */
public class MapEditorUI {

    Skin  skin;
    Stage stage;

    EditorConfig config;

    TilePicker tilePicker;
    InfoWindow infoWindow;

    class EditorConfig {

        public float picker_table_pad = 10;
        public float picker_height    = 100;
        public float picker_width     = 400;
        public float cell_height      = picker_height - 30;

        public float info_height      = 720 - picker_height;
        public float info_width       = 300;
        public float info_pad         = 10;

    }


    public MapEditorUI() {
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());
//        stage.setDebugUnderMouse(true);

        config = new EditorConfig();
        config.picker_width = stage.getWidth();
        config.info_width   = stage.getWidth() / 5;
        config.info_height  = stage.getHeight() - config.picker_height;
        initializeWidgets(config);
    }

    // -------------------------------------------------------------------------

    public void update(float delta) {
        stage.act(delta);
        Image selected = tilePicker.getSelected();
        if (selected != null) {
            infoWindow.setSelected(selected.getDrawable());
        }
    }

    public void render(SpriteBatch batch, Camera camera) {
        Gdx.gl20.glViewport(0,
                            0,
                            (int) stage.getCamera().viewportWidth,
                            (int) stage.getCamera().viewportHeight);
        stage.draw();
    }

    // -------------------------------------------------------------------------

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        tilePicker.resize(width, height);
        infoWindow.resize(width, height);
    }

    public void dispose() {
        stage.dispose();
    }

    // -------------------------------------------------------------------------

    public Stage getStage() {
        return stage;
    }

    public Image getSelectedTile() {
        return tilePicker.getSelected();
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    private void initializeWidgets(EditorConfig config) {
        tilePicker = new TilePicker("Tile Picker", skin, stage, config);
        stage.addActor(tilePicker);

        infoWindow = new InfoWindow("Info", skin, config);
        stage.addActor(infoWindow);
    }

}
