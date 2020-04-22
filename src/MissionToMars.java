import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissionToMars {

    public static void main(String[] args) {
        MissionToMars a = new MissionToMars();
        PrintWriter writer = null;

        try {
            writer = new PrintWriter("outputOVITO.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.print("");
        writer.close();

        List<List<Planet>> days = a.getDailyPlanetPositions(86400*370*2,1);
        List<List<Planet>> hours = a.getHourlyPlanetPositions(1,days.get(100));
        List<List<Planet>> minutes = a.getMinutePlanetPositions(1,hours.get(18));
        a.launchShipOnDay(days.get(61),5,86400*370*6,new Vector(3850,3850),true);
    }

    public double launchShipOnDay(List<Planet> planets, double dt, double totalTime, Vector shipVelocity, boolean animate){
        Planet sun = planets.get(0);
        Planet earth = planets.get(1);
        Planet mars = planets.get(2);

        double angle = earth.position.angle();

        Vector pos = new Vector(earth.position.x+Math.cos(angle)*(1500000+6371000),earth.position.y+Math.sin(angle)*(1500000+6371000));
        angle =  pos.perp().angle();
        Vector vel = new Vector(earth.velocity.x+(7120+shipVelocity.x)*Math.cos(angle),earth.velocity.y+(shipVelocity.y+7120)*Math.sin(angle));
        Vector acc = new Vector();
        Planet ship = new Planet(2,pos,vel,acc,2e5 ,0);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars, ship)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth, ship)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars, ship)));
        ship.setAcceleration(applyForce(ship,Arrays.asList(sun, mars, earth)));

        generateOvitoFile(Arrays.asList(sun,mars,ship,earth));

        Planet newEarth,newSun,newMars,newShip;
        double time = 0;
        double distance = Double.POSITIVE_INFINITY;
        double travelled = 0;
        boolean impulse = false;
        double minTime = 0;

        while (time < totalTime ) {
            if(time%86400 == 0){
                generateOvitoFile(Arrays.asList(ship,mars,earth,sun));
            }

            if(ship.collidesWith(mars)){
                System.out.print("Collision!\n");
                System.out.printf("Travel time: %f\n",time);
                System.out.printf("Vx:%f\tVy:%f\n",ship.velocity.x, ship.velocity.y);
                System.out.printf("Distance:%f\n",travelled);
                return time;
            }

            if(mars.position.distance(ship.position)-mars.radius > distance && !impulse){
                angle = ship.position.perp().angle();
                ship.setVelocity(ship.velocity.getAdded(new Vector(2525*Math.cos(angle),2525*Math.sin(angle))));
                impulse = true;
            }

            if((mars.position.distance(ship.position) - mars.radius) < distance){
                distance = mars.position.distance(ship.position) - mars.radius;
                minTime = time;
            }

            newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars,ship)), dt);
            newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars,ship)), dt);
            newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth,ship)), dt);
            newShip = movePlanetVerlet(ship, new ArrayList<>(Arrays.asList(sun, earth,mars)), dt);

            travelled+= ship.position.distance(newShip.position);

            earth = newEarth;
            mars = newMars;
            sun = newSun;
            ship = newShip;

            time += dt;
        }

        return minTime;
    }

    public List<List<Planet>> getDailyPlanetPositions(double totalTime, double dt){
        Vector pos = new Vector(auToMeter(-9.600619697743452e-1),auToMeter(-2.822355844063401e-1));
        Vector vel = new Vector(auDayToMeterSec(4.572972309577654e-3),auDayToMeterSec(-1.656334129232400e-2));
        Vector acc = new Vector();
        Planet earth = new Planet(1,pos,vel,acc,5.972e24,6371000 );

        pos = new Vector(auToMeter(-1.651921224501318e-1),auToMeter(-1.459738176234038));
        vel = new Vector(auDayToMeterSec(1.443361788681964e-2),auDayToMeterSec(-3.703429642664186e-4));
        acc = new Vector();
        Planet mars = new Planet(3,pos,vel,acc,6.39e23 ,3389500);

        pos = new Vector(0,0);
        vel = new Vector(0,0);
        acc = new Vector();
        Planet sun = new Planet(0,pos,vel,acc,1.989e30,696340000 );

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars)));

        Planet newEarth,newSun,newMars;
        List<List<Planet>> days = new ArrayList<>();

        double time = 0;

        while (time < totalTime ) {
            if(time % 86400 == 0){
                days.add(Arrays.asList(sun,earth,mars));
            }

            newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars)), dt);
            newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars)), dt);
            newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth)), dt);

            earth = newEarth;
            mars = newMars;
            sun = newSun;

            time += dt;
        }

        return days;
    }

    public List<List<Planet>> getHourlyPlanetPositions(double dt,List<Planet> planets){
        Planet sun = planets.get(0);
        Planet earth = planets.get(1);
        Planet mars = planets.get(2);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars)));

        Planet newEarth,newSun,newMars;
        List<List<Planet>> hours = new ArrayList<>();

        double time = 0;

        while (time < 3600*24) {
            if(time % 3600 == 0){
                hours.add(Arrays.asList(sun,earth,mars));
            }

            newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars)), dt);
            newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars)), dt);
            newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth)), dt);

            earth = newEarth;
            mars = newMars;
            sun = newSun;

            time += dt;
        }

        return hours;
    }

    public List<List<Planet>> getMinutePlanetPositions(double dt, List<Planet> planets){
        Planet sun = planets.get(0);
        Planet earth = planets.get(1);
        Planet mars = planets.get(2);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars)));

        Planet newEarth,newSun,newMars;
        List<List<Planet>> minutes = new ArrayList<>();

        double time = 0;

        while (time < 3600) {
            if(time % 60 == 0){
                minutes.add(Arrays.asList(sun,earth,mars));
            }

            newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars)), dt);
            newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars)), dt);
            newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth)), dt);

            earth = newEarth;
            mars = newMars;
            sun = newSun;

            time += dt;
        }

        return minutes;
    }

    private void calculatePrevAcceleration(Planet earth, Planet sun, Planet mars, Planet ship, double dt){
        Vector earthPrevVel = earth.getVelocity().getAdded(earth.getAcceleration().multiply(-1*dt));
        Vector earthPrevPos = earth.getPosition().getAdded(earthPrevVel.multiply(dt)).getAdded(earth.getAcceleration().multiply(dt*dt/2));

        Vector sunPrevVel = sun.getVelocity().getAdded(sun.getAcceleration().multiply(-1*dt));
        Vector sunPrevPos = sun.getPosition().getAdded(sunPrevVel.multiply(dt)).getAdded(sun.getAcceleration().multiply(dt*dt/2));

        Vector marsPrevVel = mars.getVelocity().getAdded(mars.getAcceleration().multiply(-1*dt));
        Vector marsPrevPos = mars.getPosition().getAdded(marsPrevVel.multiply(dt)).getAdded(mars.getAcceleration().multiply(dt*dt/2));

        Vector shipPrevVel = ship.getVelocity().getAdded(ship.getAcceleration().multiply(-1*dt));
        Vector shipPrevPos = ship.getPosition().getAdded(shipPrevVel.multiply(dt)).getAdded(ship.getAcceleration().multiply(dt*dt/2));

        Planet oldEarth = new Planet(100,earthPrevPos, earthPrevVel, new Vector(), 5.972e24, 6371000);
        Planet oldSun = new Planet(200,sunPrevPos, sunPrevVel, new Vector(), 1.989e30, 696340000);
        Planet oldMars = new Planet(300,marsPrevPos, marsPrevVel, new Vector(), 6.39e23, 3389500);
        Planet oldShip = new Planet(400,shipPrevPos, shipPrevVel, new Vector(), 2e5, 0);

        earth.setPrevAcc(applyForce(oldEarth, Arrays.asList(oldSun, oldMars, oldShip)));
        sun.setPrevAcc(applyForce(oldSun, Arrays.asList(oldMars, oldEarth, oldShip)));
        mars.setPrevAcc(applyForce(oldMars, Arrays.asList(oldSun, oldEarth, oldShip)));
        ship.setPrevAcc(applyForce(oldShip, Arrays.asList(oldSun, oldEarth, oldMars)));
    }

    public static void generateOvitoFile(List<Planet> planets){
        StringBuilder sb = new StringBuilder();
        sb.append(planets.size());
        sb.append("\n");
        sb.append("\n");

        for (Planet p: planets){
            sb.append(p.position.x);
            sb.append("\t");
            sb.append(p.position.y);
            sb.append("\t");

            if(p.id == 0){
                sb.append("255\t255\t00");
            }
            else if(p.id == 1){
                sb.append("00\t255\t00");
            }
            else if(p.id == 2){
                sb.append("255\t255\t255");
            }
            else{
                sb.append("255\t00\t00");
            }

            sb.append("\n");
        }

        try {
            BufferedWriter out = new BufferedWriter
                    (new FileWriter("outputOVITO.txt", true));
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception: " + e);
        }
    }

    double auToMeter(double au){
        double meters =  1.495978707e11;
        return au*meters;
    }

    double auDayToMeterSec(double auday){
        double meters =  1.495978707e11;
        double secs = 86400;

        return auday*meters/secs;
    }

    Planet movePlanetVerlet(Planet p, List<Planet> planets, double dt){
        Vector position = p.position;
        Vector velocity = p.velocity;
        Vector acceleration = p.acceleration;

        velocity = velocity.getAdded(acceleration.multiply(dt/2));
        position = position.getAdded(velocity.multiply(dt));

        acceleration = applyForce(p,planets);
        velocity = velocity.getAdded(acceleration.multiply(dt/2));

        return new Planet(p.id,position,velocity,acceleration,p.mass,p.radius);
    }

    Planet movePlanetGPC(Planet p, List<Planet> planets, double dt){
        Vector r = new Vector(p.position);
        Vector r1 = new Vector( p.velocity);
        Vector r2 = applyForce(p,planets);
        Vector r3 = new Vector(0,0);
        Vector r4 = new Vector(0,0);
        Vector r5 = new Vector(0,0);

        Vector rp;
        Vector r1p;
        Vector r2p;
        Vector rc;
        Vector r1c;

        Vector deltaa;
        Vector deltaR2;

        rp = r.getAdded(r1.multiply(getTerm(dt,1))).getAdded(r2.multiply(getTerm(dt,2))).getAdded(r3.multiply(getTerm(dt,3))).getAdded(r4.multiply(getTerm(dt,4))).getAdded(r5.multiply(getTerm(dt,5)));
        r1p = r1.getAdded(r2.multiply(getTerm(dt,1))).getAdded(r3.multiply(getTerm(dt,2))).getAdded(r4.multiply(getTerm(dt,3))).getAdded(r5.multiply(getTerm(dt,4)));
        r2p = r2.getAdded(r3.multiply(getTerm(dt,1))).getAdded(r4.multiply(getTerm(dt,2))).getAdded(r5.multiply(getTerm(dt,3)));

        deltaa = r2.substract(r2p);
        deltaR2 = deltaa.multiply(getTerm(dt,2));

        rc = rp.getAdded(deltaR2.multiply(3/20.0));
        r1c = r1p.getAdded(deltaR2.multiply((251)/(360*getTerm(dt,1))));

        r.set(rc);
        r1.set(r1c);

        return new Planet(p.id,r,r1c,r2,p.mass,p.radius);
    }

    Planet movePlanetBeeman(Planet p, List<Planet> planets, double dt){
        Vector position = new Vector(p.position);
        Vector velocity = new Vector(p.velocity);

        Vector currAcc = new Vector(p.acceleration);
        Vector prevAcc = new Vector(p.prevAcc);
        Vector nextAcc = new Vector(0,0);

        position = position.getAdded(velocity.multiply(dt)).getAdded(currAcc.multiply(2*dt*dt/3)).getAdded(prevAcc.multiply(-1*dt*dt/6));
        nextAcc.set(applyForce(p,planets));
        velocity = velocity.getAdded(nextAcc.multiply(dt/3)).getAdded(currAcc.multiply(5*dt/6)).getAdded(prevAcc.multiply(-1*dt/6));

        return new Planet(position,velocity,nextAcc,currAcc,p.mass);
    }

    private Vector applyForce(Planet p, List<Planet> planets){
        Vector force = new Vector(0,0);
        double g = 6.693e-11;

        for(Planet planet : planets){
            Vector en = (planet.position.substract(p.position).divide(planet.position.substract(p.position).length()));
            force.add(en.multiply(g*p.mass*planet.mass/p.position.distanceSq(planet.position)));
        }

        return force.divide(p.mass);
    }

    void velocityVerlet(double dt){
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(String.format("datosve%f.txt",dt));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        writer.print("");
        writer.close();

        int k = 10000;
        int m = 70;
        int gamma = 100;

        Vector position = new Vector(1,0);
        Vector velocity = new Vector(-gamma/(2.0*m),0);
        Vector acceleration = new Vector((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);

        double t = 0;
        double oldt = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%f\t%f\n",t,position.x));

        while(t<5){
            if(t-oldt >= 0.0001){
                sb.append(String.format("%f\t%f\n",t,position.x));
                oldt = t;
            }

            if(sb.length() > 10000){
                try {
                    BufferedWriter out = new BufferedWriter(
                            new FileWriter(String.format("datosve%f.txt",dt), true));
                    out.write(sb.toString());
                    out.close();
                }
                catch (IOException e) {
                    System.out.println("exception occoured" + e);
                }

                sb = new StringBuilder();
            }

            velocity = velocity.getAdded(acceleration.multiply(dt/2));
            position = position.getAdded(velocity.multiply(dt));
            acceleration.set((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);
            velocity = velocity.getAdded(acceleration.multiply(dt/2));

            t+=dt;
        }

        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(String.format("datosve%f.txt",dt), true));
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    void beeman(double dt){
        int k = 10000;
        int m = 70;
        int gamma = 100;

        Vector position = new Vector(1,0);
        Vector velocity = new Vector(-gamma/(2.0*m),0);
        Vector predVel;
        Vector currAcc = new Vector((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);
        Vector prevAcc = new Vector(0,0);
        Vector nextAcc = new Vector(0,0);

        double t = 0;
        double oldt = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%f\t%f\n",t,position.x));

        while(t<5){
            if(t-oldt >= 0.0001){
                sb.append(String.format("%f\t%f\n",t,position.x));
                oldt = t;
            }

            if(sb.length() > 10000){
                try {
                    BufferedWriter out = new BufferedWriter(
                            new FileWriter(String.format("datosbee%f.txt",dt), true));
                    out.write(sb.toString());
                    out.close();
                }
                catch (IOException e) {
                    System.out.println("exception occoured" + e);
                }

                sb = new StringBuilder();
            }

            position = position.getAdded(velocity.multiply(dt)).getAdded(currAcc.multiply(2*dt*dt/3)).getAdded(prevAcc.multiply(-1*dt*dt/6));
            predVel = velocity.getAdded(currAcc.multiply(3*dt/2)).getAdded(prevAcc.multiply(-1*dt/2));
            nextAcc.set((-k*position.x  - gamma*predVel.x)/m,(-k*position.y  - gamma*predVel.y)/m);
            velocity = velocity.getAdded(nextAcc.multiply(dt/3)).getAdded(currAcc.multiply(5*dt/6)).getAdded(prevAcc.multiply(-1*dt/6));
            prevAcc.set(currAcc);
            currAcc.set(nextAcc);

            t += dt;
        }

        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(String.format("datosbee%f.txt",dt), true));
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    void gearPredictorCorrector(double dt){

        int k = 10000;
        int m = 70;
        int gamma = 100;

        Vector r = new Vector(1,0);
        Vector r1 = new Vector( -gamma/(2.0*m),0);
        Vector r2 = new Vector((-k*r.x  - gamma*r1.x)/m,(-k*r.y  - gamma*r1.y)/m);
        Vector r3 = new Vector((-k*r1.x  - gamma*r2.x)/m,(-k*r1.y  - gamma*r2.y)/m);
        Vector r4 = new Vector((-k*r2.x  - gamma*r3.x)/m,(-k*r2.y  - gamma*r3.y)/m);
        Vector r5 = new Vector((-k*r3.x  - gamma*r4.x)/m,(-k*r3.y  - gamma*r4.y)/m);

        Vector rp = new Vector(0,0);
        Vector r1p = new Vector(0,0);
        Vector r2p = new Vector(0,0);
        Vector r3p = new Vector(0,0);
        Vector r4p = new Vector(0,0);
        Vector r5p = new Vector(0,0);
        Vector rc = new Vector(0,0);
        Vector r1c = new Vector(0,0);

        double t = 0;

        Vector deltaa = new Vector(0,0);
        Vector deltaR2 = new Vector(0,0);
        double oldt = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%f\t%f\n",t,r.x));

        while(t<5){
            if(t-oldt >= 0.0001){
                sb.append(String.format("%f\t%f\n",t,r.x));
                oldt = t;
            }

            if(sb.length() > 10000){
                try {
                    BufferedWriter out = new BufferedWriter(
                            new FileWriter(String.format("datosgp%f.txt",dt), true));
                    out.write(sb.toString());
                    out.close();
                }
                catch (IOException e) {
                    System.out.println("exception occoured" + e);
                }

                sb = new StringBuilder();
            }

            rp = r.getAdded(r1.multiply(getTerm(dt,1))).getAdded(r2.multiply(getTerm(dt,2))).getAdded(r3.multiply(getTerm(dt,3))).getAdded(r4.multiply(getTerm(dt,4))).getAdded(r5.multiply(getTerm(dt,5)));
            r1p = r1.getAdded(r2.multiply(getTerm(dt,1))).getAdded(r3.multiply(getTerm(dt,2))).getAdded(r4.multiply(getTerm(dt,3))).getAdded(r5.multiply(getTerm(dt,4)));
            r2p = r2.getAdded(r3.multiply(getTerm(dt,1))).getAdded(r4.multiply(getTerm(dt,2))).getAdded(r5.multiply(getTerm(dt,3)));
            r3p = r3.getAdded(r4.multiply(getTerm(dt,1))).getAdded(r5.multiply(getTerm(dt,2)));
            r4p = r4.getAdded(r5.multiply(getTerm(dt,1)));
            r5p.set(r5);

            deltaa = r2.substract(r2p);
            deltaR2 = deltaa.multiply(getTerm(dt,2));

            rc = rp.getAdded(deltaR2.multiply(3/16.0));
            r1c = r1p.getAdded(deltaR2.multiply((251)/(360*getTerm(dt,1))));

            r.set(rc);
            r1.set(r1c);

            r2.set((-k*r.x  - gamma*r1.x)/m,(-k*r.y  - gamma*r1.y)/m);
            r3.set((-k*r1.x  - gamma*r2.x)/m,(-k*r1.y  - gamma*r2.y)/m);
            r4.set((-k*r2.x  - gamma*r3.x)/m,(-k*r2.y  - gamma*r3.y)/m);
            r5.set((-k*r3.x  - gamma*r4.x)/m,(-k*r3.y  - gamma*r4.y)/m);

            t+=dt;
        }

        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(String.format("datosgp%f.txt",dt), true));
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    private double getTerm(double t, int order){
        return Math.pow(t,order)/factorial(order);
    }

    public long factorial(int n) {
        long fact = 1;

        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }

        return fact;
    }

    public double launchShip(int launchDay, double dt, double totalTime, Vector shipVelocity, boolean animate){
        PrintWriter writer = null;

        try {
            writer = new PrintWriter("outputOVITO.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        writer.print("");
        writer.close();

        Vector pos = new Vector(auToMeter(-9.600619697743452e-1),auToMeter(-2.822355844063401e-1));
        Vector vel = new Vector(auDayToMeterSec(4.572972309577654e-3),auDayToMeterSec(-1.656334129232400e-2));
        Vector acc = new Vector();
        Planet earth = new Planet(1,pos,vel,acc,5.972e24,6371000 );

        pos = new Vector(auToMeter(-1.651921224501318e-1),auToMeter(-1.459738176234038));
        vel = new Vector(auDayToMeterSec(1.443361788681964e-2),auDayToMeterSec(-3.703429642664186e-4));
        acc = new Vector();
        Planet mars = new Planet(3,pos,vel,acc,6.39e23 ,3389500);

        pos = new Vector(0,0);
        vel = new Vector(0,0);
        acc = new Vector();
        Planet sun = new Planet(0,pos,vel,acc,1.989e30,696340000 );

        Planet ship = new Planet(2,new Vector(0,0),new Vector(0,0),new Vector(0,0),0 ,0);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars)));

        Planet newEarth,newSun,newMars,newShip;
        int elapsedDays = 0;
        double time = 0;
        double distance = Double.POSITIVE_INFINITY;

        while (time < (totalTime +launchDay*86400) ) {
            if(time % 86400 == 0 && elapsedDays>= launchDay && animate){
                generateOvitoFile(Arrays.asList(sun,mars,ship,earth));
                elapsedDays++;

            }
            else if (time % 86400 == 0 && animate) {
                elapsedDays++;
                generateOvitoFile(Arrays.asList(sun,mars,earth));
            }
            else if(time % 86400 == 0){
                elapsedDays++;
            }

            if (elapsedDays == launchDay){
                double angle = earth.position.angle();

                pos = new Vector(earth.position.x+Math.cos(angle)*(1500000+6371000),earth.position.y+Math.sin(angle)*(1500000+6371000));
                angle =  pos.perp().angle();
                vel = new Vector(earth.velocity.x+(7120+shipVelocity.x)*Math.cos(angle),earth.velocity.y+(shipVelocity.y+7120)*Math.sin(angle));
                acc = new Vector();
                ship = new Planet(2,pos,vel,acc,2e5 ,0);

                earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars, ship)));
                mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth, ship)));
                sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars, ship)));
                ship.setAcceleration(applyForce(ship,Arrays.asList(sun, mars, earth)));

                generateOvitoFile(Arrays.asList(sun,mars,ship,earth));

                newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars, ship)), dt);
                newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars, ship)), dt);
                newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth, ship)), dt);
                newShip = movePlanetVerlet(ship, new ArrayList<>(Arrays.asList(sun, mars, earth)), dt);

                earth = newEarth;
                mars = newMars;
                sun = newSun;
                ship = newShip;

                time += dt;
                elapsedDays++;

            }
            else if(elapsedDays>launchDay){
                newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars, ship)), dt);
                newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars, ship)), dt);
                newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth, ship)), dt);
                newShip = movePlanetVerlet(ship, new ArrayList<>(Arrays.asList(sun, mars, earth)), dt);

                earth = newEarth;
                mars = newMars;
                sun = newSun;
                ship = newShip;

                if(mars.position.distance(ship.position) < distance){
                    distance = mars.position.distance(ship.position);
                }

                if(ship.collidesWith(mars)){
                    System.out.println("The ship collided with Mars");
                }
                else if(ship.collidesWith(earth)){
                    System.out.println("The ship collided with the Earth");
                }

                time += dt;
            }
            else{
                newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars)), dt);
                newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars)), dt);
                newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth)), dt);

                earth = newEarth;
                mars = newMars;
                sun = newSun;

                time += dt;
            }
        }

        return distance;
    }
}

