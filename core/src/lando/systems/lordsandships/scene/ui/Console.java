package lando.systems.lordsandships.scene.ui;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.*;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.Vector2Accessor;

import java.util.ArrayList;
import java.util.List;


/**
 * Brian Ploeckelman created on 12/17/2014.
 */
public class Console implements TextField.TextFieldListener {

    private static final int num_lines = 6;
    private static final float margin = 10f;
    private static final String title = "Console";

    int numLines;
    int currentLine;
    Label[] textLabels;
    TextField inputField;

    List<CVar> vars;

    Vector2 pos;
    Vector2 size;
    Window window;
    MutableFloat consoleAlpha;

    Stage stage;
    Skin skin;

    boolean visible;

    public Console(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        visible = false;

        currentLine = 0;
        numLines = num_lines;
        textLabels = new Label[numLines];

        inputField = new TextField("", skin);
        inputField.setTextFieldListener(this);
        inputField.setColor(0.1f, 0.1f, 0.1f, 1.0f);

        for (int i = 0; i < numLines; ++i) {
            textLabels[i] = new Label(" ", skin);
            textLabels[i].setZIndex(1);
            textLabels[i].setColor(Color.GRAY);
        }

        float text_height = inputField.getHeight();
        float padding = 2f;

        size = new Vector2(stage.getWidth(), (numLines) * (text_height + padding));
        pos = new Vector2(0, stage.getHeight());

        window = new Window(title, skin);
        window.setZIndex(0);
        window.setTitleAlignment(Align.center);
        window.setColor(Color.GRAY);
        window.setPosition(pos.x, pos.y);
        window.setSize(size.x, size.y);
        window.setKeepWithinStage(false);
        window.setMovable(false);
        window.setResizable(false);

        for (int i = numLines - 2; i >= 0; --i) {
            window.add(new Label("", skin)).width(margin);
            window.add(textLabels[i]).left()
                      .width(stage.getWidth() - 2 * margin)
                      .padTop(padding).padBottom(padding);
            window.add(new Label("", skin)).width(margin);
            window.row();
        }

        window.add(new Label("", skin)).width(margin);
        window.add(inputField).left()
                  .width(stage.getWidth() - 2 * margin)
                  .padTop(padding * 2).fill();
        window.add(new Label("", skin)).width(margin);
        window.row();

        stage.addActor(window);

        consoleAlpha = new MutableFloat(0f);

        initializeVars();
    }


    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            visible = !visible;
            float y = stage.getHeight() - (visible ? size.y : 0f);
            float a = visible ? 1f : 0f;
            float duration = visible ? 0.33f : 0.66f;
            Timeline.createParallel()
                    .push(
                        Tween.to(pos, Vector2Accessor.Y, duration)
                                .target(y)
                                .ease(Bounce.OUT)
                    )
                    .push(
                        Tween.to(consoleAlpha, 0, duration)
                                .target(a)
                                .ease(Bounce.OUT)
                    )
                    .start(GameInstance.tweens);
        }

        window.setPosition(pos.x, pos.y);
        window.setColor(1, 1, 1, consoleAlpha.floatValue());
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

        final String cmd = inputField.getText();
        final String[] tokens = cmd.split(" ");
        if (tokens.length == 0) return;

        // TODO (brian): need CCommand also

        for (CVar var : vars) {
            if (tokens[0].equals(var.key)) {
                if (tokens.length >= 2) {
                    var.value = tokens[1];
                }
                inputField.setText(var.key + " = " + var.value);
                break;
            }
        }
    }

    private void initializeVars() {
        vars = new ArrayList<CVar>();
        vars.add(new CVar("test"));
    }
}
