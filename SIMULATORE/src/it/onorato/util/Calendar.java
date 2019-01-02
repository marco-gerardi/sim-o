package it.onorato.util;
//Si è utilizzato come spunto la classe presente nell'esempio di pag 314 del libro di testo di MLS

public class Calendar {

	private double[] eventi;
	
	
	public Calendar(){
		eventi = new double[Event.NUM_EVENT_TYPES];
		
	}
	
	public Event getNext(double clock){
		Event next = new Event(Event.NUM_EVENT_TYPES, Event.INFINITY);
		for (int i = 0; i < eventi.length; i++) {
			if (eventi[i] < next.getE_time() ) {
				next= new Event(i, eventi[i]);
			}
		}
		return next;
	}
	
	// GETTER
	public double getCalendar(int type) {

		return this.eventi[type];
	}

	public double[] getCalendar() {

		return this.eventi;
	}

	// SETTER
	public void setCalendar(int type, double newValue)
	{
		getCalendar()[type] = newValue;
	}

	public void addEvento(Event event)
	{
		getCalendar()[event.getE_id()] = event.getE_time();
	}
	
}
