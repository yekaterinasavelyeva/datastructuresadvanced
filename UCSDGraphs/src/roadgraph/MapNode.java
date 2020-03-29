package roadgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import geography.GeographicPoint;

/**
 * @author Jekaterina Saveljeva
 *
 * A class which represents a vertice
 * Geographical point are the coordinates of the vertice
 *
 */
public class MapNode implements Comparable {

	private GeographicPoint location;
	private List<MapEdge> edges;

	private Double distanceFromStart = 0.0;
	private Double distanceToGoal = 0.0;

	/**
	 * Initialize a MapNode
	 */
	public MapNode(GeographicPoint location) {
		this.location = location;
		this.edges = new ArrayList<>();
	}

	public GeographicPoint getLocation() {
		return location;
	}

	public Double getDistanceFromStart() {
		return distanceFromStart;
	}

	public void setDistanceFromStart(Double distanceFromStart) {
		this.distanceFromStart = distanceFromStart;
	}

	public Double getDistanceToGoal() {
		return distanceToGoal;
	}

	public void setDistanceToGoal(Double distanceToGoal) {
		this.distanceToGoal = distanceToGoal;
	}

	/**
	 * Retrieve outgoing edges of the node
	 * @return List of edges, where edge is RoadSegment
	 */
	public List<MapEdge> getEdges() {
		return edges;
	}

	/**
	 * Adds edge to the node
	 * @param edge edge segment from particular node
	 */
	void addEdge(MapEdge edge) {
		edges.add(edge);
	}

	/**
	 * Retrieve neighbours of the current node
	 * @return list of node neighbours as geographical points
	 */
	List<MapNode> getNeighbours() {
		return edges.stream().map(s -> s.getOtherNode(this)).collect(Collectors.toList());
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof MapNode) {
			MapNode m = (MapNode) o;
			return (this.getDistanceFromStart()).compareTo(m.getDistanceFromStart());
		} else throw new IllegalArgumentException("Objects does not match");
	}
}
