package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * TitleScreen
 *
 * The game's title screen
 *
 * Brian Ploeckelman created on 5/27/2014.
 */
public class TitleScreen implements UpdatingScreen {

    private final GameInstance game;

    private BitmapFont font;
    private OrthographicCamera camera;
    private TextureRegion background;

    public TitleScreen(GameInstance game) {
        super();

        this.game = game;

        font = new BitmapFont(Gdx.files.internal("fonts/jupiter.fnt"), false);
        font.setColor(Color.WHITE);
        font.getData().setScale(2);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.update();

        background = new TextureRegion(Assets.titleScreen);
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            GameInstance.exit();
        }

        if (Gdx.input.justTouched()) {
            if (!GameInstance.screens.containsKey(Constants.test_screen)) {
                game.screens.put(Constants.test_screen, new TestScreen(game));
            }
            game.setScreen(Constants.test_screen);
        }

        camera.update();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float imageWidth    = background.getRegionWidth();
        float imageHeight   = background.getRegionHeight();
        float scaleWidth    = camera.viewportWidth  / imageWidth;
        float scaleHeight   = camera.viewportHeight / imageHeight;
        float scaleAmount   = Math.min(scaleWidth, scaleHeight);
        float adaptedWidth  = imageWidth * scaleAmount;
        float adaptedHeight = imageHeight * scaleAmount;
        float xPosition     = camera.viewportWidth  / 2f - adaptedWidth  / 2f;
        float yPosition     = camera.viewportHeight / 2f - adaptedHeight / 2f;

        Assets.batch.setProjectionMatrix(camera.combined);
        Assets.batch.begin();
        Assets.batch.draw(background, xPosition, yPosition, adaptedWidth, adaptedHeight);
        Assets.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    @Override
    public void show() {
        GameInstance.input.reset();
    }

    @Override
    public void hide() {
        GameInstance.input.reset();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {}

}
