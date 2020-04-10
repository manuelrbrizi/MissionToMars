public class App {

    public static void main(String args[]) {

        App a = new App();

        a.gearPredictorCorrector();

    }




    void gearPredictorCorrector(){

        int k =10000;
        int m = 70;
        int gamma = 100;

        double r = 1.0;
        double r1 = -gamma/(2.0*m);
        double r2 = (-k*r -gamma*r1)/m;
        double r3 = (-k*r1 -gamma*r2)/m;
        double r4 = (-k*r2 -gamma*r3)/m;
        double r5 = (-k*r3 -gamma*r4)/m;

        double rp,r1p,r2p,r3p,r4p,r5p;
        double deltat = 0.0001;
        double t = 0;

        double deltaa;
        double deltaR2;

        while(t<5){

            System.out.printf("%f\t%f\t%f\t%f\t%f\t%f\n",r,r1,r2,r3,r4,r5);

            rp  = r  + r1*getTerm(deltat,1) + r2*getTerm(deltat,2) + r3*getTerm(deltat,3) + r4*getTerm(deltat,4) + r5*getTerm(deltat,5);
            r1p = r1 + r2*getTerm(deltat,1) + r3*getTerm(deltat,2) + r4*getTerm(deltat,3) + r5*getTerm(deltat,4);
            r2p = r2 + r3*getTerm(deltat,1) + r4*getTerm(deltat,2) + r5*getTerm(deltat,3);
            r3p = r3 + r4*getTerm(deltat,1) + r5*getTerm(deltat,2);
            r4p = r4 + r5*getTerm(deltat,1);
            r5p = r5;

            deltaa = r2-r2p;

            deltaR2 = deltaa*getTerm(deltat,2);

            r = rp + (3*deltaR2)/16;
            r1 = r1p + (251*deltaR2)/(360*getTerm(deltat,1));
            r2 = r2p + (deltaR2)/(getTerm(deltat,2));
            r3 = r3p + (11*deltaR2)/(18*getTerm(deltat,3));
            r4 = r4p + (deltaR2)/(6*getTerm(deltat,4));
            r5 = r5p + (deltaR2)/(60*getTerm(deltat,5));

            t+=deltat;

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
