package lando.systems.lordsandships.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * Input
 *
 * Input processor
 *
 * Brian Ploeckelman created on 5/27/2014.
 */
public class Input extends InputAdapter {
	private final Vector2 prevMouse= new Vector2(0,0);
	private final Vector2 currMouse = new Vector2(0,0);
	private final boolean[] keys = new boolean[256];
	private final boolean[] buttons = new boolean[3];

	public Input() {
		super();
		reset();
	}

	public void reset() {
		resetKeys();
		resetButtons();
	}

	public void resetKeys() {
		for(int i = 0; i < keys.length; ++i) {
			keys[i] = false;
		}
	}

	public void resetButtons() {
		for(int i = 0; i < buttons.length; ++i) {
			buttons[i] = false;
		}
	}

	public boolean isKeyDown(int keycode) {
		return keys[keycode];
	}

	public boolean isKeyUp(int keycode) {
		return !keys[keycode];
	}

	public boolean isButtonDown(int button) {
		return buttons[button];
	}

	public boolean isButtonUp(int button) {
		return !buttons[button];
	}

	public Vector2 getCurrMouse() { return currMouse; }
	public Vector2 getPrevMouse() { return prevMouse; }

	@Override
	public boolean keyDown(int keycode) {
		keys[keycode] = true;
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		keys[keycode] = false;
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		prevMouse.set(currMouse);
		currMouse.set(screenX, screenY);
		buttons[button] = true;
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		prevMouse.set(currMouse);
		currMouse.set(screenX, screenY);
		buttons[button] = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		prevMouse.set(currMouse);
		currMouse.set(screenX, screenY);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		prevMouse.set(currMouse);
		currMouse.set(screenX, screenY);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
