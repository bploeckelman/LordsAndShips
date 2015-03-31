package lando.systems.lordsandships.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;


/**
 * Brian Ploeckelman created on 6/17/2014.
 */
public class Utils {

    /**
     * Split a texture into regions of the specified size split along a grid
     * starting at the specified row and column, splitting into the specified
     * number of tiles in both x and y directions.
     *
     * @param texture The texture to split into regions
     * @param width The width (in pixels) of a single region
     * @param height The height (in pixels) of a single region
     * @param col The tile column of the first tile to extract
     * @param row The tile row of the first tile to extract
     * @param xTiles The number of tiles to extract horizontally
     * @param yTiles The number of tiles to extract vertically
     * @return The texture regions that the texture was split into
     */
    public static TextureRegion[][] splitAndGet(Texture texture, int width, int height, int col, int row, int xTiles, int yTiles) {
        TextureRegion[][] allRegions = TextureRegion.split(texture, width, height);
        TextureRegion[][] regions = new TextureRegion[yTiles][xTiles];
        for (int y = 0; y < yTiles; ++y) {
            for (int x = 0; x < xTiles; ++x) {
                regions[y][x] = allRegions[row + y][col + x];
            }
        }
        return regions;
    }

    /**
     * Clamp the value of 'value' between min and max
     *
     * @param value The value to clamp
     * @param min The minimum possible value
     * @param max The maximum possible value
     * @return The clamped value
     */
    public static float clampf(float value, float min, float max) {
        assert(min < max);
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    /**
     * Doug's screen shaker class
     */
    public static class Shake {

        private static final int default_frequency = 35;
        private static final int default_amplitude = 10;

        float[] samples;
        Random rand          = new Random();
        float  internalTimer = 0;
        float  shakeDuration = 0;

        int     duration  = 5; // In seconds, make longer if you want more variation
        int     frequency = default_frequency; // hertz

        public float   amplitude = default_amplitude; // how much you want to shake
        public boolean falloff   = true; // if the shake should decay as it expires

        int sampleCount;

        public Shake() {
            this(default_frequency, default_amplitude);
        }

        public Shake(int frequency, int amplitude) {
            this.frequency = frequency;
            this.amplitude = amplitude;

            sampleCount = duration * frequency;
            samples = new float[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                samples[i] = rand.nextFloat() * 2f - 1f;
            }
        }

        /**
         * Called every frame will shake the camera if it has a shake duration
         *
         * @param dt     Gdx.graphics.getDeltaTime() or your dt in seconds
         * @param camera your camera
         * @param center Where the camera should stay centered on
         */
        public void update(float dt, Camera camera, Vector2 center) {
            update(dt, camera, center.x, center.y);
        }

        public void update(float dt, Camera camera, float centerx, float centery) {
            internalTimer += dt;
            if (internalTimer > duration) internalTimer -= duration;
            if (shakeDuration > 0) {
                shakeDuration -= dt;
                float shakeTime = (internalTimer * frequency);
                int first = (int) shakeTime;
                int second = (first + 1) % sampleCount;
                int third = (first + 2) % sampleCount;
                float deltaT = shakeTime - (int) shakeTime;
                float deltaX = samples[first] * deltaT + samples[second] * (1f - deltaT);
                float deltaY = samples[second] * deltaT + samples[third] * (1f - deltaT);

                camera.position.x = centerx + deltaX * amplitude * (falloff ? Math.min(shakeDuration, 1f) : 1f);
                camera.position.y = centery + deltaY * amplitude * (falloff ? Math.min(shakeDuration, 1f) : 1f);
                camera.update();
            }
        }

        /**
         * Will make the camera shake for the duration passed in in seconds
         *
         * @param d duration of the shake in seconds
         */
        public void shake(float d) {
            shakeDuration = d;
        }
    }

}
