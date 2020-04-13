import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void main(String args[]) {

        App a = new App();
//        a.beeman();

        Vector2D pos = new Vector2D(a.auToMeter(-9.600619697743452e-1),a.auToMeter(-2.822355844063401e-1));
        Vector2D vel = new Vector2D(a.auDayToMeterSec(4.572972309577654e-3),a.auDayToMeterSec(-1.656334129232400e-2));
        Vector2D acc = new Vector2D();

        Planet earth = new Planet(pos,vel,acc,5.972e24 );

        pos = new Vector2D(a.auToMeter(-1.651921224501318e-1),a.auToMeter(-1.459738176234038));
        vel = new Vector2D(a.auDayToMeterSec(1.443361788681964e-2),a.auDayToMeterSec(-3.703429642664186e-4));
        acc = new Vector2D();

        Planet mars = new Planet(pos,vel,acc,6.39e23 );



        pos = new Vector2D(0,0);
        vel = new Vector2D(0,0);
        acc = new Vector2D();

        Planet sun = new Planet(pos,vel,acc,6.39e23 );

        double angle = earth.position.getAngle();


        pos = new Vector2D(earth.position.x+Math.cos(angle)*1500000,earth.position.y+Math.sin(angle)*1500000);

        angle = earth.position.getPerp().getAngle();
        vel = new Vector2D(earth.velocity.getAdded(new Vector2D((7120+8000)*Math.cos(angle),(8000+7120)*Math.sin(angle))));
        acc = new Vector2D();

        Planet ship = new Planet(pos,vel,acc,6.39e23 );

        double time = 0;
        double dt = 10;

        Planet newShip;
        Planet newEarth;
        Planet newSun;
        Planet newMars;



        while(time < 10000 ){
//            if(time%60 == 0){
//                System.out.println(4);
//                System.out.println();
//                System.out.printf("%f\t%f\n",sun.position.x,sun.position.y);
//                System.out.printf("%f\t%f\n",earth.position.x,earth.position.y);
//                System.out.printf("%f\t%f\n",mars.position.x,mars.position.y);
//                System.out.printf("%f\t%f\n",ship.position.x,ship.position.y);
//            }

            newEarth = a.movePlanetVerlet(earth,new ArrayList<Planet>(Arrays.asList(sun, mars, ship)),dt);
            newSun = a.movePlanetVerlet(sun,new ArrayList<Planet>(Arrays.asList(earth, mars, ship)),dt);
            newMars = a.movePlanetVerlet(earth,new ArrayList<Planet>(Arrays.asList(sun, earth, ship)),dt);
            newShip = a.movePlanetVerlet(earth,new ArrayList<Planet>(Arrays.asList(sun, mars, earth)),dt);

            System.out.println("-----------");
            System.out.printf("%f\t%f\n",newSun.position.x,newSun.position.y);
            System.out.printf("%f\t%f\n",newEarth.position.x,newEarth.position.y);
            System.out.printf("%f\t%f\n",newMars.position.x,newMars.position.y);
            System.out.printf("%f\t%f\n",newShip.position.x,newShip.position.y);
            System.out.println("-----------");

            earth = newEarth;
            mars = newMars;
            sun = newSun;
            ship = newShip;

            time += dt;
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
        acceleration.set(applyForce(p,planets));

        velocity = velocity.getAdded(acceleration.getMultiplied(dt/2));

        return new Planet(position,velocity,acceleration,p.mass);
    }

    Planet movePlanetGPC(Planet p, List<Planet> planets, double dt){

        Vector2D r = new Vector2D(p.position);
        Vector2D r1 = new Vector2D( p.velocity);

        Vector2D r2 = new Vector2D(applyForce(p,planets));
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

        return new Planet(r,r1c,r2,p.mass);

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
            Vector2D en = (planet.position.getSubtracted(p.position).getDivided(Math.sqrt(planet.position.distanceSq(p.position))));
            force.add(en.getMultiplied(g*p.mass*planet.mass/Math.pow(p.position.distance(planet.position),2)));
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
