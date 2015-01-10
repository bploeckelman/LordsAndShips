package lando.systems.lordsandships.scene;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 1/9/2015.
 */
public class ParallaxBackround {

    static final float CAMERA_Z = 0.f;

    Rectangle            bounds;
    ParallaxCamera       camera;
    Array<TextureRegion> layers;

    Matrix4 parallaxMatrix = new Matrix4();


    public ParallaxBackround(float posx,
                             float posy,
                             int width,
                             int height,
                             Rectangle bounds,
                             TextureRegion... layers) {
        this.bounds = new Rectangle(bounds);
        this.camera = new ParallaxCamera(width, height);
        this.camera.position.set(posx, posy, CAMERA_Z);
        this.layers = new Array<TextureRegion>();
        for (TextureRegion layer : layers) {
            this.layers.add(layer);
        }
    }

    public void position(float x, float y) {
        camera.position.set(x, y, CAMERA_Z);

//        if (camera.position.x < bounds.width / 2 - 100) camera.position.x = bounds.width / 2 - 100;
//        if (camera.position.x > bounds.width / 2 + 100) camera.position.x = bounds.width / 2 + 100;
//        if (camera.position.y < bounds.height / 2 - 100) camera.position.y = bounds.height / 2 - 100;
//        if (camera.position.y < bounds.height / 2 + 100) camera.position.y = bounds.height / 2 + 100;

        camera.update();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void render(SpriteBatch batch) {
        parallaxMatrix.set(camera.calculateParallaxMatrix(0f, 0f));
        batch.setProjectionMatrix(parallaxMatrix);
        batch.disableBlending();
        batch.begin();
        batch.draw(layers.first(),
                   bounds.x - bounds.width / 2,
                   bounds.y - bounds.height / 2,
                   bounds.width, bounds.height);
        batch.end();
        batch.enableBlending();

        for (int i = 1; i < layers.size; ++i) {
            final TextureRegion layer = layers.get(i);
            float w = layer.getRegionWidth();
            float h = layer.getRegionHeight();
            float scale = (float) i / (float) (layers.size - 1);

            parallaxMatrix.set(camera.calculateParallaxMatrix(scale, scale));
            batch.setProjectionMatrix(parallaxMatrix);
            batch.begin();
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 6; ++y) {
                    batch.draw(layer, bounds.x + x * w, bounds.y + y * h);
                }
            }
            batch.end();
        }
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------
    class ParallaxCamera extends OrthographicCamera {
        Matrix4 parallaxView     = new Matrix4();
        Matrix4 parallaxCombined = new Matrix4();
        Vector3 tmp              = new Vector3();
        Vector3 tmp2             = new Vector3();

        public ParallaxCamera(float viewportWidth, float viewportHeight) {
            super(viewportWidth, viewportHeight);
        }

        public Matrix4 calculateParallaxMatrix(float parallaxX,
                                               float parallaxY) {
            update();
            tmp.set(position);
            tmp.x *= parallaxX;
            tmp.y *= parallaxY;

            parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
            parallaxCombined.set(projection);
            Matrix4.mul(parallaxCombined.val, parallaxView.val);
            return parallaxCombined;
        }
    }

}
