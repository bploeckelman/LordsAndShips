package lando.systems.lordsandships.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.lordsandships.LordsAndShips;
import lando.systems.lordsandships.tweens.Vector2Accessor;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;

/**
 * TitleScreen
 *
 * The game's title screen
 *
 * Brian Ploeckelman created on 5/27/2014.
 */
public class TitleScreen implements Screen {
	private static final String title1 = "BattleLord Spaceships";
	private static final String title2 = "         vs";
	private static final String title3 = "SpaceLord Battleships";

	private final LordsAndShips game;

	private BitmapFont font;
	private OrthographicCamera camera;
	private TextureRegion background;

	private Vector2 titlePosLine1 = new Vector2();
	private Vector2 titlePosLine2 = new Vector2();
	private Vector2 titlePosLine3 = new Vector2();

	public TitleScreen(LordsAndShips game) {
		super();

		this.game = game;

		font = new BitmapFont(Gdx.files.internal("fonts/jupiter.fnt"), false);
		font.setColor(Color.WHITE);
		font.setScale(2);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Constants.win_width, Constants.win_height);
		camera.update();

		float title_line2_height = Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 2) + (font.getLineHeight() / 2);

		titlePosLine1.set(Constants.win_width, Gdx.graphics.getHeight() - 50);
		titlePosLine2.set(145, Gdx.graphics.getHeight() + title_line2_height);
		titlePosLine3.set(-Constants.win_width, font.getLineHeight() + 50);

		Timeline.createSequence()
				.push(Tween.to(titlePosLine1, Vector2Accessor.XY, 0.5f)
						.target(140, Gdx.graphics.getHeight() - 50)
						.ease(Back.OUT))
				.pushPause(0.1f)
				.push(Tween.to(titlePosLine2, Vector2Accessor.XY, 0.5f)
						.target(140, title_line2_height)
						.ease(Bounce.INOUT))
				.pushPause(0.5f)
				.push(Tween.to(titlePosLine3, Vector2Accessor.XY, 0.5f)
						.target(140, font.getLineHeight() + 50)
						.ease(Back.OUT))
				.start(game.tween);

		background = Assets.atlas.findRegion("gametex");
	}

	public void update(float delta) {
		if (game.input.isKeyDown(Keys.ESCAPE)) {
			game.exit();
		} else if (Gdx.input.justTouched()) {
			game.gameScreen = new GameScreen(game);
			game.setScreen(game.gameScreen);
		}

		game.tween.update(delta);

		camera.update();
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Assets.batch.setProjectionMatrix(camera.combined);
		Assets.batch.begin();
		Assets.batch.draw(background
				, Constants.win_half_width  - background.getRegionWidth() / 2
				, Constants.win_half_height - background.getRegionHeight() / 2);
		font.drawMultiLine(Assets.batch, title1, titlePosLine1.x, titlePosLine1.y);
		font.drawMultiLine(Assets.batch, title2, titlePosLine2.x, titlePosLine2.y);
		font.drawMultiLine(Assets.batch, title3, titlePosLine3.x, titlePosLine3.y);
		Assets.batch.end();
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
		camera.update();
	}

	@Override
	public void show() {
		game.input.reset();
	}

	@Override
	public void hide() {
		game.input.reset();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
