public class App {

    public static void main(String args[]) {

        App a = new App();
        a.beeman();

    }


    void beeman(){
        int k =10000;
        int m = 70;
        int gamma = 100;

        double x = 1.0;
        double v = -gamma/(2.0*m);
        double curra = (-k*x  - gamma*v)/m;
        double preva = curra;
        double nexta;

        double vp;

        double t = 0;
        double dt = 0.0001;

        while(t<5){
            System.out.printf("%f\t%f\n",t,x);

            x = x + v*dt + (2*curra*dt*dt)/3 - (preva*dt*dt)/6;

            vp = v + (3*curra*dt)/2 + - (preva*dt)/2;

            nexta = (-k*x  - gamma*vp)/m;

            v = vp + (nexta*dt)/3 + (5*curra*dt)/6 - (preva*dt)/6;

            preva = curra;
            curra = nexta;


            t += dt;
        }

    }



    void gearPredictorCorrector(){

        int k =10000;
        int m = 70;
        int gamma = 100;

        double r = 1.0;
        double r1 = -gamma/(2.0*m);
        double r2 = (-k*r  - gamma*r1)/m;
        double r3 = (-k*r1 - gamma*r2)/m;
        double r4 = (-k*r2 - gamma*r3)/m;
        double r5 = (-k*r3 - gamma*r4)/m;

        double rp,r1p,r2p,r3p,r4p,r5p;
        double rc,r1c,r2c,r3c,r4c,r5c;
        double dt = 0.0001;
        double t = 0;

        double deltaa;
        double deltaR2;

        while(t<5){

            System.out.printf("%f\t%f\n",t,r);

            rp  = r  + r1*getTerm(dt,1) + r2*getTerm(dt,2) + r3*getTerm(dt,3) + r4*getTerm(dt,4) + r5*getTerm(dt,5);
            r1p = r1 + r2*getTerm(dt,1) + r3*getTerm(dt,2) + r4*getTerm(dt,3) + r5*getTerm(dt,4);
            r2p = r2 + r3*getTerm(dt,1) + r4*getTerm(dt,2) + r5*getTerm(dt,3);
            r3p = r3 + r4*getTerm(dt,1) + r5*getTerm(dt,2);
            r4p = r4 + r5*getTerm(dt,1);
            r5p = r5;

            deltaa = r2-r2p;

            deltaR2 = deltaa*getTerm(dt,2);

            rc  = rp  + (3*deltaR2)/16;
            r1c = r1p + (251*deltaR2)/(360*getTerm(dt,1));
            r2c = r2p + (deltaR2)/(getTerm(dt,2));
            r3c = r3p + (11*deltaR2)/(18*getTerm(dt,3));
            r4c = r4p + (deltaR2)/(6*getTerm(dt,4));
            r5c = r5p + (deltaR2)/(60*getTerm(dt,5));

            r = rc;
            r1 = r1c;
            r2 = (-k*r  - gamma*r1)/m;
            r3 = (-k*r1 - gamma*r2)/m;
            r4 = (-k*r2 - gamma*r3)/m;
            r5 = (-k*r3 - gamma*r4)/m;

            t+=dt;

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
}
