package lando.systems.lordsandships.scene.level.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Brian Ploeckelman created on 4/11/2015.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Light extends GameObject {

    private static final float default_size = 200f;

    Vector2       position;
    Vector2       size;
    Color         color;
    TextureRegion customTexture;
    boolean       enabled;

    // TODO : add pulse, color change characteristics

    public Light() {
        position      = new Vector2();
        size          = new Vector2(default_size, default_size);
        color         = new Color(1, 1, 1, 1);
        customTexture = null;
        enabled       = false;
    }

    public void enable()  { enabled = true;  }
    public void disable() { enabled = false; }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setSize(float w, float h) {
        size.set(w, h);
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

}
