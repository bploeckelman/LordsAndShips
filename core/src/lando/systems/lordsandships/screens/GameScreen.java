package lando.systems.lordsandships.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.scene.LevelGenerator;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.TileMap;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

import java.util.*;
import java.util.List;
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

	private static final float key_move_amount = 16;

	private TileMap tileMap;
	private OrthographicCamera camera;
	private OrthoCamController camController;
	private InputMultiplexer inputMux;
	private LevelGenerator.Settings settings;

	private Entity player;

	private long startTime = TimeUtils.nanoTime();

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.position.set(0,0,0);

		camController = new OrthoCamController(camera);

		inputMux = new InputMultiplexer();
		inputMux.addProcessor(camController);
		inputMux.addProcessor(game.input);
		Gdx.input.setInputProcessor(inputMux);

		// ***************** TESTING ****************
		settings = new LevelGenerator.Settings();
		settings.separationIterations = 10;
		settings.initialRooms = 200;
		settings.selectedRooms = 50;
		settings.widthMin = 3;
		settings.widthMax = 20;
		settings.heightMin = 4;
		settings.heightMax = 15;
		LevelGenerator.generateLevel(settings);
		tileMap = new TileMap(LevelGenerator.mst, LevelGenerator.selectedRooms);

		player = new Entity(
				Assets.atlas.findRegion("tile-box"),
				tileMap.spawnX * 16,
				tileMap.spawnY * 16,
				16, 16);
	}

	Vector3 mouseCoords = new Vector3();
	private void update(float delta) {
		if (game.input.isKeyDown(Input.Keys.ESCAPE)) {
			game.exit();
		}

		// ***************** TESTING ****************
		if (Gdx.input.justTouched()) {
			mouseCoords.set(game.input.getCurrMouse().x, game.input.getCurrMouse().y, 0);
			mouseCoords = camera.unproject(mouseCoords);
			player.boundingBox.x = mouseCoords.x;
			player.boundingBox.y = mouseCoords.y;
//			if (game.input.isKeyDown(Input.Keys.SHIFT_LEFT)) LevelGenerator.generateInitialRooms(settings);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_LEFT)) LevelGenerator.selectRooms(settings);
//			else if (game.input.isKeyDown(Input.Keys.ALT_LEFT)) LevelGenerator.generateRoomGraph(settings);
//			else if (game.input.isKeyDown(Input.Keys.SHIFT_RIGHT)) LevelGenerator.calculateMinimumSpanningTree(settings);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_RIGHT)) LevelGenerator.generateTilesFromRooms();
//			else {
//				LevelGenerator.separateInitialRooms(settings);
//			}
		}

		updateEntities(delta);

		camera.position.lerp(player.getPosition(), 4*delta);
		camera.update();
	}

	private void updateEntities(float delta) {
		updatePlayers(delta);
		resolveCollisions();
	}

	private void updatePlayers(float delta) {
		float dx, dy;

		if (game.input.isKeyDown(Input.Keys.A)) { dx = -key_move_amount; }
		else if (game.input.isKeyDown(Input.Keys.D)) { dx =  key_move_amount; }
		else {
			dx = 0f;
			player.velocity.x = 0f;
		}

		if (game.input.isKeyDown(Input.Keys.W)) { dy =  key_move_amount; }
		else if (game.input.isKeyDown(Input.Keys.S)) { dy = -key_move_amount; }
		else {
			dy = 0f;
			player.velocity.y = 0f;
		}

		player.velocity.x += dx;
		player.velocity.y += dy;

		player.update(delta);
	}

	List<TileMap.Tile> collisionTiles = new ArrayList<TileMap.Tile>(10);
	Rectangle tileRect = new Rectangle();
	Rectangle intersection = new Rectangle();
	private void resolveCollisions() {
		// Get grid tiles that the player overlaps
		tileMap.getCollisionTiles(player, collisionTiles);

		// For each overlapped blocking tile:
		for (TileMap.Tile tile : collisionTiles) {
			if (!tileMap.isBlocking(tile.getGridX(), tile.getGridY())) {
				tileRect.set(0,0,0,0);
				intersection.set(0,0,0,0);
				continue;
			}

			// find amount of overlap on each axis
			tileRect.set(tile.getWorldMinX(), tile.getWorldMinY(), 16f, 16f);
			if (player.boundingBox.overlaps(tileRect)) {
				Intersector.intersectRectangles(player.boundingBox, tileRect, intersection);
				// Move out of shallower overlap axis
				if (intersection.width < intersection.height) {
					// Move out of X axis first..
					if (player.boundingBox.x <= tileRect.x + tileRect.width
					 && player.boundingBox.x >= tileRect.x) {
						player.boundingBox.x = tileRect.x + tileRect.width + 0.01f;
						player.velocity.x = 0f;
					} else if (player.boundingBox.x + player.boundingBox.width >= tileRect.x
							&& player.boundingBox.x <= tileRect.x) {
						player.boundingBox.x = tileRect.x - player.boundingBox.width - 0.01f;
						player.velocity.x = 0f;
					}
				} else {
					// Move out of Y axis first..
					if (player.boundingBox.y <= tileRect.y + tileRect.height
					 && player.boundingBox.y >= tileRect.y) {
						player.boundingBox.y = tileRect.y + tileRect.height + 0.01f;
						player.velocity.y = 0f;
					} else if (player.boundingBox.y + player.boundingBox.height >= tileRect.y
							&& player.boundingBox.y <= tileRect.y) {
						player.boundingBox.y = tileRect.y - player.boundingBox.height - 0.01f;
						player.velocity.y = 0f;
					}
				}
			} else {
				intersection.set(0, 0, 0, 0);
			}
		}
		// resolve collision:
		// move player out on shallowest axis by overlap amount on that axis
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0.08f,0.04f,0.0f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		tileMap.render(camera);

		if (camController.debugRender) {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			LevelGenerator.debugRender(camera);
		}

		Assets.batch.begin();
		player.render(Assets.batch);
		Assets.batch.end();

		if (camController.debugRender) {
			float minx = Float.MAX_VALUE;
			float miny = Float.MAX_VALUE;
			float maxx = Float.MIN_VALUE;
			float maxy = Float.MIN_VALUE;
			for (TileMap.Tile t : collisionTiles) {
				if (minx > t.getWorldMinX()) minx = t.getWorldMinX();
				if (miny > t.getWorldMinY()) miny = t.getWorldMinY();
				if (maxx < t.getWorldMaxX()) maxx = t.getWorldMaxX();
				if (maxy < t.getWorldMaxY()) maxy = t.getWorldMaxY();
			}
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Assets.shapes.setProjectionMatrix(camera.combined);
			Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
			Assets.shapes.setColor(0, 1, 0, 0.5f);
			Assets.shapes.rect(minx, miny, maxx - minx, maxy - miny);
			Assets.shapes.setColor(1, 0, 0, 0.75f);
			Assets.shapes.rect(intersection.x, intersection.y, intersection.width, intersection.height);
			Assets.shapes.end();
			Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
			Assets.shapes.setColor(1, 0, 1, 1);
			Assets.shapes.rect(player.boundingBox.x, player.boundingBox.y, player.boundingBox.width, player.boundingBox.height);
			Assets.shapes.end();
		}

		if (TimeUtils.nanoTime() - startTime >= 1000000000) {
			System.out.println("fps( " + Gdx.graphics.getFramesPerSecond() + " )");
			startTime = TimeUtils.nanoTime();
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
		camera.position.set(player.boundingBox.x, player.boundingBox.y, 0);
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
