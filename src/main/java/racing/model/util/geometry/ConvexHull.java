package racing.model.util.geometry;

import java.util.Stack;
import racing.model.util.geometry.Point;

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
// von https://en.wikipedia.org/wiki/Gift_wrapping_algorithm (16. Jun. 2026, 11.40)
public class ConvexHull {

    public ConvexHull() {}

    // vgl. Wikipedia
    public Point[] jarvis(Point[] s) {
        Stack<Point> hull = new Stack<>();

        Point pointOnHull = leftmostPoint(s);

        do {
            hull.push(pointOnHull);

            Point endpoint = s[0];

            for (int j = 0; j < s.length; j++) {
                if (
                    endpoint.equals(pointOnHull) ||
                    isLeft(pointOnHull, endpoint, s[j])
                ) {
                    endpoint = s[j];
                }
            }

            pointOnHull = endpoint;
        } while (!pointOnHull.equals(hull.get(0)));

        return hull.toArray(new Point[0]);
    }

    private Point leftmostPoint(Point[] s) {
        Point r = s[0];

        for (Point p : s) {
            if (p.getX() < r.getX()) {
                r = p;
            }
        }

        return r;
    }

    private boolean isLeft(Point a, Point b, Point c) {
        int cross =
            (b.getX() - a.getX()) * (c.getY() - a.getY()) -
            (b.getY() - a.getY()) * (c.getX() - a.getX());

        return cross > 0;
    }
}
