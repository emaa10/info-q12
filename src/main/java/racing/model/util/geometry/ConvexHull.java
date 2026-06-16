package racing.model.util.geometry;

// TODO: Stack selber implementieren, war faul
import java.util.Stack;

public class ConvexHull {

    public ConvexHull() {}

    public int[] jarvis(Point[] s) {}

    /*
    algorithm jarvis(S) is
        // S is the set of points
        // P will be the set of points which form the convex hull. Final set size is i.
        pointOnHull := leftmost point in S // which is guaranteed to be part of the CH(S)
        i := 0
        repeat
            P[i] := pointOnHull
            endpoint := S[0]      // initial endpoint for a candidate edge on the hull
            for j from 0 to |S| do
                // endpoint == pointOnHull is a rare case and can happen only when j == 1 and a better endpoint has not yet been set for the loop
                if (endpoint == pointOnHull) or (S[j] is on left of line from P[i] to endpoint) then
                    endpoint := S[j]   // found greater left turn, update endpoint
            i := i + 1
            pointOnHull := endpoint
        until endpoint == P[0]      // wrapped around to first hull point
    */
}
