package lando.systems.lordsandships.scene.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Brian Ploeckelman created on 3/4/2015.
 */
public class InfoWindow extends Window {

    Image selectedImage;

    public InfoWindow(String title, Skin skin, MapEditorUI.EditorConfig config) {
        super(title, skin);

        this.setMovable(false);
        this.setResizable(false);
        this.setSize(config.info_width, config.info_height);
        this.setPosition(0, config.picker_height);

        final Table table = new Table(skin);
        table.setFillParent(true);
        table.pad(config.info_pad);

        selectedImage = new Image(new Texture("images/tile-floor1.png"));
        table.add(selectedImage)
             .size(config.info_width - 2*config.info_pad)
             .colspan(2)
             .spaceBottom(20);
        table.row();

        table.add("Info 1a").spaceBottom(5);
        table.add("Info 1b");
        table.row();
        table.add("Info 2a").spaceBottom(5);
        table.add("Info 2b");
        table.row();
        table.add("Info 3a").spaceBottom(5);
        table.add("Info 3b");
        table.row();
        table.add("Info 4a").spaceBottom(5);
        table.add("Info 4b");
        table.row();

        this.add(table).expandY();
    }

    public void resize(float width, float height) {
        this.setHeight(Math.max(0, height - 100));
    }

    public void setSelected(Drawable drawable) {
        selectedImage.setDrawable(drawable);
    }
}
