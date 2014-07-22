package lando.systems.lordsandships;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.screens.GameScreen;
import lando.systems.lordsandships.screens.TitleScreen;
import lando.systems.lordsandships.tweens.ColorAccessor;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Input;

/**
 * LordsAndShips
 *
 * The main game class, responsible for startup and shutdown.
 * The rest of the behavior is handled by the current Screen.
 *
 * Brian Ploeckelman created on 5/27/2014
 */
public class LordsAndShips extends Game {
	public final Input input = new Input();

	public TweenManager tween;
	public TitleScreen titleScreen;
	public GameScreen gameScreen;

	@Override
	public void create () {
		Assets.load();

		tween = new TweenManager();
		Tween.registerAccessor(Vector2.class, new Vector2Accessor());
		Tween.registerAccessor(Color.class,   new ColorAccessor());

		Gdx.input.setInputProcessor(input);
		Gdx.input.setCursorCatched(false);

		titleScreen = new TitleScreen(this);
		setScreen(titleScreen);
	}

	public void exit() {
		Gdx.app.exit();
	}

	@Override
	public void dispose() {
		if (gameScreen != null) gameScreen.dispose();
		if (titleScreen != null) titleScreen.dispose();
		Assets.dispose();
	}

}
