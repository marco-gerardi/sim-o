package it.onorato.generator;
public class RandomGenerator {
	//dichiarazione e inizializzazione costanti del generatore
	private static final long A = 1220703125;
	private static final long X = 5;
	private static final long C = 0;
	private static final long M = 214748648;
	//dichiarazione variabili del generatore
	private long a, x, c, m;

	//costruttori del generatore
	public RandomGenerator(long a, long x, long c, long m){
		this.a = a;
		this.x = x;
		this.c = c;
		this.m = m;
	}

	public RandomGenerator(long x){
		this(A, x, C, M);
	}
	
	public RandomGenerator(){
		this(A, X, C, M);
	}

	public RandomGenerator(RandomGenerator rg){
		this(rg.getA(), rg.getX(), C, M);
	}

	//genera il numero successivo della sequenza random
	public long getNextNumber()	{
		x = (a * x + c) % m;
		//System.out.println("rnd: "+x);
		return x;
	}

	//metodi get che restituiscono i valori delle variabili della classe
	public long getModulo() { return m;	}
	public long getA() { return this.a;	}
	public long getX() { return this.x;	}
	public long getC() { return this.c; }
}
