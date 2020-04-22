public class Planet {

    int id;
    Vector position;
    Vector velocity;
    Vector acceleration;
    Vector prevAcc;
    double mass;
    double radius;

    public Planet(){}

    public Planet(int id, Vector position, Vector velocity, Vector acceleration, double mass, double radius) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.mass = mass;
        this.radius = radius;
    }

    public Planet(Vector position, Vector velocity, Vector acceleration, Vector prevAcc, double mass) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.prevAcc = prevAcc;
        this.mass = mass;
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public Vector getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public Vector getPrevAcc() {
        return prevAcc;
    }

    public void setPrevAcc(Vector prevAcc) {
        this.prevAcc = prevAcc;
    }

    public boolean collidesWith(Planet p){
        return position.distance(p.position) - radius - p.radius <= 0;
    }
}
