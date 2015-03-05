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

    Window tilePicker;

    class EditorConfig {

        public float picker_table_pad = 10;
        public float picker_height = 100;
        public float cell_height = picker_height - 30;

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
//        stage.setDebugUnderMouse(true);
    }

    // -------------------------------------------------------------------------

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        tilePicker.setWidth(width);
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
        TextureRegion[][] tiles = TextureRegion.split(Assets.oryxWorld, 24, 24);

        Table table = new Table(skin);
        table.padLeft(config.picker_table_pad);
        table.padRight(config.picker_table_pad);
        table.bottom();

        for (TextureRegion[] tileRow : tiles) {
            for (TextureRegion tile : tileRow) {
                Image image = new Image(tile);

                table.add(image)
                     .size(config.cell_height)
                     .fill()
                     .space(10);
            }
        }

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFillParent(true);
        scroll.setScrollingDisabled(false, true);
        scroll.setScrollBarPositions(false, false);
        scroll.setSmoothScrolling(true);

        tilePicker = new Window("Tile Picker", skin);
        tilePicker.setSize(stage.getWidth(), config.picker_height);
        tilePicker.add(scroll);

        stage.addActor(tilePicker);
    }

}
