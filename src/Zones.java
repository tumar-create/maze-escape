import java.util.*;

public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] slidesFrom = Graphs.slidesFrom(s);
        int[][] slidesInto = Graphs.slidesInto(s);

        // Pass 1: iterative DFS on slidesFrom to record finish order
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int orderIdx = 0;

        int[] stack = new int[n];
        int[] edgeIdx = new int[n];

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;

            int top = 0;
            stack[0] = start;
            edgeIdx[0] = 0;
            visited[start] = true;

            while (top >= 0) {
                int u = stack[top];
                int[] outgoing = slidesFrom[u];
                if (edgeIdx[top] < outgoing.length) {
                    int slide = outgoing[edgeIdx[top]++];
                    int v = s.to()[slide];
                    if (!visited[v]) {
                        visited[v] = true;
                        top++;
                        stack[top] = v;
                        edgeIdx[top] = 0;
                    }
                } else {
                    order[orderIdx++] = u;
                    top--;
                }
            }
        }

        // Pass 2: iterative DFS on reversed graph (slidesInto) in reverse finish order
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zoneCount = 0;

        int[] stack2 = new int[n];

        for (int i = n - 1; i >= 0; i--) {
            int root = order[i];
            if (zoneOf[root] != -1) continue;

            int currentZone = zoneCount++;
            zoneOf[root] = currentZone;
            int top2 = 0;
            stack2[0] = root;

            while (top2 >= 0) {
                int u = stack2[top2--];
                for (int slide : slidesInto[u]) {
                    int v = s.from()[slide];
                    if (zoneOf[v] == -1) {
                        zoneOf[v] = currentZone;
                        stack2[++top2] = v;
                    }
                }
            }
        }

        return zoneOf;
    }

    private record ZoneEdge(int to, long cost) {}

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        if (zoneCount <= 0) {
            return new long[]{0, 0};
        }

        // Aggregate cheapest slides between different zones
        Map<Long, Long> minEdge = new HashMap<>();
        for (int i = 0; i < s.count(); i++) {
            int zU = zoneOf[s.from()[i]];
            int zV = zoneOf[s.to()[i]];
            if (zU != zV) {
                long key = (((long) zU) << 32) | (zV & 0xFFFFFFFFL);
                long time = s.time()[i];
                minEdge.merge(key, time, Math::min);
            }
        }

        @SuppressWarnings("unchecked")
        List<ZoneEdge>[] adj = new ArrayList[zoneCount];
        for (int i = 0; i < zoneCount; i++) {
            adj[i] = new ArrayList<>();
        }
        for (Map.Entry<Long, Long> entry : minEdge.entrySet()) {
            long key = entry.getKey();
            int u = (int) (key >>> 32);
            int v = (int) (long) key;
            adj[u].add(new ZoneEdge(v, entry.getValue()));
        }

        // 1. Flow stages: longest chain of zones in the zone graph
        int[] stages = new int[zoneCount];
        Arrays.fill(stages, 1);
        long maxStages = 1;
        for (int z = 0; z < zoneCount; z++) {
            maxStages = Math.max(maxStages, stages[z]);
            for (ZoneEdge e : adj[z]) {
                stages[e.to()] = Math.max(stages[e.to()], stages[z] + 1);
            }
        }

        // 2. Fastest route from zone of cell 0 to zone of cell n-1
        int startZone = zoneOf[0];
        int exitZone = zoneOf[s.n() - 1];

        long fastestCost = 0;
        if (startZone != exitZone) {
            long[] dist = new long[zoneCount];
            Arrays.fill(dist, Long.MAX_VALUE);
            dist[startZone] = 0;

            for (int z = startZone; z < zoneCount; z++) {
                if (dist[z] != Long.MAX_VALUE) {
                    for (ZoneEdge e : adj[z]) {
                        if (dist[z] + e.cost() < dist[e.to()]) {
                            dist[e.to()] = dist[z] + e.cost();
                        }
                    }
                }
            }
            fastestCost = dist[exitZone];
        }

        return new long[]{maxStages, fastestCost};
    }
}
