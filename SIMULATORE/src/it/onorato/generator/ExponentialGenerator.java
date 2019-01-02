package it.onorato.generator;
/**
 * Classe presente a Pag. 317 del libro
 * Vengono generati numeri casuali distribuiti esponenzialmente 
 * con un parametro medio, lambda
 * 
 * @author Emiddio Onorato
 */


public class ExponentialGenerator {

	private UniformGenerator ug;
	private double lambda, eg;
	private long x;

	public ExponentialGenerator(UniformGenerator ug, double lambda)
	{
		this.setLambda(lambda);
		this.setUg(ug);
	}
	
	public ExponentialGenerator(long x, double lambda)
	{
		this.setLambda(lambda);
		this.ug = new UniformGenerator(x,0, 1);
		this.ug.getNextNumber();
		this.setUg(ug);
	}
	
	//genera il numero successivo della sequenza esponenziale
	public double getNextExp()
	{
		this.setValueNextExp(-1.0 * this.getLambda() * Math.log(this.getUg().getNextNumber()));
		return this.getValueNextExp();
	}
	
	public double getLambda() { return this.lambda; }
	public void setLambda(double lambda) { this.lambda = lambda; }
	public long getXn() { return this.x; }
	public void setXn(long x) { this.x = x;}
	public double getValueNextExp() { return this.eg; }
	public void setValueNextExp(double eg) { this.eg = eg; }
	public UniformGenerator getUg() { return this.ug; }
	public void setUg(UniformGenerator ug) { this.ug = ug; }
}
