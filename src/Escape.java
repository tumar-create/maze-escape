import java.util.*;

public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int rows = w.rows();
        int cols = w.cols();
        int target = n - 1;

        int[][] open = Graphs.open(w, broken);

        int deadEnds = 0;
        for (int i = 0; i < n; i++) {
            if (open[i].length == 1) {
                deadEnds++;
            }
        }

        if (n == 1) {
            return new Result(new int[]{0}, 1, deadEnds);
        }

        boolean[] visited = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        int[] stack = new int[n];
        int[] edgeIndex = new int[n];
        int top = 0;

        stack[0] = 0;
        edgeIndex[0] = 0;
        visited[0] = true;
        int visitedCount = 1;
        boolean found = false;

        while (top >= 0) {
            int u = stack[top];
            if (u == target) {
                found = true;
                break;
            }

            int r = u / cols;
            int c = u % cols;

            boolean moved = false;
            while (edgeIndex[top] < 4) {
                int dir = edgeIndex[top]++;
                int v = -1;
                if (dir == 0 && r > 0) {
                    v = (r - 1) * cols + c; // up
                } else if (dir == 1 && c < cols - 1) {
                    v = r * cols + (c + 1); // right
                } else if (dir == 2 && r < rows - 1) {
                    v = (r + 1) * cols + c; // down
                } else if (dir == 3 && c > 0) {
                    v = r * cols + (c - 1); // left
                }

                if (v != -1 && !visited[v] && isOpen(open[u], v)) {
                    visited[v] = true;
                    visitedCount++;
                    parent[v] = u;
                    if (v == target) {
                        found = true;
                        break;
                    }
                    top++;
                    stack[top] = v;
                    edgeIndex[top] = 0;
                    moved = true;
                    break;
                }
            }

            if (found) {
                break;
            }

            if (!moved && edgeIndex[top] >= 4) {
                top--;
            }
        }

        // Reconstruct path from target to 0
        int pathLen = 0;
        for (int curr = target; curr != -1; curr = parent[curr]) {
            pathLen++;
            if (curr == 0) break;
        }

        int[] path = new int[pathLen];
        int curr = target;
        for (int i = pathLen - 1; i >= 0; i--) {
            path[i] = curr;
            curr = parent[curr];
        }

        return new Result(path, visitedCount, deadEnds);
    }

    private static boolean isOpen(int[] neighbors, int v) {
        for (int neighbor : neighbors) {
            if (neighbor == v) {
                return true;
            }
        }
        return false;
    }
}
