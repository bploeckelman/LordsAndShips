package lando.systems.lordsandships.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

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
	public static ShapeRenderer shapes;

	public static TextureRegion libgdx;
	public static TextureRegion gametex;
	public static Texture playertex;
	public static Texture enemytex;

	public static TextureAtlas atlas;


	public static void load() {
		rand = new Random(1);

		batch = new SpriteBatch();
		shapes = new ShapeRenderer();

		atlas = new TextureAtlas(Gdx.files.internal("atlas/game.atlas"));
		libgdx = atlas.findRegion("badlogic");
		gametex = atlas.findRegion("gametex");

		playertex = new Texture("darkknight.png");
		enemytex = new Texture("character-sheet.png");
	}

	public static void dispose() {
		atlas.dispose();
		enemytex.dispose();
		playertex.dispose();
		shapes.dispose();
		batch.dispose();
	}

}
