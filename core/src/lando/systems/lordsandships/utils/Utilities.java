package lando.systems.lordsandships.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
}
