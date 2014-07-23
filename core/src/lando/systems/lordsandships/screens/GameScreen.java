package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.scene.levelgen.LevelGenParams;
import lando.systems.lordsandships.scene.levelgen.LevelGenerator;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.TileMap;
import lando.systems.lordsandships.scene.particles.ExplosionEmitter;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

import java.util.*;
import java.util.List;

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
	private static final float camera_shake_scale = 1.5f;

	private TileMap tileMap;
	private OrthographicCamera camera;
	private OrthographicCamera uiCamera;
	private OrthoCamController camController;
	private BitmapFont font;
	private Vector3 temp = new Vector3();

	private Player player;
	private Array<Enemy> enemies;

	private ExplosionEmitter explosionEmitter = new ExplosionEmitter();

	private long startTime = TimeUtils.nanoTime();

	public GameScreen(LordsAndShips game) {
		super();

		this.game = game;

		font = new BitmapFont(Gdx.files.internal("fonts/tolkien.fnt"), false);

		Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("images/cursor.png"));
		Gdx.input.setCursorImage(cursorPixmap, 8, 8);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.position.set(0,0,0);
		camera.update();

		uiCamera = new OrthographicCamera();
		uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		uiCamera.update();

		camController = new OrthoCamController(camera);

		InputMultiplexer inputMux = new InputMultiplexer();
		inputMux.addProcessor(camController);
		inputMux.addProcessor(game.input);
		Gdx.input.setInputProcessor(inputMux);

		// ***************** TESTING ****************
		LevelGenParams params = new LevelGenParams();
		params.numInitialRooms = 200;
		params.numSelectedRooms = 50;
		params.roomWidthMin = 3;
		params.roomWidthMax = 20;
		params.roomHeightMin = 4;
		params.roomHeightMax = 15;

		LevelGenerator.generateLevel(params);
		tileMap = new TileMap(LevelGenerator.mst, LevelGenerator.selectedRooms);

		player = new Player(
				Assets.playertex,
				tileMap.spawnX * 16,
				tileMap.spawnY * 16,
				16, 16, 0.1f);

		enemies = new Array<Enemy>(50);
	}

	Vector3 playerPosition = new Vector3();
	Vector3 mouseCoords = new Vector3();
	private void update(float delta) {
		game.tween.update(delta);

		if (game.input.isKeyDown(Input.Keys.ESCAPE)) {
			game.exit();
		}

		mouseCoords.set(game.input.getCurrMouse().x, game.input.getCurrMouse().y, 0);
		mouseCoords = camera.unproject(mouseCoords);

		if (Gdx.input.justTouched() && Gdx.input.isKeyPressed(Input.Keys.F)) {
			enemies.add(new Enemy(Assets.enemytex, mouseCoords.x, mouseCoords.y, 16, 24, 0.3f));
		}

		// ***************** TESTING ****************
		if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
			player.boundingBox.x = mouseCoords.x;
			player.boundingBox.y = mouseCoords.y;
//			if (game.input.isKeyDown(Input.Keys.SHIFT_LEFT)) LevelGenerator.generateInitialRooms(params);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_LEFT)) LevelGenerator.selectRooms(params);
//			else if (game.input.isKeyDown(Input.Keys.ALT_LEFT)) LevelGenerator.generateRoomGraph(params);
//			else if (game.input.isKeyDown(Input.Keys.SHIFT_RIGHT)) LevelGenerator.calculateMinimumSpanningTree(params);
//			else if (game.input.isKeyDown(Input.Keys.CONTROL_RIGHT)) LevelGenerator.generateTilesFromRooms();
//			else {
//				LevelGenerator.separateInitialRooms(params);
//			}
		}

		updateEntities(delta);

		playerPosition.set(player.getPosition().x, player.getPosition().y, 0);
		camera.position.lerp(playerPosition, 4*delta);

		camera.update();
	}

	private void updateEntities(float delta) {
		updatePlayers(delta);
		resolveCollisions();
		for (Enemy enemy : enemies) {
			if (!enemy.isAlive()) continue;
			enemy.update(delta);
		}
	}

	// TODO : too many temp vectors
	Vector3 mouse = new Vector3();
	Vector2 dir = new Vector2();
	// TODO : move this stuff
	boolean isSlashing = false;
	Color slashColor = new Color(1,1,1,0);
	float slashRotation = 0;
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

		if (game.input.isButtonDown(Input.Buttons.LEFT) && !game.input.isKeyDown(Input.Keys.F)) {
			// TODO : unproject mouse coords every frame and reference that value here
			mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(mouse);
			dir.set(player.getDirection(mouse.x, mouse.y));

			player.attack(dir, game);

			// TODO : move to weapon
//			player.shoot(dir);
//			player.punch();

			// displace the camera a bit
			dir.scl(-1);
			camera.translate(dir);
		}

		player.velocity.x += dx;
		player.velocity.y += dy;

		player.update(delta);
	}

	List<TileMap.Tile> collisionTiles = new ArrayList<TileMap.Tile>(10);
	Rectangle tileRect = new Rectangle();
	Rectangle intersection = new Rectangle();
	private void resolveCollisions() {
		// Resolve bullet collisions
		for (Bullet bullet : player.getBullets()) {
			if (bullet.isAlive()) {
				// Check the bullet against the map
				tileMap.getCollisionTiles(bullet, collisionTiles);
				for (TileMap.Tile tile : collisionTiles) {
					if (tileMap.isBlocking(tile.getGridX(), tile.getGridY())) {
						Assets.gunshot_impact.play(0.05f);
						bullet.kill();
					}
				}

				// Check the bullet against enemies
				if (bullet.isAlive()) {
					for (Enemy enemy : enemies) {
						if (!enemy.isAlive()) continue;

						if (Intersector.overlaps(bullet.boundingBox, enemy.boundingBox)) {
							enemy.takeDamage(bullet.damageAmount, bullet.velocity);
							Assets.getRandomHitSound().play();
							if (!enemy.isAlive()) {
								explosionEmitter.addSmallExplosion(enemy.position);
							}
							bullet.kill();
						}
					}
				}
			}
		}

		for (Enemy enemy : enemies) {
			if (!enemy.isAlive()) continue;
			resolveCollisions(enemy);
		}

		resolveCollisions(player);
	}

	private void resolveCollisions(Entity entity) {
		// Get grid tiles that the entity overlaps
		tileMap.getCollisionTiles(entity, collisionTiles);

		// For each overlapped blocking tile:
		for (TileMap.Tile tile : collisionTiles) {
			if (!tileMap.isBlocking(tile.getGridX(), tile.getGridY())) {
				tileRect.set(0,0,0,0);
				intersection.set(0,0,0,0);
				continue;
			}

			final float bounds_feather = 0.0075f;

			// find amount of overlap on each axis
			tileRect.set(tile.getWorldMinX(), tile.getWorldMinY(), 16f, 16f);
			if (entity.boundingBox.overlaps(tileRect)) {
				Intersector.intersectRectangles(entity.boundingBox, tileRect, intersection);
				// Move out of shallower overlap axis
				if (intersection.width < intersection.height) {
					// Move out of X axis first..
					if (entity.boundingBox.x <= tileRect.x + tileRect.width
					 && entity.boundingBox.x >= tileRect.x) {
						entity.boundingBox.x = tileRect.x + tileRect.width + bounds_feather;
						entity.velocity.x = 0f;
					} else if (entity.boundingBox.x + entity.boundingBox.width >= tileRect.x
							&& entity.boundingBox.x <= tileRect.x) {
						entity.boundingBox.x = tileRect.x - entity.boundingBox.width - bounds_feather;
						entity.velocity.x = 0f;
					}
				} else {
					// Move out of Y axis first..
					if (entity.boundingBox.y <= tileRect.y + tileRect.height
					 && entity.boundingBox.y >= tileRect.y) {
						entity.boundingBox.y = tileRect.y + tileRect.height + bounds_feather;
						entity.velocity.y = 0f;
					} else if (entity.boundingBox.y + entity.boundingBox.height >= tileRect.y
							&& entity.boundingBox.y <= tileRect.y) {
						entity.boundingBox.y = tileRect.y - entity.boundingBox.height - bounds_feather;
						entity.velocity.y = 0f;
					}
				}
			} else {
				intersection.set(0, 0, 0, 0);
			}
		}
		// resolve collision:
		// move entity out on shallowest axis by overlap amount on that axis
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
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

		Assets.batch.setProjectionMatrix(camera.combined);
		Assets.batch.begin();

		for (Enemy enemy : enemies) {
			if (!enemy.isAlive()) continue;
			enemy.render(Assets.batch);
		}

		Assets.shapes.setProjectionMatrix(camera.combined);
		player.render(Assets.batch);

		explosionEmitter.render(Assets.batch);

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
			startTime = TimeUtils.nanoTime();
		}

		Gdx.gl20.glViewport(0, 0, (int) uiCamera.viewportWidth, (int) uiCamera.viewportHeight);
		String line1 = "Hold 'F' + Left Click to spawn 'enemy'";
		String line2 = "FPS: " + Gdx.graphics.getFramesPerSecond();
		Assets.batch.setProjectionMatrix(uiCamera.combined);
		Assets.batch.begin();
		font.setScale(0.5f);
		font.setColor(Color.WHITE);
		font.draw(Assets.batch, line1, 20, Gdx.graphics.getHeight() - 20);
		font.draw(Assets.batch, line2, 20, Gdx.graphics.getHeight() - 20 - font.getLineHeight() - 5);
		Assets.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
		camera.position.set(player.boundingBox.x, player.boundingBox.y, 0);
		camera.update();

		uiCamera.setToOrtho(false, width, height);
		uiCamera.update();
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
		explosionEmitter.dispose();
		font.dispose();
	}
}
