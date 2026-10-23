/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        // Custom checks:
        // 1. Union of a set with itself
        check(!d.union(2, 2), "union(2, 2) with itself returns false");
        check(d.count() == 3, "count remains 3 after self-union");

        // 2. Count after further unions down to a single component
        check(d.union(4, 5), "union(4, 5) joins the last two singletons");
        check(d.count() == 2, "2 sets: {0,1,2,3} and {4,5}");
        check(d.union(0, 4), "union(0, 4) joins all into one set");
        check(d.count() == 1, "1 set remaining");
        check(d.connected(1, 5), "1 and 5 are now connected");

        // 3. A large chain to test scaling and path compression
        int chainSize = 10000;
        DisjointSet chain = new DisjointSet(chainSize);
        boolean chainOk = true;
        for (int i = 0; i < chainSize - 1; i++) {
            chainOk &= chain.union(i, i + 1);
        }
        check(chainOk, "all chain links joined successfully");
        check(chain.count() == 1, "large chain: 1 set at the end");
        check(chain.connected(0, chainSize - 1), "large chain: start and end are connected");
        check(!chain.union(0, chainSize - 1), "large chain: duplicate union returns false");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
    }
}
