package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * TitleScreen
 *
 * The game's title screen
 *
 * Brian Ploeckelman created on 5/27/2014.
 */
public class TitleScreen implements Screen {
	private static final String title1 = "BattleLord Spaceships";
	private static final String title2 = "         vs";
	private static final String title3 = "SpaceLord Battleships";

	private final LordsAndShips game;

	private BitmapFont font;
	private OrthographicCamera camera;
	private float r = 0.f, g = 0.f, b = 0.f;

	public TitleScreen(LordsAndShips game) {
		super();

		this.game = game;

		font = new BitmapFont(Gdx.files.internal("fonts/jupiter.fnt"), false);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.update();
	}

	public void update(float delta) {
		if (game.input.isKeyDown(Keys.ESCAPE)) {
			game.exit();
		} else if (Gdx.input.justTouched()) {
			game.gameScreen = new GameScreen(game);
			game.setScreen(game.gameScreen);
		}

		camera.update();
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(r,g,b,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Assets.batch.setProjectionMatrix(camera.combined);
		Assets.batch.begin();
		Assets.batch.draw(Assets.gametex
				, Constants.win_half_width - Assets.gametex.getRegionWidth() / 2
				, Constants.win_half_height - Assets.gametex.getRegionHeight() / 2);
		font.setColor(Color.WHITE);
		font.setScale(2);
		font.drawMultiLine(Assets.batch, title1, 150, Gdx.graphics.getHeight() - 50);
		font.drawMultiLine(Assets.batch, title2, 150, Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 2) + (font.getLineHeight() / 2));
		font.drawMultiLine(Assets.batch, title3, 150, font.getLineHeight() + 50);
		Assets.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
		camera.update();
	}

	@Override
	public void show() {
		game.input.reset();
	}

	@Override
	public void hide() {
		game.input.reset();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
