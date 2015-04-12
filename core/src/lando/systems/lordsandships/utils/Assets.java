package lando.systems.lordsandships.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;

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

    public static SpriteBatch   batch;
    public static ShapeRenderer shapes;
    public static ShaderProgram multitexShaderProgram;
    public static ShaderProgram postShaderProgram;
    public static ShaderProgram ambientShaderProgram;
    public static ShaderProgram testShaderProgram;
    public static BitmapFont    font;

    public static Texture oryxWorld;
    public static Texture oryxCreatures;

    public static Texture starfieldLayer0;
    public static Texture starfieldLayer1;
    public static Texture titleScreen;

    public static Texture       lightmaptex;
    public static Texture       playertex;
    public static Texture       enemytex;
    public static Texture       avatartex;
    public static Texture       healthIconFull;
    public static Texture       healthIconHalf;
    public static Texture       healthIconEmpty;
    public static TextureRegion shadow;

    public static TextureAtlas atlas;
    public static TextureAtlas uiAtlas;
    public static TextureAtlas raphAtlas;
    public static TextureAtlas raphAllAtlas;
    public static TextureAtlas collection;

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
    public static Sound bow_shot1;
    public static Sound spear_stab1;
    public static Sound axe_swing1;
    public static Sound bing;

    public static void load() {
        rand = new Random(1);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        final FileHandle vertSource = Gdx.files.internal("shaders/default.vert");
        multitexShaderProgram = compileShaderProgram(vertSource, Gdx.files.internal("shaders/multitex.frag"));
        postShaderProgram = compileShaderProgram(vertSource, Gdx.files.internal("shaders/post.frag"));
        ambientShaderProgram = compileShaderProgram(vertSource, Gdx.files.internal("shaders/ambient.frag"));
        testShaderProgram = compileShaderProgram(vertSource, Gdx.files.internal("shaders/test.frag"));

        font = new BitmapFont();

        atlas = new TextureAtlas(Gdx.files.internal("atlas/game.atlas"));
        uiAtlas = new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas"));
        raphAtlas = new TextureAtlas(Gdx.files.internal("raph-atlas/raph.atlas"));
        raphAllAtlas = new TextureAtlas(Gdx.files.internal("raph-atlas/raph-atlas-all.atlas"));
        collection = new TextureAtlas(Gdx.files.internal("raph-atlas/raph-collection.atlas"));

        oryxWorld = new Texture("oryx/world.png");
        oryxCreatures = new Texture("oryx/creatures.png");

        starfieldLayer0 = new Texture("starfield_0.png");
        starfieldLayer1 = new Texture("starfield_1.png");
        titleScreen = new Texture("raph-atlas/misc/towertitle.png");

        lightmaptex = new Texture("lightmap1.png");
        playertex = new Texture("darkknight.png");
        enemytex = new Texture("character-sheet.png");
        avatartex = new Texture("avatar.png");
        healthIconFull = new Texture("raph-atlas/misc/healthicon.png");
        healthIconHalf = new Texture("raph-atlas/misc/healthicon_half.png");
        healthIconEmpty = new Texture("raph-atlas/misc/healthicon_empty.png");
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
        bow_shot1 = Gdx.audio.newSound(Gdx.files.internal("sounds/bow-shot-1.mp3"));
        spear_stab1 = Gdx.audio.newSound(Gdx.files.internal("sounds/spear-stab-1.mp3"));
        axe_swing1 = Gdx.audio.newSound(Gdx.files.internal("sounds/axe-swing-1.mp3"));
        bing = Gdx.audio.newSound(Gdx.files.internal("sounds/bing.mp3"));
    }

    public static void dispose() {
        bing.dispose();
        axe_swing1.dispose();
        spear_stab1.dispose();
        bow_shot1.dispose();
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

        collection.dispose();
        raphAllAtlas.dispose();
        raphAtlas.dispose();
        uiAtlas.dispose();
        atlas.dispose();
        enemytex.dispose();
        playertex.dispose();
        avatartex.dispose();
        healthIconFull.dispose();
        healthIconHalf.dispose();
        healthIconEmpty.dispose();
        lightmaptex.dispose();

        starfieldLayer1.dispose();
        starfieldLayer0.dispose();

        oryxCreatures.dispose();
        oryxWorld.dispose();

        font.dispose();

        ambientShaderProgram.dispose();
        postShaderProgram.dispose();
        multitexShaderProgram.dispose();
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

    public static void renderRect(Rectangle rectangle) {
        Assets.shapes.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    private static ShaderProgram compileShaderProgram(FileHandle vertSource, FileHandle fragSource) {
        ShaderProgram shader = new ShaderProgram(vertSource, fragSource);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Failed to compile shader program:\n" + shader.getLog());
        }
        else if (shader.getLog().length() > 0) {
            Gdx.app.error("SHADER", "ShaderProgram compilation log:\n" + shader.getLog());
        }
        return shader;
    }

}
