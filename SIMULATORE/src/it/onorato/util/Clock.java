package it.onorato.util;
// Si è utilizzato come spunto la classe presente nell'esempio di pag 314 del libro di testo di MLS

public class Clock {
	private double wallTimeStart, wallTimeStop;
	private double simTime;

	// COSTRUTTORE
	public Clock()
	{
		this.wallTimeStart = System.currentTimeMillis();
		setSimTime(0.0);
	}

	// GETTER
	public double getSimTime()
	{
		return this.simTime;
	}
	
	public double getWallTimeDuration()
	{
		stopTheClock();
		return this.wallTimeStop - this.wallTimeStart;
	}


	// SETTER
	public void setSimTime(double simTime)
	{
		this.simTime = simTime;
	}

	public void stopTheClock()
	{
		this.wallTimeStop = System.currentTimeMillis();
	}
}


