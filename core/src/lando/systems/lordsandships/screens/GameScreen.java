package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * GameScreen
 *
 * The main game screen
 *
 * Brian Ploeckelman created on 5/28/2014.
 */
public class GameScreen implements Screen {
	private final LordsAndShips game;

	private OrthographicCamera camera;

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
	}

	@Override
	public void render(float delta) {
		Gdx.gl20.glClearColor(0,0,0,1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Assets.batch.setProjectionMatrix(camera.combined);
		Assets.batch.begin();
		Assets.batch.draw(Assets.gametex, 0, 0, Constants.win_width, Constants.win_height);
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
