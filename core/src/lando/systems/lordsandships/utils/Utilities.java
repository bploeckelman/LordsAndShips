package lando.systems.lordsandships.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Utilities {

	/**
	 * Split a texture into regions of the specified size split along a grid
	 * starting at the specified row and column, splitting into the specified
	 * number of tiles in both x and y directions.
	 *
	 * @param texture The texture to split into regions
	 * @param width The width (in pixels) of a single region
	 * @param height The height (in pixels) of a single region
	 * @param col The tile column of the first tile to extract
	 * @param row The tile row of the first tile to extract
	 * @param xTiles The number of tiles to extract horizontally
	 * @param yTiles The number of tiles to extract vertically
	 * @return The texture regions that the texture was split into
	 */
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
