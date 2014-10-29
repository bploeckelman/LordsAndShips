package lando.systems.lordsandships.utils.graph;

/**
 * An undirected edge between vertices u and v in a graph
 *
 * Brian Ploeckelman created on 7/14/2014.
 */
public abstract class Edge<V extends Vertex> {
    public V u;
    public V v;

    public Edge(V u, V v) {
        this.u = u;
        this.v = v;
    }

    public abstract boolean equals(Object other);

    public abstract int hashCode();

}
