package it.onorato.util;

public class Machine {
	
	private Job job, tmp;
	
	/***
	 * Costruttore oggetto Machine
	 */
	public Machine(){
		this.job = null;
	}

	/*
	 * Controlla se la machine non contiene job al suo interno
	 */
	public boolean getOccupied(){
		if(this.job==null) return false;
		return true;
	}
	
	//Rimuove il job dalla machine
	 
	/*public Job removeJob(){
		tmp = this.job; 
		this.job = null; 
		return tmp;
	} */
	
	public void removeJob(){ 
		job = null; 
	}
	/***
	 * Assegna il job al processore
	 * @param job = job da assegnare
	 */
	public void setJob(Job job){ this.job=job; }
	
	public Job getJob() {  // restituisco il job all'esterno 
		return this.job; 	
		
	}
	
	/*public boolean addJob(Job j){
		if(!this.getOccupied()){ 
			this.job = j;
			return true; 
		}
		return false; 
	}*/
	
}
