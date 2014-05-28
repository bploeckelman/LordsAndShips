package lando.systems.lordsandships;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import lando.systems.lordsandships.screens.TitleScreen;
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

	public TitleScreen titleScreen;

	@Override
	public void create () {
		Assets.load();

		Gdx.input.setInputProcessor(input);
		Gdx.input.setCursorCatched(false);

		titleScreen = new TitleScreen(this);
		setScreen(titleScreen);
	}

	@Override
	public void dispose() {
		Assets.dispose();
	}

}
