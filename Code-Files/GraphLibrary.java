import net.datastructures.Vertex;

import java.util.*;

/**
 * Library for graph analysis
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 * @author Tim Pierson, Dartmouth CS10, provided for Winter 2024
 * @author Jacob Bacus
 */
public class GraphLibrary {
    /**
     * Takes a random walk from a vertex, up to a given number of steps
     * So a 0-step path only includes start, while a 1-step path includes start and one of its out-neighbors,
     * and a 2-step path includes start, an out-neighbor, and one of the out-neighbor's out-neighbors
     * Stops earlier if no step can be taken (i.e., reach a vertex with no out-edge)
     * @param g		graph to walk on
     * @param start	initial vertex (assumed to be in graph)
     * @param steps	max number of steps
     * @return		a list of vertices starting with start, each with an edge to the sequentially next in the list;
     * 			    null if start isn't in graph
     */
    public static <V,E> List<V> randomWalk(Graph<V,E> g, V start, int steps) {
        List<V> res = new ArrayList<>();
        if (!g.hasVertex(start)) {
            return res;
        }
        V current = start;
        if(g.outDegree(current) == 0) {
            return res;
        } else {
            res.add(current);
        }
        for (int i = 0; i < steps; i++) {
            List<V> currNeighbors = new ArrayList<>();
            for(V vertex : g.outNeighbors(current)) {
                currNeighbors.add(vertex);
            }
            if(currNeighbors.isEmpty()) {
                return res;
            }
            int randIndex = (int)(Math.random() * currNeighbors.size());
            res.add(currNeighbors.get(randIndex));
            current = currNeighbors.get(randIndex);
        }
        return res;
    }

    /**
     * Orders vertices in decreasing order by their in-degree
     * @param g		graph
     * @return		list of vertices sorted by in-degree, decreasing (i.e., largest at index 0)
     */
    public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
        List<V> res = new ArrayList<>();
        for (V v : g.vertices()) {
            res.add(v);
        }
        res.sort((V v1, V v2) -> g.inDegree(v2) - g.inDegree(v1));
        return res;
    }

    /**
     * Takes a graph of vertices and edges and returns a graph of shortest paths
     * @param g input graph
     * @param source center of "universe" in graph
     * @return graph of shortest paths
     * @param <V> vertices type
     * @param <E> edge type
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) {
        Graph<V, E> res = new AdjacencyMapGraph<>();
        Queue<V> toVisit = new LinkedList<>();
        Map<V, V> visited = new HashMap<>();
        visited.put(source, null);
        toVisit.add(source);
        while(!toVisit.isEmpty()) {
            V curr = toVisit.remove();
            res.insertVertex(curr);
            for (V vertex : g.outNeighbors(curr)) {
                if(!visited.containsKey(vertex)) {
                    toVisit.add(vertex);
                    visited.put(vertex, curr);
                }
            }
        }
        for (V vertex : visited.keySet()) {
            if(visited.get(vertex) != null) {
                res.insertDirected(vertex, visited.get(vertex), g.getLabel(visited.get(vertex), vertex));
            }
        }

        return res;
    }

    /**
     * Reconstructs a path from a vertice to the center of the "universe"
     * @param tree tree of shortest paths
     * @param v starting vertice
     * @return list that represents path to center
     * @param <V> vertices type
     * @param <E> edge type
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
        List<V> res = new ArrayList<>();
        V curr = v;
        while(tree.outDegree(curr) != 0) {
            res.add(curr);
            for (V vertex : tree.outNeighbors(curr)) {
                curr = vertex;
            }
        }
        res.add(curr);
        return res;
    }

    /**
     * Finds vertices that are in a graph, but not a subgraph
     * @param graph the base graph
     * @param subgraph the subgraph (from bfs)
     * @return a set of all vertices in graph not in subgraph
     * @param <V> vertice type
     * @param <E> edge type
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
        Set<V> res = new HashSet<>();
        for(V vertex : graph.vertices()) {
            if (!subgraph.hasVertex(vertex)) {
                res.add(vertex);
            }
        }
        return res;
    }

    /**
     * function that finds the average length of a path from all vertices to a root
     * @param tree graph of shortest paths
     * @param root the central point of the graph
     * @return average distance from node to root
     * @param <V> vertice type
     * @param <E> edge type
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        return averageSeparationRecursion(tree, root, 0.0) / (tree.numVertices() - 1);
    }

    /**
     * recrusive helper function for averageSeparation
     * @param tree tree of shortest paths
     * @param root current root node
     * @param depth current depth of recursion
     * @return returns the depth of various nodes up the tree
     * @param <V> the vertice type
     * @param <E> the edge type
     */
    private static <V,E> double averageSeparationRecursion(Graph<V,E> tree, V root, double depth) {
        double res = 0.0;
        for(V vertex : tree.inNeighbors(root)) {
            res += averageSeparationRecursion(tree, vertex, depth + 1);
        }
        res = res + depth;
        return res;
    }

}