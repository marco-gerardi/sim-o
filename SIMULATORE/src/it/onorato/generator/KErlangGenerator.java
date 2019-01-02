package it.onorato.generator;

public class KErlangGenerator {

	//costanti per seme generatore e k 
	private final static int defaultK = 2;
	private final static long defaultX = 111;
	//dichiarazione variabili generatore
	private ExponentialGenerator eg;
	private int k;
	private double ts;
	private double min;
	private double max;
	private int n;
	private double sum;

	//Costruttori del generatore
	public KErlangGenerator(ExponentialGenerator eg, int k, double ts)
	{
		this.eg = eg;
		this.k = k;
		this.ts = ts;
		this.min = (Double.MAX_VALUE);
		this.max = Double.MIN_VALUE;
		this.n = 0;
		this.sum = 0;
	}

	public KErlangGenerator(UniformGenerator ug, int k, double ts)
	{
		this(new ExponentialGenerator(ug, ts / k), k, ts);
	}

	public KErlangGenerator(RandomGenerator rg, int k, double ts)
	{
		this(new UniformGenerator(rg, 0, 1), k, ts);
	}

	public KErlangGenerator(long x, int k, double ts)
	{
		this(new UniformGenerator(x, 0, 1), k, ts);
	}

	public KErlangGenerator(long x, double ts)
	{
		this(new UniformGenerator(x, 0, 1), defaultK, ts);
	}

	public KErlangGenerator(double ts)
	{
		this(new UniformGenerator(defaultX, 0, 1), defaultK, ts);
	}

	//genera il numero successivo della sequenza k erlangiana e somma i numeri della sequenza 
	public double getNextNumber()
	{
		double nextNumber = 0;
		for (int i = 0; i < k; i++)
		{
			nextNumber += eg.getNextExp();
		}
		if (nextNumber < this.max)
		{
			this.min = (nextNumber);
		}
		if (nextNumber > this.max)
		{
			this.max = nextNumber;
		}
		this.n++;
		this.sum += nextNumber;
		return nextNumber;
	}

	//metodi get che restituiscono i valori delle variabili della classe e la media della sequenza generata
	public ExponentialGenerator getEG() { return this.eg; }
	public double getTs() { return this.ts; }
	public double getK() { return this.k; }
	public double getMIN() { return this.min;}
	public double getMAX() { return this.max; }
	public double getAverage() {return this.sum / this.n;}
}