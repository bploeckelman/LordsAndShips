package lando.systems.lordsandships.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import lando.systems.lordsandships.GameInstance;
import lando.systems.lordsandships.utils.Constants;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		TexturePacker.Settings settings = new TexturePacker.Settings();
//		settings.filterMin = Texture.TextureFilter.MipMapNearestNearest;
//		settings.filterMag = Texture.TextureFilter.MipMapNearestNearest;
//		settings.maxWidth = 1024;
//		settings.maxHeight = 1024;
//		TexturePacker.process(settings,
//		                      "C:\\Users\\brian_000\\Desktop\\workspace\\0-raph\\converted",
//		                      "C:\\Users\\brian_000\\Desktop\\workspace\\0-raph\\Pixels\\allabaster\\characters\\0_all\\atlas",
//		                      "raph-collection");
//		TexturePacker.process(settings, "./images", "./atlas", "game");

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = Constants.win_width;
		config.height = Constants.win_height;
		config.resizable = false;
		new LwjglApplication(new GameInstance(), config);
	}
}
