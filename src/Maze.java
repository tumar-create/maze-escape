import java.util.*;

public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[w.cells() - 1];
        int count = 0;
        for (int k = 0; k < w.count(); k++) {
            if (dsu.union(w.a()[k], w.b()[k])) {
                broken[count++] = k;
                if (count == w.cells() - 1) {
                    break;
                }
            }
        }
        return count == broken.length ? broken : Arrays.copyOf(broken, count);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];
        for (int i = 0; i < w.count(); i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingInt(i -> w.cost()[i]));

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[w.cells() - 1];
        int count = 0;
        for (int k : order) {
            if (dsu.union(w.a()[k], w.b()[k])) {
                broken[count++] = k;
                if (count == w.cells() - 1) {
                    break;
                }
            }
        }
        return count == broken.length ? broken : Arrays.copyOf(broken, count);
    }

    private record Edge(int to, int cost) {}

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        int[] head = new int[n];
        int[] next = new int[2 * w.count()];
        int[] to = new int[2 * w.count()];
        int[] cost = new int[2 * w.count()];
        Arrays.fill(head, -1);
        int edgeCount = 0;

        for (int k = 0; k < w.count(); k++) {
            int u = w.a()[k], v = w.b()[k], c = w.cost()[k];
            to[edgeCount] = v; cost[edgeCount] = c; next[edgeCount] = head[u]; head[u] = edgeCount++;
            to[edgeCount] = u; cost[edgeCount] = c; next[edgeCount] = head[v]; head[v] = edgeCount++;
        }

        boolean[] inMst = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(Edge::cost));
        inMst[0] = true;
        int visitedCount = 1;

        for (int e = head[0]; e != -1; e = next[e]) {
            pq.add(new Edge(to[e], cost[e]));
        }

        long totalCost = 0;
        while (!pq.isEmpty() && visitedCount < n) {
            Edge edge = pq.poll();
            if (inMst[edge.to()]) {
                continue;
            }
            inMst[edge.to()] = true;
            totalCost += edge.cost();
            visitedCount++;

            for (int e = head[edge.to()]; e != -1; e = next[e]) {
                int neighbor = to[e];
                if (!inMst[neighbor]) {
                    pq.add(new Edge(neighbor, cost[e]));
                }
            }
        }

        return totalCost;
    }
}
