package lando.systems.lordsandships.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Graph class, connects vertices with undirected edges
 *
 * Brian Ploeckelman created on 6/12/2014.
 */
public class Graph<T>
{
	private Map<T, Set<T>> adjacencyLists;

	public Graph() {
		adjacencyLists = new HashMap<T, Set<T>>();
	}

	/**
	 * Add the specified vertex to the graph, if not already in the graph
	 *
	 * @param v The vertex to be added to the graph
	 */
	public void addVertex(T v) {
		if (!adjacencyLists.containsKey(v)) {
			adjacencyLists.put(v, null);
		}
	}

	/**
	 * Remove the specified vertex and all its associated edges from the graph
	 * if such a vertex exists. Returns true if the specified vertex existed
	 * and was removed.
	 *
	 * @param v The vertex to be removed from the graph
	 * @return True if the vertex was removed from the graph, false otherwise
	 * TODO : fix returned result always being false
	 */
	public boolean removeVertex(T v) {
		boolean result = false;

		if (!adjacencyLists.containsKey(v)) {
			return result;
		}

		Set<T> neighbors = null;
		for (T vert : adjacencyLists.keySet()) {
			if (v == vert) continue;
			neighbors = adjacencyLists.get(vert);
			if (neighbors != null) {
				result &= neighbors.remove(v);
			}
		}

		adjacencyLists.remove(v);
		result &= adjacencyLists.containsKey(v);

		return result;
	}

	/**
	 * Add edge between vertices v and w
	 *
	 * @param v One vertex on the edge to be added
	 * @param w The other vertex on the edge to be added
	 */
	public void addEdge(T v, T w) {
		Set<T> neighbors;

		// Add edge v-w
		if (adjacencyLists.containsKey(v)) {
			neighbors = adjacencyLists.get(v);
			if (neighbors == null) {
				neighbors = new HashSet<T>();
			}
			neighbors.add(w);
		} else {
			neighbors = new HashSet<T>();
			neighbors.add(w);
			adjacencyLists.put(v, neighbors);
		}

		// Add edge w-v
		if (adjacencyLists.containsKey(w)) {
			neighbors = adjacencyLists.get(w);
			if (neighbors == null) {
				neighbors = new HashSet<T>();
			}
			neighbors.add(v);
		} else {
			neighbors = new HashSet<T>();
			neighbors.add(v);
			adjacencyLists.put(w, neighbors);
		}
	}

	/**
	 * Remove the specified edge from the graph, if such an edge exists.
	 * Returns true if the edge existed and was removed.
	 *
	 * @param v One vertex on the edge to be removed
	 * @param w The other vertex on the edge to be removed
	 * @return True if the edge was removed from the graph, false otherwise
	 */
	public boolean removeEdge(T v, T w) {
		boolean result = false;

		// Remove edge v-w
		Set<T> neighbors = adjacencyLists.get(v);
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
	 * Get the number of vertices in the graph
	 *
	 * @return The number of vertices in the graph
	 */
	public int V() {
		return adjacencyLists.size();
	}

	/**
	 * Get the number of edges in the graph
	 *
	 * @return The number of edges in the graph
	 */
	public int E() {
		int numEdges = 0;

		for (T v : adjacencyLists.keySet()) {
			numEdges += adjacencyLists.get(v).size();
		}
		numEdges /= 2; // v-w and w-v are the same edge

		return numEdges;
	}

	/**
	 * Get the degree (number of connected edges) for the specified vertex
	 *
	 * @param v The vertex to get the degree of
	 * @return The degree for the specified vertex
	 */
	public int degree(T v) {
		Set<T> neighbors = adjacencyLists.get(v);
		return (neighbors == null) ? 0 : neighbors.size();
	}

	/**
	 * Get the container of vertices
	 *
	 * @return The container of vertices in the graph
	 */
	public Iterable<T> vertices() {
		return adjacencyLists.keySet();
	}

	/**
	 * Get the container vertices adjacent to the specified vertex
	 *
	 * @param v The vertex to get the adjacent vertices for
	 * @return The vertices that are adjacent to the specified vertex
	 */
	public Iterable<T> adjacentTo(T v) {
		return adjacencyLists.get(v);
	}

	/**
	 * Does the graph contain the specified vertex?
	 *
	 * @param v The vertex to find in the graph
	 * @return True if the specified vertex is in the graph, false otherwise
	 */
	public boolean hasVertex(T v) {
		return adjacencyLists.containsKey(v);
	}

	/**
	 * Does the graph contain an edge between the specified vertices?
	 *
	 * @param v One vertex on the edge to find in the graph
	 * @param w The other vertex on the edge to find in the graph
	 * @return True if the edge v-w (or w-v) is in the graph, false otherwise
	 *
	 * TODO : assumes that if v-w is not in the adjacency list then neither is w-v
	 */
	public boolean hasEdge(T v, T w) {
		Set<T> neighbors = adjacencyLists.get(v);
		if (neighbors == null) {
			return false;
		}
		return neighbors.contains(w);
	}

	/**
	 * Compare this graph to the specified graph, return true if they have
	 * all the same vertices and edges.
	 *
	 * @param g The graph to compare to this graph
	 * @return True if the graphs consist of the same vertices and edges, false otherwise
	 */
	public boolean equals(Graph g) {
		Set<T> neighbors = null;
		for (T v : adjacencyLists.keySet()) {
			if (!g.hasVertex(v)) {
				return false;
			}

			neighbors = adjacencyLists.get(v);
			for (T w : neighbors) {
				if (!g.hasEdge(v, w)) {
					return false;
				}
			}
		}

		return true;
	}
}
