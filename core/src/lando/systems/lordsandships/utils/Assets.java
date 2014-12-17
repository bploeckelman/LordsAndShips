package lando.systems.lordsandships.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

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

    public static Texture playertex;
    public static Texture enemytex;
    public static Texture avatartex;
    public static TextureRegion shadow;

    public static TextureAtlas atlas;
    public static TextureAtlas world;

    public static Sound gunshot_shot;
    public static Sound gunshot_impact;
    public static Sound gunshot_empty;
    public static Sound gunshot_reload;
    public static Sound hit1;
    public static Sound hit2;
    public static Sound hit3;
    public static Sound hit4;
    public static Sound hit_melee1;
    public static Sound sword_slice1;

    public static void load() {
        rand = new Random(1);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        atlas = new TextureAtlas(Gdx.files.internal("atlas/game.atlas"));
        world = new TextureAtlas(Gdx.files.internal("atlas/world.atlas"));

        playertex = new Texture("darkknight.png");
        enemytex = new Texture("character-sheet.png");
        avatartex = new Texture("avatar.png");
        shadow = atlas.findRegion("shadow");

        gunshot_shot   = Gdx.audio.newSound(Gdx.files.internal("sounds/gunshot-shot-1.mp3"));
        gunshot_impact = Gdx.audio.newSound(Gdx.files.internal("sounds/gunshot-impact-1.mp3"));
        gunshot_empty  = Gdx.audio.newSound(Gdx.files.internal("sounds/gunshot-empty-1.mp3"));
        gunshot_reload = Gdx.audio.newSound(Gdx.files.internal("sounds/gunshot-reload-1.mp3"));
        hit1 = Gdx.audio.newSound(Gdx.files.internal("sounds/hit-1.mp3"));
        hit2 = Gdx.audio.newSound(Gdx.files.internal("sounds/hit-2.mp3"));
        hit3 = Gdx.audio.newSound(Gdx.files.internal("sounds/hit-3.mp3"));
        hit4 = Gdx.audio.newSound(Gdx.files.internal("sounds/hit-4.mp3"));
        hit_melee1 = Gdx.audio.newSound(Gdx.files.internal("sounds/hit-melee-1.mp3"));
        sword_slice1 = Gdx.audio.newSound(Gdx.files.internal("sounds/sword-slice-1.mp3"));
    }

    public static void dispose() {
        sword_slice1.dispose();
        hit_melee1.dispose();
        hit4.dispose();
        hit3.dispose();
        hit2.dispose();
        hit1.dispose();
        gunshot_reload.dispose();
        gunshot_empty.dispose();
        gunshot_impact.dispose();
        gunshot_shot.dispose();

        world.dispose();
        atlas.dispose();
        enemytex.dispose();
        playertex.dispose();
        avatartex.dispose();

        shapes.dispose();
        batch.dispose();
    }

    public static Sound getRandomHitSound() {
        switch (MathUtils.random(1,4)) {
            case 1: return hit1;
            case 2: return hit2;
            case 3: return hit3;
            case 4: return hit4;
        }
        return hit1;
    }

}