package lando.systems.lordsandships.scene.editor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/4/2015.
 */
public class TilePicker extends Window {

    private Array<Image> tiles;
    private Image selected;
    private ScrollPane scroll;

    public TilePicker(final String title,
                      final Skin skin,
                      final Stage stage,
                      final MapEditorUI.EditorConfig config) {
        super(title, skin);

        this.setSize(config.picker_width, config.picker_height);

        final Table table = new Table(skin);
        table.padLeft(config.picker_table_pad);
        table.padRight(config.picker_table_pad);
        table.bottom();

        tiles = new Array<Image>();
        final TextureRegion[][] textures = TextureRegion.split(Assets.oryxWorld, 24, 24);
        for (TextureRegion[] tileRow : textures) {
            for (TextureRegion tile : tileRow) {
                final Image image = new Image(tile);
                image.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (event.getButton() == Input.Buttons.LEFT) {
                            selected = image;
                            scroll.setCancelTouchFocus(true);
                            event.stop();
                        }
                    }
                });
                tiles.add(image);

                table.add(image)
                     .size(config.cell_height)
                     .fill()
                     .space(10);
            }
        }

        scroll = new ScrollPane(table);
        scroll.setFillParent(true);
        scroll.setScrollingDisabled(false, true);
        scroll.setScrollBarPositions(false, false);
        scroll.setSmoothScrolling(true);
        scroll.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event,
                              float x,
                              float y,
                              int pointer,
                              Actor fromActor) {
                stage.setScrollFocus(scroll);
            }

            public void exit(InputEvent event,
                             float x,
                             float y,
                             int pointer,
                             Actor toActor) {
                stage.setScrollFocus(null);
            }
        });

        this.add(scroll);
    }

    public void resize(float width, float height) {
        this.setWidth(width);
    }

    public Image getSelected() {
        return selected;
    }

}
