package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Elastic;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quint;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * Brian Ploeckelman created on 2/23/2015.
 */
public class PlayerSelectScreen extends InputAdapter implements UpdatingScreen {
    private final GameInstance game;

    private OrthographicCamera camera;

    private Vector3 mouseWorldCoords  = new Vector3();
    private Vector3 mouseScreenCoords = new Vector3();

    private PlayerIcon selectedPlayer;
    private PlayerIcon playerIcons[];
    private NinePatch  border;
    private Color      backgroundColor;

    private Animation  selectAnim;
    private float      selectAnimTimer;
    private Rectangle  selectAnimBounds;

    final float anim_speed = 0.3f;

    public PlayerSelectScreen(GameInstance game) {
        super();

        this.game = game;

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
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);

        final int   num_players = PlayerType.NUM_PLAYERS.value();
        final float size = camera.viewportWidth / 6;
        final float ypos = camera.viewportHeight / 2 - size / 2;
        final float margin = size / 4;
        final float padding = (camera.viewportWidth -
                               (num_players * size + (num_players - 1) * margin)) / 2;

        selectedPlayer = null;
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
            String iconRegionName = "sHero" + playerType.name() + "Icon";
            playerIcons[i].iconTexture = new TextureRegion(Assets.raphAtlas.findRegion(iconRegionName, 0));
        }

        selectAnim = null;
        selectAnimTimer = 0;
        selectAnimBounds = new Rectangle();

        backgroundColor = new Color(1, 1, 1, 1);
    }

    // -------------------------------------------------------------------------
    // Game Loop Methods
    // -------------------------------------------------------------------------

    @Override
    public void update(float delta) {
        updateMouseVectors();

        if (selectAnim != null && selectedPlayer != null) {
            selectAnimTimer += delta;
            if (selectAnimTimer >= selectAnim.getAnimationDuration()) {
                selectAnimTimer = 0;
                selectAnim = null;
                launchGame(selectedPlayer.type);
            }
        } else {
            for (PlayerIcon icon : playerIcons) {
                icon.update(delta, mouseWorldCoords.x, mouseWorldCoords.y);
            }

            final PlayerIcon currentSelectedPlayer = getSelectedIcon();
            if (selectedPlayer != currentSelectedPlayer) {
                selectedPlayer = currentSelectedPlayer;

                if (selectedPlayer != null) {
                    final Color newColor = selectedPlayer.type.color();
                    Tween.to(backgroundColor, ColorAccessor.RGB, anim_speed * 2)
                         .target(newColor.r, newColor.g, newColor.b)
                         .ease(Quint.OUT)
                         .start(GameInstance.tweens);
                }
            }
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
        if (selectAnim != null) {
            Assets.batch.draw(selectAnim.getKeyFrame(selectAnimTimer),
                              selectAnimBounds.x,
                              selectAnimBounds.y,
                              selectAnimBounds.width,
                              selectAnimBounds.height);
        }
        Assets.batch.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (selectedPlayer == null) {
            return super.touchDown(screenX, screenY, pointer, button);
        }

        // Start the attack animation for the selected player
        if (selectAnim == null) {
            selectAnim = selectedPlayer.attackAnim;
            selectAnimTimer = 0;
            selectAnimBounds.set(selectedPlayer.bounds);
            selectAnimBounds.y += 100;
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    private void launchGame(PlayerType selectedPlayerType) {
        GameScreen gameScreen = (GameScreen) GameInstance.screens.get(Constants.game_screen);
        gameScreen.create(selectedPlayerType);
        game.setScreen(Constants.game_screen);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            GameInstance.exit();
        }
        return true;
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
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(GameInstance.input);
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    @Override
    public void hide() {
        GameInstance.input.reset();
        Gdx.input.setInputProcessor(GameInstance.input);
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
        return null;
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

    public enum PlayerType {

        Cloak(0), Feather(1), Horns(2), Locks(3), NUM_PLAYERS(4);

        private static final Color[] colors = {
                new Color(0.466f, 0.702f, 0.404f, 1),
                new Color(0.984f, 0.396f, 0.000f, 1),
                new Color(0.698f, 0.397f, 0.718f, 1),
                new Color(0.411f, 0.592f, 0.922f, 1)
        };

        private int value;
        private PlayerType(int value) {
            this.value = value;
        }
        public int value() {
            return this.value;
        }
        public final Color color() {
            return colors[value];
        }

    }

    public class PlayerIcon {

        final PlayerType type;

        Rectangle     bounds;
        TextureRegion iconTexture;
        float         iconTimer;
        Animation     iconAnim;
        Tween         iconTween;
        MutableFloat  bounce;
        boolean       active;
        Animation     attackAnim;


        public PlayerIcon(PlayerType type) {
            this.type = type;
            bounce = new MutableFloat(0);
            bounds = new Rectangle();
            iconTimer = 0;
            active = false;

            attackAnim = createAttackAnim(type);
        }

        public void update(float delta, float mousex, float mousey) {
            if (bounds.contains(mousex, mousey)) {
                if (!active) {
                    iconTween = Tween.to(bounce, -1, anim_speed)
                                     .target(100)
                                     .ease(Elastic.OUT)
                                     .start(GameInstance.tweens);
                }
                active = true;
                iconTimer += delta;
            } else {
                if (active) {
                    iconTween = Tween.to(bounce, -1, anim_speed)
                                     .target(0)
                                     .ease(Elastic.OUT)
                                     .start(GameInstance.tweens);
                }
                active = false;
                iconTimer = 0;
            }
        }

        public void draw(SpriteBatch batch) {
            float padding = 40;

            batch.draw(iconTexture,
                       bounds.x,
                       bounds.y - bounce.floatValue(),
                       bounds.width,
                       bounds.height);
            border.draw(batch,
                        bounds.x,
                        bounds.y + bounce.floatValue(),
                        bounds.width,
                        bounds.height);
            batch.draw(iconAnim.getKeyFrame(iconTimer),
                       bounds.x + padding,
                       bounds.y + padding + bounce.floatValue(),
                       bounds.width - 2 * padding,
                       bounds.height - 2 * padding);
        }

        private Animation createAttackAnim(PlayerType type) {
            String regionName;
            int    num_keyframes;
            switch (type.value) {
                case 0:
                    regionName = "sBow";
                    num_keyframes = 8;
                    break;
                case 1:
                    regionName = "sSpear";
                    num_keyframes = 16;
                    break;
                case 2:
                    regionName = "sAxe";
                    num_keyframes = 24;
                    break;
                case 3:
                    regionName = "sSword";
                    num_keyframes = 12;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot create player attack animation for invalid type: " + type.toString());
            }

            // Super hack, super hack, its super hacky... yeowwwwwww
            TextureRegion[] keyframes;
            if (type.value == 0 || type.value == 3) {
                keyframes = new TextureRegion[2*num_keyframes];
                for (int i = 0; i < 2*num_keyframes; ++i) {
                    keyframes[i] = new TextureRegion(Assets.raphAtlas.findRegion(regionName, i % num_keyframes));
                }
            } else {
                keyframes = new TextureRegion[num_keyframes];
                for (int i = 0; i < num_keyframes; ++i) {
                    keyframes[i] = new TextureRegion(Assets.raphAtlas.findRegion(regionName, i));
                }
            }
            return new Animation(0.05f, keyframes);
        }

    }

}
