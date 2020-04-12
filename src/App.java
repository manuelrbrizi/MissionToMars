public class App {

    public static void main(String args[]) {

        App a = new App();
        a.beeman();

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
