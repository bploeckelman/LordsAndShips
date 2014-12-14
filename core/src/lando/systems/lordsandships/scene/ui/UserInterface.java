package lando.systems.lordsandships.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
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

    // -------------------------------------------------------------------------

    public UserInterface() {
        skin  = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        initializeWidgets();
    }

    // -------------------------------------------------------------------------

    public void update(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    // -------------------------------------------------------------------------

    private void initializeWidgets() {
        initializeButtons();
        initializeWindow();
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
        window.setSize(stage.getWidth() - margin_left - margin_right, 64f);
        window.setTitleAlignment(Align.left);
//        window.pad(margin_top, margin_left, margin_bottom, margin_right);
        window.left();

        avatar = new Image(Assets.avatartex);
        avatar.setScale(0.5f, 0.5f);
        avatar.setPosition(10, stage.getHeight() - avatar.getHeight() * 0.5f - 10);

        stage.addActor(avatar);

//        stage.addActor(window);
    }
}
