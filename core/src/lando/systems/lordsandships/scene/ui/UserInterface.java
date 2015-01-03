package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import lando.systems.lordsandships.GameInstance;
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

    Arsenal arsenal;
    Console console;

    GameInstance game;

    // -------------------------------------------------------------------------

    public UserInterface(GameInstance game) {
        this.game = game;
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        arsenal = new Arsenal();

        initializeWidgets();
    }

    // -------------------------------------------------------------------------

    public void update(float delta) {
        console.update(delta);
        if (console.visible)
            stage.setKeyboardFocus(console.inputField);
        else
            stage.setKeyboardFocus(null);

        stage.act(delta);
    }

    public void render(SpriteBatch batch, Camera camera) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);

        stage.draw();
//        stage.setDebugUnderMouse(true);

        arsenal.render(batch, camera);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() { return stage; }

    public Console getConsole() { return console; }

    public Arsenal getArsenal() { return arsenal; }

    // -------------------------------------------------------------------------

    private void initializeWidgets() {
        initializeButtons();
        initializeWindow();
        console = new Console(game, stage, skin);
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
