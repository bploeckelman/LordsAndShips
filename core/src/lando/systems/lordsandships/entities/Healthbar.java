package lando.systems.lordsandships.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 4/1/2015.
 */
public class Healthbar {

    public float     value;
    public float     maxValue;
    public float     pad;
    public Rectangle bounds;
    TextureRegion    inside;
    TextureRegion    outside;

    public Healthbar() {
        outside = new TextureRegion(Assets.raphAllAtlas.findRegion("television_blank"));
        inside = new TextureRegion(Assets.raphAllAtlas.findRegion("health"));
    }

    public void render(SpriteBatch batch) {
        batch.draw(outside, bounds.x, bounds.y, bounds.width, bounds.height);
        float inner_width = (value / maxValue) * (bounds.width - 2 * pad);
        batch.draw(inside, bounds.x + pad, bounds.y + pad, inner_width, bounds.height);
    }

}
