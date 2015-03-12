package lando.systems.lordsandships.scene.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.lordsandships.utils.Assets;

/**
 * Brian Ploeckelman created on 3/11/2015.
 */
public class BSP {

    class Leaf {
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

    Leaf root;
    int  depth;

    final boolean discard_by_ratio   = true;
    final float   split_ratio_height = 0.45f;
    final float   split_ratio_width  = 0.45f;

    public BSP(Rectangle rootRect, int depth) {
        root = new Leaf(null, rootRect);
        partition(root, depth);

        BSP.Leaf leaf = root;
        while (leaf.child1 != null) {
            leaf = leaf.child1;
        }
        this.depth = leaf.level;
    }

    Array<Leaf> leafCache = null;
    public Array<Leaf> getLeaves() {
        if (leafCache == null) {
            leafCache = root.getLeaves();
        }
        return leafCache;
    }

    public Array<Leaf> getLevel(int i, Array<Leaf> queue) {
        return root.getLevel(i, queue);
    }

    private Leaf partition(Leaf leaf, int iteration) {
        if (iteration != 0) {
            //Gdx.app.log("PARTITION", "iter(" + iteration + "); partitioning leaf " + leaf.rect.toString());
            Leaf[] children = splitLeaf(leaf);
            leaf.child1 = partition(children[0], iteration - 1);
            leaf.child2 = partition(children[1], iteration - 1);
        }
        return leaf;
    }

    private Leaf[] splitLeaf(Leaf leaf) {
        Leaf[] children = new Leaf[2];
        if (leaf == null || leaf.bounds == null) {
            return children;
        }

        Rectangle[] rects = new Rectangle[2];

        if (Assets.rand.nextBoolean()) {
            // Split vertical
            int n = (int) leaf.bounds.height;
            float split_size = Assets.rand.nextInt(n) + 1;
            //Gdx.app.log("SPLIT_LEAF", "\tvertical split: " + split_size + " for n(" + n + ")");

            rects[0] = new Rectangle(leaf.bounds.x, leaf.bounds.y, leaf.bounds.width, split_size);
            rects[1] = new Rectangle(leaf.bounds.x,
                                     leaf.bounds.y + rects[0].height,
                                     leaf.bounds.width,
                                     leaf.bounds.height - rects[0].height);

            if (discard_by_ratio) {
                float rect0_ratio_h = rects[0].height / rects[0].width;
                float rect1_ratio_h = rects[1].height / rects[1].width;
                if (rect0_ratio_h < split_ratio_height || rect1_ratio_h < split_ratio_height) {
                    //Gdx.app.log("DISCARD", "discarding split ratio: 0->" + rect0_ratio_h + ", 1->" + rect1_ratio_h);
                    return splitLeaf(leaf);
                } else {
                    //Gdx.app.log("ACCEPT", "accepting split ratio: 0->" + rect0_ratio_h + ", 1->" + rect1_ratio_h);
                }
            }
        } else {
            // Split horizontal
            int n = (int) leaf.bounds.width;
            float split_size = Assets.rand.nextInt(n);
            //Gdx.app.log("SPLIT_LEAF", "\thorizontal split: " + split_size + " for n(" + n + ")");

            rects[0] = new Rectangle(leaf.bounds.x, leaf.bounds.y, split_size, leaf.bounds.height);
            rects[1] = new Rectangle(leaf.bounds.x + rects[0].width,
                                     leaf.bounds.y,
                                     leaf.bounds.width - rects[0].width,
                                     leaf.bounds.height);


            if (discard_by_ratio) {
                float rect0_ratio_w = rects[0].width / rects[0].height;
                float rect1_ratio_w = rects[1].width / rects[1].height;
                if (rect0_ratio_w < split_ratio_width || rect1_ratio_w < split_ratio_width) {
                    //Gdx.app.log("DISCARD", "discarding split ratio: 0->" + rect0_ratio_w + ", 1->" +
                    // rect1_ratio_w);
                    return splitLeaf(leaf);
                } else {
                    //Gdx.app.log("ACCEPT", "accepting split ratio: 0->" + rect0_ratio_w + ", 1->" + rect1_ratio_w);
                }
            }
        }

        children[0] = new Leaf(leaf, rects[0]);
        children[1] = new Leaf(leaf, rects[1]);

        return children;
    }
}
