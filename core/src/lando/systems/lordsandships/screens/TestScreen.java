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
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.entities.enemies.Bat;
import lando.systems.lordsandships.entities.enemies.Batclops;
import lando.systems.lordsandships.entities.enemies.SlimeSmall;
import lando.systems.lordsandships.scene.OrthoCamController;
import lando.systems.lordsandships.scene.level.Level;
import lando.systems.lordsandships.scene.level.Room;
import lando.systems.lordsandships.scene.level.objects.Light;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.ui.UserInterface;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.tweens.Vector3Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;
import lando.systems.lordsandships.utils.Utils;
import lando.systems.lordsandships.weapons.Weapon;

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
    Utils.Shake        screenShaker;

    float         ambientIntensity;
    Color         ambientColor;
    FrameBuffer   sceneFBO;
    FrameBuffer   lightmapFBO;
    FrameBuffer   screenFBO;
    ShaderProgram multitexShader;
    ShaderProgram ambientShader;
    ShaderProgram postShader;
    ShaderProgram sobelShader;
    float         accum;
    MutableFloat  pauseTimer;
    MutableFloat  pulse;
    MutableFloat  counter;

    boolean debugRenderEnemies = false;
    boolean lightEnabled       = true;
    boolean doPost             = false;
    float   angle_speed        = 1f;
    float angle;

    UserInterface ui;
    Level         level;
    Player        player;
    Array<Enemy>  enemies;

    public TestScreen(GameInstance game) {
        this.game = game;
        create();
    }

    public void create() {
        bgColor = new Color(0.1f, 0.1f, 0.1f, 1f);

        screenShaker = new Utils.Shake(50, 15);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width, Constants.win_half_height, 0);
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        ui = new UserInterface(game);

        ambientIntensity = 0.8f;
        ambientColor = new Color(0.6f, 0.6f, 0.7f, ambientIntensity);

        multitexShader = Assets.multitexShaderProgram;
        postShader = Assets.postShaderProgram;
        ambientShader = Assets.ambientShaderProgram;
        sobelShader = Assets.testShaderProgram;

        sceneFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Constants.win_width, Constants.win_height, false);
        lightmapFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Constants.win_width, Constants.win_height, false);
        screenFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Constants.win_width, Constants.win_height, false);

//        level = new Level(Constants.win_width, Constants.win_height, 2);
        level = new Level(2000, 2000, 3);
        Rectangle bounds = level.occupied().room().bounds();
        player = new Player(PlayerSelectScreen.PlayerType.Cloak,
                            bounds.x + bounds.width / 2f,
                            bounds.y + bounds.height / 2f,
                            16, 16, 0.1f);

        spawnEnemies();

        pauseTimer = new MutableFloat(0f);
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    private Vector3 playerPos       = new Vector3();
    private Vector2 nearestEnemyPos = new Vector2();
    private Enemy   nearestEnemy    = null;

    @Override
    public void update(float delta) {
        updateMouseVectors(camera);

        if (pauseTimer.floatValue() > 0f) {
            pauseTimer.setValue(pauseTimer.floatValue() - delta);
            return;
        }

        boolean allDead = true;
        float nearestDist = Float.MAX_VALUE;
        nearestEnemyPos.set(0, 0);
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                allDead = false;
                float dist = player.getCenterPos().dst(enemy.getCenterPos());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestEnemyPos.set(enemy.getCenterPos());
                    nearestEnemy = enemy;
                }
            }
        }
        if (allDead) {
            kickoffRoomTransition();
        }

        level.update(delta);
        playerUpdate(delta);
        enemiesUpdate(delta);

        float scale = camera.position.cpy().sub(playerPos).len() / 20;
        camera.position.lerp(playerPos, scale * delta);

        if (!transition) {
            constrainCamera(camera, level.occupied().room().bounds());
            resolveCollisions();
        }

        camera.update();
        uiCamera.update();
        ui.update(delta);
        ui.getArsenal().updateCurrentWeapon(player);

        counter = new MutableFloat(0f);
        pulse = new MutableFloat(0.f);
        Tween.to(pulse, -1, 3.f)
             .target(1.f)
             .ease(Circ.OUT)
             .repeatYoyo(-1, 0.33f)
             .start(GameInstance.tweens);

        screenShaker.update(delta, camera, camera.position.x, camera.position.y);
    }

    @Override
    public void render(float delta) {
        final SpriteBatch batch = Assets.batch;
        accum += delta;

        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);

        // Render lightmap into framebuffer
        if (lightEnabled) {
            lightmapFBO.begin();
            {
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                batch.setProjectionMatrix(camera.combined);
                batch.setShader(null);
                batch.begin();
                {
                    // draw ambient layer
                    batch.setColor(ambientColor);
                    batch.draw(Assets.whiteSquare,
                               level.occupied().room().bounds().x,
                               level.occupied().room().bounds().y,
                               level.occupied().room().bounds().width,
                               level.occupied().room().bounds().height);

                    batch.setColor(1, 1, 1, 1);

                    angle += delta * angle_speed;
                    while (angle > MathUtils.PI2) angle -= MathUtils.PI2;

                    final float sz = 300;
                    final float d = sz * 0.1f;
                    final float light_size = sz - d + d * (float) Math.sin(angle) + d * MathUtils.random();
                    batch.draw(Assets.lightmaptex1,
                               player.getCenterPos().x - light_size / 2f,
                               player.getCenterPos().y - light_size / 2f,
                               light_size, light_size);


                    int i = 0;
                    for (Light light : level.occupied().room().getLights()) {
                        final float sconce_sz_x = light.getSize().x;
                        final float sconce_sz_y = light.getSize().y;
                        final float sconce_d_x = sconce_sz_x * 0.1f;
                        final float sconce_d_y = sconce_sz_y * 0.1f;
                        final float sconce_size_x = sconce_sz_x - sconce_d_x + sconce_d_x * (float) Math.sin(angle) + sconce_d_x * MathUtils.random();
                        final float sconce_size_y = sconce_sz_y - sconce_d_y + sconce_d_y * (float) Math.sin(angle) + sconce_d_y * MathUtils.random();
                        final float xPosition = light.getPosition().x - sconce_size_x / 2f;
                        final float yPosition = light.getPosition().y - sconce_size_y / 2f;

                        if (player.getCollisionBounds().contains(light.getPosition().x, light.getPosition().y)) {
                            if (!light.isTransitioning()) {
                                light.fadeOut(0.1f);
                            }
                        } else {
                            if (!light.isEnabled()) {
                                light.fadeIn(0.1f);
                            }
                        }

                        if (!light.isEnabled()) continue;

                        batch.setColor(1, 1, 1, light.getAlpha().floatValue());
                        batch.draw(Assets.lightmaptex2, xPosition, yPosition, sconce_size_x, sconce_size_y);
                    }
                    batch.setColor(1, 1, 1, 1);
                }
                batch.end();
            }
            lightmapFBO.end();
        }

        // Render world and entities into fbo
        sceneFBO.begin();
        {
            Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            // Render level
            batch.setProjectionMatrix(camera.combined);
            batch.setShader(ambientShader);
            batch.begin();
            {
                batch.setColor(1, 1, 1, roomAlpha.floatValue());

                ambientShader.setUniformf("u_ambient", ambientColor.r, ambientColor.g, ambientColor.b, ambientColor.a);

                level.render(batch, camera);
                player.render(batch);
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    enemy.render(batch);
                }

                final float indicator_scale = 32;
                final float min_draw_dist2 = (indicator_scale + 32) * (indicator_scale + 32);
                final Vector2 dir = nearestEnemyPos.cpy().sub(player.getCenterPos());
                if (dir.len2() > min_draw_dist2 && nearestEnemy != null) {
                    dir.nor();
                    batch.draw(nearestEnemy.getKeyframe(),
                               player.getCenterPos().x + dir.x * indicator_scale - 4,
                               player.getCenterPos().y + dir.y * indicator_scale - 4,
                               8, 8);
                }
            }
            batch.end();

            if (debugRenderEnemies) {
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    enemy.renderDebug();
                }
            }
        }
        sceneFBO.end();

        // Composite lightmap onto
        screenFBO.begin();
        {
            final TextureRegion sceneRegion = new TextureRegion(sceneFBO.getColorBufferTexture());
            final TextureRegion lightmapRegion = new TextureRegion(lightmapFBO.getColorBufferTexture());
            sceneRegion.flip(false, true);
            lightmapRegion.flip(false, true);

            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.setProjectionMatrix(uiCamera.combined);
            if (lightEnabled) batch.setShader(multitexShader);
            else              batch.setShader(null);
            batch.begin();
            {
                if (lightEnabled) {
                    multitexShader.setUniformf("u_time", accum);
                    multitexShader.setUniformi("u_texture", 0);
                    multitexShader.setUniformi("u_texture1", 1);
                    lightmapRegion.getTexture().bind(1);
                }

                Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
                batch.draw(sceneRegion, 0, 0, sceneRegion.getRegionWidth(), sceneRegion.getRegionHeight());
            }
            batch.end();
        }
        screenFBO.end();

        // Render post processing
        final TextureRegion screenRegion = new TextureRegion(screenFBO.getColorBufferTexture());
        screenRegion.flip(false, true);

        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setShader(postShader);
        if (doPost) batch.setShader(postShader);
        else        batch.setShader(null);
        batch.begin();
        {
            Vector3 worldPos = new Vector3(player.getCenterPos().x, player.getCenterPos().y, 0f);
            Vector3 screenPos = camera.project(worldPos);
            float width  = screenRegion.getRegionWidth();
            float height = screenRegion.getRegionHeight();

//            postShader.setUniformf("u_pulse", pulse.floatValue());
            postShader.setUniformf("u_time", accum);
            postShader.setUniformf("u_resolution", width, height);
            postShader.setUniformf("u_screenPos", screenPos.x, screenPos.y);

            batch.draw(screenRegion, 0, 0, width, height);
        }
        batch.end();

        // Render the user interface
        batch.setShader(null);
        ui.render(batch, uiCamera);
        renderPlayerHealth(batch);
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
            return kickoffRoomTransition();
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
    // Delegating Methods
    // ------------------
    // NOTE: mainly used for console commands, find a cleaner way to handle them
    // -------------------------------------------------------------------------

    public boolean toggleLights() {
        return (lightEnabled = !lightEnabled);
    }

    public boolean toggleDebugRenderEnemy() {
        return (debugRenderEnemies = !debugRenderEnemies);
    }

    public boolean toggleWeaponBounds() {
        return player.toggleWeaponBounds();
    }

    public boolean toggleMouseLook() {
        return player.toggleMouseLook();
    }

    public boolean toggleLevelRender() {
        return (level.renderAllRooms = !level.renderAllRooms);
    }

    // -------------------------------------------------------------------------
    // Implementation Methods
    // -------------------------------------------------------------------------

    private void enableInput() {
        OrthoCamController camController = new OrthoCamController(camera);
        camController.camera_zoom.setValue(0.4f);
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
        GameInstance.mousePlayerDirection.set(
                mouseWorldCoords.x - player.getCenterPos().x,
                mouseWorldCoords.y - player.getCenterPos().y);
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

        // Dash!
        final float dash_multiplier = 1.5f;
        if (GameInstance.input.isKeyDown(Input.Keys.SHIFT_LEFT)) {
            if (!player.dashing) {
                player.dashing = true;
                Player.max_vel_x *= dash_multiplier;
                Player.max_vel_y *= dash_multiplier;
            }
        } else {
            if (player.dashing) {
                Player.max_vel_x /= dash_multiplier;
                Player.max_vel_y /= dash_multiplier;
            }
            player.dashing = false;
        }

        // Attack!
        if (GameInstance.input.isButtonDown(Input.Buttons.LEFT)
         || GameInstance.input.isKeyDown(Input.Keys.CONTROL_LEFT)) {
            temp.set(GameInstance.mousePlayerDirection).nor();
            player.attack(temp);
            // special effects
//            if (!doPost) {
//                doPost = true;
//                accum = 0f;
//                counter.setValue(0f);
//                Tween.to(counter, -1, 3.0f)
//                     .setCallback(new TweenCallback() {
//                         @Override
//                         public void onEvent(int type, BaseTween<?> source) {
//                             doPost = false;
//                         }
//                     })
//                     .start(GameInstance.tweens);
//            }
        }

        player.velocity.x += dx;
        player.velocity.y += dy;

        player.update(delta);

        playerPos.set(player.position.x, player.position.y, 0);
    }

    /**
     * Update enemies
     */
    private void enemiesUpdate(float delta) {
        Weapon weapon = player.getCurrentWeapon();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (player.getCenterPos().dst(enemy.getCenterPos()) < camera.viewportHeight / 2f) {
                    enemy.target = player;
                } else {
                    enemy.target = null;
                }

                if (player.getCurrentWeapon().collides(enemy.getCollisionBounds())) {
                    screenShaker.shake(0.25f);
                    pauseTimer.setValue(0.03f);

                    boolean killed = enemy.takeDamage(weapon.getDamage(), weapon.getDirection());
                    if (killed) {
                        // TODO: poof... splat...
                    }
                }
            }
            if (enemy.isAlive()) enemy.update(delta);
        }
    }

    /**
     * Spawn a bunch of enemies in the current room
     */
    private void spawnEnemies() {
        if (enemies == null) enemies = new Array<Enemy>();
        else                 enemies.clear();

        final Room room = level.occupied().room();
        final Rectangle bounds = room.bounds();
        final Vector2 pos = new Vector2();

        final float spawn_probability = 0.05f;
        for (int y = 0; y < room.tilesHigh(); ++y) {
            for (int x = 0; x < room.tilesWide(); ++x) {
                if (!room.walkable(x, y)) continue;

                pos.set(bounds.x + x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2f,
                        bounds.y + y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2f);

                float r = (float) Math.random();
                if (r <= spawn_probability) {
                    Enemy enemy;

                    float num_enemies = 4;
                    float d = (spawn_probability - r) / num_enemies;
                    if      (r <= 1*d) enemy = new Bat(Assets.enemytex, pos.x, pos.y, 0, 0, 0.09f);
                    else if (r <= 2*d) enemy = new Batclops(Assets.enemytex, pos.x, pos.y, 0, 0, 0.1f);
                    else if (r <= 3*d) enemy = new SlimeSmall(Assets.enemytex, pos.x, pos.y, 0, 0, 0.1f);
                    else               enemy = new Enemy(Assets.enemytex, pos.x, pos.y, 0, 0, 0.1f);

                    enemies.add(enemy);
                }
            }
        }
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
        resolveCollisions(player);

        for (Bullet bullet : player.getBullets()) {
            // Check the bullet against the map
            if (bullet.isAlive()) {
                getCollisionTiles(bullet, collisionTiles);
                for (Tile tile : collisionTiles) {
                    if (!level.occupied().room().walkable(tile.getGridX(), tile.getGridY())) {
                        Assets.gunshot_impact.play(0.05f);
                        bullet.kill();
                    }
                }
            }

            // Check the bullet against enemies
            if (bullet.isAlive()) {
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    if (Intersector.overlaps(bullet.boundingBox, enemy.boundingBox)) {
                        // TODO (brian): move hit sound and death effect to enemy.takeDamage()
                        enemy.takeDamage(bullet.damageAmount, bullet.velocity);
                        bullet.kill();
                    }
                }
            }
        }

        for (int i = 0; i < enemies.size; ++i) {
            final Enemy enemy = enemies.get(i);
            if (!enemy.isAlive()) continue;
            for (int j = i + 1; j < enemies.size; ++j) {
                final Enemy other = enemies.get(j);
                if (!other.isAlive()) continue;
                resolveCollisions(enemy, other);
            }
            resolveCollisions(player, enemy);
            resolveCollisions(enemy);
        }
    }

    private void resolveCollisions(Entity entity, Entity other) {
        final float push_scale = (entity == player) ? 1.5f : 1.1f;
        if (Intersector.overlaps(entity.boundingBox, other.boundingBox)) {
            // Find amount of overlap on each axis
            if (entity.boundingBox.overlaps(other.boundingBox)) {
                Intersector.intersectRectangles(entity.boundingBox, other.boundingBox, intersection);

                if (entity instanceof Player) {
                    entity.takeDamage(1, entity.getCenterPos().cpy().sub(other.getCenterPos()).nor());
                }

                // Move out of shallower overlap axis
                if (intersection.width < intersection.height) {
                    // Move out of X axis first..
                    if (entity.boundingBox.x > other.boundingBox.x
                     && entity.boundingBox.x < other.boundingBox.x + other.boundingBox.width) {
                        entity.boundingBox.x = other.boundingBox.x + other.boundingBox.width;
                        other.velocity.x = entity.velocity.x * push_scale;
                    }
                    else if (entity.boundingBox.x < other.boundingBox.x
                     && entity.boundingBox.x > other.boundingBox.x - entity.boundingBox.width) {
                        entity.boundingBox.x = other.boundingBox.x - entity.boundingBox.width;
                        other.velocity.x = entity.velocity.x * push_scale;
                    }
                } else {
                    // Move out of Y axis first..
                    if (entity.boundingBox.y > other.boundingBox.y
                     && entity.boundingBox.y < other.boundingBox.y + other.boundingBox.height) {
                        entity.boundingBox.y = other.boundingBox.y + other.boundingBox.height;
                        other.velocity.y = entity.velocity.y * push_scale;
                    }
                    else if (entity.boundingBox.y < other.boundingBox.y
                     && entity.boundingBox.y > other.boundingBox.y - entity.boundingBox.height) {
                        entity.boundingBox.y = other.boundingBox.y - entity.boundingBox.height;
                        other.velocity.y = entity.velocity.y * push_scale;
                    }
                }
            } else {
                intersection.set(0, 0, 0, 0);
            }

        }
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

    private boolean kickoffRoomTransition() {
        if  (transition) return true;
        else transition = true;

        final Rectangle targetBounds = level.getNextRoomBounds();
        if (targetBounds == null) return true;

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
                                   spawnEnemies();
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
                           .target(0.1f, 0.1f, 0.1f)
                           .delay(1))
                .start(GameInstance.tweens);

        return transition;
    }

    private void renderPlayerHealth(SpriteBatch batch) {
        final float spacing = 5;
        final float margin = 10;
        final float width  = 2f * Assets.healthIconFull.getWidth();
        final float height = 2f * Assets.healthIconFull.getHeight();
        final int num_health_icons = 5;
        final float icon_increment = 100 / num_health_icons;

        float x = margin;
        float y = camera.viewportHeight - margin - height;

        batch.begin();
        for (int i = 0; i < num_health_icons; ++i) {
            Texture icon = Assets.healthIconEmpty;
            if      (player.health >= (i + 1) * icon_increment) icon = Assets.healthIconFull;
            else if (player.health >   i * icon_increment)      icon = Assets.healthIconHalf;
            batch.draw(icon, x, y, width, height);
            x += width + spacing;
        }
        batch.end();
    }

}
