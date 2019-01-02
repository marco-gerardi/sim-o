package it.onorato.generator;


public class HyperExpGenerator {
	
    private long seed1, seed2;
    private double avg;
    private double p;
    ExponentialGenerator exp;
    UniformGenerator ug;
	
    public HyperExpGenerator(long seed1, long seed2, double avg, double p) {
        
        this.setSeed1(seed1);
        this.setSeed2(seed2);
        this.setP(p);
        this.setExp(new ExponentialGenerator(this.getSeed1(),1)); // seed, media
        this.setUg(new UniformGenerator(this.getSeed1(),0,1));
        this.setAvg(avg);
    }
	
    // restituisce il valore della seq. iperesponenziale
    public double getNextHyperExp() {
        double ri = this.getUg().getNextNumber();
        //System.out.println("uniforme: "+ri);
        double xi = this.getExp().getNextExp();
        //System.out.println("esponenziale: "+xi);
        if (ri <= this.getP())
            return xi * (this.getAvg() / (2.0 * this.getP()));
        else
            return xi * (this.getAvg() / (2.0 * (1.0 - this.getP())));
    }
    
    public long getSeed1() { return seed1;}
    public void setSeed1(long seed1) { this.seed1 = seed1; }
    public long getSeed2() { return seed2; }
    public void setSeed2(long seed2) { this.seed2 = seed2; }
    public double getAvg() {return avg;}
    public void setAvg(double avg) {this.avg = avg;}
    public double getP() {return p;}
    public void setP(double p) {this.p = p;}
    public void setExp(ExponentialGenerator exp) { this.exp = exp;}
    public ExponentialGenerator getExp() { return exp;}
    public void setUg(UniformGenerator ug) { this.ug = ug;}
    public UniformGenerator getUg() { return ug;}
	
}