package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
	private final LordsAndShips game;

	private OrthographicCamera camera;
	private float r = 0.f, g = 0.f, b = 0.f;
	private float THRESHOLD = 0.016f;
	private float accum = 0.f;

	public TitleScreen(LordsAndShips game) {
		super();

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
	}

	@Override
	public void render(float delta) {
		if (game.input.isKeyDown(Keys.ESCAPE)) {
			Gdx.app.exit();
		} else if (Gdx.input.justTouched()) {
//			game.setScreen(game.gameScreen);
		}

		Gdx.gl.glClearColor(r,g,b,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tick...
		accum += delta;
		if (accum > THRESHOLD) {
			accum = 0;

			r += Assets.rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;
			g += Assets.rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;
			b += Assets.rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;

			if (r > 1.f) r = 1.f;
			if (g > 1.f) g = 1.f;
			if (b > 1.f) b = 1.f;
		}

		Assets.batch.begin();
		Assets.batch.draw(Assets.libgdx
				, Constants.win_half_width  - Assets.libgdx.getWidth()  / 2
				, Constants.win_half_height - Assets.libgdx.getHeight() / 2);
		Assets.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
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
