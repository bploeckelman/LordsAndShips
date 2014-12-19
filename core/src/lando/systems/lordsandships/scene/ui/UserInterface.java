package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 11/15/2014.
 */
public class UserInterface implements Disposable {

    final float margin_left   = 5;
    final float margin_right  = 5;
    final float margin_top    = 10;
    final float margin_bottom = 10;

    Skin skin;
    Stage stage;

    Image avatar;
    Label label;
    Window window;

    Console console;

    // -------------------------------------------------------------------------

    public UserInterface() {
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        initializeWidgets();
    }

    // -------------------------------------------------------------------------

    public void update(float delta) {
        console.update(delta);
        if (console.visible)
            stage.setKeyboardFocus(console.inputField);

        stage.act(delta);
    }

    public void draw() {
        stage.draw();
        stage.setDebugUnderMouse(true);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() { return stage; }

    // -------------------------------------------------------------------------

    private void initializeWidgets() {
        initializeButtons();
        initializeWindow();
        console = new Console(stage, skin);
    }

    private void initializeButtons() {
        // ...
    }

    private void initializeWindow() {
        label = new Label("Label", skin);

        window = new Window("Menu", skin);
        window.row();
        window.add(label).width(stage.getWidth()).align(Align.left);
        window.pack();
        window.setPosition(margin_left, stage.getHeight() - margin_top);
        window.setSize(stage.getWidth() - margin_left - margin_right, 256f);
        window.setTitleAlignment(Align.left);
//        window.pad(margin_top, margin_left, margin_bottom, margin_right);
        window.left();
        window.setZIndex(0);

        avatar = new Image(Assets.avatartex);
        avatar.setColor(1,1,1,0.5f);
        avatar.setScale(0.5f, 0.5f);
        avatar.setPosition(10, stage.getHeight() - avatar.getHeight() * 0.5f - 20);
        avatar.setZIndex(1);

        window.addActor(avatar);

//        stage.addActor(window);
//        stage.addActor(avatar);
    }
}
