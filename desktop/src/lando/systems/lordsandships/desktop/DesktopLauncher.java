package lando.systems.lordsandships.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.utils.Constants;

public class DesktopLauncher {
	public static void main (String[] arg) {
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.maxWidth = 1024;
		settings.maxHeight = 1024;
		TexturePacker.process(settings, "./images", "./atlas", "game");

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = Constants.win_width;
		config.height = Constants.win_height;
		new LwjglApplication(new LordsAndShips(), config);
	}
}
