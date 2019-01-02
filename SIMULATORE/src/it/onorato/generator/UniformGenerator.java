package it.onorato.generator;

public class UniformGenerator {
	//dichiarazione variabili della classe
	private RandomGenerator rg;
	private double a, b;

	//Costruttori del generatore
	public UniformGenerator(RandomGenerator rg, double a, double b)
	{
		this.a = a;
		this.b = b;
		this.rg = rg;
	}

	public UniformGenerator(long x, double a, double b)
	{
		this(new RandomGenerator(x), a, b);
	}
	
	public UniformGenerator(double a, double b)
	{
		this(new RandomGenerator(), a, b);
	}

	//genera il numero successivo della sequenza uniforme
	public double getNextNumber()
	{
		return a + (b - a) * (rg.getNextNumber() / ((double) rg.getModulo()));
	}

	//metodi get che restituiscono i valori delle variabili della classe
	public double getA() {	return this.a; 	}
	public double getB() {	return this.b; 	}
	public RandomGenerator getRG() 	{ return this.rg; }
}
