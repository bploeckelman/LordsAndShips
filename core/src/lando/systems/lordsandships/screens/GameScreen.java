package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Cubic;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.Tile;
import lando.systems.lordsandships.scene.TileMap;
import lando.systems.lordsandships.scene.levelgen.LevelGenParams;
import lando.systems.lordsandships.scene.levelgen.Room;
import lando.systems.lordsandships.scene.levelgen.TinyDungeonGenerator;
import lando.systems.lordsandships.scene.particles.ExplosionEmitter;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;
import lando.systems.lordsandships.utils.graph.Graph;
import lando.systems.lordsandships.weapons.Handgun;
import lando.systems.lordsandships.weapons.Sword;
import lando.systems.lordsandships.weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GameScreen
 *
 * The main game screen
 *
 * Brian Ploeckelman created on 5/28/2014.
 */
public class GameScreen implements UpdatingScreen {
    private final GameInstance game;

    private static final float key_move_amount = 16;
    private static final float camera_shake_scale = 1.5f;

    private TinyDungeonGenerator dungeonGenerator;
    private TileMap tileMap;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private OrthoCamController camController;
    private BitmapFont font;

    private Vector2 temp2 = new Vector2();
    private Vector3 temp3 = new Vector3();
    private Vector3 playerPosition    = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();
    private Vector3 mouseWorldCoords  = new Vector3();

    private Player player;
    private Array<Enemy> enemies;

    private TextureRegion weaponIcon;
    private Vector2 weaponIconPos = new Vector2(30, 30);
    private Vector2 weaponIconSize = new Vector2(64, 64);

    private ExplosionEmitter explosionEmitter = new ExplosionEmitter();
    private Animation sparkle;
    private MutableFloat sparkle_accum = new MutableFloat(0);
    private boolean sparkling = false;
    private boolean generatingLevel = false;
    private final LevelGenParams params;

    public GameScreen(GameInstance game) {
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
        tileMap = new TileMap();

        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(camController);
        inputMux.addProcessor(game.input);
        Gdx.input.setInputProcessor(inputMux);

        params = new LevelGenParams();
        params.numInitialRooms = 200;
        params.numSelectedRooms = 50;
        params.roomWidthMin = 3;
        params.roomWidthMax = 20;
        params.roomHeightMin = 4;
        params.roomHeightMax = 15;

        dungeonGenerator = new TinyDungeonGenerator();

        player = new Player(
                Assets.playertex,
                100 * 16, 100 * 16,
//                tileMap.spawnX * 16,
//                tileMap.spawnY * 16,
                16, 16, 0.1f);

        if (player.getCurrentWeapon() instanceof Sword) {
            weaponIcon = Assets.atlas.findRegion("sword");
        } else if (player.getCurrentWeapon() instanceof  Handgun) {
            weaponIcon = Assets.atlas.findRegion("gun");
        }

        enemies = new Array<Enemy>(50);

        sparkle = new Animation(0.05f,
                Assets.atlas.findRegion("sparkle_small1"),
                Assets.atlas.findRegion("sparkle_small2"),
                Assets.atlas.findRegion("sparkle_small3"),
                Assets.atlas.findRegion("sparkle_small4"),
                Assets.atlas.findRegion("sparkle_small5"),
                Assets.atlas.findRegion("sparkle_small6"),
                Assets.atlas.findRegion("sparkle_small7"),
                Assets.atlas.findRegion("sparkle_small8"),
                Assets.atlas.findRegion("sparkle_small9"),
                Assets.atlas.findRegion("sparkle_small10"));
        sparkle.setPlayMode(Animation.PlayMode.NORMAL);
    }

    private void regenerateLevel() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                generatingLevel = true;
                Gdx.app.log("GAME_SCREEN", "Generating level...");
                final Graph<Room> roomGraph = dungeonGenerator.generateRoomGraph(params);

                Gdx.app.log("GAME_SCREEN", "Generating tilemap...");
                try { Thread.sleep(2000); } catch (Exception e) {}
                camController.debugRender = false;
                tileMap.generateTilesFromGraph(roomGraph);

                Gdx.app.log("GAME_SCREEN", "Level and tilemap generation complete.");
                generatingLevel = false;
            }
        });
    }

    @Override
    public void update(float delta) {
        updateMouseVectors();

        // DEBUG : Place enemies
        if (Gdx.input.justTouched() && Gdx.input.isKeyPressed(Input.Keys.F)) {
            enemies.add(new Enemy(Assets.enemytex, mouseWorldCoords.x, mouseWorldCoords.y, 16, 24, 0.3f));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !generatingLevel) {
            regenerateLevel();
        }

        // TODO : extract this functionality out to a more generic place
        // Switch weapons
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            if (player.getCurrentWeapon() instanceof Handgun) {
                player.setWeapon(Weapon.TYPE_SWORD);
                Timeline.createSequence()
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        weaponIcon = Assets.atlas.findRegion("sword");
                                        Assets.sword_slice1.play(0.1f);
                                    }
                                }))
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                        .start(GameInstance.tweens);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            if (player.getCurrentWeapon() instanceof Sword) {
                player.setWeapon(Weapon.TYPE_HANDGUN);
                Timeline.createSequence()
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.3f)
                                .target(-weaponIconSize.y)
                                .ease(Cubic.OUT)
                                .setCallback(new TweenCallback() {
                                    @Override
                                    public void onEvent(int type, BaseTween<?> source) {
                                        weaponIcon = Assets.atlas.findRegion("gun");
                                        Assets.gunshot_reload.play(0.4f);
                                    }
                                }))
                        .push(Tween.to(weaponIconPos, Vector2Accessor.Y, 0.7f)
                                .target(30)
                                .ease(Bounce.OUT))
                        .start(GameInstance.tweens);
            }
        }

        updateEntities(delta);

        playerPosition.set(player.getPosition().x, player.getPosition().y, 0);
        camera.position.lerp(playerPosition, 4*delta);

        camera.update();
    }

    private void updateMouseVectors() {
        mouseScreenCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        mouseWorldCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldCoords);
        GameInstance.mousePlayerDirection.set(
                mouseWorldCoords.x - player.getCenterPos().x,
                mouseWorldCoords.y - player.getCenterPos().y);
    }

    private void updateEntities(float delta) {
        updatePlayers(delta);
        // TODO
        resolveCollisions();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (player.getCurrentWeapon().collides(enemy.getCollisionBounds())) {
                    enemy.takeDamage(player.getCurrentWeapon().getDamage(), player.getCurrentWeapon().getDirection());
                    Assets.getRandomHitSound().play();
                    if (!enemy.isAlive()) {
                        explosionEmitter.addSmallExplosion(enemy.position);
                    }
                }
            }
            if (enemy.isAlive()) {
                enemy.update(delta);
            }
        }
    }

    private void updatePlayers(float delta) {
        float dx, dy;

        if      (game.input.isKeyDown(Input.Keys.A)) { dx = -key_move_amount; }
        else if (game.input.isKeyDown(Input.Keys.D)) { dx =  key_move_amount; }
        else {
            dx = 0f;
            player.velocity.x = 0f;
        }

        if      (game.input.isKeyDown(Input.Keys.W)) { dy =  key_move_amount; }
        else if (game.input.isKeyDown(Input.Keys.S)) { dy = -key_move_amount; }
        else {
            dy = 0f;
            player.velocity.y = 0f;
        }

        // Attack!
        if ((game.input.isButtonDown(Input.Buttons.LEFT) && !game.input.isKeyDown(Input.Keys.F))
         || game.input.isKeyDown(Input.Keys.CONTROL_LEFT)) {
            temp2.set(GameInstance.mousePlayerDirection).nor();
            player.attack(temp2);
            camera.translate(temp2.scl(-1));
            if (!sparkling) {
                sparkling = true;
                sparkle_accum.setValue(0f);
                Tween.to(sparkle_accum, 0, 0.2f)
                        .target(sparkle.getAnimationDuration())
                        .ease(Linear.INOUT)
                        .setCallback(new TweenCallback() {
                            @Override
                            public void onEvent(int type, BaseTween<?> source) {
                                sparkling = false;
                            }
                        })
                        .start(GameInstance.tweens);
            }
        }

        player.velocity.x += dx;
        player.velocity.y += dy;

        player.update(delta);
    }

    List<Tile> collisionTiles = new ArrayList<Tile>(10);
    Rectangle tileRect = new Rectangle();
    Rectangle intersection = new Rectangle();
    private void resolveCollisions() {
        // Resolve bullet collisions
        for (Bullet bullet : player.getBullets()) {
            if (bullet.isAlive()) {
                // Check the bullet against the map
                tileMap.getCollisionTiles(bullet, collisionTiles);
                for (Tile tile : collisionTiles) {
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
            // TODO
            resolveCollisions(enemy);
        }

        // TODO
        resolveCollisions(player);
    }

    private void resolveCollisions(Entity entity) {
        if (!tileMap.hasTiles) return;
        // Get grid tiles that the entity overlaps
        tileMap.getCollisionTiles(entity, collisionTiles);

        // For each overlapped blocking tile:
        for (Tile tile : collisionTiles) {
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
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Assets.batch.setProjectionMatrix(camera.combined);
        Assets.shapes.setProjectionMatrix(camera.combined);

        renderDebug();

        if (tileMap != null && tileMap.hasTiles) {
            tileMap.render(camera);
        }

        Assets.batch.begin();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            enemy.render(Assets.batch);
        }
        player.render(Assets.batch);
        if (sparkling) {
            TextureRegion keyframe = sparkle.getKeyFrame(sparkle_accum.floatValue());
            Assets.batch.draw(keyframe, player.getCenterPos().x, player.getCenterPos().y);
        }
        explosionEmitter.render(Assets.batch);
        Assets.batch.end();


        uiRender();
    }

    private void renderDebug() {
        if (!camController.debugRender) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        dungeonGenerator.render(camera, Assets.shapes);
    }

    private void uiRender() {
        Gdx.gl20.glViewport(0, 0, (int) uiCamera.viewportWidth, (int) uiCamera.viewportHeight);
        final String line1 = "Hold 'F' + Left Click to spawn 'enemy'";
        final String line2 = "Press '1' or '2' to switch weapons";
        final String line3 = "FPS: " + Gdx.graphics.getFramesPerSecond();
        final float line_spacing = 5;
        final float line_offset = 20;

        Assets.batch.setProjectionMatrix(uiCamera.combined);
        Assets.batch.begin();

        // Draw help text
        font.setScale(0.5f);
        font.setColor(Color.WHITE);
        font.draw(Assets.batch, line1, line_offset, Gdx.graphics.getHeight() - line_offset);
        font.draw(Assets.batch, line2, line_offset, Gdx.graphics.getHeight() - line_offset - 1 * (font.getLineHeight() - line_spacing));
        font.draw(Assets.batch, line3, line_offset, Gdx.graphics.getHeight() - line_offset - 2 * (font.getLineHeight() - line_spacing));

        // Draw current weapon icon
        Assets.batch.draw(weaponIcon, weaponIconPos.x, weaponIconPos.y, weaponIconSize.x, weaponIconSize.y);
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
//        tileMap.dispose();
        explosionEmitter.dispose();
        font.dispose();
    }
}
