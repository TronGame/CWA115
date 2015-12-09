package cwa115.trongame.Utils;

/**
 * Created by Peter on 19/11/2015.
 */
public class Line {
    Vector2D start;
    Vector2D end;

    public Line(Vector2D start, Vector2D end) {
        this.start = start;
        this.end = end;
    }

    public boolean isDefined() {
        return !start.equals(end);
    }

    public double getLength() {
        return start.subtract(end).getLength();
    }

    public double getSlope() {
        return (end.y - start.y) / (end.x - start.x);
    }

    public double getYIntercept() {
        return start.y - getSlope()*start.x;
    }

    public double getY(double x) {
        return getSlope()*x + getYIntercept();
    }

    public Vector2D getIntersect(Line other) {
        if (!isDefined() || !other.isDefined())
            return null;

        double xCo;
        double yCo;
        if (Double.isInfinite(getSlope())) {
            xCo = start.x;
            yCo = other.getY(xCo);
        } else if (Double.isInfinite(other.getSlope())) {
            xCo = other.start.x;
            yCo = getY(xCo);
        } else {
            xCo = -(getYIntercept() - other.getYIntercept()) / (getSlope() - other.getSlope());
            yCo = getY(xCo);
        }
        return new Vector2D(xCo, yCo);
    }

    public Vector2D[] getPointAtDist(double dist, Vector2D pt) {
        if (!isDefined())
            return new Vector2D[0];

        double r = getSlope();
        double q = getYIntercept();

        if (distanceTo(pt) >= dist) {
            return new Vector2D[0];
        }

        double xCo1; double xCo2; double yCo1; double yCo2;

        if (Double.isInfinite(r)) {
            xCo1 = start.x;
            yCo1 = pt.y + Math.sqrt(Math.pow(dist, 2)-Math.pow(start.x-pt.x, 2));

            xCo2 = start.x;
            yCo2 = pt.y - Math.sqrt(Math.pow(dist, 2)-Math.pow(start.x-pt.x, 2));
        } else {
            double a = Math.pow(r, 2)+1;
            double b = 2*q*r - 2*pt.x - 2*r*pt.y;
            double c = Math.pow(q, 2) - 2*q*pt.y + Math.pow(pt.x, 2) + Math.pow(pt.y, 2) - Math.pow(dist, 2);
            if (a==0) {
                return new Vector2D[] {
                        new Vector2D(-c/b, getY(-c/b))
                };
            }
            xCo1 = -(b + Math.sqrt(Math.pow(b, 2) - 4*a*c))/(2*a);
            yCo1 = getY(xCo1);

            xCo2 = -(b - Math.sqrt(Math.pow(b, 2) - 4*a*c))/(2*a);
            yCo2 = getY(xCo2);
        }

        return new Vector2D[] {
                new Vector2D(xCo1, yCo1),
                new Vector2D(xCo2, yCo2)
        };
    }

    public boolean isOn(Vector2D pt) {
        return ((start.x <= pt.x && pt.x <= end.x) || (start.x >= pt.x && pt.x >= end.x)) && 
                ((start.y <= pt.y && pt.y <= end.y) || (start.y >= pt.y && pt.y >= end.y));
    }

    public double distanceTo(Vector2D pt) {
        double a = getSlope();
        double b = -1;
        double c = getYIntercept();

        if (Double.isInfinite(a)) {
            return Math.abs(pt.x - start.x);
        }

        return Math.abs(a*pt.x + b*pt.y + c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    public Vector2D getClosestPoint(Vector2D pt) {
        double a = getSlope();
        double b = -1;
        double c = getYIntercept();

        if (Double.isInfinite(a)) {
            return new Vector2D(start.x, pt.y);
        }

        double xCo = (b*(b*pt.x - a*pt.y) - a*c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        double yCo = getY(xCo);

        return new Vector2D(xCo, yCo);
    }
}
