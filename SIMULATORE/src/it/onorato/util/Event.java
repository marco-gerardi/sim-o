package it.onorato.util;
//si è utilizzato come spunto la classe presente nell'esempio di pag 314 del libro di testo di MLS

public class Event{
	
	//dichiarazione e inizializzazione variabili che rappresentano gli eventi
	public final static int NUM_EVENT_TYPES = 6;
	public final static int Fine_M1 = 0;
	public final static int Fine_M2 = 1;
	public final static int Fine_M3 = 2;
	public final static int Fine_M4 = 3;
	public final static int OSSERVAZIONE = 4;
	public final static int FINESIM = 5;
	public final static double INFINITY = Double.MAX_VALUE;
	
	//dichiarazione variabili id e tempo dell'evento
	private int e_id;
	private double e_time;

	
	//costruttore
	public Event(int e_id)
	{
		this.e_id = e_id;
	}
	
	//costruttore
	public Event(int e_id, double e_time)
	{
		this.e_id = e_id;
		this.e_time = e_time;
	}

	public int getE_id() { return this.e_id; } //metodo che restituisce l'id del job
	
	public double getE_time() { return this.e_time; } //metodo che restituisce il tempo del job

}
