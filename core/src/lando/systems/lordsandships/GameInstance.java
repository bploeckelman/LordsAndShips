package lando.systems.lordsandships;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.screens.*;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.tweens.Vector3Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;
import lando.systems.lordsandships.utils.Input;

import java.util.HashMap;
import java.util.Map;

/**
 * LordsAndShips
 *
 * The main game class, responsible for startup and shutdown.
 * The rest of the behavior is handled by the current Screen.
 *
 * Brian Ploeckelman created on 5/27/2014
 */
public class GameInstance extends Game {
    public static final Input input = new Input();
    public static final TweenManager tweens = new TweenManager();
    public static final Map<String, UpdatingScreen> screens = new HashMap<String, UpdatingScreen>();

    public static final Vector2 mousePlayerDirection = new Vector2();

    @Override
    public void create () {
        Assets.load();

        Tween.registerAccessor(Vector2.class, new Vector2Accessor());
        Tween.registerAccessor(Vector3.class, new Vector3Accessor());
        Tween.registerAccessor(Color.class,   new ColorAccessor());

        Gdx.input.setInputProcessor(input);
        Gdx.input.setCursorCatched(false);

        screens.put(Constants.title_screen,         new TitleScreen(this));
        screens.put(Constants.game_screen,          new GameScreen(this));
        screens.put(Constants.player_select_screen, new PlayerSelectScreen(this));
        screens.put(Constants.map_editor_screen,    new MapEditorScreen(this));
        screens.put(Constants.test_screen,          new TestScreen(this));

//        setScreen(Constants.title_screen);
//        setScreen(Constants.game_screen);
//        setScreen(Constants.player_select_screen);
//        setScreen(Constants.map_editor_screen);
        setScreen(Constants.test_screen);
    }

    @Override
    public void render() {
//        if (input.isKeyDown(Keys.ESCAPE)) exit();

        final float delta = Gdx.graphics.getDeltaTime();

        tweens.update(delta);

        final UpdatingScreen screen = (UpdatingScreen) getScreen();
        if (screen != null) {
            screen.update(delta);
            screen.render(delta);
        }
    }

    @Override
    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        Assets.dispose();
    }

    public void setScreen(String screen) {
        super.setScreen(screens.get(screen));
    }

    public static void exit() {
        Gdx.app.exit();
    }

}
