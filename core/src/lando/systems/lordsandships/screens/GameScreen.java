package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.scene.LevelGenerator;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.TileMap;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * GameScreen
 *
 * The main game screen
 *
 * Brian Ploeckelman created on 5/28/2014.
 */
public class GameScreen implements Screen {
	private final LordsAndShips game;

	private static final float key_move_amount = 128f;

	private TileMap tileMap;
	private OrthographicCamera camera;
	private OrthoCamController camController;
	private InputMultiplexer inputMux;
	private LevelGenerator.Settings settings;

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;
//		this.tileMap = new TileMap(40, 25);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.position.set(15 * 32 / 2, 10 * 32 / 2, 0);

		camController = new OrthoCamController(camera);

		inputMux = new InputMultiplexer();
		inputMux.addProcessor(camController);
		inputMux.addProcessor(game.input);
		Gdx.input.setInputProcessor(inputMux);

		// ***************** TESTING ****************
		settings = new LevelGenerator.Settings();
		settings.separationIterations = 500;
		settings.initialRooms = 100;
		settings.selectedRooms = 15;
		settings.widthMin = 3;
		settings.widthMax = 15;
		settings.heightMin = 4;
		settings.heightMax = 15;
//		LevelGenerator.generateInitialRooms(settings);
		LevelGenerator.generateLevel(settings);
		tileMap = new TileMap(LevelGenerator.mst, LevelGenerator.selectedRooms);
	}

	private void update(float delta) {
		if (game.input.isKeyDown(Input.Keys.ESCAPE)) {
			game.exit();
		}

		// ***************** TESTING ****************
		if (Gdx.input.justTouched()) {
//			if (game.input.isKeyDown(Input.Keys.SHIFT_LEFT)) LevelGenerator.generateInitialRooms(settings);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_LEFT)) LevelGenerator.selectRooms(settings);
//			else if (game.input.isKeyDown(Input.Keys.ALT_LEFT)) LevelGenerator.generateRoomGraph(settings);
//			else if (game.input.isKeyDown(Input.Keys.SHIFT_RIGHT)) LevelGenerator.calculateMinimumSpanningTree(settings);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_RIGHT)) LevelGenerator.generateTilesFromRooms();
//			else {
//				LevelGenerator.separateInitialRooms(settings);
//			}
		}

		float dx = 0;
		float dy = 0;
		     if (game.input.isKeyDown(Input.Keys.A)) { dx = -key_move_amount * delta; }
		else if (game.input.isKeyDown(Input.Keys.D)) { dx =  key_move_amount * delta; }
		     if (game.input.isKeyDown(Input.Keys.W)) { dy =  key_move_amount * delta; }
		else if (game.input.isKeyDown(Input.Keys.S)) { dy = -key_move_amount * delta; }
		camera.position.add(dx, dy, 0);

		camera.update();
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// **************** TESTING ***************
		LevelGenerator.debugRender(camera);

//		Assets.batch.setProjectionMatrix(camera.combined);
//		Assets.batch.begin();
//		Assets.batch.draw(Assets.gametex, 0, 0, Constants.win_width, Constants.win_height);
//		Assets.batch.end();
//
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
