package lando.systems.lordsandships.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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

	public static TextureRegion libgdx;
	public static TextureRegion gametex;

	public static TextureAtlas atlas;


	public static void load() {
		rand = new Random();

		batch = new SpriteBatch();

		atlas = new TextureAtlas(Gdx.files.internal("atlas/game.atlas"));
		libgdx = atlas.findRegion("badlogic");
		gametex = atlas.findRegion("gametex");
	}

	public static void dispose() {
		atlas.dispose();
		batch.dispose();
	}

}
