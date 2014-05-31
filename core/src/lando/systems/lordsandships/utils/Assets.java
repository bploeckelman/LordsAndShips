package lando.systems.lordsandships.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Random;

/**
 * Assets
 *
 * Contains all game assets
 *
 * Brian Ploeckelman created on 5/27/2014.
 */
public class Assets {

	public static Random rand;

	public static SpriteBatch batch;

	public static Texture libgdx;
	public static Texture gametex;
	public static Texture tiles;


	public static void load() {
		rand = new Random();

		batch = new SpriteBatch();

		libgdx = new Texture("badlogic.jpg");
		gametex = new Texture("gametex.png");
		tiles = new Texture("tiles.png");
	}

	public static void dispose() {
		tiles.dispose();
		gametex.dispose();
		libgdx.dispose();
		batch.dispose();
	}

}
