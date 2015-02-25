package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * Brian Ploeckelman created on 2/23/2015.
 */
public class PlayerSelectScreen implements UpdatingScreen {
//    private final GameInstance game;

    private OrthographicCamera camera;

    private Vector3 mouseWorldCoords  = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();

    private PlayerIcon playerIcons[];
    private NinePatch border;

    private Color backgroundColor;

    public PlayerSelectScreen(GameInstance game) {
        super();

//        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.win_width, Constants.win_height);
        camera.position.set(0, 0, 0);
        camera.translate(Constants.win_half_width,
                         Constants.win_half_height,
                         0);
        camera.update();

        border = new NinePatch(Assets.uiAtlas.findRegion("default-round"));

        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(GameInstance.input);
        Gdx.input.setInputProcessor(inputMux);

        final int   num_players = PlayerType.NUM_PLAYERS.value();
        final float size = camera.viewportWidth / 6;
        final float ypos = camera.viewportHeight / 2 - size / 2;
        final float margin = size / 4;
        final float padding = (camera.viewportWidth -
                               (num_players * size + (num_players - 1) * margin)) / 2;

        final float anim_speed = 0.15f;
        playerIcons = new PlayerIcon[num_players];
        for (int i = 0; i < num_players; ++i) {
            PlayerType playerType = PlayerType.values()[i];
            String regionName = "sHero" + playerType.name() + "Down";
            playerIcons[i] = new PlayerIcon(playerType);
            playerIcons[i].bounds.set(padding + i * (size + margin), ypos - 15, size, size);
            playerIcons[i].iconAnim = new Animation(anim_speed,
                                                    Assets.raphAtlas.findRegion(regionName, 0),
                                                    Assets.raphAtlas.findRegion(regionName, 1),
                                                    Assets.raphAtlas.findRegion(regionName, 2),
                                                    Assets.raphAtlas.findRegion(regionName, 3));
            playerIcons[i].iconAnim.setPlayMode(Animation.PlayMode.LOOP);
            playerIcons[i].iconTween = Tween.to(playerIcons[i].bounce, -1, 0.25f)
                                            .target(30)
                                            .ease(Linear.INOUT)
                                            .repeatYoyo(1, 0);
        }

        backgroundColor = new Color(1, 1, 1, 1);
        Tween.to(backgroundColor, ColorAccessor.R, 3).target(0).repeatYoyo(Tween.INFINITY, 1).start(GameInstance.tweens);
        Tween.to(backgroundColor, ColorAccessor.G, 4).target(0).repeatYoyo(Tween.INFINITY, 2).start(GameInstance.tweens);
        Tween.to(backgroundColor, ColorAccessor.B, 5).target(0).repeatYoyo(Tween.INFINITY, 3).start(GameInstance.tweens);
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();

        for (PlayerIcon icon : playerIcons) {
            icon.update(delta, mouseWorldCoords.x, mouseWorldCoords.y);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glClearColor(backgroundColor.r,
                            backgroundColor.g,
                            backgroundColor.b,
                            backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Assets.batch.setProjectionMatrix(camera.combined);
        Assets.batch.begin();
        for (PlayerIcon icon : playerIcons) {
            icon.draw(Assets.batch);
        }
        Assets.batch.end();
    }

    // -------------------------------------------------------------------------
    // Lifecycle Methods
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.position.set(0, 0, 0);
        camera.translate(width / 2, height / 2, 0);
        camera.update();
    }

    @Override
    public void show() {
        GameInstance.input.reset();
    }

    @Override
    public void hide() {
        GameInstance.input.reset();
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

    // -------------------------------------------------------------------------
    // Delegating Methods
    // ------------------
    // NOTE: mainly used for console commands, find a cleaner way to handle them
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // Implementation Details
    // -------------------------------------------------------------------------

    private PlayerIcon getSelectedIcon() {
        for (PlayerIcon playerIcon : playerIcons) {
            if (playerIcon.active) {
                return playerIcon;
            }
        }
        return playerIcons[0];
    }


    private void updateMouseVectors() {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        mouseScreenCoords.set(mx, my, 0);
        mouseWorldCoords.set(mx, my, 0);
        camera.unproject(mouseWorldCoords);
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    enum PlayerType {

        Cloak(0), Feather(1), Horns(2), Locks(3), NUM_PLAYERS(4);

        private int value;
        private PlayerType(int value) {
            this.value = value;
        }
        public int value() {
            return this.value;
        }

    }

    class PlayerIcon {

        final PlayerType type;

        Rectangle     bounds;
        float         iconTimer;
        Animation     iconAnim;
        Tween         iconTween;
        MutableFloat  bounce;
        boolean       active;


        public PlayerIcon(PlayerType type) {
            this.type = type;
            bounce = new MutableFloat(0);
            bounds = new Rectangle();
            iconTimer = 0;
            active = false;
        }

        public void update(float delta, float mousex, float mousey) {
            if (bounds.contains(mousex, mousey)) {
                if (iconTween.isFinished() || !iconTween.isStarted()) {
                    bounce.setValue(0);
                    iconTween = Tween.to(bounce, -1, 0.25f)
                                     .target(30)
                                     .ease(Linear.INOUT)
                                     .repeatYoyo(1, 0).start(GameInstance.tweens);
                }
                active = true;
                iconTimer += delta;
            } else {
                active = false;
                iconTimer = 0;
            }
        }

        public void draw(SpriteBatch batch) {
            float padding = 40;
            border.draw(batch, bounds.x, bounds.y + bounce.floatValue(), bounds.width, bounds.height);
            batch.draw(iconAnim.getKeyFrame(iconTimer),
                       bounds.x + padding, bounds.y + padding + bounce.floatValue(),
                       bounds.width - 2 * padding, bounds.height - 2 * padding);
        }

    }

}
