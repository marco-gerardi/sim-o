package it.onorato.util;

//Si utilizza  come spunto la classe presente nell'esempio di pag 314 del libro di testo di MLS

public class Job{
	
	private int id;
	private double tmpArrivo; 
	private double tmpExec; 
	Job next;
	
	public Job(int id,double tmpArrivo, double tmpExec){
			this.id = id;
			this.tmpArrivo = tmpArrivo; 
			this.tmpExec = tmpExec; 
			this.next = null;
	}
	
	public Job(){
	}
		
	public int getId(){
		return this.id; 
	}
	public double getArrivo(){
		return this.tmpArrivo; 
	}
	
	public double getExec(){ 	
		return this.tmpExec;
	} 
}