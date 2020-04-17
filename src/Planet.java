import java.security.PublicKey;

public class Planet {

    int id;
    Vector2D position;
    Vector2D velocity;
    Vector2D acceleration;
    Vector2D prevAcc;
    double mass;
    double radius;

    public Planet(){

    }
    public Planet(int id, Vector2D position, Vector2D velocity, Vector2D acceleration, double mass, double radius) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.mass = mass;
        this.radius = radius;
    }

    public Planet(Vector2D position, Vector2D velocity, Vector2D acceleration, Vector2D prevAcc, double mass) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.prevAcc = prevAcc;
        this.mass = mass;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public Vector2D getPrevAcc() {
        return prevAcc;
    }

    public void setPrevAcc(Vector2D prevAcc) {
        this.prevAcc = prevAcc;
    }

    public boolean collidesWith(Planet p){
        return position.distance(p.position) - radius - p.radius <= 0;
    }
}
