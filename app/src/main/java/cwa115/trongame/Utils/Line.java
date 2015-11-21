package cwa115.trongame.Utils;

/**
 * Created by Peter on 19/11/2015.
 */
public class Line {
    Vector2D start;
    Vector2D end;

    public Line(Vector2D start, Vector2D end) {
        if (start.equals(end))
            return;
        this.start = start;
        this.end = end;
    }

    public double getLength() {
        return start.subtract(end).getLength();
    }

    public double getRico() {
        return (end.y - start.y) / (end.x - start.x);
    }

    public double getYIntercept() {
        return start.y - getRico()*start.x;
    }

    public double getY(double x) {
        return getRico()*x + getYIntercept();
    }

    public Vector2D getIntersect(Line other) {
        double xCo = - (getYIntercept()-other.getYIntercept()) / (getRico()-other.getRico());
        double yCo = getY(xCo);
        return new Vector2D(xCo, yCo);
    }

    public Vector2D[] getPointAtDist(double dist, Vector2D pt) {
        double r = getRico();
        double q = getYIntercept();

        if (distanceTo(pt) >= dist) {
            return new Vector2D[0];
        }

        double xCo1 = (pt.x + pt.y*r - r*q + Math.sqrt(-Math.pow(pt.x*r, 2) + 2*pt.x*pt.y*r - 2*pt.x*r*q - Math.pow(pt.y, 2) + 2*pt.y*q + Math.pow(dist*r, 2) + Math.pow(dist, 2) - Math.pow(q, 2))) / (Math.pow(r, 2) - 1);
        double yCo1 = getY(xCo1);

        double xCo2 = (pt.x + pt.y*r - r*q - Math.sqrt(-Math.pow(pt.x*r, 2) + 2*pt.x*pt.y*r - 2*pt.x*r*q - Math.pow(pt.y, 2) + 2*pt.y*q + Math.pow(dist*r, 2) + Math.pow(dist, 2) - Math.pow(q, 2))) / (Math.pow(r, 2) - 1);
        double yCo2 = getY(xCo1);

        return new Vector2D[] {
                new Vector2D(xCo1, yCo1),
                new Vector2D(xCo2, yCo2)
        };
    }

    public boolean isOn(Vector2D pt) {
        return (start.x <= pt.x && pt.x <= end.x) || (start.x >= pt.x && pt.x >= end.x);
    }

    public double distanceTo(Vector2D pt) {
        double a = getRico();
        double b = -1;
        double c = getYIntercept();

        return Math.abs(a*pt.x + b*pt.y + c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public Vector2D getClosestPoint(Vector2D pt) {
        double a = getRico();
        double b = -1;
        double c = getYIntercept();

        double xCo = (b*(b*pt.x - a*pt.y) - a*c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        double yCo = getY(xCo);

        return new Vector2D(xCo, yCo);
    }
}
