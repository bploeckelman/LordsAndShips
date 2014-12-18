package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;


/**
 * Brian Ploeckelman created on 12/17/2014.
 */
public class Console implements TextInputListener {

    private static final int num_lines = 6;
    private static final float margin_left = 20f;
    private static final String title = "Console";

    int numLines;
    int currentLine;
    String[] textLines;
    TextField[] textFields;

    Vector2 pos;
    Vector2 size;
    Window window;

    Stage stage;
    Skin skin;

    public Console(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        currentLine = 0;
        numLines = num_lines;
        textLines = new String[numLines];
        textFields = new TextField[numLines];

        for (int i = 0; i < numLines; ++i) {
            textLines[i] = "foo:" + i;
            textFields[i] = new TextField(textLines[i], skin);
            textFields[i].setZIndex(1);
            textFields[i].setDisabled(i != 0);
            if (textFields[i].isDisabled())
                textFields[i].setColor(Color.GRAY);
            else
                textFields[i].setColor(Color.LIGHT_GRAY);

        }

        float text_height = textFields[0].getHeight();
        float padding = 2f;

        size = new Vector2(stage.getWidth(), (numLines + 1) * (text_height + padding));
        pos = new Vector2(0, stage.getHeight() - size.y);

        window = new Window(title, skin);
        window.setZIndex(0);
        window.setTitleAlignment(Align.center);
        window.setPosition(pos.x, pos.y);
        window.setSize(size.x, size.y);
        window.setColor(Color.GRAY);

        for (int i = numLines - 1; i >= 0; --i) {
            TextField field = textFields[i];
            window.row();
            window.add(field).width(size.x - (padding * 2))
                  .padTop(padding).padBottom(padding)
                  .align(Align.left).fill();
        }

        stage.addActor(window);
    }


    public void update(float dt) {

    }

    @Override
    public void input(String text) {
        Gdx.app.log("foo", "text: " + text);
    }

    @Override
    public void canceled() {
        Gdx.app.log("foo", "cxl");
    }
}
