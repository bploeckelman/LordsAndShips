package lando.systems.lordsandships.scene;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import lando.systems.lordsandships.utils.Utils;


/**
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/utils/OrthoCamController.java
 */
public class OrthoCamController extends InputAdapter {
	final OrthographicCamera camera;
	final Vector3 curr = new Vector3();
	final Vector3 last = new Vector3(-1, -1, -1);
	final Vector3 delta = new Vector3();
	final float zoom_scale = 0.025f;
	final float min_camera_zoom = 0.1f;
	final float initial_camera_zoom = 0.25f;

	public boolean debugRender = false;

	public OrthoCamController (OrthographicCamera camera) {
		this.camera = camera;
		this.camera.zoom = initial_camera_zoom;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
//		camera.unproject(curr.set(x, y, 0));
//		if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
//			camera.unproject(delta.set(last.x, last.y, 0));
//			delta.sub(curr);
//			camera.position.add(delta.x, delta.y, 0);
//		}
//		last.set(x, y, 0);
		return false;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		last.set(-1, -1, -1);
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		camera.zoom += zoom_scale * amount;
		if (camera.zoom < min_camera_zoom) {
			camera.zoom = min_camera_zoom;
		}
		return false;
	}

	@Override
	public boolean keyDown (int keycode) {
//		if (keycode == Keys.SPACE) debugRender = !debugRender;
		if (keycode == Keys.NUM_0) Utils.saveScreenshot();
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

}
