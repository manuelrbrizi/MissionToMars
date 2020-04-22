public class Vector {

    public double x;
    public double y;

    public Vector() { }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector v) {
        set(v);
    }

    public void add(Vector v) {
        this.x += v.x;
        this.y += v.y;
    }


    public Vector getAdded(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }


    public Vector substract(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }



    public Vector multiply(double scalar) {
        return new Vector(x * scalar, y * scalar);
    }


    public Vector divide(double scalar) {
        return new Vector(x / scalar, y / scalar);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector v) {
        this.x = v.x;
        this.y = v.y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double distance(Vector v) {
        double vx = v.x - this.x;
        double vy = v.y - this.y;
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double distanceSq(Vector v) {
        double vx = v.x - this.x;
        double vy = v.y - this.y;
        return (vx * vx + vy * vy);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public Vector perp() {
        return new Vector(-y, x);
    }


}