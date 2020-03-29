/**
 * @author UCSD MOOC development team and YOU
 * 
 * A class which reprsents a graph of geographic locations
 * Nodes in the graph are intersections between 
 *
 */
package roadgraph;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import geography.GeographicPoint;
import util.GraphLoader;

/**
 * @author UCSD MOOC development team and Jekaterina Saveljeva
 * 
 * A class which represents a graph of geographic locations
 * Nodes in the graph are intersections between 
 *
 */
public class MapGraph {

	private Map<GeographicPoint, MapNode> nodes;

	/** 
	 * Create a new empty MapGraph 
	 */
	public MapGraph()
	{
		nodes = new HashMap<>();
	}
	
	/**
	 * Get the number of vertices (road intersections) in the graph
	 * @return The number of vertices in the graph.
	 */
	public int getNumVertices()
	{
		return nodes.size();
	}
	
	/**
	 * Return the intersections, which are the vertices in this graph.
	 * @return The vertices in this graph as GeographicPoints
	 */
	public Set<GeographicPoint> getVertices()
	{
		return new HashSet<>(nodes.keySet());
	}
	
	/**
	 * Get the number of road segments in the graph
	 * @return The number of edges in the graph.
	 */
	public int getNumEdges()
	{
		List<MapEdge> edges = new ArrayList<>();
		nodes.keySet().forEach(k -> edges.addAll(nodes.get(k).getEdges()));
		return edges.size();
	}
	
	/** Add a node corresponding to an intersection at a Geographic Point
	 * If the location is already in the graph or null, this method does 
	 * not change the graph.
	 * @param location  The location of the intersection
	 * @return true if a node was added, false if it was not (the node
	 * was already in the graph, or the parameter is null).
	 */
	public boolean addVertex(GeographicPoint location)
	{
		if (location == null || nodes.containsKey(location))
			return false;
		MapNode node = new MapNode(location);
		nodes.put(location, node);
		return true;
	}
	
	/**
	 * Adds a directed edge to the graph from pt1 to pt2.  
	 * Precondition: Both GeographicPoints have already been added to the graph
	 * @param from The starting point of the edge
	 * @param to The ending point of the edge
	 * @param roadName The name of the road
	 * @param roadType The type of the road
	 * @param length The length of the road, in km
	 * @throws IllegalArgumentException If the points have not already been
	 *   added as nodes to the graph, if any of the arguments is null,
	 *   or if the length is less than 0.
	 */
	public void addEdge(GeographicPoint from, GeographicPoint to, String roadName,
			String roadType, double length) throws IllegalArgumentException {

		if(!nodes.containsKey(from) || !nodes.containsKey(to) || isNull(from, to, roadName, roadType) || length < 0.0)
			throw new IllegalArgumentException("There are no such points in graph!");
		nodes.get(from).addEdge(new MapEdge(roadName, roadType,nodes.get(from), nodes.get(to), length));
		
	}

	/**
	 * Checks that any of parameters is null
	 * @param params var-args of objects to check for null
	 * @return true if any of objects is null
	 */
	private boolean isNull(Object ... params) {
		for(Object param : params) {
			if (param == null)
				return true;
		}
		return false;
	}

	/** Find the path from start to goal using breadth first search
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @return The list of intersections that form the shortest (unweighted)
	 *   path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> bfs(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
        Consumer<GeographicPoint> temp = (x) -> {};
        return bfs(start, goal, temp);
	}
	
	/** Find the path from start to goal using breadth first search
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @param nodeSearched A hook for visualization.  See assignment instructions for how to use it.
	 * @return The list of intersections that form the shortest (unweighted)
	 *   path from start to goal (including both start and goal).
	 */
	public List<GeographicPoint> bfs(GeographicPoint start,
			 					     GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{
		SimpleCacheManager cacheManager = SimpleCacheManager.getInstance();
		List<GeographicPoint> cachedPath = cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(start, goal));
		if(cachedPath != null) {
			System.out.println("Returning path from cache ----------------->");
			return cachedPath;
		}
		Queue<MapNode> queue = new LinkedList<>();
		Set<MapNode> visited = new HashSet<>();
		Map<MapNode, MapNode> pathMap = new LinkedHashMap<>();
		List<GeographicPoint> path = null;
		MapNode goalNode = nodes.get(goal);
		MapNode startNode = nodes.get(start);

		queue.add(startNode);
			while (!queue.isEmpty()) {

				MapNode current = queue.poll();
				nodeSearched.accept(current.getLocation());
				if (current.equals(goalNode)) {
					path = constructPath(pathMap, startNode, goalNode);
					cacheManager.put(new AbstractMap.SimpleImmutableEntry<>(start, goal), path);
					break;
				}
				if(cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(current.getLocation(), goal)) != null) {
					return getCachedPath(goal, cacheManager, pathMap, startNode, current);
				}
				for (MapNode point : current.getNeighbours()) {
					if(visited.add(point)) {
						pathMap.put(point, current);
						queue.add(point);
					}
				}
			}

		return path;
	}

	/** Construct the path from map provided in BFS
	 *
	 * @param start The starting location
	 * @param goal The end location
	 * @return The list of intersections that form the shortest (unweighted)
	 *   path from start to goal (including both start and goal).
	 */
	private List<GeographicPoint> constructPath(Map<MapNode, MapNode> pathMap, MapNode start, MapNode goal) {
		LinkedList<GeographicPoint> path = new LinkedList<>();
		MapNode curr = goal;

		while (!curr.equals(start)) {
			path.addFirst(curr.getLocation());
			curr = pathMap.get(curr);
		}

		path.addFirst(start.getLocation());
		return path;
	}
	

	/** Find the path from start to goal using Dijkstra's algorithm
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @return The list of intersections that form the shortest path from 
	 *   start to goal (including both start and goal).
	 */
	public List<GeographicPoint> dijkstra(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
		// You do not need to change this method.
        Consumer<GeographicPoint> temp = (x) -> {};
        return dijkstra(start, goal, temp);
	}
	
	/** Find the path from start to goal using Dijkstra's algorithm
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @param nodeSearched A hook for visualization.  See assignment instructions for how to use it.
	 * @return The list of intersections that form the shortest path from 
	 *   start to goal (including both start and goal).
	 */
	public List<GeographicPoint> dijkstra(GeographicPoint start,
										  GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{
		SimpleCacheManager cacheManager = SimpleCacheManager.getInstance();
		List<GeographicPoint> cachedPath = cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(start, goal));
		if(cachedPath != null) {
			System.out.println("Returning path from cache ----------------->");
			return cachedPath;
		}
		PriorityQueue<MapNode> queue = new PriorityQueue<>(10,
				Comparator.comparing(MapNode::getDistanceFromStart));
		Set<MapNode> visited = new HashSet<>();
		Map<MapNode, MapNode> pathMap = new LinkedHashMap<>();
		List<GeographicPoint> path = null;
		MapNode goalNode = nodes.get(goal);
		MapNode startNode = nodes.get(start);

		for (MapNode n : nodes.values()) {
			n.setDistanceFromStart(Double.POSITIVE_INFINITY);
		}
		startNode.setDistanceFromStart(0.0);
		int count = 0; // count visited


		queue.add(startNode);
		while (!queue.isEmpty()) {

			MapNode current = queue.poll();
			count++;
			nodeSearched.accept(current.getLocation());
			if(visited.add(current)) {
				if (current.equals(goalNode)) {
					path = constructPath(pathMap, startNode, goalNode);
					System.out.println("Nodes visited in search: " + count + "\n");
					cacheManager.put(new AbstractMap.SimpleImmutableEntry<>(start, goal), path);
					break;
				}
				if(cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(current.getLocation(), goal)) != null) {
					return getCachedPath(goal, cacheManager, pathMap, startNode, current);
				}
				for (MapEdge edge : current.getEdges()) {
					MapNode otherNode = edge.getOtherNode(current);
					if (!visited.contains(otherNode)) {
						Double pathFromStart = current.getDistanceFromStart() + edge.getLength();
						if (pathFromStart < otherNode.getDistanceFromStart()) {
							otherNode.setDistanceFromStart(pathFromStart);
							pathMap.put(otherNode, current);
							queue.add(otherNode);
						}
					}
				}
			}
		}

		return path;
	}

	/** Find the path from start to goal using A-Star search
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @return The list of intersections that form the shortest path from 
	 *   start to goal (including both start and goal).
	 */
	public List<GeographicPoint> aStarSearch(GeographicPoint start, GeographicPoint goal) {
		// Dummy variable for calling the search algorithms
        Consumer<GeographicPoint> temp = (x) -> {};
        return aStarSearch(start, goal, temp);
	}
	
	/** Find the path from start to goal using A-Star search
	 * 
	 * @param start The starting location
	 * @param goal The goal location
	 * @param nodeSearched A hook for visualization.  See assignment instructions for how to use it.
	 * @return The list of intersections that form the shortest path from 
	 *   start to goal (including both start and goal).
	 */
	public List<GeographicPoint> aStarSearch(GeographicPoint start,
											 GeographicPoint goal, Consumer<GeographicPoint> nodeSearched)
	{
		SimpleCacheManager cacheManager = SimpleCacheManager.getInstance();
		List<GeographicPoint> cachedPath = cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(start, goal));
		if(cachedPath != null) {
			System.out.println("Returning path from cache ----------------->");
			return cachedPath;
		}

		PriorityQueue<MapNode> queue = new PriorityQueue<>(10,
				Comparator.comparing(n -> n.getDistanceToGoal() + n.getDistanceFromStart()));
		Set<MapNode> visited = new HashSet<>();
		Map<MapNode, MapNode> pathMap = new LinkedHashMap<>();
		List<GeographicPoint> path = null;
		MapNode goalNode = nodes.get(goal);
		MapNode startNode = nodes.get(start);

		for (MapNode n : nodes.values()) {
		n.setDistanceToGoal(Double.POSITIVE_INFINITY);
		n.setDistanceFromStart(Double.POSITIVE_INFINITY);
	}

		startNode.setDistanceFromStart(0.0);
		startNode.setDistanceToGoal(0.0);
		int count = 0; // count visited

		queue.add(startNode);
		while (!queue.isEmpty()) {

			MapNode current = queue.poll();
			count++;
			nodeSearched.accept(current.getLocation());
			if(visited.add(current)) {

				if (current.equals(goalNode)) {
					path = constructPath(pathMap, startNode, goalNode);
					System.out.println("Nodes visited in search: "+count + "\n");
					cacheManager.put(new AbstractMap.SimpleImmutableEntry<>(start, goal), path);
					break;
				}
				if(cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(current.getLocation(), goal)) != null) {
					return getCachedPath(goal, cacheManager, pathMap, startNode, current);
				}

				for (MapEdge edge : current.getEdges()) {
					MapNode otherNode = edge.getOtherNode(current);
					if (!visited.contains(otherNode)) {
						double pathFromStart = current.getDistanceFromStart() + edge.getLength();
						double pathToEnd = otherNode.getLocation().distance(goalNode.getLocation());
						if (pathFromStart < otherNode.getDistanceFromStart()) {
							otherNode.setDistanceFromStart(pathFromStart);
							otherNode.setDistanceToGoal(pathToEnd);
							pathMap.put(otherNode, current);
							queue.add(otherNode);
						}
					}
				}
			}
		}

		return path;
	}

	private List<GeographicPoint> getCachedPath(GeographicPoint goal, SimpleCacheManager cacheManager, Map<MapNode, MapNode> pathMap, MapNode startNode, MapNode current) {
		System.out.println("//////// Retrieving part of path from cache -------");
		List<GeographicPoint> pathToCached = constructPath(pathMap, startNode, current);
		LinkedList<GeographicPoint> wholePath = new LinkedList<>(pathToCached);
		wholePath.removeLast();
		wholePath.addAll(cacheManager.get(new AbstractMap.SimpleImmutableEntry<>(current.getLocation(), goal)));
		cacheManager.put(new AbstractMap.SimpleImmutableEntry<>(startNode.getLocation(), goal), wholePath);
		return wholePath;
	}


	public static void main(String[] args)
	{
		System.out.print("Making a new map...");
		MapGraph firstMap = new MapGraph();
		System.out.print("DONE. \nLoading the map...");
		GraphLoader.loadRoadMap("data/testdata/simpletest.map", firstMap);
		System.out.println("DONE.");

		// You can use this method for testing.
		System.out.println("Num nodes: " + firstMap.getNumVertices()); // should be 9
		System.out.println("Num edges: " + firstMap.getNumEdges()); // should be 22

		List<GeographicPoint> r = firstMap.bfs(new GeographicPoint(1.0, 1.0), new GeographicPoint(8.0, -1.0));
		System.out.println(r); // (1, 1) -> (4, 1) -> (7, 3) -> (8, -1)

		/* Here are some test cases you should try before you attempt
		 * the Week 4 End of Week Quiz, EVEN IF you score 100% on the
		 * programming assignment.
		 */

		MapGraph simpleTestMap = new MapGraph();
		GraphLoader.loadRoadMap("data/testdata/simpletest.map", simpleTestMap);

		GeographicPoint testStart = new GeographicPoint(1.0, 1.0);
		GeographicPoint testEnd = new GeographicPoint(8.0, -1.0);

		System.out.println("Test 1 using simpletest: Dijkstra should be 9 and AStar should be 5");
		List<GeographicPoint> testroute = simpleTestMap.dijkstra(testStart,testEnd);
		List<GeographicPoint> testroute2 = simpleTestMap.aStarSearch(testStart,testEnd);


		MapGraph testMap = new MapGraph();
		GraphLoader.loadRoadMap("data/maps/utc.map", testMap);

		// A very simple test using real data
		testStart = new GeographicPoint(32.869423, -117.220917);
		testEnd = new GeographicPoint(32.869255, -117.216927);
		System.out.println("Test 2 using utc: Dijkstra should be 13 and AStar should be 5");
		testroute = testMap.dijkstra(testStart,testEnd);
		testroute2 = testMap.aStarSearch(testStart,testEnd);


		// A slightly more complex test using real data
		testStart = new GeographicPoint(32.8674388, -117.2190213);
		testEnd = new GeographicPoint(32.8697828, -117.2244506);
		System.out.println("Test 3 using utc: Dijkstra should be 37 and AStar should be 10");
		testroute = testMap.dijkstra(testStart,testEnd);
		testroute2 = testMap.aStarSearch(testStart,testEnd);



		/* Use this code in Week 4 End of Week Quiz */
		MapGraph theMap = new MapGraph();
		System.out.print("DONE. \nLoading the map...");
		GraphLoader.loadRoadMap("data/maps/utc.map", theMap);
		System.out.println("DONE.");

		GeographicPoint start = new GeographicPoint(32.8648772, -117.2254046);
		GeographicPoint end = new GeographicPoint(32.8660691, -117.217393);

		List<GeographicPoint> route = theMap.dijkstra(start,end);
		List<GeographicPoint> route2 = theMap.aStarSearch(start,end);
	}

	private static String printPath(List<GeographicPoint> path) {
		String ret = "";
		if (path == null) return "";
		for (GeographicPoint point : path) {
			ret += point + "\n";
		}
		System.out.println(ret);
		return ret;
	}


}
