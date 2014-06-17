package lando.systems.lordsandships.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.nio.ByteBuffer;

/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Utilities {

	public static TextureRegion[][] splitAndGet(Texture texture, int width, int height, int col, int row, int xTiles, int yTiles) {
		TextureRegion[][] allRegions = TextureRegion.split(texture, width, height);
		TextureRegion[][] regions = new TextureRegion[yTiles][xTiles];
		for (int y = 0; y < yTiles; ++y) {
			for (int x = 0; x < xTiles; ++x) {
				regions[y][x] = allRegions[row + y][col + x];
			}
		}
		return regions;
	}

	/**
	 * Get a Pixmap for the specified region of the screen
	 *
	 * https://github.com/libgdx/libgdx/wiki/Take-a-Screenshot
	 *
	 * @param x Lower left corner x coordinate
	 * @param y Lower left corner y coordinate
	 * @param w Width of Pixmap
	 * @param h Height of Pixmap
	 * @param yDown Whether to flip the Pixmap upside down or not
	 * @return A Pixmap corresponding to the specified region of the screen
	 */
	public static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown){
		final Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(x, y, w, h);

		if (yDown) {
			// Flip the pixmap upside down
			ByteBuffer pixels = pixmap.getPixels();
			int numBytes = w * h * 4;
			byte[] lines = new byte[numBytes];
			int numBytesPerLine = w * 4;
			for (int i = 0; i < h; i++) {
				pixels.position((h - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
		}

		return pixmap;
	}

	/**
	 * Save a screenshot of the current game window
	 *
	 * https://github.com/libgdx/libgdx/wiki/Take-a-Screenshot
	 */
	private static int screenshotCounter = 0;
	public static void saveScreenshot(){
		try{
			FileHandle fh;
			String id;
			do {
				id = String.format("%02d", ++screenshotCounter);
				fh = new FileHandle("LordsAndShips-Screenshot-" + id + ".png");
			} while (fh.exists());

			Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
			PixmapIO.writePNG(fh, pixmap);
			pixmap.dispose();
		}catch (Exception e){
			System.err.println("Error saving screenshot: " + e.getMessage() + "\n" + e.getStackTrace());
		}
	}

}
