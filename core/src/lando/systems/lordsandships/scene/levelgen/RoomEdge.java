package lando.systems.lordsandships.scene.levelgen;

import lando.systems.lordsandships.utils.graph.Edge;

/**
 * An undirected edge between Rooms u and v in a graph
 *
 * Brian Ploeckelman created on 7/14/2014.
 */
public class RoomEdge extends Edge<Room> {

	public RoomEdge(Room u, Room v) {
		super(u, v);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof Edge)) return false;

		Edge that = (Edge) other;

		return ((this.u.getId() == that.u.getId() && this.v.getId() == that.v.getId())
			 || (this.u.getId() == that.v.getId() && this.v.getId() == that.u.getId()));
	}

	@Override
	public int hashCode() {
		     if (u.rect.x < v.rect.x) { return u.getId(); }
		else if (u.rect.x > v.rect.x) { return v.getId(); }
		else if (u.rect.y < v.rect.y) { return u.getId(); }
		else if (u.rect.y > v.rect.y) { return v.getId(); }
		else return u.getId();
	}

}
