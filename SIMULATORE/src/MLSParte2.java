import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import it.onorato.util.Phase;

public class MLSParte2 {

	public static void main(String[] args) {
		//Parametri dei generatori  
		double semeExp = 21; 
		double mediaExp = 1.25;
		double semeHyper2 = 25;
		double mediaHyper2 = 1.25;
		double pHyper2 = 0.3;
		double semeHyper3 = 25;
		double mediaHyper3 = 2.5;
		double pHyper3 = 0.3;
		double seme2Erlang = 11;
		double media2Erlang = (1/0.7);
		double semeUnif = 111;
		int inJob = 1;
		int fase;
		String fileName;
		
		System.out.println("SIMULAZIONE MLS - ONORATO EMDDIO - MAT: 0009490"); 
		System.out.println("");
		
		// Creazione simulazione 
		Simulazione simulation = new Simulazione(); 
		simulation.Inizializzazione( semeExp, mediaExp,  semeHyper2,  mediaHyper2,  pHyper2, semeHyper3,  mediaHyper3,  pHyper3,  seme2Erlang,  media2Erlang, semeUnif); 

		char tipoFase = 0; 
		do{
			System.out.println("Per ottenere il tempo T0 (millisecondi) necessario ad eliminare la polarizzazione iniziale");
			System.out.println("bisogna eseguire la fase di Stabilizzazione");
			System.out.println("Vuoi eseguire la fase di Stabilizzazione per ottenere il tempo T0?");
			System.out.println("s = si -> fase di Stabilizzazione");
			System.out.println("n = no -> fase Statistica");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				tipoFase = (char)br.read();
			} catch (IOException e) {} 

		} while(tipoFase!='s' && tipoFase!='n');
		
		// Creazione File per salvare i dati della simulazione
		SimpleDateFormat sdf = new SimpleDateFormat ("dd-MM-yyyy_HH-mm-ss"); 
		String strDateTime = sdf.format (new Date());
		
		if(tipoFase=='s'){
			fase = Phase.STABILIZZAZIONE; 
			fileName = "MLS_STAB_"+inJob+"_"+strDateTime+".txt"; 
		}
		else {
			fase = Phase.RUN_VUOTO;
			fileName = "MLS_STAT_"+inJob+"_"+strDateTime+".txt"; 
		}
			
		
		do {
			System.out.println("Indicare il numero dei job circolanti nella rete da 1 a 12:");

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			try {
				inJob = Integer.parseInt(in.readLine());
			}catch (IOException e) {}
		} while(inJob < 1 || inJob > 12);
		
		try { 
			simulation.run(inJob,fase,fileName,strDateTime); 
		}catch (Exception e){ 
			System.out.println(e.getMessage());  
		} 

	}

}
