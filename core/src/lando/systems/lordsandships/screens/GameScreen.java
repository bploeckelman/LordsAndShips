package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.TileMap;
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

	private TileMap tileMap;
	private OrthographicCamera camera;
	private OrthoCamController camController;

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;
		this.tileMap = new TileMap(40, 25);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.position.set(15 * 32 / 2, 10 * 32 / 2, 0);
		camController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render(float delta) {
		Gdx.gl20.glClearColor(0,0,0,1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		Assets.batch.setProjectionMatrix(camera.combined);
		Assets.batch.begin();
		Assets.batch.draw(Assets.gametex, 0, 0, Constants.win_width, Constants.win_height);
		Assets.batch.end();

		tileMap.render(camera);
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
