package lando.systems.lordsandships.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import lando.systems.lordsandships.utils.Assets;

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
	/**
	 * Settings class
	 *
	 * Defines level generation parameters
	 */
	public static class Settings
	{
		public int xMax;
		public int yMax;
		public int widthMin;
		public int widthMax;
		public int heightMin;
		public int heightMax;
		public int initialRooms;
		public int selectedRooms;
		public int separationIterations;

		public Settings() {
			// TODO : make defaults constants
			this.xMax = 100;
			this.yMax = 75;
			this.widthMin = 4;
			this.widthMax = 10;
			this.heightMin = 4;
			this.heightMax = 10;
			this.initialRooms = 150;
			this.selectedRooms = 15;
			this.separationIterations = 50;
		}
	}

	// TODO : make private
	public static List<Room> initialRooms = null;
	public static List<Room> selectedRooms = null;
	public static int[][] tiles = null;
	public static FloatArray points = null;
	public static ShortArray triangles = null;
	public static Graph mst = null;
	public static Graph delaunay = null;

	/**
	 * Main generation interface
	 *
	 * @param settings The level generation parameters to use
	 * @return array of tile type ids specifying the layout of the resulting level
	 */
	public static int[][] generateLevel (Settings settings) {
		startGeneration(settings);
		return tiles;
	}

	// -------------------------------------------------------------------------
	// Private implementation details
	// -------------------------------------------------------------------------
	private static void startGeneration (Settings settings) {
		generateInitialRooms(settings);
		separateInitialRooms(settings);
		selectRooms(settings);
		generateRoomGraph();
		generateCorridors();
		generateTilesFromRooms();
	}

	/**
	 * Randomly create new rooms based on the specified parameters
	 * @param settings The randomization parameters
	 */
	public static void generateInitialRooms(Settings settings) {
		float x, y, w, h;
		initialRooms = new ArrayList<Room>(settings.initialRooms);
		for (int i = 0; i < settings.initialRooms; ++i) {
			x = (Assets.rand.nextFloat() * settings.xMax);
			y = (Assets.rand.nextFloat() * settings.yMax);
			w = (Assets.rand.nextFloat() * ((settings.widthMax - settings.widthMin) + 1)) + settings.widthMin;
			h = (Assets.rand.nextFloat() * ((settings.heightMax - settings.heightMin) + 1)) + settings.heightMin;
			Room room = new Room(x,y,w,h);
			initialRooms.add(room);
		}

		System.out.println("Generated " + settings.initialRooms + " initial rooms.");
	}

	/**
	 * Move initial rooms away from each other
	 * @param settings The randomization parameters
	 */
	public static void separateInitialRooms(Settings settings) {
		Vector2 separation = new Vector2();
		Vector2 cohesion = new Vector2();
		int iterationsRun = 0;
		float sk = 0.05f;
		float ck = 0.00f; // ignore cohesion for now

		System.out.print("Separating rooms... " );

		for (int i = 0; i < settings.separationIterations; ++i) {
			for (Room room : initialRooms) {
				cohesion.set(computeCohesion(room));
				separation.set(computeSeparation(room));

				if (separation.x == 0 && separation.y == 0) {
					room.vel.set(0, 0);
				} else {
					room.vel.x += ck * cohesion.x + sk * separation.x;
					room.vel.y += ck * cohesion.y + sk * separation.y;
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
	 * @param settings The randomization parameters
	 */
	public static void selectRooms(Settings settings) {
		final int midWidth  = (settings.widthMax  - settings.widthMin)  / 2;
		final int midHeight = (settings.heightMax - settings.heightMin) / 2;

		int roomsSelected = 0;

		selectedRooms = new ArrayList<Room>(settings.selectedRooms);
		for (Room room : initialRooms) {
			if (roomsSelected == settings.selectedRooms) break;

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
	public static void generateRoomGraph() {
		points = new FloatArray();
		for (Room room : selectedRooms) {
			// Snap to integer positions
			room.rect.set((float) Math.floor(room.rect.x), (float) Math.floor(room.rect.y),
					      (float) Math.floor(room.rect.width), (float) Math.floor(room.rect.height));
			room.rect.getCenter(room.center);

			points.add(room.center.x);
			points.add(room.center.y);
		}

		// Compute Dalaunay triangulation
		final DelaunayTriangulator triangulator = new DelaunayTriangulator();
		triangles = triangulator.computeTriangles(points, false);
		delaunay = generateDelaunayGraph();

//		calculateMinimumSpanningTree();
	}

	private static void generateCorridors() {

	}

	private static void generateTilesFromRooms() {

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
//		cohesion.scl(0.01f);
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
		int neighborCount = 0;

		for (Room neighbor : initialRooms) {
			if (room == neighbor) continue;

			if (Intersector.overlaps(room.rect, neighbor.rect)) {
				Intersector.intersectRectangles(room.rect, neighbor.rect, intersection);
				separation.x += neighbor.center.x - room.center.x;
				separation.y += neighbor.center.y - room.center.y;
				neighborCount++;
			} else {
				intersection.set(0,0,1,1);
			}

			separation.scl(intersection.width * intersection.height);
		}

		if (neighborCount == 0) {
			return new Vector2();
		}

		separation.scl(-1f / (float) (neighborCount));
		separation.nor();
		return separation;
	}

	/**
	 * Calculate a minimum spanning tree for the existing delaunay graph usin
	 * Prim's algorithm
	 */
	public static void calculateMinimumSpanningTree() {
		// Create vertex sets:
		// V - all existing graph vertices
		// V_new - vertices connected to the minimum spanning tree
		Set<Room> V_new = new HashSet<Room>();
		Set<Room> V = new HashSet<Room>();
		for (Room room : delaunay.vertices()) {
			V.add(room);
		}

		mst = new Graph();

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
	}

	/**
	 * Generate a graph from the existing Delaunay triangulation of selected rooms
	 *
	 * @return
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
			if (room.isSelected) Assets.shapes.setColor(1, 0, 0, 1);
			else {
				if (triangles == null) Assets.shapes.setColor(0.7f, 0.7f, 0.7f, 1);
				else                   Assets.shapes.setColor(0.25f, 0.25f, 0.25f, 0.5f);
			}

			Assets.shapes.rect(room.rect.x * 1, room.rect.y * 1,
							   room.rect.width * 1, room.rect.height * 1);
		}
		Assets.shapes.end();

		// Room outlines
		Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
		Assets.shapes.setColor(0, 0, 1, 1);
		for (Room room : initialRooms) {
			Assets.shapes.rect(room.rect.x * 1, room.rect.y * 1,
							   room.rect.width * 1, room.rect.height * 1);
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
						points.get(p1), points.get(p1 + 1),
						points.get(p2), points.get(p2 + 1),
						points.get(p3), points.get(p3 + 1)
				);
			}
			Assets.shapes.end();
		}

		// Minimum spanning tree from Dalaunay triangulation
		if (mst != null) {
			Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
			Assets.shapes.setColor(1,0,1,1);
			for (Room u : mst.vertices()) {
				for (Room v : mst.vertices()) {
					if (mst.hasEdge(u, v)) {
						Assets.shapes.line(u.center, v.center);
					}
				}
			}
			Assets.shapes.end();
		}

		// Grid viz
//		Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
//		Assets.shapes.setColor(1,0,0,0.15f);
//		for (int y = 0; y < 101; ++y) {
//			for (int x = 0; x < 101; ++x) {
//				Assets.shapes.line(x, 0, x, 100);
//				Assets.shapes.line(0, y, 100, y);
//			}
//		}
//		Assets.shapes.end();

		// Center of mass of rooms
		Assets.shapes.begin(ShapeRenderer.ShapeType.Line);
		Assets.shapes.setColor(1,1,0,1);
		Vector2 center = new Vector2();
		for (Room room : initialRooms) {
			center.add(room.center);
		}
		center.scl(1f / (float) initialRooms.size());
		Assets.shapes.circle(center.x, center.y, 8, 16);
		Assets.shapes.end();
	}

	// -------------------------------------------------------------------------
	/**
	 * Room class
	 */
	public static class Room
	{
		public Rectangle rect;
		public Vector2 center;
		public Vector2 vel;
		public boolean isSelected;
		// TODO : add other contents once level layout is done

		Room(float x, float y, float w, float h) {
			center = new Vector2();
			rect = new Rectangle(x,y,w,h);
			rect.getCenter(center);
			vel= new Vector2();
			isSelected = false;
		}
	}

	/**
	 * Graph class, connects Rooms with undirected edges
	 */
	public static class Graph
	{
		private Map<Room, Set<Room>> adjacencyLists;

		public Graph() {
			adjacencyLists = new HashMap<Room, Set<Room>>();
		}

		/**
		 * Add the specified vertex to the graph, if not already in the graph
		 *
		 * @param v
		 */
		public void addVertex(Room v) {
			if (!adjacencyLists.containsKey(v)) {
				adjacencyLists.put(v, null);
			}
		}

		/**
		 * Remove the specified vertex and all its associated edges from the graph
		 * if such a vertex exists. Returns true if the specified vertex existed
		 * and was removed.
		 *
		 * @param v
		 * @return
		 */
		public boolean removeVertex(Room v) {
			boolean result = false;

			if (!adjacencyLists.containsKey(v)) {
				return result;
			}

			Set<Room> neighbors = null;
			for (Room room : adjacencyLists.keySet()) {
				if (v == room) continue;
				neighbors = adjacencyLists.get(room);
				if (neighbors != null) {
					result &= neighbors.remove(v);
				}
			}

			adjacencyLists.remove(v);
			result &= adjacencyLists.containsKey(v);

			return result;
		}

		/**
		 * Add edge v-w
		 *
		 * @param v
		 * @param w
		 */
		public void addEdge(Room v, Room w) {
			Set<Room> neighbors = null;

			// Add edge v-w
			if (adjacencyLists.containsKey(v)) {
				neighbors = adjacencyLists.get(v);
				if (neighbors == null) {
					neighbors = new HashSet<Room>();
				}
				neighbors.add(w);
			} else {
				neighbors = new HashSet<Room>();
				neighbors.add(w);
				adjacencyLists.put(v, neighbors);
			}

			// Add edge w-v
			if (adjacencyLists.containsKey(w)) {
				neighbors = adjacencyLists.get(w);
				if (neighbors == null) {
					neighbors = new HashSet<Room>();
				}
				neighbors.add(v);
			} else {
				neighbors = new HashSet<Room>();
				neighbors.add(v);
				adjacencyLists.put(w, neighbors);
			}
		}

		/**
		 * Remove the specified edge from the graph, if such an edge exists.
		 * Returns true if the edge existed and was removed.
		 *
		 * @param v
		 * @param w
		 * @return
		 */
		public boolean removeEdge(Room v, Room w) {
			boolean result = false;

			// Remove edge v-w
			Set<Room> neighbors = adjacencyLists.get(v);
			if (neighbors != null) {
				result = neighbors.remove(w);
			}

			// Remove edge w-v
			neighbors = adjacencyLists.get(w);
			if (neighbors != null) {
				result &= neighbors.remove(v);
			}

			return result;
		}

		/**
		 * Return the number of vertices in the graph
		 * @return
		 */
		public int V() {
			return adjacencyLists.size();
		}

		/**
		 * Get the number of edges in the graph
		 * @return
		 */
		public int E() {
			int numEdges = 0;

			for (Room room : adjacencyLists.keySet()) {
				numEdges += adjacencyLists.get(room).size();
			}
			numEdges /= 2; // v-w and w-v are the same edge

			return numEdges;
		}

		/**
		 * Get the degree of the specified vertex
		 * @param v
		 * @return
		 */
		public int degree(Room v) {
			Set<Room> neighbors = adjacencyLists.get(v);
			return (neighbors == null) ? 0 : neighbors.size();
		}

		/**
		 * Get the container of vertices
		 * @return
		 */
		public Iterable<Room> vertices() {
			return adjacencyLists.keySet();
		}

		/**
		 * Get the container vertices adjacent to the specified vertex
		 * @param v
		 * @return
		 */
		public Iterable<Room> adjacentTo(Room v) {
			return adjacencyLists.get(v);
		}

		/**
		 * Does the graph contain the specified vertex?
		 * @param v
		 * @return
		 */
		public boolean hasVertex(Room v) {
			return adjacencyLists.containsKey(v);
		}

		/**
		 * Does the graph contain an edge between the specified vertices?
		 * @param v
		 * @param w
		 * @return
		 */
		public boolean hasEdge(Room v, Room w) {
			Set<Room> neighbors = adjacencyLists.get(v);
			if (neighbors == null) {
				return false;
			}
			return neighbors.contains(w);
		}

		/**
		 * Compare this graph to the specified graph, return true if they have
		 * all the same vertices and edges.
		 *
		 * @param g
		 * @return
		 */
		public boolean equals(Graph g) {
			Set<Room> neighbors = null;
			for (Room v : adjacencyLists.keySet()) {
				if (!g.hasVertex(v)) {
					return false;
				}

				neighbors = adjacencyLists.get(v);
				for (Room w : neighbors) {
					if (!g.hasEdge(v, w)) {
						return false;
					}
				}
			}

			return true;
		}
	}

}
