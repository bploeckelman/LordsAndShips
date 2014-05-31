package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
	private InputMultiplexer inputMux;

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;
		this.tileMap = new TileMap(40, 25);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.position.set(15 * 32 / 2, 10 * 32 / 2, 0);
		camController = new OrthoCamController(camera);

		inputMux = new InputMultiplexer();
		inputMux.addProcessor(camController);
		inputMux.addProcessor(game.input);
		Gdx.input.setInputProcessor(inputMux);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		     if (game.input.isKeyDown(Input.Keys.A)) { camera.position.add(-1,0,0); }
		else if (game.input.isKeyDown(Input.Keys.D)) { camera.position.add( 1,0,0); }

		     if (game.input.isKeyDown(Input.Keys.W)) { camera.position.add(0, 1,0); }
		else if (game.input.isKeyDown(Input.Keys.S)) { camera.position.add(0,-1,0); }

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
		tileMap.dispose();
	}
}
