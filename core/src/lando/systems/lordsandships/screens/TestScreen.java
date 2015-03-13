package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Circ;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.level.Level;
import lando.systems.lordsandships.scene.level.Room;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.ui.UserInterface;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.tweens.Vector3Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Brian Ploeckelman created on 3/7/2015.
 */
public class TestScreen extends InputAdapter implements UpdatingScreen {

    final GameInstance game;

    private static final float key_move_amount = 16;

    // TODO : extract to UpdatingScreen?
    private Vector3 mouseWorldCoords  = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();
    private Vector2 temp              = new Vector2();

    Color              bgColor;
    OrthographicCamera camera;
    OrthographicCamera uiCamera;

    UserInterface ui;
    Level         level;
    Player        player;

    public TestScreen(GameInstance game) {
        this.game = game;
        create();
    }

    public void create() {
        bgColor = new Color(0.43f, 0.43f, 0.43f, 1);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width, Constants.win_half_height, 0);
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        ui = new UserInterface(game);

        level = new Level();
        Rectangle bounds = level.occupied().room().bounds();
        player = new Player(PlayerSelectScreen.PlayerType.Cloak,
                            bounds.x + bounds.width / 2f,
                            bounds.y + bounds.height / 2f,
                            16, 16, 0.1f);
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    private Vector3 playerPos = new Vector3();

    @Override
    public void update(float delta) {
        level.update(delta);
        playerUpdate(delta);
        camera.position.lerp(playerPos, 1.0f * delta);
        if (!transition) {
            constrainCamera(camera, level.occupied().room().bounds());
            resolveCollisions();
        }
        camera.update();
        uiCamera.update();
        ui.update(delta);
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        final SpriteBatch batch = Assets.batch;
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        {
            batch.setColor(1, 1, 1, roomAlpha.floatValue());
            level.render(batch, camera);
            player.render(batch);
        }
        batch.end();

//        level.renderDebug(camera);

        ui.render(batch, uiCamera);
    }

    // -------------------------------------------------------------------------
    // InputAdapter Overrides
    // -------------------------------------------------------------------------

    MutableFloat roomAlpha = new MutableFloat(1);
    boolean transition = false;

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
//            game.setScreen(Constants.player_select_screen);
            GameInstance.exit();
        } else if (keycode == Input.Keys.SPACE) {
            if  (transition) return true;
            else transition = true;

            final Rectangle targetBounds = level.getNextRoomBounds();
            Timeline.createParallel()
                    .push(Tween.to(roomAlpha, -1, 1)
                               .target(0.0f)
                               .ease(Circ.OUT)
                               .setCallback(new TweenCallback() {
                                   @Override
                                   public void onEvent(int type, BaseTween<?> source) {
                                       level.nextRoom();
                                       Rectangle bounds = level.occupied().room().bounds();
                                       player.position.x = bounds.x + bounds.width / 2f;
                                       player.position.y = bounds.y + bounds.height / 2f;
                                       player.boundingBox.x = player.position.x;
                                       player.boundingBox.y = player.position.y;
                                   }
                               }))
                    .push(Tween.to(bgColor, ColorAccessor.RGB, 1)
                               .target(0,0,0)
                               .ease(Circ.OUT))
                    .push(Tween.to(camera.position, Vector3Accessor.XY, 1)
                               .target(targetBounds.x + targetBounds.width / 2f,
                                       targetBounds.y + targetBounds.height / 2f)
                               .ease(Expo.INOUT)
                               .delay(0.5f))
                    .push(Tween.to(roomAlpha, -1, 0.5f)
                               .target(1)
                               .ease(Circ.IN)
                               .delay(1)
                               .setCallback(new TweenCallback() {
                                   @Override
                                   public void onEvent(int type, BaseTween<?> source) {
                                       transition = false;
                                   }
                               }))
                    .push(Tween.to(bgColor, ColorAccessor.RGB, 1)
                               .target(0.43f, 0.43f, 0.43f)
                               .delay(1))
                    .start(GameInstance.tweens);
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();

        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        ui.resize(width, height);
    }

    @Override
    public void show() {
        enableInput();
    }

    @Override
    public void hide() {
        disableInput();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        ui.dispose();
    }


    // -------------------------------------------------------------------------
    // Implementation Methods
    // -------------------------------------------------------------------------

    private void enableInput() {
        OrthoCamController camController = new OrthoCamController(camera);
        camController.camera_zoom.setValue(0.5f);
        camController.scrolled(0);

        GameInstance.input.reset();
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(ui.getStage());
        inputMux.addProcessor(camController);
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    private void disableInput() {
        GameInstance.input.reset();
        Gdx.input.setInputProcessor(GameInstance.input);
    }

    // TODO : extract to UpdatingScreen?
    private void updateMouseVectors(Camera camera) {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenCoords.set(mx, my, 0);
        mouseWorldCoords.set(mx, my, 0);
        camera.unproject(mouseWorldCoords);
    }

    /**
     * Map user input to player behavior
     * @param delta
     */
    private void playerUpdate(float delta) {
        float dx, dy;

        if      (GameInstance.input.isKeyDown(Input.Keys.A)) { dx = -key_move_amount; }
        else if (GameInstance.input.isKeyDown(Input.Keys.D)) { dx =  key_move_amount; }
        else {
            dx = 0f;
            player.velocity.x = 0f;
        }

        if      (GameInstance.input.isKeyDown(Input.Keys.W)) { dy =  key_move_amount; }
        else if (GameInstance.input.isKeyDown(Input.Keys.S)) { dy = -key_move_amount; }
        else {
            dy = 0f;
            player.velocity.y = 0f;
        }

        // Attack!
        if (GameInstance.input.isButtonDown(Input.Buttons.LEFT)
            || GameInstance.input.isKeyDown(Input.Keys.CONTROL_LEFT)) {
            temp.set(GameInstance.mousePlayerDirection).nor();
            player.attack(temp);
            // TODO (brian): camera shake and attack special effects
        }

        player.velocity.x += dx;
        player.velocity.y += dy;

        player.update(delta);

        playerPos.set(player.position.x, player.position.y, 0);
    }

    /**
     * Constrain a camera to the specified bounds
     * @param camera
     * @param bounds
     */
    private void constrainCamera(OrthographicCamera camera, Rectangle bounds) {
        // TODO : limit zoom level to min room dimension
        float effectiveViewportWidth  = camera.viewportWidth  * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float hw = effectiveViewportWidth  / 2f;
        float hh = effectiveViewportHeight / 2f;

        if (camera.position.x - hw < bounds.x)
            camera.position.x = bounds.x + hw;
        if (camera.position.x + hw > bounds.x + bounds.width)
            camera.position.x = bounds.x + bounds.width - hw;

        if (camera.position.y - hh < bounds.y)
            camera.position.y = bounds.y + hh;
        if (camera.position.y + hh > bounds.y + bounds.height)
            camera.position.y = bounds.y + bounds.height - hh;

        if (effectiveViewportWidth  > bounds.width)  camera.position.x = bounds.x + bounds.width  / 2f;
        if (effectiveViewportHeight > bounds.height) camera.position.y = bounds.y + bounds.height / 2f;
    }

    /**
     * Check for and resolve any collisions between Entities and the Level
     */
    // TODO (brian): collision detection needs some significant cleanup and simplification
    // Working data for collision detection
    private List<Tile> collisionTiles = new ArrayList<Tile>(10);
    private Rectangle  tileRect       = new Rectangle();
    private Rectangle  intersection   = new Rectangle();

    public void getCollisionTiles(Entity entity, List<Tile> collisionTiles) {
        Room room = level.occupied().room();
        int entityMinX = (int) (entity.boundingBox.x - room.bounds().x) / Tile.TILE_SIZE;
        int entityMinY = (int) (entity.boundingBox.y - room.bounds().y) / Tile.TILE_SIZE;
        int entityMaxX = (int)((entity.boundingBox.x + entity.boundingBox.width)  - room.bounds().x) / Tile.TILE_SIZE;
        int entityMaxY = (int)((entity.boundingBox.y + entity.boundingBox.height) - room.bounds().y) / Tile.TILE_SIZE;

        collisionTiles.clear();
        for (int y = entityMinY; y <= entityMaxY; ++y) {
            for (int x = entityMinX; x <= entityMaxX; ++x) {
                if (x >= 0 && x < room.tilesWide()
                 && y >= 0 && y < room.tilesHigh())
                    collisionTiles.add(room.tile(x,y));
            }
        }
    }

    private void resolveCollisions() {
        // TODO : Resolve bullet collisions
        // TODO : Resolve enemy collisions
        resolveCollisions(player);
    }

    private void resolveCollisions(Entity entity) {
        final float bounds_feather = 0.0075f;

        // Get grid tiles that the entity overlaps
        getCollisionTiles(entity, collisionTiles);
        Room room = level.occupied().room();

        // For each overlapped blocking tile:
        for (Tile tile : collisionTiles) {
            if (room.walkable(tile.getGridX(), tile.getGridY())) {
                tileRect.set(0, 0, 0, 0);
                intersection.set(0, 0, 0, 0);
                continue;
            }

            // Find amount of overlap on each axis
            tileRect.set(tile.getWorldMinX() + room.bounds().x,
                         tile.getWorldMinY() + room.bounds().y,
                         Tile.TILE_SIZE, Tile.TILE_SIZE);
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
    }

}
