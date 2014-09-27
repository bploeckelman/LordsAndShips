package lando.systems.lordsandships.scene.levelgen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.*;

/**
 * Random level generator based on the following algorithm:
 *
 * TinyDungeon Generation Algorithm
 * http://www.reddit.com/r/gamedev/comments/1dlwc4/procedural_dungeon_generation_algorithm_explained/
 * --------------------------------
 * Generate X rooms randomly placed/sized
 * Separate generated rooms (steering avoidance)
 *      - find all rooms w/in current room neighborhood
 *      - move current room away from neighbors
 *      - separation speed proportional to overlap distance
 *      * if still overlaps after separation iterations, trash those rooms
 * Select Y rooms from those generated
 *      - select rooms with w/h above some threshold
 * Generate room graph (min spanning tree) connecting selected rooms
 *      - vertices of graph are center points of selected rooms
 *      - calculate delaunay triangulation to connect vertices
 *      - construct MST from delaunay graph
 *      - re-incorporate Z% of remaining edges from delaunay graph to add cycles
 * Generate corridors as needed to connect selected rooms along graph edges
 *      - foreach edge connecting 2 rooms, generate straight or L corridor
 *      * any unselected rooms that intersect the corridors get included
 *
 * Brian Ploeckelman created on 9/23/2014.
 */
public class TinyDungeonGenerator implements RoomGraphGenerator {

    private static final String tag = "LEVEL_GEN";
    private static final int tile_size = 16;
    private static final int delay_ms_create = 7;
    private static final int delay_ms_separate = 4;
    private static final int delay_ms_select = 8;
    private static final int delay_ms_reposition = 1;
    private static final int delay_ms_mst = 50;

    private static final Random random = new Random();
    private static final Random selectRandom = new Random();

    private LevelGenParams params;

    private List<Room> rooms;
    private Graph<Room> graph;

    private Graph<Room> mst;
    private FloatArray vertices;
    private ShortArray triIndices;


    public TinyDungeonGenerator() {
        rooms = new ArrayList<Room>();
        graph = new Graph<Room>();
        mst = new Graph<Room>();
        vertices = new FloatArray();
        triIndices = new ShortArray();
    }

    @Override
    public Graph<Room> generateRoomGraph(final LevelGenParams params) {
        this.params = params;

        random.setSeed(params.randomSeed);
        selectRandom.setSeed(params.randomSeed);

        createRooms();
        separateRooms();
        selectRooms();
        repositionRooms();
        generateGraph();

        return graph;
    }

    /**
     * Draw rooms for debug visualization
     *
     * @param camera the camera used for viewing the rooms
     */
    public void render(Camera camera, ShapeRenderer shapes) {
        shapes.setProjectionMatrix(camera.combined);

        // Draw interior room bounds
        shapes.begin(ShapeType.Filled);
        {
            for (int i = rooms.size() - 1; i >= 0; --i) {
                final Room room = rooms.get(i);
                if      (room.isSelected) shapes.setColor(1.0f,  0.0f,  0.0f,  0.05f);
                else if (graph.V() > 0)   shapes.setColor(0.7f,  0.7f,  0.7f,  0.2f);
                else                      shapes.setColor(0.25f, 0.25f, 0.25f ,0.05f);
                shapes.rect(room.rect.x     * tile_size, room.rect.y      * tile_size,
                            room.rect.width * tile_size, room.rect.height * tile_size);
            }
        }
        shapes.end();

        // Draw room bounds outlines
        shapes.begin(ShapeType.Line);
        {
            for (int i = rooms.size() - 1; i >= 0; --i) {
                final Room room = rooms.get(i);
                if (room.isSelected) shapes.setColor(0, 1, 0, 0.75f);
                else                 shapes.setColor(0, 0, 1, 0.75f);
                shapes.rect(room.rect.x     * tile_size, room.rect.y      * tile_size,
                            room.rect.width * tile_size, room.rect.height * tile_size);
            }
        }
        shapes.end();

        // Draw graphs
        shapes.begin(ShapeType.Line);
        {
            // Delaunay triangles from selected rooms
            shapes.setColor(0,0.2f,0,0.5f);
            for (int i = triIndices.size - 4; i >= 0; i -= 3) {
                int p1 = triIndices.get(i + 0) * 2;
                int p2 = triIndices.get(i + 1) * 2;
                int p3 = triIndices.get(i + 2) * 2;
                shapes.triangle(
                        vertices.get(p1) * 16, vertices.get(p1 + 1) * 16,
                        vertices.get(p2) * 16, vertices.get(p2 + 1) * 16,
                        vertices.get(p3) * 16, vertices.get(p3 + 1) * 16
                );
            }

            // Minimum spanning tree from Delaunay triangulation
            final int numRooms = mst.vertexSet().size();
            final Room[] mstRooms = new Room[numRooms];
            mst.vertexSet().toArray(mstRooms);

            shapes.setColor(1,0,1,1);
            for (int iu = numRooms - 1; iu >= 0; --iu) {
                for (int iv = numRooms - 1; iv >= 0; --iv) {
                    final Room u = mstRooms[iu];
                    final Room v = mstRooms[iv];
                    if (u == v) continue;

                    if (mst.hasEdge(u, v)) {
                        shapes.line(
                                u.center.x * 16, u.center.y * 16,
                                v.center.x * 16, v.center.y * 16);
                    }
                }
            }
        }
        shapes.end();

        // Coordinate frame
        shapes.begin(ShapeType.Line);
        {
            shapes.setColor(1, 0, 0, 0.45f);
            shapes.line(0, 0, 3000, 0);
            shapes.setColor(0, 1, 0, 0.45f);
            shapes.line(0, 0, 0, 2000);
        }
        shapes.end();
    }


    // ------------------------------------------------------------------------
    //      Implementation Details
    // ------------------------------------------------------------------------

    /**
     * Randomly create new rooms based on specified parameters
     */
    private void createRooms() {
        rooms = new ArrayList<Room>(params.numInitialRooms);

        int w_range = (params.roomWidthMax - params.roomWidthMin) + 1;
        int h_range = (params.roomHeightMax - params.roomHeightMin) + 1;
        Rectangle bounds = new Rectangle();

        for (int i = 0; i < params.numInitialRooms; ++i) {
            // Random room position
            bounds.x = random.nextFloat() * params.mapMaxX;
            bounds.y = random.nextFloat() * params.mapMaxY;

            // Random integer room width/height
            bounds.width = random.nextInt(w_range) + params.roomWidthMin;
            bounds.height = random.nextInt(h_range) + params.roomHeightMin;

            rooms.add(new Room(bounds));

            try { Thread.sleep(delay_ms_create); } catch (Exception e) {}
        }

        Gdx.app.log(tag, "Created " + rooms.size() + " initial rooms");
    }

    /**
     * Move specified rooms away from each other
     */
    private void separateRooms() {
        float sk = 1.0f; // separation amount scale factor
        int numIterationsRun = 0;

        final Vector2 separation = new Vector2();

        Gdx.app.log(tag, "Separating rooms...");

        // Continue moving rooms away from each other
        // until there are no more overlapping rooms
        boolean overlapping = true;
        while (overlapping) {
            overlapping = false;

            for (Room room : rooms) {
                separation.set(computeSeparation(room));

                if (separation.x == 0 && separation.y == 0) {
                    room.vel.set(0, 0);
                } else {
                    overlapping = true;
                    room.vel.x = sk * separation.x;
                    room.vel.y = sk * separation.y;
                }

                // Reposition room bounds based on velocity
                room.center.add(room.vel);
                room.rect.setCenter(room.center);

            }

            ++numIterationsRun;

            try { Thread.sleep(delay_ms_separate); } catch (Exception e) {}
        }

        Gdx.app.log(tag, "Separating rooms... complete : " + numIterationsRun + " iterations");
    }

    /**
     * Compute a new velocity for specified room in order to separate it from
     * overlapping rooms.
     *
     * @param room the room to separate
     *
     * @return the velocity that moves the room away from overlapping rooms
     */
    private Vector2 computeSeparation(Room room) {
        final Rectangle intersection = new Rectangle();
        final Vector2 separation = new Vector2();
        final Vector2 temp = new Vector2();

        int numNeighbors = 0;
        float distSquared;

        for (Room neighbor : rooms) {
            if (room == neighbor) continue;

            if (Intersector.overlaps(room.rect, neighbor.rect)) {
                // Calculate intersection coefficient
                Intersector.intersectRectangles(room.rect, neighbor.rect, intersection);
                float area = intersection.width * intersection.height;
                if (area < 1) area = 1;

                distSquared = neighbor.center.dst2(room.center);
                temp.set(room.center);
                temp.sub(neighbor.center).scl(area / distSquared);

                separation.add(temp);

                ++numNeighbors;
            }
        }

        if (numNeighbors == 0) {
            return new Vector2();
        }

        return separation.scl(1f / (float) numNeighbors);
    }

    /**
     * Select some rooms that will make up the main rooms in the level
     */
    private void selectRooms() {
        final int mid_width = (params.roomWidthMax - params.roomWidthMin) / 2;
        final int mid_height = (params.roomHeightMax - params.roomHeightMin) / 2;
        final Random rand = new Random();

        int i = 0;
        Room room;
        List<Room> selected = new ArrayList<Room>(params.numSelectedRooms);
        while (selected.size() < params.numSelectedRooms) {
            if (i >= rooms.size()) {
                i = 0;
            }

            room = rooms.get(i++);
            if (room.rect.width > mid_width
             && room.rect.height > mid_height
             && rand.nextBoolean()) {
                selected.add(room);
                room.isSelected = true;
            } else {
                room.isSelected = false;
            }

            try { Thread.sleep(delay_ms_select); } catch (Exception e) {}
        }

        Gdx.app.log(tag, "Selected " + params.numSelectedRooms + " rooms");
    }

    /**
     * Generate the final room graph
     */
    private void generateGraph() {
        graph = new Graph<Room>();

        // Snap room bounds to integer coordinates and
        // generate graph vertices from selected room centers
        vertices = new FloatArray();
        for (Room room : rooms) {
            room.rect.set(
                    (float) Math.floor(room.rect.x),
                    (float) Math.floor(room.rect.y),
                    (float) Math.floor(room.rect.width),
                    (float) Math.floor(room.rect.height));
            room.rect.getCenter(room.center);

            if (room.isSelected) {
                vertices.add(room.center.x);
                vertices.add(room.center.y);
            }
        }

        // Compute Delaunay triangulation of graph vertices
        final DelaunayTriangulator triangulator = new DelaunayTriangulator();
        triIndices = triangulator.computeTriangles(vertices, false);
        final Graph<Room> delaunay = generateDelaunayGraph(vertices, triIndices);

        Gdx.app.log(tag, "Computed Delaunay triangulation");

        // Make graph fully connected and acyclic,
        generateMinSpanningTree(delaunay);
        Gdx.app.log(tag, "Computed minimum spanning tree");

        // Restore some cycles in the minimum spanning tree
        restoreExtraEdges(delaunay, params);
        Gdx.app.log(tag, "Restored " + (100f * params.percentCycleEdges) + "% of cycle edges");
    }

    /**
     * Generate a graph from the existing Delaunay triangulation of the selected
     * rooms from the specified list of rooms.
     *
     * @param vertices  the vertices of the Delaunay triangulation [x0, y0, x1,
     *                  y1, ..., xN, yN]
     * @param triangles the triangle indices of the specified vertices
     *
     * @return the Delaunay triangulation of selected rooms as a graph
     */
    private Graph<Room> generateDelaunayGraph(final FloatArray vertices, final ShortArray triangles) {
        // Generate graph structure from Dalaunay triangulation
        final Graph<Room> delaunay = new Graph<Room>();
        final Vector2 v1 = new Vector2();
        final Vector2 v2 = new Vector2();
        final Vector2 v3 = new Vector2();

        for (Room room : rooms) {
            if (!room.isSelected) continue;

            for (int i = 0; i < triangles.size; i += 3) {
                // Get triangle indices
                int p1 = triangles.get(i + 0) * 2;
                int p2 = triangles.get(i + 1) * 2;
                int p3 = triangles.get(i + 2) * 2;

                // Get triangle vertices
                v1.set(vertices.get(p1), vertices.get(p1 + 1));
                v2.set(vertices.get(p2), vertices.get(p2 + 1));
                v3.set(vertices.get(p3), vertices.get(p3 + 1));

                // Add an edge between rooms
                if (room.center.equals(v1)) {
                    for (Room r : rooms) {
                        if (!r.isSelected || room == r) continue;
                        if (r.center.equals(v2)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v3)) delaunay.addEdge(room, r);
                    }
                } else if (room.center.equals(v2)) {
                    for (Room r : rooms) {
                        if (!r.isSelected || room == r) continue;
                        if (r.center.equals(v1)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v3)) delaunay.addEdge(room, r);
                    }
                } else if (room.center.equals(v3)) {
                    for (Room r : rooms) {
                        if (!r.isSelected || room == r) continue;
                        if (r.center.equals(v1)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v2)) delaunay.addEdge(room, r);
                    }
                }
            }
        }

        return delaunay;
    }

    /**
     * Calculate a min spanning tree for specified graph using Prim's algorithm
     *
     * @param graph  the graph to calculate a min spanning tree for
     */
    private void generateMinSpanningTree(final Graph<Room> graph) {
        // Create vertex sets:
        // V - all existing graph vertices
        // V_new - vertices connected to the minimum spanning tree
        final Set<Room> V_new = new HashSet<Room>();
        final Set<Room> V = new HashSet<Room>();
        for (Room room : graph.vertices()) {
            V.add(room);
        }

        mst = new Graph<Room>();

        // Make sure there are vertices in the delaunay graph
        if (!V.iterator().hasNext()) {
            return;
        }

        // Add an arbitrary vertex to the mst graph
        Room room = V.iterator().next();
        mst.addVertex(room);
        V_new.add(room);

        // Repeatedly add an edge {u0, v0} with minimal weight...
        while (!V_new.equals(V)) {
            Room u0 = null;
            Room v0 = null;
            float minDist = Float.MAX_VALUE;

            // ...such that u0 is in V_new...
            for (Room u : V_new) {
                // ...and v0 is not in V_new
                for (Room v : V) {
                    if (V_new.contains(v)) {
                        continue;
                    }

                    // Find minimum distance edge
                    // from a room 'u' in V_new...
                    // to a room 'v' not in V_new
                    if (graph.hasEdge(u, v)) {
                        float dist = u.center.dst(v.center);
                        if (minDist > dist) {
                            minDist = dist;
                            u0 = u;
                            v0 = v;
                        }
                    }
                }
            }

            // Add v0 to V_new and {u0, v0} to minimum spanning tree
            mst.addEdge(u0, v0);
            V_new.add(v0);

            try { Thread.sleep(delay_ms_mst); } catch (Exception e) {}
        }
    }

    /**
     * Add some edges back to destination graph from source graph. It is assumed
     * that the destination graph is a transformed version of the source graph
     * (a minimum spanning tree for example)
     *
     * @param source the source graph
     * @param params the level generation parameters
     */
    private void restoreExtraEdges(final Graph<Room> source, final LevelGenParams params) {
        final float max_edges_to_restore = params.percentCycleEdges * source.E();

        int numEdgesAdded = 0;
        for (Room u : source.vertices()) {
            for (Room v : source.vertices()) {
                if (u == v) continue;

                // TODO : add some randomness to this edge selection
                if (source.hasEdge(u, v) && !graph.hasEdge(u, v)) {
                    graph.addEdge(u, v);
                    ++numEdgesAdded;

                    if (numEdgesAdded >= max_edges_to_restore) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Reposition the bounds of each room in the graph so they are within the
     * first quadrant.
     */
    private void repositionRooms() {
        // Find minimum room position
        final Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
        for (Room room : rooms) {
            if (min.x > room.rect.x) min.x = room.rect.x;
            if (min.y > room.rect.y) min.y = room.rect.y;
        }

        // Shift rooms to 1st quadrant (x >= 0, y >= 0) and re-snap to integer bounds
        for (Room room : rooms) {
            room.rect.x = (int) Math.floor(room.rect.x - min.x);
            room.rect.y = (int) Math.floor(room.rect.y - min.y);
            room.rect.getCenter(room.center);
            try { Thread.sleep(delay_ms_reposition); } catch (Exception e) {}
        }
    }

}
