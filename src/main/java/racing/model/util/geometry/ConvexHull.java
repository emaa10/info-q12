package racing.model.util.geometry;

import java.util.Stack;
import racing.model.util.geometry.Point;

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
