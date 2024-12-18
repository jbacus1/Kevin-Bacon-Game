import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Runs a Kevin Bacon game where you see how many movies it takes to link actors
 * Author: Jacob Bacus
 */
public class BaconGame {

    // Tracks if game is running
    public boolean gameRunning = true;

    // Graph currently in use by game
    public Graph<String, Set<String>> gameGraph;

    // Current center of graph
    public String center;

    // Actors and movies loaded in
    public Graph<String, Set<String>> myGraph;

    // Getter for myGraph (necessary for main)
    public Graph<String, Set<String>> getMyGraph() {return myGraph;}

    /**
     * Generates a graph containing all actors and movies
     * @param moviesPath path to movies file
     * @param actorsPath path to actors file
     * @param movieActorsPath path from a movie to an actor
     * @throws IOException errors in file reader
     */
    public void generateGraph(String moviesPath, String actorsPath, String movieActorsPath) throws IOException {
        Graph<String, Set<String>> res = new AdjacencyMapGraph<>();
        Map<String, String> idToMovie = new HashMap<>();
        Map<String, String> idToActor = new HashMap<>();
        Map<String, Set<String>> movieIdtoActorIdSet = new HashMap<>();
        BufferedReader movies = null;
        BufferedReader actors = null;
        BufferedReader movieActors = null;
        try {
            movies = new BufferedReader(new FileReader(moviesPath));
            String movieInput;
            while ((movieInput = movies.readLine()) != null) {
                String[] movieInputList = movieInput.split("\\|");
                idToMovie.put(movieInputList[0], movieInputList[1]);
            }

            actors = new BufferedReader(new FileReader(actorsPath));
            String actorInput;
            while ((actorInput = actors.readLine()) != null) {
                String[] actorInputList = actorInput.split("\\|");
                idToActor.put(actorInputList[0], actorInputList[1]);
            }

            movieActors = new BufferedReader(new FileReader(movieActorsPath));
            String movieActorInput;
            while((movieActorInput = movieActors.readLine()) != null) {
                String[] movieActorInputList = movieActorInput.split("\\|");
                if(!movieIdtoActorIdSet.containsKey(movieActorInputList[0])) {
                    movieIdtoActorIdSet.put(movieActorInputList[0], new HashSet<>());
                    Set<String> currActorSet = movieIdtoActorIdSet.get(movieActorInputList[0]);
                    currActorSet.add(movieActorInputList[1]);
                } else {
                    Set<String> currActorSet = movieIdtoActorIdSet.get(movieActorInputList[0]);
                    currActorSet.add(movieActorInputList[1]);
                }
            }

            for (String actorId : idToActor.keySet()) {
                res.insertVertex(idToActor.get(actorId));
            }

            for (String movieId : movieIdtoActorIdSet.keySet()) {
                Set<String> actorSet = movieIdtoActorIdSet.get(movieId);
                List<String> actorList = new ArrayList<>(actorSet);
                for(int i = 0; i < actorList.size(); i++) {
                    for(int j = i + 1; j < actorList.size(); j++) {
                        String actor1 = idToActor.get(actorList.get(i));
                        String actor2 = idToActor.get(actorList.get(j));
                        Set<String> moviesSet = res.getLabel(actor1, actor2);
                        if (moviesSet == null) {
                            moviesSet = new HashSet<>();
                            res.insertUndirected(actor1, actor2, moviesSet);
                        }
                        moviesSet.add(idToMovie.get(movieId));
                    }
                }
            }

        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            if(movies != null){
                movies.close();
            }
            if(actors != null){
                actors.close();
            }
            if(movieActors != null){
                movieActors.close();
            }
        }
        myGraph = res;
    }

    /**
     * Takes input from game and performs appropriate actions
     * @param input the String that was entered
     * @param graph current graph of all points
     */
    public void handleInput(String input, Graph<String, Set<String>> graph) {
        if (Objects.equals(input, "q") || Objects.equals(input, "Q")) {
            gameRunning = false;
        } else if (Objects.equals(input.charAt(0), 'u') || Objects.equals(input.charAt(0), 'U')){
            generateUniverse(input.substring(2), graph);
        } else if (Objects.equals(input.charAt(0), 'a') || Objects.equals(input.charAt(0), 'A')){
            System.out.println("The average separation from " + center + " is: ");
            System.out.println(GraphLibrary.averageSeparation(gameGraph, center));
            System.out.println(" ");
        } else if (Objects.equals(input.charAt(0), 's') || Objects.equals(input.charAt(0), 'S')){
            System.out.println(gameGraph.numVertices() - 1 + " actors are connected to the center");
            System.out.println(" ");
        } else if (Objects.equals(input.charAt(0), 'p') || Objects.equals(input.charAt(0), 'P')){
            generatePath(input.substring(2));
        } else if (Objects.equals(input.charAt(0), 'd') || Objects.equals(input.charAt(0), 'D')){
            System.out.println("The actors with the top 5 most costars are: ");
            System.out.println(highestDegree());
            System.out.println(" ");
        } else if (Objects.equals(input.charAt(0), 'l') || Objects.equals(input.charAt(0), 'L')){
            System.out.println("The actors with the lowest 5 average separation values are: ");
            System.out.println(lowestSeparation());
            System.out.println(" ");
        } else {
            System.out.println("Invalid Input");
            System.out.println(" ");
        }
    }

    /**
     * Finds the vertices that have the lowest average degree from their adjacent nodes
     * @return 5 vertices with the lowest degree (of separation)
     */
    public List<String> lowestSeparation(){
        List<String> res = new ArrayList<>();
        Map<String, Double> separationMap = new HashMap<>();
        PriorityQueue<String> resQueue = new PriorityQueue<>(Comparator.comparingDouble(separationMap::get));
        for(String actor : myGraph.vertices()) {
            Graph<String, Set<String>> currGraph = GraphLibrary.bfs(myGraph, actor);
            separationMap.put(actor, GraphLibrary.averageSeparation(currGraph, actor));
            resQueue.add(actor);
        }
        for(int i = 0; i < 5 && !resQueue.isEmpty(); i++) {
            res.add(resQueue.poll());
        }
        return res;
    }

    /**
     * Finds the vertices that have the most connections to them i.e. costars
     * @return 5 vertices with highest inDegree
     */
    public List<String> highestDegree(){
        List<String> list = GraphLibrary.verticesByInDegree(myGraph);
        List<String> res = new ArrayList<>();
        for(int i = 0; i < 5 && i < list.size(); i++) {
            res.add(list.get(i));
        }
        return res;
    }

    /**
     * Takes path from BFS and prints out the traversal
     * @param start the starting vertice going to the center
     */
    public void generatePath(String start) {
        List<String> path = GraphLibrary.getPath(gameGraph, start);
        System.out.println(start + "'s number is " + (path.size() - 1));
        System.out.println(" ");
        for (int i = 1; i < path.size(); i++) {
            System.out.println(path.get(i-1) + " appeared in " + gameGraph.getLabel(path.get(i-1), path.get(i)) + " with " + path.get(i));
        }
        System.out.println(" ");
    }

    /**
     * Generates paths in reference to the current center
     * @param name name of the new center
     * @param graph graph of all actors and movies
     */
    public void generateUniverse(String name, Graph<String, Set<String>> graph) {
        gameGraph = GraphLibrary.bfs(graph, name);
        center = name;
        System.out.println(name + " is now the center of the Universe");
        System.out.println(" ");
    }

    /**
     * Runs the game with a while loop, asks for user input
     * @throws IOException from BufferedReaders
     */
    public static void main(String[] args) throws IOException {
        String movieFileName = "movies.txt";
        String actorFileName = "actors.txt";
        String movieActorFileName = "movie-actors.txt";

        System.out.println("Welcome to the Kevin Bacon Game!");
        System.out.println("\nCommands: ");
        System.out.println("\tq = quit game");
        System.out.println("\tu <name> = generate a game with <name> at universe center");
        System.out.println("\tp <name> = find shortest path from an actor to the center of the universe");
        System.out.println("\ta = find average length of actors connected to the current center");
        System.out.println("\ts = returns the number of actors connected to the current center");
        System.out.println("\td = returns actors with top 5 most costars");
        System.out.println("\tl = returns actors with top 5 smallest average path lengths (This might take a while)\n");
        BaconGame myGame = new BaconGame();
        myGame.generateGraph(movieFileName, actorFileName, movieActorFileName);

        while (myGame.gameRunning) {
            Scanner myScanner = new Scanner(System.in);
            String input = myScanner.nextLine();
            myGame.handleInput(input, myGame.getMyGraph());
        }
    }
}
