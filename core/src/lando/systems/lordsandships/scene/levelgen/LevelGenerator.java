package lando.systems.lordsandships.scene.levelgen;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import lando.systems.lordsandships.utils.Assets;
import lando.systems.lordsandships.utils.graph.Graph;

import java.util.*;

/**
 * LevelGenerator
 *
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
 *      - if still overlaps after separation iterations, trash those rooms
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
 * Brian Ploeckelman created on 5/31/2014.
 */
public class LevelGenerator
{
    // TODO : make private
    public static List<Room> initialRooms = null;
    public static List<Room> selectedRooms = null;
    public static FloatArray points = null;
    public static ShortArray triangles = null;
    public static Graph<Room> mst = null;
    public static Graph<Room> delaunay = null;

    /**
     * Main generation interface
     *
     * @param params The level generation parameters to use
     * @return array of tile type ids specifying the layout of the resulting level
     */
    public static void generateLevel(LevelGenParams params) {
        generateInitialRooms(params);
        separateInitialRooms(params);
        selectRooms(params);
        generateRoomGraph(params);
        generateCorridors();
        generateTilesFromRooms();
    }

    // -------------------------------------------------------------------------
    // Private implementation details
    // -------------------------------------------------------------------------
    /**
     * Randomly create new rooms based on the specified parameters
     *
     * @param params The randomization parameters
     */
    public static void generateInitialRooms(LevelGenParams params) {
        int wRange = (params.roomWidthMax  - params.roomWidthMin)  + 1;
        int hRange = (params.roomHeightMax - params.roomHeightMin) + 1;
        float x, y, w, h;

        initialRooms = new ArrayList<Room>(params.numInitialRooms);
        for (int i = 0; i < params.numInitialRooms; ++i) {
            // Random room position
            x = (Assets.rand.nextFloat() * params.mapMaxX);
            y = (Assets.rand.nextFloat() * params.mapMaxY);

            // Random (integer) room width/height
            w = Assets.rand.nextInt(wRange) + params.roomWidthMin;
            h = Assets.rand.nextInt(hRange) + params.roomHeightMin;

            Room room = new Room(x,y,w,h);

            initialRooms.add(room);
        }

        System.out.println("Generated " + params.numInitialRooms + " initial rooms.");
    }

    /**
     * Move initial rooms away from each other
     *
     * @param params The randomization parameters
     */
    public static void separateInitialRooms(LevelGenParams params) {
        Vector2 separation = new Vector2();
        Vector2 cohesion = new Vector2();
        int iterationsRun = 0;
        float sk = 1.0f;
        float ck = 0.0f; // ignore cohesion for now

        System.out.print("Separating rooms... " );

        // Continue separating until there are no more ovelapping rooms
        boolean overlapping = true;
        while (overlapping) {
            overlapping = false;

            for (Room room : initialRooms) {
                cohesion.set(computeCohesion(room));
                separation.set(computeSeparation(room));

                if (separation.x == 0 && separation.y == 0) {
                    room.vel.set(0, 0);
                } else {
                    overlapping = true;
                    room.vel.x = ck * cohesion.x + sk * separation.x;
                    room.vel.y = ck * cohesion.y + sk * separation.y;
                }

                // Reposition the room's rectangle based on its velocity
                room.center.add(room.vel);
                room.rect.setCenter(room.center);
            }

            iterationsRun++;
        }

        System.out.println("done! Iterations run: " + iterationsRun);
    }

    /**
     * Pick a number of the initial rooms to make up the main rooms in the level
     *
     * @param params The randomization parameters
     */
    public static void selectRooms(LevelGenParams params) {
        final int midWidth  = (params.roomWidthMax  - params.roomWidthMin)  / 2;
        final int midHeight = (params.roomHeightMax - params.roomHeightMin) / 2;

        int roomsSelected = 0;

        selectedRooms = new ArrayList<Room>(params.numSelectedRooms);
        for (Room room : initialRooms) {
            if (roomsSelected == params.numSelectedRooms) break;

            if (room.rect.width > midWidth && room.rect.height > midHeight) {
                selectedRooms.add(room);
                room.isSelected = true;
                roomsSelected++;
            } else {
                room.isSelected = false;
            }
        }

        System.out.println("Selected " + roomsSelected + " rooms.");
    }

    /**
     * Generate a Delaunay Triangulation of selected room centers,
     * then use that to calculate a Minimum Spanning Tree connecting
     * the selected rooms.  Tweak the MST to get the final graph for
     * use in constructing corridors.
     */
    public static void generateRoomGraph(LevelGenParams params) {
        // Snap to integer positions
        for (Room room : initialRooms) {
            room.rect.set(
                (float) Math.floor(room.rect.x),
                (float) Math.floor(room.rect.y),
                (float) Math.floor(room.rect.width),
                (float) Math.floor(room.rect.height));
            room.rect.getCenter(room.center);
        }

        // Generate 'vertices' for graph using room centers
        points = new FloatArray();
        for (Room room : selectedRooms) {
            points.add(room.center.x);
            points.add(room.center.y);
        }

        // Compute Dalaunay triangulation of room centers
        final DelaunayTriangulator triangulator = new DelaunayTriangulator();
        triangles = triangulator.computeTriangles(points, false);
        delaunay = generateDelaunayGraph();

        calculateMinimumSpanningTree(params);
    }

    private static void generateCorridors() {
        // TODO : removeme?
    }

    public static void generateTilesFromRooms() {
        // Find minimum room position
        Vector2 min = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
        for (Room room : initialRooms) {
            if (min.x > room.rect.x) {
                min.x = room.rect.x;
            }
            if (min.y > room.rect.y) {
                min.y = room.rect.y;
            }
        }

        // Shift rooms to 1st quadrant (x >= 0, y >= 0) and snap to integer coords
        for (Room room : initialRooms) {
            room.rect.setPosition(
                (int) Math.floor(room.rect.x - min.x),
                (int) Math.floor(room.rect.y - min.y));
            room.rect.getCenter(room.center);
        }

        // Move room centers (graph vertices) to 1st quadrant also
        for (int i = 0; i < points.size; i += 2) {
            points.set(i+0, points.get(i+0) - min.x);
            points.set(i+1, points.get(i+1) - min.y);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Compute a new velocity for the specified room to move it closer to nearby rooms
     *
     * @param room The specified room to move closer to nearby rooms
     * @return The velocity vector that moves the specified room closer to nearby rooms
     */
    private static Vector2 computeCohesion(Room room) {
        Vector2 cohesion = new Vector2();
        int neighborCount = 0;

        for (Room neighbor : initialRooms) {
            if (room == neighbor) continue;

            // Could calculate cohesion only with 'close' neighbors
            // instead of towards center of mass of all other rooms

            cohesion.add(neighbor.center);
            neighborCount++;
        }

        if (neighborCount == 0) {
            return new Vector2();
        }

        cohesion.scl(1f / (float) (neighborCount));
        cohesion.set(cohesion.x - room.center.x, cohesion.y - room.center.y);
        cohesion.nor();
        return cohesion;
    }

    /**
     * Compute a new velocity for the specified room to separate it from overlapping rooms
     *
     * @param room The specified room to separate from other rooms
     * @return The velocity vector that moves the specified room away from overlapping rooms
     */
    private static Vector2 computeSeparation(Room room) {
        Rectangle intersection = new Rectangle();
        Vector2 separation = new Vector2();
        Vector2 temp = new Vector2();
        int neighborCount = 0;
        float dst2;

        for (Room neighbor : initialRooms) {
            if (room == neighbor) continue;

            if (Intersector.overlaps(room.rect, neighbor.rect)) {
                // Calculate intersection coefficient
                Intersector.intersectRectangles(room.rect, neighbor.rect, intersection);
                float area = intersection.width * intersection.height;
                if (area < 1) {
                    area = 1;
                }

                dst2 = neighbor.center.dst2(room.center);
                temp.set(room.center);
                temp.sub(neighbor.center).scl(area / dst2);

                separation.add(temp);

                neighborCount++;
            }
        }

        if (neighborCount == 0) {
            return new Vector2();
        }

        separation.scl(1f / (float) (neighborCount));
        return separation;
    }

    /**
     * Randomly add edges to minimum spanning tree from Delauanay graph.
     * Added edges introduce cycles to the min spanning tree graph.
     *
     * @param params The level generation parameters to use
     */
    public static void addCycleEdgesToMST(LevelGenParams params) {
        int numEdgesAdded = 0;
        for (Room u : delaunay.vertices()) {
            for (Room v : delaunay.vertices()) {
                if (u == v) continue;

                if (delaunay.hasEdge(u, v) && !mst.hasEdge(u, v)) {
                    mst.addEdge(u, v);
                    if (++numEdgesAdded >= params.percentCycleEdges * delaunay.E()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Calculate a minimum spanning tree for the existing delaunay graph using
     * Prim's algorithm
     *
     * @param params The level generation parameters to use
     */
    public static void calculateMinimumSpanningTree(LevelGenParams params) {
        // Create vertex sets:
        // V - all existing graph vertices
        // V_new - vertices connected to the minimum spanning tree
        Set<Room> V_new = new HashSet<Room>();
        Set<Room> V = new HashSet<Room>();
        for (Room room : delaunay.vertices()) {
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
                    if (delaunay.hasEdge(u, v)) {
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
        }

        System.out.println("Generated minimum spanning tree");
        addCycleEdgesToMST(params);
        System.out.println("Induced cycles into minimum spanning tree from delaunay graph");
    }

    /**
     * Generate a graph from the existing Delaunay triangulation of selected rooms
     *
     * @return The generated Dalaunay triangulation graph
     */
    private static Graph generateDelaunayGraph() {
        // Generate graph structure from Dalaunay triangulation
        Graph delaunay = new Graph();
        Vector2 v1 = new Vector2();
        Vector2 v2 = new Vector2();
        Vector2 v3 = new Vector2();

        for (Room room : selectedRooms) {
            for (int i = 0; i < triangles.size; i += 3) {
                // Get triangle indices
                int p1 = triangles.get(i + 0) * 2;
                int p2 = triangles.get(i + 1) * 2;
                int p3 = triangles.get(i + 2) * 2;

                // Get triangle vertices
                v1.set(points.get(p1), points.get(p1 + 1));
                v2.set(points.get(p2), points.get(p2 + 1));
                v3.set(points.get(p3), points.get(p3 + 1));

                // Add an edge between rooms
                if (room.center.equals(v1)) {
                    for (Room r : selectedRooms) {
                        if (room == r) continue;
                        if (r.center.equals(v2)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v3)) delaunay.addEdge(room, r);
                    }
                }
                else if (room.center.equals(v2)) {
                    for (Room r : selectedRooms) {
                        if (room == r) continue;
                        if (r.center.equals(v1)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v3)) delaunay.addEdge(room, r);
                    }
                }
                else if (room.center.equals(v3)) {
                    for (Room r : selectedRooms) {
                        if (room == r) continue;
                        if (r.center.equals(v1)) delaunay.addEdge(room, r);
                        else if (r.center.equals(v2)) delaunay.addEdge(room, r);
                    }
                }
            }
        }
        System.out.println("Generated Delaunay graph");
        return delaunay;
    }

    // -------------------------------------------------------------------------
    /**
     * Draw rooms for debug visualization
     *
     * @param camera The Camera used for viewing rooms
     */
    public static void debugRender(Camera camera) {
        Assets.shapes.setProjectionMatrix(camera.combined);

        // Initial and selected room interiors
        Assets.shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Room room : initialRooms) {
            if (room.isSelected) Assets.shapes.setColor(1,0,0,0.25f);
            else {
                if (triangles == null) Assets.shapes.setColor(0.7f, 0.7f, 0.7f, 1);
                else                   Assets.shapes.setColor(0.25f, 0.25f, 0.25f, 0.5f);
            }

            Assets.shapes.rect(room.rect.x * 16, room.rect.y * 16,
                               room.rect.width * 16, room.rect.height * 16);
        }
        Assets.shapes.end();

        // Room outlines
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
        Assets.shapes.setColor(0, 0, 1, 1);
        for (Room room : initialRooms) {
            if (room.isSelected) continue;
            Assets.shapes.rect(room.rect.x * 16, room.rect.y * 16,
                               room.rect.width * 16, room.rect.height * 16);
        }
        Assets.shapes.end();

        // Initial room velocities (during separation phase)
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
        Assets.shapes.setColor(0, 1, 0, 1);
        Vector2 tmp = new Vector2();
        for (Room room : initialRooms) {
            Assets.shapes.line(room.center, tmp.set(room.center).add(room.vel));
        }
        Assets.shapes.end();

        // Delaunay triangles from selected rooms
        if (triangles != null) {
            Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
            Assets.shapes.setColor(0,0.5f,0,0.8f);
            for (int i = 0; i < triangles.size; i += 3) {
                int p1 = triangles.get(i + 0) * 2;
                int p2 = triangles.get(i + 1) * 2;
                int p3 = triangles.get(i + 2) * 2;
                Assets.shapes.triangle(
                        points.get(p1) * 16, points.get(p1 + 1) * 16,
                        points.get(p2) * 16, points.get(p2 + 1) * 16,
                        points.get(p3) * 16, points.get(p3 + 1) * 16
                );
            }
            Assets.shapes.end();
        }

        // Minimum spanning tree from Delaunay triangulation
        if (mst != null) {
            Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
            Assets.shapes.setColor(1,0,1,1);
            for (Room u : mst.vertices()) {
                for (Room v : mst.vertices()) {
                    if (mst.hasEdge(u, v)) {
                        Assets.shapes.line(
                            u.center.x * 16, u.center.y * 16,
                            v.center.x * 16, v.center.y * 16);
                    }
                }
            }
            Assets.shapes.end();
        }

        // Grid viz
        Assets.shapes.begin(ShapeRenderer.ShapeType.Line);

        Assets.shapes.setColor(1, 0, 0, 0.45f);
        Assets.shapes.line(0, 0, 1000, 0);
        Assets.shapes.setColor(0, 1, 0, 0.45f);
        Assets.shapes.line(0, 0, 0, 1000);
//        for (int y = 0; y < 101; ++y) {
//            for (int x = 0; x < 101; ++x) {
//                Assets.shapes.line(x, 0, x, 100);
//                Assets.shapes.line(0, y, 100, y);
//            }
//        }
        Assets.shapes.end();
    }

}
