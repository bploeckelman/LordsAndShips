package lando.systems.lordsandships.utils.graph;

/**
 * A vertex in a graph
 *
 * Brian Ploeckelman created on 7/14/2014.
 */
public abstract class Vertex {
	private static int nextId = 0;

	protected int id;

	public Vertex() {
		id = nextId++;
	}

	public int getId() { return id; }
}
