import it.onorato.generator.*;
import it.onorato.util.Calendar;
import it.onorato.util.Machine;
import it.onorato.util.Clock;
import it.onorato.util.Event;
import it.onorato.util.Job;
import it.onorato.util.Phase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

// Classe Simulazione
public class Simulazione{

	public ExponentialGenerator TimeServiceM1; // Per generare i tempi di servizio del Machine M1
	public HyperExpGenerator TimeServiceM2; // Per generare i tempi di servizio del Machine M2
	public HyperExpGenerator TimeServiceM3; // Per generare i tempi di servizio del Machine M3
	public KErlangGenerator TimeServiceM4; // Per generare i tempi di servizio del Machine M4
	public UniformGenerator RoutingOut; //Routing job uscenti dal centro 1 e dal centro 4
	public UniformGenerator RoutingM2Out; //Routing job uscenti dal centro 2 
	
	public List<Job> CodaM1Fifo, CodaM2Lifo, CodaM3Lifo, CodaM4Sptf;
	
	public ArrayList<Double> array_Oss; //contiene le oss effettuate durante la fase START e stabilizzazione
	public ArrayList<Double> array_Oss_Stat; //contiene le oss effettuate durante la fase statistica 
	public ArrayList<Double> array_Batch; //contine i risultati della simulazione
	public ArrayList<Double> arrayCampionaria;
	ArrayList<Double> tputStima = new ArrayList<Double>(); 
	boolean delayAttivo = true; //variabile per gestire se il delay è attivo durante la fase di statistica
	
	Clock clock;
	Calendar calendar;
	
	Machine M1;	//Machine M1
	Machine M2;	//Machine M2
	Machine M3;	//Machine M3
	Machine M4;	//Machine M4
	
	double media;
	double varianza;
	double t0; 
	double T0_calcolato = 0.0;
	public double th = 0.0;
	public double NX;
	int fase; 
	int n;
	int n_zero;
	int pRun=0;
	//int nStab; // quante osservazioni fare per raggiungere lo stato di equilibrio 
	int p_batch; // numero di run indipendenti da eseguire nella fase statistica 
	int numeroOss=0;
	String fileNameSim,fileNameStat;
	double T_oss = 12.86; //intervallo tra osservazioni
	double ua2 = 1.645; 
	int firsttime = 0;	//variabile di appoggio
	int JobIn=0;	//Quanti job circolano nella rete chiusa
	int NJobOut;	//numero job uscenti dalla linea gammma out
	Job tmpJob;
	DecimalFormat df;
	double sommaOss;
	double mediaCampionaria;
	double sommaMedia;
	double sommaVarianza;
	double e_n;
	double s2_n;
	
	public void Inizializzazione(double semeExp, double mediaExp, double semeHyper2, double mediaHyper2, double pHyper2, double semeHyper3, double mediaHyper3, double pHyper3, double seme2Erlang, double media2Erlang, double semeUnif){
		
		//Inizializzo i Generatori
		this.TimeServiceM1 = new ExponentialGenerator(5, mediaExp);
		this.TimeServiceM2 = new HyperExpGenerator(5,7, mediaHyper2, pHyper2); 
		this.TimeServiceM3 = new HyperExpGenerator(7,5, mediaHyper3, pHyper3);
		this.TimeServiceM4 = new KErlangGenerator(21,2,media2Erlang);
		this.RoutingOut = new UniformGenerator(139, 0, 1);
		this.RoutingM2Out = new UniformGenerator(139,1,10);
		
		//Inizializzo le Code 
		CodaM1Fifo = new ArrayList<Job>();
		CodaM2Lifo = new ArrayList<Job>(); 
		CodaM3Lifo = new ArrayList<Job>(); 
		CodaM4Sptf = new ArrayList<Job>();
				
		clock = new Clock();
		array_Oss = new ArrayList<Double>();
		array_Oss_Stat = new ArrayList<Double>();
		array_Batch = new ArrayList<Double>();
		arrayCampionaria = new ArrayList<Double>();
		// istanzio il calendar
		calendar = new Calendar(); 
		
		double TM1 = TimeServiceM1.getNextExp(); // genero il tempo di servizio del centro1 M1
		
		calendar.addEvento(new Event(Event.Fine_M1,clock.getSimTime()+TM1));
		calendar.addEvento(new Event(Event.Fine_M2, Event.INFINITY));
		calendar.addEvento(new Event(Event.Fine_M3, Event.INFINITY));
		calendar.addEvento(new Event(Event.Fine_M4, Event.INFINITY));
		calendar.addEvento(new Event(Event.OSSERVAZIONE, clock.getSimTime() + T_oss));
		calendar.addEvento(new Event(Event.FINESIM, Event.INFINITY));
		
		// istanzio i centri di servizio
		M1 = new Machine();
		M2 = new Machine();
		M3 = new Machine();
		M4 = new Machine();

	}

	// Funzione per gestire le esecuzioni delle varie fasi
	
	public void run(int nJob, int tipoFase, String fileName, String strDateTime) throws IOException {
		fileNameSim = fileName;
				
		PrintWriter pw = new PrintWriter(new FileWriter(new File(fileNameSim)));

		int idx = strDateTime.indexOf("_");
		String timestamp = strDateTime.substring(0, idx);
		String time = strDateTime.substring(idx+1);
		time = time.replace("-", ":"); 
		System.out.println("Inizio Simulazione");
		System.out.println("Giorno "+timestamp+" Ora "+time); 
		System.out.println("");
		
		pw.println("Inizio Simulazione");
		pw.println("Giorno "+timestamp+" Ora "+time);
		pw.println("");
		
		// formattazione del tempo di durata delle fasi della simulazione
		df = new DecimalFormat("#,##0.00"); 
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ITALY));
		
		//Verifica quale fase viene scelta dall'utente
		if(tipoFase==0){
			fase = Phase.STABILIZZAZIONE;
			System.out.println("Si esegue il run di Stabilizzazione");
		}else if(tipoFase==1){
			fase = Phase.RUN_VUOTO;
		}

		//Inserisci Jobs nella coda M1 in base all'input dall'utente
		JobIn = nJob;
		System.out.println("Viene impostato il numero dei job presenti nella rete (1-12): " + JobIn + " job");
		
		jobinCodaS1();
		
		while(fase!=Phase.STOP){
			switch(fase){
			case Phase.STABILIZZAZIONE: // Ottenere T0 per la fase di stabilizzazione
				n_zero=200;
				System.out.println("Viene settata la lunghezza del run di stabilizzazione: "+n_zero);
				pRun=30;
				System.out.println("Viene settata il numero dei p_run che compongono il run di stabilizzazione: "+pRun);
				t0 = System.currentTimeMillis(); //Variabile contenente tempo inizio simulazione

				pw.println("Run di Stabilizzazione con " + JobIn + " job circolanti nella rete"); 
				pw.println("Viene settato con n0 = " + n_zero + " e p run = " + pRun); 
				pw.println("");
				
				System.out.println("Run di Stabilizzazione con " + JobIn + " job circolanti nella rete"); 
				System.out.println("Viene settato con n0 = " + n_zero + " e p run = " + pRun); 
				System.out.println("");

				pw.println("Oss\tMedia\t\t\tVarianze\t");
				for(n=1; n<=n_zero; n++){
					// Ottengo i throughput del sistema
					for(int prun = 1;prun<=pRun;prun++){
						array_Oss = new ArrayList<Double>();
						while(array_Oss.size() < n ){
							scheduler();							
						}
					}
					numeroOss=0;				
					// Calcolo gli stimatori di Gordon
					pw.printf("%s\t%s\t%s\t%n", n , e_n, s2_n);
				}
				pw.println("");
				pw.println("T0: "+clock.getSimTime()); 
				//fase = Phase.STATISTICA; 
				break;
			case Phase.RUN_VUOTO: // Fase run vuoto
				System.out.println("");
				System.out.println("Si esegue un run a vuoto per un tempo simulato pari a T0");
				n_zero = 200;
				t0 = System.currentTimeMillis();
				
				Scanner in = new Scanner(System.in); 
				in = new Scanner(System.in); 
				do {
					System.out.println("Inserire il valore di T0 (in millisecondi) ottenuto nella fase di Stabilizzazione");
					T0_calcolato=in.nextDouble(); 
				} while(T0_calcolato==0.0);
				
				System.out.println("Il programma girerà a vuoto per il tempo T0: "+T0_calcolato); //debug

				while(array_Oss.size()<n_zero){ 
					//Run a vuoto della simulazione per un tempo T0 ottenuto durante la fase Pilota
					if (delayAttivo) {
						
						if (clock.getSimTime() <= T0_calcolato) {
							
							array_Oss.clear();

							}
						else {delayAttivo = false;
						System.out.println("Il tempo di esecuzione a vuoto risulta essere  " + clock.getSimTime()); 
						}
					}
					//System.out.println("Dimensione OsserStabil dopo il while " + OsserStabil.size()); 

					scheduler();
				}
				calendar.addEvento(new Event(Event.FINESIM, clock.getSimTime()));
				scheduler();
				
				break;
			case Phase.STATISTICA: // Fase statistica 
				System.out.println("");
				System.out.println("Si esegue il run di Statistica");
				n_zero = 200;
				System.out.println("Viene settata la lunghezza del run statistico: "+n_zero);
				p_batch = 30;
				System.out.println("Viene settato il numero di p batch che compongono il run statistico: "+p_batch);
				t0 = System.currentTimeMillis();

				while(array_Batch.size()<p_batch){
					
					while(array_Oss_Stat.size()<n_zero){

						scheduler();
					}
					array_Batch.add(array_Oss_Stat.get((int)(n_zero/2)));
					array_Oss_Stat.clear();
				}
				
				//crea e salva i dati nel file
				pw.println("Run Statistico con " + JobIn + " job circolanti nella rete"); 
				pw.println("Viene settato con n0 = " + n_zero + " e p batch = " + p_batch); 
				pw.println(""); 
 
				System.out.println("Run Statistico con " + JobIn + " job circolanti nella rete"); 
				System.out.println("Viene settato con n0 = " + n_zero + " e p batch = " + p_batch); 
				System.out.println(""); 
				
				pw.println("Throughput\t\tOss\t");

				for (int i=0; i<array_Batch.size();i++) {
					tputStima.add(array_Batch.get(i)); 
					//Stampa risultati nel file
					pw.printf("%s\t%s\t%n", array_Batch.get(i) , n_zero);
				}
				
				calendar.addEvento(new Event(Event.FINESIM, clock.getSimTime()));
				scheduler();
				break;

			case Phase.CONFIDENZA: //intervallo di confidenza della media del Throughput al 90% 
				
				System.out.println("");
				System.out.println("Si calcola l'intervallo di confidenza");
				System.out.println("");
				// Calcolo la media campionaria e varianza campionaria
				createMediaVarianza(tputStima);
				
				System.out.println("Media Campionaria: "+e_n);
				System.out.println("Varianza Campionaria: "+s2_n);

				double range = ua2*(Math.sqrt(s2_n)/(Math.sqrt(p_batch))); 
				double min = e_n - range;
				double max = e_n + range;
				System.out.println("Range: "+range);
				System.out.println("Min (Jobs/sec) = " + min);
				System.out.println("Max (Jobs/sec) = " + max);
				System.out.println("");
				// Stampa a schermo i risultati
				System.out.println("Intervallo di confidenza al 90%: "+e_n+" +/- "+range+"\n"); 
				System.out.println("I dati sono disponibili nel file "+fileName); 
				
				// Stampa i risultati nel file
				pw.println(""); 
				pw.println("Media Campionaria: "+e_n);
				pw.println("Varianza Campionaria: "+s2_n);
				pw.println("Range: "+range);
				pw.println("Min (Jobs/sec) = " + min);
				pw.println("Max (Jobs/sec) = " + max);
				pw.println("Intervallo di confidenza al 90%: "+e_n+" +/- "+range+"\n"); 

				fase = Phase.STOP; 
				break; 

			} 
		} 
		System.out.println(""); 
		System.out.println("FINE SIMULAZIONE (durata complessiva: " + df.format((clock.getWallTimeDuration())/1000) + " secondi)"); 

		pw.println(""); 
		pw.println("Osservazione eseguite ogni T_oss= " + T_oss + "secondi"); 
		pw.println("FINE SIMULAZIONE (durata complessiva: " + df.format((clock.getWallTimeDuration())/1000) + " secondi)"); 
		
		// Chiudi e salva il file 
		pw.close(); 
	}

	/**
	 * Lo Scheduler estrae il prossimo evento e lancia la routine ad esso riferita, utilizzato come spunto l'esempio di pag.324
	 * del libro di testo di Metodi e Linguaggi di Simulazione
	 * */
	
	public void scheduler(){ 
		Event next = calendar.getNext(clock.getSimTime());
		if (next!=null){ 
			clock.setSimTime(next.getE_time()); 
			routine(next.getE_id()); 
		} 
	}

	
	/**********************************************
	* ROUTINE
	**********************************************/
	//Routine Fine M1
	public void simFineM1(){
		if((this.fase==Phase.STABILIZZAZIONE && firsttime == 0)||
				(this.fase==Phase.RUN_VUOTO && firsttime == 0)){

			CodaM2Lifo.clear();
			CodaM3Lifo.clear();
			CodaM4Sptf.clear();

			M1.removeJob();
			M2.removeJob();
			M3.removeJob();
			M4.removeJob();
		
			//tmpJob = CodaM1Fifo.remove(CodaM1Fifo.size()-1); //Estrae il job dalla coda 
			tmpJob = CodaM1Fifo.remove(0);
			//M1.addJob(tmpJob); //Aggiunge al Machine M1 il job estratto dalla coda
			M1.setJob(tmpJob);
			firsttime++; //per eseguire il blocco di istruzioni sopra, soltanto una volta durante il run
		}
		
		tmpJob = M1.getJob(); //rimuovere job dal centro M1		
		M1.removeJob();
		NX = RoutingOut.getNextNumber(); 
		if (NX > 0.3) { // il job va verso il centro M2
			if (!M2.getOccupied()){ //M2 Libero
				M2.setJob(tmpJob); // occupo M2 col job che prima era in M1
				double TM2 = TimeServiceM2.getNextHyperExp();// prevedo tempo di servizio  Tm2(j) 
				calendar.addEvento(new Event(Event.Fine_M2, clock.getSimTime()+ TM2)); // prevedo il prox evento di fine M2 
			} else { // M2 è occupato e quindi lo metto in coda M2
				CodaM2Lifo.add(0,tmpJob);   // inserisco il Job in coda M2 con disciplina LIFO
			} 
		}
		else { // il job va verso il centro M3
			if (!M3.getOccupied()) //M3 Libero
			{
				M3.setJob(tmpJob); // occupo M3 col job che prima era in M1
				double TM3 = TimeServiceM3.getNextHyperExp(); // prevedo tempo di servizio  Tm3(j) 
				calendar.addEvento(new Event(Event.Fine_M3, clock.getSimTime() + TM3)); // prevedo il prox evento di fine M3
			}
			else { // M3 è occupato e quindi lo metto in coda M3
				CodaM3Lifo.add(0,tmpJob); // inserisco il Job in coda M3 con disciplina LIFO
			}
		}
		if(!CodaM1Fifo.isEmpty()){ // coda M1 non è vuota
			tmpJob = CodaM1Fifo.remove(0); // rimuovo job dalla coda M1 con disciplina FIFO e lo metto dentro M1
			M1.setJob(tmpJob); 
			double TM1 = TimeServiceM1.getNextExp(); // prevedo tempo di servizio  Tm1(j) 
			calendar.addEvento(new Event(Event.Fine_M1, clock.getSimTime()+TM1)); // prevedo il prox evento di fine M1
		}
		else { // coda M1 vuota
			calendar.addEvento(new Event(Event.Fine_M1, Event.INFINITY)); // schedulo prossimo evento di Fine M1 non prevedibile
		}
	} // fine evento FineM1
	
	//Routine Fine M2
	public void simFineM2() {
		tmpJob = M2.getJob(); //rimuovere job dal centro M2
		M2.removeJob();
		NX = RoutingM2Out.getNextNumber(); 
		if (valueRoutingM2(NX)){ // il job va verso il centro M1 se la variabile è pari
			if (!M1.getOccupied()){ //M1 Libero
				M1.setJob(tmpJob); // occupo M1 col job che prima era in M2
				double TM1 = TimeServiceM1.getNextExp();// prevedo tempo di servizio Tm1(j) 
				calendar.addEvento(new Event(Event.Fine_M1, clock.getSimTime()+TM1)); // prevedo il prox evento di fine M1 
			} else { // M1 è occupato e quindi lo metto in coda M1
				CodaM1Fifo.add(0,tmpJob); // inserisco il Job in coda M1 con disciplina FIFO
			} 
		}
		else { // il job va verso il centro M4
			if (!M4.getOccupied()){ //M4 Libero
				M4.setJob(tmpJob); // occupo M4 col job che prima era in M2
				double TM4 = TimeServiceM4.getNextNumber();// prevedo tempo di servizio  Tm4(j) 
				calendar.addEvento(new Event(Event.Fine_M4, clock.getSimTime()+TM4)); // prevedo il prox evento di fine M4
			}
			else { // M4 è occupato e quindi lo metto in coda M4
				addJobToSPTF(tmpJob); // inserisco il Job in coda M4 con disciplina SPTF
			}
		}
		if(!CodaM2Lifo.isEmpty()){ // coda M2 non è vuota
			tmpJob = CodaM2Lifo.remove(CodaM2Lifo.size()-1);  // rimuovo job dalla coda M2 con disciplina LIFO e lo metto dentro M2
			M2.setJob(tmpJob);
			double TM2 = TimeServiceM2.getNextHyperExp();// prevedo tempo di servizio  Tm2(j) 
			calendar.addEvento(new Event(Event.Fine_M2, clock.getSimTime()+TM2)); // prevedo il prox evento di fine M2
		}
		else { // coda M2 vuota
			calendar.addEvento(new Event(Event.Fine_M2, Event.INFINITY)); // schedulo prossimo evento di Fine M2 non prevedibile
		}
	} // fine evento FineM2 
	
	//Routine Fine M3
	public void simFineM3() {
		tmpJob = M3.getJob(); //rimuovere job dal centro M3
		M3.removeJob();
		if (!M4.getOccupied()){ //M4 Libero
			M4.setJob(tmpJob); // occupo M4 col job che prima era in M3
			double TM4 = TimeServiceM4.getNextNumber(); // prevedo tempo di servizio Tm4(j) 
			calendar.addEvento(new Event(Event.Fine_M4, clock.getSimTime()+TM4)); // prevedo il prox evento di fine M4
		} else { // M4 è occupato e quindi lo metto in coda M4
			addJobToSPTF(tmpJob); // inserisco il Job in coda M4 con disciplina SPTF
		} 
		if(!CodaM3Lifo.isEmpty()){ // coda M3 non è vuota
			tmpJob = CodaM3Lifo.remove(CodaM3Lifo.size()-1); // rimuovo job dalla coda M3 con disciplina LIFO e lo metto dentro M3
			M3.setJob(tmpJob);
			double TM3 = TimeServiceM3.getNextHyperExp(); // prevedo tempo di servizio Tm3(j) 
			calendar.addEvento(new Event(Event.Fine_M3, clock.getSimTime()+TM3)); // prevedo il prox evento di fine M3
		}
		else { // coda M3 vuota
			calendar.addEvento(new Event(Event.Fine_M3, Event.INFINITY)); // schedulo prossimo evento di Fine M3 non prevedibile
		}
    } // fine evento FineM3 
	
	//Routine Fine M4
	public void simFineM4() {
		tmpJob = M4.getJob();
		M4.removeJob();
		NX = RoutingOut.getNextNumber(); 
		if (NX <= 0.1) { // il job va verso il centro M3
			if (!M3.getOccupied()){ //M3 Libero
				M3.setJob(tmpJob); // occupo M3 col job che prima era in M4
				double TM3 = TimeServiceM3.getNextHyperExp(); // prevedo tempo di servizio Tm3(j) 
				calendar.addEvento(new Event(Event.Fine_M3, clock.getSimTime()+TM3)); // prevedo il prox evento di fine M3 
			} else { // M3 è occupato e quindi lo metto in coda M3
				CodaM3Lifo.add(0,tmpJob);  // inserisco il Job in coda M3 con disciplina LIFO
			} 
		}
		else { // il job va verso il centro M1
			NJobOut++; // incremento la variabile NJobOUT
			if (!M1.getOccupied()) {//M1 Libero
				M1.setJob(tmpJob); // occupo M1 col job che prima era in M4
				double TM1 = TimeServiceM1.getNextExp(); // prevedo tempo di servizio Tm1(j) 
				calendar.addEvento(new Event(Event.Fine_M1, clock.getSimTime()+TM1)); // prevedo il prox evento di fine M1
			}
			else { // M1 è occupato e quindi lo metto in coda M1
				CodaM1Fifo.add(0,tmpJob);  // inserisco il Job in coda M1 con disciplina FIFO
			}
		}
		if(!CodaM4Sptf.isEmpty()){ // coda M4 non è vuota
			tmpJob = CodaM4Sptf.get(0);  
			CodaM4Sptf.remove(0); // rimuovo job dalla coda M4 con disciplina SPTF e lo metto dentro M4
			M4.setJob(tmpJob);
			double TM4 = TimeServiceM4.getNextNumber(); // prevedo tempo di servizio Tm4(j) 
			calendar.addEvento(new Event(Event.Fine_M4, clock.getSimTime()+TM4)); // prevedo il prox evento di fine M4
			}
		else { // coda M4 vuota
			calendar.addEvento(new Event(Event.Fine_M4, Event.INFINITY)); // schedulo prossimo evento di Fine M4 non prevedibile
		}
    } // fine evento FineM4
	
	//Routine Osservazione
	private void Osservazione(){
		
		th = (NJobOut/T_oss); // calcolo throughput

		calendar.addEvento(new Event(Event.OSSERVAZIONE, clock.getSimTime()+T_oss));
		
		if(this.fase==Phase.STABILIZZAZIONE){
			
			array_Oss.add(th);
			NJobOut=0; //azzero NJobOut per la prossima osservazione

			if (array_Oss.size()==n) {
				
				createMediaVarianza(array_Oss);
								
				if (arrayCampionaria.size()==(n*pRun)) {		
					arrayCampionaria.clear();							
				}
									
			}
			
			if( n==n_zero && numeroOss==pRun ) {
				//System.out.println("Imposto il fine sim con n= " +n + " e prun= " + numeroOss);
				calendar.addEvento(new Event(Event.FINESIM, clock.getSimTime()));
				scheduler();
				
			}
		}
		if (this.fase==Phase.RUN_VUOTO) {
			array_Oss.add(th);
			NJobOut=0; //azzero NJobOut per la prossima osservazione
		}
		if(this.fase==Phase.STATISTICA){
		    array_Oss_Stat.add(th);
			NJobOut=0; //azzero NJobOut per la prossima osservazione
		}
	}
	
	// Routine
	public void routine(int tipo){ 

		switch (tipo){ 
		case Event.Fine_M1: // routine Fine_M1 
			simFineM1();	
			break; 
		case Event.Fine_M2: // routine Fine_M2 
			simFineM2();
			break;
		case Event.Fine_M3: // routine Fine_M3 
			simFineM3();
			break;
		case Event.Fine_M4: // routine Fine_M4 
			simFineM4();
			break;	
		case Event.OSSERVAZIONE: // routine Osservazione
			Osservazione();				 
			break;
			
		// routine Fine Simulazione	

		case Event.FINESIM: 
			if(this.fase==Phase.STABILIZZAZIONE){ 
				System.out.println("T0 ottenuto dalla fase di Stabilizzazione");
				System.out.println("T0: "+clock.getSimTime());
				System.out.println("STOP della fase di Stabilizzazione");
				fase = Phase.STOP;
				calendar.addEvento(new Event(Event.FINESIM, Event.INFINITY));
				System.out.println("I dati sono disponibili nel file "+fileNameSim);
				break;
			} else if(this.fase==Phase.RUN_VUOTO){
				fase = Phase.STATISTICA;
				calendar.addEvento(new Event(Event.FINESIM, Event.INFINITY));
				System.out.println("Stop del run a vuoto");
				
				break;
			} else if(this.fase==Phase.STATISTICA){
				calendar.addEvento(new Event(Event.FINESIM, Event.INFINITY));
				System.out.println("STOP della fase Statistica");
				//Creare l'intervallo di confidenza
				fase = Phase.CONFIDENZA; 
				break;
			}else {
				break;
			}
			
		} 
		
	}


	/**
	 * Funzioni di appoggio 
	 **/ 
	
	/*// Calcola la media degli elementi di un Array 
	public static double listMedia(ArrayList<Double> l) { 
		double sum = 0.0; 
		for (Double m : l)
			sum += m; 
		return sum / l.size(); 
	} 
	
	// Calcola la varianza degli elementi di un Array
	public static double listVarianza(ArrayList<Double> l) { 
		double avg = listMedia(l); 
		double sum = 0.0; 
		for (Double v : l)
			sum += Math.pow(v - avg, 2.0); 
		return sum / (l.size()-1); 
	} */
	
	
	// Calcola media e varianza degli elementi di un Array
	public void createMediaVarianza(ArrayList<Double> array) { 
		for (int i=0; i<array.size();i++) {
			arrayCampionaria.add(array.get(i)); 
		}	
		
		ResetStatoIniziale();
		numeroOss++;
		
		sommaMedia = 0.0;
		sommaVarianza = 0.0;
		e_n = 0.0;				
		
		for (int j = 0; j < arrayCampionaria.size(); j++) { 
			sommaMedia += arrayCampionaria.get(j);
		}

		e_n = sommaMedia/arrayCampionaria.size(); // tramite lo stimatore di Gordon per la media calcolo e(n)
		
		for (int z = 0; z < arrayCampionaria.size(); z++) {
			sommaVarianza += Math.pow(arrayCampionaria.get(z) - e_n, 2);
		}
		
		s2_n = sommaVarianza/(arrayCampionaria.size()-1);
	} 
	
	// aggiungi un job alla codaS4 SPTF e ordinali per tempo di processamento	
		private void addJobToSPTF(Job newJob)
		{
			CodaM4Sptf.add(newJob);

			if (CodaM4Sptf.size() > 1) {
				Collections.sort(CodaM4Sptf, new Comparator<Job>() {
					public int compare(Job job1, Job job2) {
						return Double.compare(job1.getExec(), job2.getExec());
					}
				});
			}
		}
		
		
		public void jobinCodaS1(){

		
		switch (JobIn) {
	
	
		case 1:
		
		Job nuovo1;
		Job nuovo2;
		Job nuovo3;
		Job nuovo4;
		Job nuovo5;
		Job nuovo6;
		Job nuovo7;
		Job nuovo8;
		Job nuovo9;
		Job nuovo10;
		Job nuovo11;
		Job nuovo12;

		
		nuovo1 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		
		break;
		
	case 2:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		
		break;
		
	case 3:


		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
					
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		
		break;
		
	case 4:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
					
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		
		break;
		
	case 5:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
					
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		
		break;
		
	case 6:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		
		break;
		
	case 7:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		
		break;
	
	case 8: 
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		nuovo8 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		CodaM1Fifo.add(nuovo8);
		
		break;
		
	case 9: 
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		nuovo8 = new Job();
		nuovo9 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		CodaM1Fifo.add(nuovo8);
		CodaM1Fifo.add(nuovo9);
		
		break;
		
	case 10: 
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		nuovo8 = new Job();
		nuovo9 = new Job();
		nuovo10 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		CodaM1Fifo.add(nuovo8);
		CodaM1Fifo.add(nuovo9);
		CodaM1Fifo.add(nuovo10);
		
		break;
		
	case 11:
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		nuovo8 = new Job();
		nuovo9 = new Job();
		nuovo10 = new Job();
		nuovo11 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		CodaM1Fifo.add(nuovo8);
		CodaM1Fifo.add(nuovo9);
		CodaM1Fifo.add(nuovo10);
		CodaM1Fifo.add(nuovo11);
		
		break;
		
	case 12: 
		

		nuovo1 = new Job();
		nuovo2 = new Job();
		nuovo3 = new Job();
		nuovo4 = new Job();
		nuovo5 = new Job();
		nuovo6 = new Job();
		nuovo7 = new Job();
		nuovo8 = new Job();
		nuovo9 = new Job();
		nuovo10 = new Job();
		nuovo11 = new Job();
		nuovo12 = new Job();
		
		CodaM1Fifo.add(nuovo1);
		CodaM1Fifo.add(nuovo2);
		CodaM1Fifo.add(nuovo3);
		CodaM1Fifo.add(nuovo4);
		CodaM1Fifo.add(nuovo5);
		CodaM1Fifo.add(nuovo6);
		CodaM1Fifo.add(nuovo7);
		CodaM1Fifo.add(nuovo8);
		CodaM1Fifo.add(nuovo9);
		CodaM1Fifo.add(nuovo10);
		CodaM1Fifo.add(nuovo11);
		CodaM1Fifo.add(nuovo12);
		
		break;
	
	}
}

		public void ResetStatoIniziale() {
			
			//viene reimpostato lo stato iniziale per iniziare il run successivo
			CodaM1Fifo.clear();				
			CodaM2Lifo.clear();
			CodaM3Lifo.clear();
			CodaM4Sptf.clear();
			
			M1.removeJob();
			M2.removeJob();
			M3.removeJob();
			M4.removeJob();
			
			jobinCodaS1();
			
			//tmpJob = CodaM1Fifo.remove(CodaM1Fifo.size()-1); //Estrae il job dalla coda 
			tmpJob = CodaM1Fifo.remove(0); //Estrae il job dalla coda 
			M1.setJob(tmpJob);
			//M1.addJob(tmpJob); //Aggiunge al Machine M1 il job estratto dalla coda
	
			calendar.addEvento(new Event(Event.Fine_M1,clock.getSimTime()+TimeServiceM1.getNextExp()));
			calendar.addEvento(new Event(Event.Fine_M2, Event.INFINITY));
			calendar.addEvento(new Event(Event.Fine_M3, Event.INFINITY));
			calendar.addEvento(new Event(Event.Fine_M4, Event.INFINITY));
			calendar.addEvento(new Event(Event.OSSERVAZIONE, clock.getSimTime()+T_oss));
			calendar.addEvento(new Event(Event.FINESIM, Event.INFINITY));
		}
		
		public boolean valueRoutingM2(double x ) {
			
			int numeroPari = (int) x;
			if ((numeroPari % 2) == 0) {
				return true;
			} else {
				return false;
			}
		}
		
} 





