import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void main(String args[]) {

        App a = new App();
        double distance;
        double minDistance = Double.POSITIVE_INFINITY;
        int minDay = 1;



        List<List<Planet>> days = a.getDailyPlanetPositions(86400*370*2,1);
        List<List<Planet>> hours = a.getHourlyPlanetPositions(1,days.get(96));
        List<List<Planet>> minutes = a.getMinutePlanetPositions(1,hours.get(5));



        for(int i = 0; i<minutes.size();i++){
            distance = a.launchShipOnDay(minutes.get(i),5,86400*370,new Vector2D(8000,8000),true);
            System.out.printf("Launch hour: %d, Distance: %f\n",i,distance);
            if(distance < minDistance){
                minDistance = distance;
                minDay = i;
            }
        }

        System.out.printf("%d\t%f\n",minDay,minDistance);


    }


    public double launchShip(int launchDay, double dt, double totalTime, Vector2D shipVelocity, boolean animate){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("outputOVITO.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.print("");
        writer.close();

        Vector2D pos = new Vector2D(auToMeter(-9.600619697743452e-1),auToMeter(-2.822355844063401e-1));
        Vector2D vel = new Vector2D(auDayToMeterSec(4.572972309577654e-3),auDayToMeterSec(-1.656334129232400e-2));
        Vector2D acc = new Vector2D();
        Planet earth = new Planet(1,pos,vel,acc,5.972e24,6371000 );


        pos = new Vector2D(auToMeter(-1.651921224501318e-1),auToMeter(-1.459738176234038));
        vel = new Vector2D(auDayToMeterSec(1.443361788681964e-2),auDayToMeterSec(-3.703429642664186e-4));
        acc = new Vector2D();
        Planet mars = new Planet(3,pos,vel,acc,6.39e23 ,3389500);


        pos = new Vector2D(0,0);
        vel = new Vector2D(0,0);
        acc = new Vector2D();
        Planet sun = new Planet(0,pos,vel,acc,1.989e30,696340000 );


        Planet ship = new Planet(2,new Vector2D(0,0),new Vector2D(0,0),new Vector2D(0,0),0 ,0);


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

                double angle = earth.position.getAngle();

                pos = new Vector2D(earth.position.x+Math.cos(angle)*(1500000+6371000),earth.position.y+Math.sin(angle)*(1500000+6371000));
                angle =  pos.getPerp().getAngle();
                vel = new Vector2D(earth.velocity.x+(7120+shipVelocity.x)*Math.cos(angle),earth.velocity.y+(shipVelocity.y+7120)*Math.sin(angle));

                acc = new Vector2D();

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

    public double launchShipOnDay(List<Planet> planets,double dt, double totalTime, Vector2D shipVelocity, boolean animate){

        Planet sun = planets.get(0);
        Planet earth = planets.get(1);
        Planet mars = planets.get(2);

        double angle = earth.position.getAngle();

        Vector2D pos = new Vector2D(earth.position.x+Math.cos(angle)*(1500000+6371000),earth.position.y+Math.sin(angle)*(1500000+6371000));
        angle =  pos.getPerp().getAngle();
        Vector2D vel = new Vector2D(earth.velocity.x+(7120+shipVelocity.x)*Math.cos(angle),earth.velocity.y+(shipVelocity.y+7120)*Math.sin(angle));

        Vector2D acc = new Vector2D();

        Planet ship = new Planet(2,pos,vel,acc,2e5 ,0);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars, ship)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth, ship)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars, ship)));
        ship.setAcceleration(applyForce(ship,Arrays.asList(sun, mars, earth)));

        generateOvitoFile(Arrays.asList(sun,mars,ship,earth));

        Planet newEarth,newSun,newMars,newShip;
        double time = 0;
        double distance = Double.POSITIVE_INFINITY;

        while (time < totalTime ) {
            if(mars.position.distance(ship.position) < distance){
                distance = mars.position.distance(ship.position);
            }
            newEarth = movePlanetVerlet(earth, new ArrayList<>(Arrays.asList(sun, mars,ship)), dt);
            newSun = movePlanetVerlet(sun, new ArrayList<>(Arrays.asList(earth, mars,ship)), dt);
            newMars = movePlanetVerlet(mars, new ArrayList<>(Arrays.asList(sun, earth,ship)), dt);
            newShip = movePlanetVerlet(ship, new ArrayList<>(Arrays.asList(sun, earth,mars)), dt);

            earth = newEarth;
            mars = newMars;
            sun = newSun;
            ship = newShip;

            if(ship.collidesWith(mars)){
                System.out.println("The ship collided with Mars");
            }
            else if(ship.collidesWith(earth)){
                System.out.println("The ship collided with the Earth");
            }

            time += dt;

        }

        return distance;

    }

    private void calculatePrevAcceleration(Planet earth, Planet sun, Planet mars, Planet ship, double dt){

        Vector2D earthPrevVel = earth.getVelocity().getAdded(earth.getAcceleration().getMultiplied(-1*dt));
        Vector2D earthPrevPos = earth.getPosition().getAdded(earthPrevVel.getMultiplied(dt)).getAdded(earth.getAcceleration().getMultiplied(dt*dt/2));

        Vector2D sunPrevVel = sun.getVelocity().getAdded(sun.getAcceleration().getMultiplied(-1*dt));
        Vector2D sunPrevPos = sun.getPosition().getAdded(sunPrevVel.getMultiplied(dt)).getAdded(sun.getAcceleration().getMultiplied(dt*dt/2));

        Vector2D marsPrevVel = mars.getVelocity().getAdded(mars.getAcceleration().getMultiplied(-1*dt));
        Vector2D marsPrevPos = mars.getPosition().getAdded(marsPrevVel.getMultiplied(dt)).getAdded(mars.getAcceleration().getMultiplied(dt*dt/2));

        Vector2D shipPrevVel = ship.getVelocity().getAdded(ship.getAcceleration().getMultiplied(-1*dt));
        Vector2D shipPrevPos = ship.getPosition().getAdded(shipPrevVel.getMultiplied(dt)).getAdded(ship.getAcceleration().getMultiplied(dt*dt/2));

        Planet oldEarth = new Planet(100,earthPrevPos, earthPrevVel, new Vector2D(), 5.972e24, 6371000);
        Planet oldSun = new Planet(200,sunPrevPos, sunPrevVel, new Vector2D(), 1.989e30, 696340000);
        Planet oldMars = new Planet(300,marsPrevPos, marsPrevVel, new Vector2D(), 6.39e23, 3389500);
        Planet oldShip = new Planet(400,shipPrevPos, shipPrevVel, new Vector2D(), 2e5, 0);

        earth.setPrevAcc(applyForce(oldEarth, Arrays.asList(oldSun, oldMars, oldShip)));
        sun.setPrevAcc(applyForce(oldSun, Arrays.asList(oldMars, oldEarth, oldShip)));
        mars.setPrevAcc(applyForce(oldMars, Arrays.asList(oldSun, oldEarth, oldShip)));
        ship.setPrevAcc(applyForce(oldShip, Arrays.asList(oldSun, oldEarth, oldMars)));

        //System.out.printf("EA = %f, SUN = %f, MA = %f, SH = %f\n", earth.getPrevAcc().getLength(), sun.getPrevAcc().getLength(), mars.getPrevAcc().getLength(), ship.getPrevAcc().getLength());
    }

    public List<List<Planet>> getDailyPlanetPositions(double totalTime, double dt){

        Vector2D pos = new Vector2D(auToMeter(-9.600619697743452e-1),auToMeter(-2.822355844063401e-1));
        Vector2D vel = new Vector2D(auDayToMeterSec(4.572972309577654e-3),auDayToMeterSec(-1.656334129232400e-2));
        Vector2D acc = new Vector2D();
        Planet earth = new Planet(1,pos,vel,acc,5.972e24,6371000 );


        pos = new Vector2D(auToMeter(-1.651921224501318e-1),auToMeter(-1.459738176234038));
        vel = new Vector2D(auDayToMeterSec(1.443361788681964e-2),auDayToMeterSec(-3.703429642664186e-4));
        acc = new Vector2D();
        Planet mars = new Planet(3,pos,vel,acc,6.39e23 ,3389500);


        pos = new Vector2D(0,0);
        vel = new Vector2D(0,0);
        acc = new Vector2D();
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

        while (time < 3600*24 ) {
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

    public List<List<Planet>> getMinutePlanetPositions(double dt,List<Planet> planets){

        Planet sun = planets.get(0);
        Planet earth = planets.get(1);
        Planet mars = planets.get(2);

        earth.setAcceleration(applyForce(earth,Arrays.asList(sun, mars)));
        mars.setAcceleration(applyForce(mars,Arrays.asList(sun, earth)));
        sun.setAcceleration(applyForce(sun,Arrays.asList(earth, mars)));

        Planet newEarth,newSun,newMars;
        List<List<Planet>> minutes = new ArrayList<>();

        double time = 0;

        while (time < 3600 ) {
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

            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter("outputOVITO.txt", true));
            out.write(sb.toString());
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
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

        Vector2D position = p.position;
        Vector2D velocity = p.velocity;
        Vector2D acceleration = p.acceleration;


        velocity = velocity.getAdded(acceleration.getMultiplied(dt/2));
        position = position.getAdded(velocity.getMultiplied(dt));

        //CALCULO DE LA FUERZA ACA
        acceleration = applyForce(p,planets);

        velocity = velocity.getAdded(acceleration.getMultiplied(dt/2));

        return new Planet(p.id,position,velocity,acceleration,p.mass,p.radius);
    }

    Planet movePlanetGPC(Planet p, List<Planet> planets, double dt){

        Vector2D r = new Vector2D(p.position);
        Vector2D r1 = new Vector2D( p.velocity);

        Vector2D r2 = applyForce(p,planets);
        Vector2D r3 = new Vector2D(0,0);
        Vector2D r4 = new Vector2D(0,0);
        Vector2D r5 = new Vector2D(0,0);

        Vector2D rp;
        Vector2D r1p;
        Vector2D r2p;


        Vector2D rc;
        Vector2D r1c;

        Vector2D deltaa;
        Vector2D deltaR2;



        rp = r.getAdded(r1.getMultiplied(getTerm(dt,1))).getAdded(r2.getMultiplied(getTerm(dt,2))).getAdded(r3.getMultiplied(getTerm(dt,3))).getAdded(r4.getMultiplied(getTerm(dt,4))).getAdded(r5.getMultiplied(getTerm(dt,5)));
        r1p = r1.getAdded(r2.getMultiplied(getTerm(dt,1))).getAdded(r3.getMultiplied(getTerm(dt,2))).getAdded(r4.getMultiplied(getTerm(dt,3))).getAdded(r5.getMultiplied(getTerm(dt,4)));
        r2p = r2.getAdded(r3.getMultiplied(getTerm(dt,1))).getAdded(r4.getMultiplied(getTerm(dt,2))).getAdded(r5.getMultiplied(getTerm(dt,3)));

        deltaa = r2.getSubtracted(r2p);

        deltaR2 = deltaa.getMultiplied(getTerm(dt,2));

        rc = rp.getAdded(deltaR2.getMultiplied(3/20.0));
        r1c = r1p.getAdded(deltaR2.getMultiplied((251)/(360*getTerm(dt,1))));

        r.set(rc);
        r1.set(r1c);

        return new Planet(p.id,r,r1c,r2,p.mass,p.radius);

    }

    Planet movePlanetBeeman(Planet p, List<Planet> planets, double dt){

        Vector2D position = new Vector2D(p.position);
        Vector2D velocity = new Vector2D(p.velocity);
        //CALCULAR FUERZA
        Vector2D currAcc = new Vector2D(p.acceleration);
        Vector2D prevAcc = new Vector2D(p.prevAcc);
        Vector2D nextAcc = new Vector2D(0,0);



        position = position.getAdded(velocity.getMultiplied(dt)).getAdded(currAcc.getMultiplied(2*dt*dt/3)).getAdded(prevAcc.getMultiplied(-1*dt*dt/6));

        //CALCULAR LA FUERZA
        nextAcc.set(applyForce(p,planets));

        velocity = velocity.getAdded(nextAcc.getMultiplied(dt/3)).getAdded(currAcc.getMultiplied(5*dt/6)).getAdded(prevAcc.getMultiplied(-1*dt/6));

        return new Planet(position,velocity,nextAcc,currAcc,p.mass);

    }

    private Vector2D applyForce(Planet p, List<Planet> planets){

        Vector2D force = new Vector2D(0,0);
        double g = 6.693e-11;

        for(Planet planet : planets){
            Vector2D en = (planet.position.getSubtracted(p.position).getDivided(planet.position.getSubtracted(p.position).getLength()));
            force.add(en.getMultiplied(g*p.mass*planet.mass/p.position.distanceSq(planet.position)));
        }

        return force.getDivided(p.mass);
    }


    void velocityVerlet(){
        int k =10000;
        int m = 70;
        int gamma = 100;

        Vector2D position = new Vector2D(1,0);
        Vector2D velocity = new Vector2D(-gamma/(2.0*m),0);
        Vector2D acceleration = new Vector2D((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);

        double t = 0;
        double dt = 0.0001;

        while(t<5){
            System.out.printf("%f\t%f\n",t,position.x);


            velocity = velocity.getAdded(acceleration.getMultiplied(dt/2));
            position = position.getAdded(velocity.getMultiplied(dt));
            acceleration.set((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);
            velocity = velocity.getAdded(acceleration.getMultiplied(dt/2));


            t+=dt;
        }

    }

    void beeman(){
        int k =10000;
        int m = 70;
        int gamma = 100;

        Vector2D position = new Vector2D(1,0);
        Vector2D velocity = new Vector2D(-gamma/(2.0*m),0);
        Vector2D predVel;
        Vector2D currAcc = new Vector2D((-k*position.x  - gamma*velocity.x)/m,(k*position.y  - gamma*velocity.y)/m);
        Vector2D prevAcc = new Vector2D(0,0);
        Vector2D nextAcc = new Vector2D(0,0);


        double t = 0;
        double dt = 0.0001;

        while(t<5){
            System.out.printf("%f\t%f\n",t,position.x);

            position = position.getAdded(velocity.getMultiplied(dt)).getAdded(currAcc.getMultiplied(2*dt*dt/3)).getAdded(prevAcc.getMultiplied(-1*dt*dt/6));

            predVel = velocity.getAdded(currAcc.getMultiplied(3*dt/2)).getAdded(prevAcc.getMultiplied(-1*dt/2));

            nextAcc.set((-k*position.x  - gamma*predVel.x)/m,(-k*position.y  - gamma*predVel.y)/m);

            velocity = velocity.getAdded(nextAcc.getMultiplied(dt/3)).getAdded(currAcc.getMultiplied(5*dt/6)).getAdded(prevAcc.getMultiplied(-1*dt/6));

            prevAcc.set(currAcc);
            currAcc.set(nextAcc);


            t += dt;
        }

    }



    void gearPredictorCorrector(){

        int k =10000;
        int m = 70;
        int gamma = 100;

        Vector2D r = new Vector2D(1,0);
        Vector2D r1 = new Vector2D( -gamma/(2.0*m),0);
        Vector2D r2 = new Vector2D((-k*r.x  - gamma*r1.x)/m,(-k*r.y  - gamma*r1.y)/m);
        Vector2D r3 = new Vector2D((-k*r1.x  - gamma*r2.x)/m,(-k*r1.y  - gamma*r2.y)/m);
        Vector2D r4 = new Vector2D((-k*r2.x  - gamma*r3.x)/m,(-k*r2.y  - gamma*r3.y)/m);
        Vector2D r5 = new Vector2D((-k*r3.x  - gamma*r4.x)/m,(-k*r3.y  - gamma*r4.y)/m);

        Vector2D rp = new Vector2D(0,0);
        Vector2D r1p = new Vector2D(0,0);
        Vector2D r2p = new Vector2D(0,0);
        Vector2D r3p = new Vector2D(0,0);
        Vector2D r4p = new Vector2D(0,0);
        Vector2D r5p = new Vector2D(0,0);
        Vector2D rc = new Vector2D(0,0);
        Vector2D r1c = new Vector2D(0,0);

        double dt = 0.0001;
        double t = 0;

        Vector2D deltaa = new Vector2D(0,0);
        Vector2D deltaR2 = new Vector2D(0,0);


        while(t<5){

            System.out.printf("%f\t%f\n",t,r.x);

            rp = r.getAdded(r1.getMultiplied(getTerm(dt,1))).getAdded(r2.getMultiplied(getTerm(dt,2))).getAdded(r3.getMultiplied(getTerm(dt,3))).getAdded(r4.getMultiplied(getTerm(dt,4))).getAdded(r5.getMultiplied(getTerm(dt,5)));
            r1p = r1.getAdded(r2.getMultiplied(getTerm(dt,1))).getAdded(r3.getMultiplied(getTerm(dt,2))).getAdded(r4.getMultiplied(getTerm(dt,3))).getAdded(r5.getMultiplied(getTerm(dt,4)));
            r2p = r2.getAdded(r3.getMultiplied(getTerm(dt,1))).getAdded(r4.getMultiplied(getTerm(dt,2))).getAdded(r5.getMultiplied(getTerm(dt,3)));
            r3p = r3.getAdded(r4.getMultiplied(getTerm(dt,1))).getAdded(r5.getMultiplied(getTerm(dt,2)));
            r4p = r4.getAdded(r5.getMultiplied(getTerm(dt,1)));
            r5p.set(r5);

            deltaa = r2.getSubtracted(r2p);

            deltaR2 = deltaa.getMultiplied(getTerm(dt,2));

            rc = rp.getAdded(deltaR2.getMultiplied(3/16.0));
            r1c = r1p.getAdded(deltaR2.getMultiplied((251)/(360*getTerm(dt,1))));

            r.set(rc);
            r1.set(r1c);

            r2.set((-k*r.x  - gamma*r1.x)/m,(-k*r.y  - gamma*r1.y)/m);
            r3.set((-k*r1.x  - gamma*r2.x)/m,(-k*r1.y  - gamma*r2.y)/m);
            r4.set((-k*r2.x  - gamma*r3.x)/m,(-k*r2.y  - gamma*r3.y)/m);
            r5.set((-k*r3.x  - gamma*r4.x)/m,(-k*r3.y  - gamma*r4.y)/m);


            t+=dt;

        }


    }

//    void gearPredictorCorrector(){
//
//        int k =10000;
//        int m = 70;
//        int gamma = 100;
//
//
//        double r = 1.0;
//        double r1 = -gamma/(2.0*m);
//        double r2 = (-k*r  - gamma*r1)/m;
//        double r3 = (-k*r1 - gamma*r2)/m;
//        double r4 = (-k*r2 - gamma*r3)/m;
//        double r5 = (-k*r3 - gamma*r4)/m;
//
//        double rp,r1p,r2p,r3p,r4p,r5p;
//        double rc,r1c,r2c,r3c,r4c,r5c;
//        double dt = 0.0001;
//        double t = 0;
//
//        double deltaa;
//        double deltaR2;
//
//        while(t<5){
//
//            System.out.printf("%f\t%f\n",t,r);
//
//            rp  = r  + r1*getTerm(dt,1) + r2*getTerm(dt,2) + r3*getTerm(dt,3) + r4*getTerm(dt,4) + r5*getTerm(dt,5);
//            r1p = r1 + r2*getTerm(dt,1) + r3*getTerm(dt,2) + r4*getTerm(dt,3) + r5*getTerm(dt,4);
//            r2p = r2 + r3*getTerm(dt,1) + r4*getTerm(dt,2) + r5*getTerm(dt,3);
//            r3p = r3 + r4*getTerm(dt,1) + r5*getTerm(dt,2);
//            r4p = r4 + r5*getTerm(dt,1);
//            r5p = r5;
//
//            deltaa = r2-r2p;
//
//            deltaR2 = deltaa*getTerm(dt,2);
//
//            rc  = rp  + (3*deltaR2)/16;
//            r1c = r1p + (251*deltaR2)/(360*getTerm(dt,1));
//            r2c = r2p + (deltaR2)/(getTerm(dt,2));
//            r3c = r3p + (11*deltaR2)/(18*getTerm(dt,3));
//            r4c = r4p + (deltaR2)/(6*getTerm(dt,4));
//            r5c = r5p + (deltaR2)/(60*getTerm(dt,5));
//
//            r = rc;
//            r1 = r1c;
//            r2 = (-k*r  - gamma*r1)/m;
//            r3 = (-k*r1 - gamma*r2)/m;
//            r4 = (-k*r2 - gamma*r3)/m;
//            r5 = (-k*r3 - gamma*r4)/m;
//
//            t+=dt;
//
//        }
//
//
//    }

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
}
