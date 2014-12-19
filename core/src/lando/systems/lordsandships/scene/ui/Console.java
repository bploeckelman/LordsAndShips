package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;


/**
 * Brian Ploeckelman created on 12/17/2014.
 */
public class Console implements TextField.TextFieldListener {

    private static final int num_lines = 6;
    private static final float margin_left = 20f;
    private static final String title = "Console";

    int numLines;
    int currentLine;
    Label[] textLabels;
    TextField inputField;

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
        textLabels = new Label[numLines];

        inputField = new TextField("", skin);
        inputField.setTextFieldListener(this);

        for (int i = 0; i < numLines; ++i) {
            textLabels[i] = new Label(" ", skin);
            textLabels[i].setZIndex(1);
            textLabels[i].setColor(Color.GRAY);
        }

        float text_height = inputField.getHeight();
        float padding = 2f;

        size = new Vector2(stage.getWidth(), (numLines + 1) * (text_height + padding));
        pos = new Vector2(0, stage.getHeight() - size.y);

        window = new Window(title, skin);
        window.setZIndex(0);
        window.setTitleAlignment(Align.center);
        window.setPosition(pos.x, pos.y);
        window.setSize(size.x, size.y);
        window.setColor(Color.GRAY);

        for (int i = numLines - 2; i >= 0; --i) {
            Label label = textLabels[i];
            window.row();
            window.add(label).width(size.x - (padding * 2))
                  .padTop(padding).padBottom(padding)
                  .fill();
        }

        window.row();
        window.add(inputField).width(size.x - (padding * 2))
              .padTop(padding * 2).padBottom(padding / 2)
              .fill();

        stage.addActor(window);
    }


    public void update(float dt) {

    }

    @Override
    public void keyTyped(TextField textField, char c) {
        if (c != '\r' && c != '\n')
            return;

        // Move each label text up a line
        for (int i = textLabels.length - 1; i > 0; --i) {
            textLabels[i].setText(textLabels[i - 1].getText());
        }

        processInput();

        final String text = inputField.getText().isEmpty() ? " " : inputField.getText();
        textLabels[0].setText(text);
        inputField.setText("");
    }

    private void processInput() {
        Gdx.app.log("INPUT", inputField.getText());
    }
}
