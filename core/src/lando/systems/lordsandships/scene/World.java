package lando.systems.lordsandships.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.entities.Bullet;
import lando.systems.lordsandships.entities.Enemy;
import lando.systems.lordsandships.entities.Entity;
import lando.systems.lordsandships.entities.Player;
import lando.systems.lordsandships.scene.levelgen.LevelGenParams;
import lando.systems.lordsandships.scene.levelgen.Room;
import lando.systems.lordsandships.scene.levelgen.RoomGraphGenerator;
import lando.systems.lordsandships.scene.levelgen.TinyDungeonGenerator;
import lando.systems.lordsandships.scene.tilemap.Tile;
import lando.systems.lordsandships.scene.tilemap.TileMap;
import lando.systems.lordsandships.screens.PlayerSelectScreen;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Brian Ploeckelman created on 1/2/2015.
 */
public class World {

    private static final float key_move_amount = 16;

    private RoomGraphGenerator dungeonGenerator;
    private TileMap tileMap;
    private Player player;
    private Array<Enemy> enemies;

    private Camera camera;
    private ParallaxBackround background;

    private Vector2 temp = new Vector2();

    // TODO (brian): move camera out to View class and inject View class dependency here instead
    public World(Camera camera) {
        this.camera = camera;

        player = new Player(
                Assets.playertex,
                100 * 16, 75 * 16,
                16, 16, 0.1f);

        enemies = new Array<Enemy>(50);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        background = new ParallaxBackround(0, 0, width, height,
                                           new Rectangle(0, 0, width, height),
                                           new TextureRegion(Assets.starfieldLayer0),
                                           new TextureRegion(Assets.starfieldLayer1));
    }

    // -------------------------------------------------------------------------
    // Public Interface
    // -------------------------------------------------------------------------

    public void initializePlayer(PlayerSelectScreen.PlayerType playerType) {
        player = new Player(playerType,
                100 * 16, 75 * 16,
                16, 16, 0.1f);
    }

    /**
     * Run the level generation process
     */
    public void regenerateLevel() {
        dungeonGenerator = new TinyDungeonGenerator();
        tileMap = new TileMap();

        Gdx.app.log("WORLD", "Generating level...");
        final LevelGenParams params = new LevelGenParams();
        params.numInitialRooms  = 200;
        params.numSelectedRooms = 50;
        params.roomWidthMin     = 3;
        params.roomWidthMax     = 20;
        params.roomHeightMin    = 4;
        params.roomHeightMax    = 15;
        final Graph<Room> roomGraph = dungeonGenerator.generateRoomGraph(params);

        Gdx.app.log("WORLD", "Generating tilemap...");
        tileMap.generateTilesFromGraph(roomGraph);

        Gdx.app.log("WORLD", "Placing enemies...");
        enemies.clear();
        Vector2 pos = new Vector2();
        for (int i = 0; i < 100; ++i) {
            pos.set(tileMap.getRandomFloorTile());
            enemies.add(new Enemy(Assets.enemytex,
                    pos.x * Tile.TILE_SIZE, pos.y * Tile.TILE_SIZE,
                    Tile.TILE_SIZE, 24, 0.15f));
        }

        Gdx.app.log("WORLD", "Level and tilemap generation complete.");

        player.boundingBox.x = tileMap.spawnX * Tile.TILE_SIZE;
        player.boundingBox.y = tileMap.spawnY * Tile.TILE_SIZE;
    }

    /**
     * Update the world based on the specified delta time
     *
     * @param delta the elapsed time (in seconds) since the last update
     */
    public void update(float delta) {
        updateEnemies(delta);
        updatePlayer(delta);

        resolveCollisions();

        background.position(camera.position.x, camera.position.y);
    }

    public void render(SpriteBatch batch, Camera camera) {
        background.render(batch);

        // TODO (brian): this isn't really needed now that multithreaded regeneration has been removed
        if (tileMap != null && tileMap.hasTiles) {
            tileMap.render(camera);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            enemy.render(batch);
        }
        player.render(batch);
        batch.end();
    }

    public void debugRender(ShapeRenderer shapes, Camera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        ((TinyDungeonGenerator) dungeonGenerator).render(shapes, camera);
    }


    // -------------------------------------------------------------------------
    // Private Implementation
    // -------------------------------------------------------------------------

    private void updatePlayer(float delta) {
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
    }

    private void updateEnemies(float delta) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                if (player.getCurrentWeapon().collides(enemy.getCollisionBounds())) {
                    // TODO (brian): move hit sound and death effect to enemy.takeDamage()
                    enemy.takeDamage(player.getCurrentWeapon().getDamage(), player.getCurrentWeapon().getDirection());
                }
            }

            if (enemy.isAlive()) {
                enemy.update(delta);
            }
        }
    }


    // TODO (brian): collision detection needs some significant cleanup and simplification
    // Working data for collision detection
    private List<Tile> collisionTiles = new ArrayList<Tile>(10);
    private Rectangle tileRect     = new Rectangle();
    private Rectangle intersection = new Rectangle();

    private void resolveCollisions() {
        // Resolve bullet collisions
        for (Bullet bullet : player.getBullets()) {
            // Check the bullet against the map
            if (bullet.isAlive()) {
                tileMap.getCollisionTiles(bullet, collisionTiles);
                for (Tile tile : collisionTiles) {
                    if (tileMap.isBlocking(tile.getGridX(), tile.getGridY())) {
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

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            resolveCollisions(enemy);
        }

        resolveCollisions(player);
    }

    private void resolveCollisions(Entity entity) {
        if (!tileMap.hasTiles) return;

        final float bounds_feather = 0.0075f;

        // Get grid tiles that the entity overlaps
        tileMap.getCollisionTiles(entity, collisionTiles);

        // For each overlapped blocking tile:
        for (Tile tile : collisionTiles) {
            if (!tileMap.isBlocking(tile.getGridX(), tile.getGridY())) {
                tileRect.set(0,0,0,0);
                intersection.set(0,0,0,0);
                continue;
            }

            // Find amount of overlap on each axis
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
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------
    public Player getPlayer() { return player; }

}
