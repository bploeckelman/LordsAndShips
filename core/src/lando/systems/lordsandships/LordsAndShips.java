package lando.systems.lordsandships;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.Constants;
import lando.systems.lordsandships.utils.Input;

import java.util.Random;

public class LordsAndShips extends Game {
	private lando.systems.lordsandships.utils.Input input;

	float r = 0.f, g = 0.f, b = 0.f;
	float accum = 0.f;
	float THRESHOLD = 0.016f;

	Random rand = new Random();
	
	@Override
	public void create () {
		Assets.load();

		input = new Input();
		Gdx.input.setInputProcessor(input);
		Gdx.input.setCursorCatched(false);
	}

	@Override
	public void dispose() {
		Assets.dispose();
	}

	@Override
	public void render () {
		if (input.isKeyDown(com.badlogic.gdx.Input.Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		Gdx.gl.glClearColor(r,g,b,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tick...
		accum += Gdx.graphics.getDeltaTime();
		if (accum > THRESHOLD) {
			accum = 0;

			r += rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;
			g += rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;
			b += rand.nextBoolean() ? (float) Math.random() * 0.01f : 0;

			if (r > 1.f) r = 1.f;
			if (g > 1.f) g = 1.f;
			if (b > 1.f) b = 1.f;
		}

		Assets.batch.begin();
		Assets.batch.draw(Assets.libgdx
				, Constants.win_half_width  - Assets.libgdx.getWidth()  / 2
				, Constants.win_half_height - Assets.libgdx.getHeight() / 2);
		Assets.batch.end();
	}
}
