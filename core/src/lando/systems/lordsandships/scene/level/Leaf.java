package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Brian Ploeckelman created on 4/12/2015.
 */
public class Leaf {

    Rectangle bounds;
    Leaf      parent, child1, child2;
    Room room;
    int  level;

    public Leaf(Leaf parent, Rectangle bounds) {
        this.parent = parent;
        this.bounds = bounds;
        this.child1 = null;
        this.child2 = null;
        this.room   = null;
        this.level  = (parent == null) ? 1 : parent.level + 1;
    }

    public Room room() {
        return room;
    }

    public Array<Leaf> getLeaves() {
        Array<Leaf> leaves = new Array<Leaf>();

        if (child1 == null && child2 == null) {
            leaves.add(this);
        } else {
            if (child1 != null) leaves.addAll(child1.getLeaves());
            if (child2 != null) leaves.addAll(child2.getLeaves());
        }

        return leaves;
    }

    public Array<Leaf> getLevel(int i, Array<Leaf> queue) {
        if (queue == null) {
            queue = new Array<Leaf>();
        }

        if (i == 1) {
            queue.add(this);
        } else {
            if (child1 != null) child1.getLevel(i - 1, queue);
            if (child2 != null) child2.getLevel(i - 1, queue);
        }

        return queue;
    }

}
