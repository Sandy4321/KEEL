
package keel.Algorithms.Fuzzy_Rule_Learning.Genetic.ClassifierSLAVE2;

import java.io.*;
import java.util.Random;

import org.core.Files;
import org.core.Randomize;


/**	
	 * <p>
	 * Main Class of the Program
	 * It reads the configuration file (data-set files and parameters) and launch the algorithm
	 * </p>
	 */
public class main_c {
	
	
	
	static final double MISSING = -999999999;

	/**Default constructor. */
	public main_c (){
	}
	
	
	   /**
  * <p>
  * It launches the algorithm
  * </p>
  * @param parameters parseParameters It contains the input files, output files and parameters.
     * @throws java.io.IOException if the algorithm can not be executed.
  */
	
	public void execute(parseParameters parameters) throws IOException{
		// TODO Auto-generated method stub

		
		
		Randomize.setSeed (Long.parseLong (parameters.getParameter (0)));
		Parameters.tam_population = Integer.parseInt(parameters.getParameter(1));
		Parameters.iter_no_changes = Integer.parseInt(parameters.getParameter(2));
		Parameters.prob_mut_bin = Double.parseDouble(parameters.getParameter(3));
		Parameters.prob_cross_bin = Double.parseDouble(parameters.getParameter(4));
		Parameters.prob_mut_real = Double.parseDouble(parameters.getParameter(5));
		Parameters.prob_cross_real = Double.parseDouble(parameters.getParameter(6));
		Parameters.Lambda = Double.parseDouble(parameters.getParameter(7));
		

		String fichname_tra, fichname_tst, fich_extension, fich_salida_tst, fich_salida_tra;
		char[] nomfich_tra;
		char[] nomfich_tst;
		
		boolean somethingWrong = false;
				
		myDataset train_n = new myDataset();
		myDataset test_n = new myDataset();
		    
		try {
			System.out.println("\nReading the training set: " + parameters.getTrainingInputFile());

			train_n.readClassificationSet(parameters.getTrainingInputFile(), true);
			System.out.println("\nReading the test set: " + parameters.getTestInputFile());

			test_n.readClassificationSet(parameters.getTestInputFile(), false);
		}	
		catch (IOException e) {
			System.err.println("There was a problem while reading the input data-sets: " + e);

			somethingWrong = true;
		}
		
		if (somethingWrong)
			System.exit(0);
		
		NameClasses nom_clas = new NameClasses ();
		
		fichname_tra = parameters.getTrainingInputFile();
		fichname_tst = parameters.getTestInputFile();

		nomfich_tra = fichname_tra.toCharArray();
		nomfich_tst = fichname_tst.toCharArray();
		
		vectorvar V = new vectorvar (nomfich_tra, train_n, nom_clas, 5);
		example_set E = new example_set (train_n);
		example_set E_tst = new example_set (test_n);
		
		Update_MissingValues (E, train_n);
		Update_MissingValues (E_tst, test_n);
		
		fich_extension = parameters.getOutputFile(0);
		
		String cad_aux = new String ("");
		

		cad_aux = cad_aux + "Experiment: " + fichname_tra + "\n";

		cad_aux = cad_aux + "===================================================\n\n";
		
	
		int NPobBin = 1;
		
		int NPobEnt = 2;
		
		int NPobRea = 2;               //5
		
		int Elite = 1;
		
		int NFitness = 3;
		
		int Indiv = Parameters.tam_population;
		
		int NPobTotal = NPobBin + NPobEnt + NPobRea;
		
		int[] rango = new int[NPobEnt];
		
		double[] rangoi = new double[NPobRea];
		double[] rangos = new double[NPobRea];
		

		int[] tama = new int[NPobTotal];
		double[] mut = new double[NPobTotal];
		double[] cruce = new double[NPobTotal];
		

		for (int i=0;i<NPobBin; i++){
			mut[i] = Parameters.prob_mut_bin; 
			cruce[i] = Parameters.prob_cross_bin;
		}

		for (int i=NPobBin;i<NPobBin+NPobEnt; i++){
			mut[i] = 0.0;
			cruce[i] = 0.0;
		}

		mut[NPobBin] = 0.01;  // 0.01
		cruce[NPobBin] = 0.2;


		for (int i=NPobBin+NPobEnt;i<NPobBin+NPobEnt+NPobRea; i++){
			mut[i] = 0;     // 0.01
			cruce[i] = 0;
		}
		

		int antecedente;
		

		V.Encode (tama, rango, 0, 0);
		
		rangoi[0] = 0.0;
		rangos[0] = 1.0;
		
		rangoi[1] = -1.0;
		rangos[1] = 1.0;
		
		tama[1] = 1;
		
		tama[2] = 1;
		
		tama[3] = V.N_Antecedent() + 1;
		
		tama[4] = E.N_Examples() + 1;
		
		mut[0] = Parameters.prob_mut_bin/tama[0];
		cruce[0] = Parameters.prob_cross_bin;
		
		mut[NPobBin+NPobEnt] = Parameters.prob_mut_real / V.N_Antecedent();
		cruce[NPobBin+NPobEnt] = Parameters.prob_cross_real;


		multipopulation G = new multipopulation (NPobBin, NPobEnt, NPobRea, rango, rangoi, rangos, mut, cruce, Elite, Indiv, tama, NFitness);


		ruleset R = new ruleset ();
		R.AddDomain (V);
		
		int n, z;
		double accuracy_old;
		double accuracy_new;
		int iteraciones;
		double test;
		double cardinal;
		double variables_por_regla;
		double variables_usadas;
		int[] frecuencia_variables = new int[V.N_Antecedent()];
		int condiciones;
		double tiempo_eje;
		
		long tiempo0, tiempo1; 
		
		double total_tiempo;
		int iteraciones_parcial;
		
		double[] agregado;
		double[] peso_agregado;
		double[] valor = new double[3];
		

		double[][][] Tabla;


		example_set E_Par, E_Par_Test;
		double[][] Med_Inform = ReserveForInformationMeasures (V);

		int[] ejemplos_por_clase = new int[rango[0]];
		
	//	for (int par=0; par<num_par; par++){
			System.out.println ("\n\nStarting program... " + "\n");
						

			cad_aux = cad_aux + "\n===============\n Run " + "\n===============\n";
			//buf = cad_aux.getBytes();
			//out.write (buf);

			//out.close();

			iteraciones = 0;
			E_Par = E;
			E_Par.UnMarkAll ();
			E_Par_Test = E_tst;
			E_Par_Test.UnMarkAll ();
			
			n = E_Par.Not_Covered_Examples ();
			z = E_Par_Test.Not_Covered_Examples ();
			
			int Total_ejemplos = E_Par.N_Examples ();
			agregado = new double[Total_ejemplos];
			peso_agregado = new double[Total_ejemplos];
			
			for (int i=0; i<Total_ejemplos; i++){
				agregado[i] = 0;
				peso_agregado[i] = 0;
			}

			tiempo0 = System.currentTimeMillis ();
			total_tiempo = 0;
			Tabla = Create_Adaptation_Table (V, E_Par);
			
			int anterior_por_clase;
			
			Files.writeFile (fich_extension, cad_aux);
			
			for (int clase=0; clase<rango[0]; clase++){
				E_Par.UnMarkAll ();
				do{
					cad_aux = new String ("");
					
					tiempo1 = System.currentTimeMillis ();
					total_tiempo = (total_tiempo + (1.0*(tiempo1-tiempo0)/1000));
					tiempo0 = tiempo1;
					E_Par.Examples_per_Class (V.Consequent (), rango[0], ejemplos_por_clase);
					anterior_por_clase = ejemplos_por_clase[clase];
					
					System.out.println ("Accuracy on training: " + Successes (R, V, E_Par) + "\n");

					
					cad_aux = cad_aux + "Accuracy on training: " + Successes (R, V, E_Par) + "\n";
					
					cad_aux = cad_aux + "Number of examples: " + n + "\n";

					for (int i=0;i<rango[0]; i++){
						cad_aux = cad_aux + "Class " + i + ": " + ejemplos_por_clase[i] + "\n";
					}	

					InformationMeasures (V, E_Par, Med_Inform, Tabla);
					
					iteraciones_parcial = GA (G, V, E_Par, R, Med_Inform, clase, Total_ejemplos, Tabla);
					valor[0] = G.ValueFitness (0);
					iteraciones += iteraciones_parcial;

					n = E_Par.Not_Covered_Examples ();
					E_Par.Examples_per_Class (V.Consequent (), rango[0], ejemplos_por_clase);
					
					Files.addToFile (fich_extension, cad_aux);


				}while (valor[0]>0 && ejemplos_por_clase[clase]>0 && anterior_por_clase>ejemplos_por_clase[clase]);

				if (valor[0] <= 0){
					R.Remove ();
				}

			} 
		
			
			fich_salida_tst = parameters.getTestOutputFile();
			fich_salida_tra = parameters.getTrainingOutputFile();
			
			
			String[] clases_nomb = new String[train_n.getnClasses()];

		    for (int i = 0; i < train_n.getnClasses(); i++) {
		      clases_nomb[i] = train_n.getOutputValue(i);
		    }  
		    

			double training = Successes (R, V, E_Par, fich_extension, fich_salida_tra, clases_nomb, train_n);
					
			test = Successes (R, V, E_Par_Test, fich_extension, fich_salida_tst, clases_nomb, test_n);			
			
			cardinal = R.N_rule ();
			
			variables_por_regla = R.Variables_per_rule ();
			variables_usadas = R.Frecuence_each_Variables (frecuencia_variables);
			condiciones = R.Conditions_per_RB ();
			
			tiempo1 = System.currentTimeMillis ();
			tiempo_eje = (total_tiempo + (1.0*(tiempo1 - tiempo0)/1000));
			tiempo0 = tiempo1;
			
			cad_aux = new String ("");

			cad_aux = cad_aux + "----------------------------------------------------\n";

			cad_aux = cad_aux + "Accuracy on training: " + training + "\n";
			
			cad_aux = cad_aux + "Accuracy on test: " + test + "\n";
			
			cad_aux = cad_aux + "Number of rules: " + cardinal + "\n";
			
			cad_aux = cad_aux + "Variables per rule: " + variables_por_regla + "\n";
			
			cad_aux = cad_aux + "Variables used: " + variables_usadas + "\n";
			
			cad_aux = cad_aux + "Time: " + tiempo_eje + "\n";
			
			cad_aux = cad_aux + "Iterations: " + iteraciones + "\n";
			
			cad_aux = cad_aux + "Conditions: " + condiciones + "\n";
			
			cad_aux = cad_aux + "----------------------------------------------------\n";
			
			cad_aux = cad_aux + "------------------- RULES -------------------------\n";
			
			cad_aux = cad_aux + "----------------------------------------------------\n";
			
			Files.addToFile (fich_extension, cad_aux);
			
			R.SaveRuleInterpreted_append (fich_extension);

		
			
			cad_aux = new String ("");
			
			cad_aux = cad_aux + "\tAccuracy on training set" + " is: " + training + "\n";
			
			cad_aux = cad_aux + "\tAccuracy on test set" + " is: " + test + "\n";
			
			cad_aux = cad_aux + "\tNumber of rules" + " is: " + cardinal + "\n";
			
			cad_aux = cad_aux + "\tVariables per rule" + " is: " + variables_por_regla + "\n";
			
			cad_aux = cad_aux + "\tVariables used" + " is: " + variables_usadas + "\n";
			
			cad_aux = cad_aux + "\tTime" + " is: " + tiempo_eje + "\n";
			
			cad_aux = cad_aux + "\tIterations" + " is: " + iteraciones + "\n";
			
			cad_aux = cad_aux + "\tConditions" + " is: " + condiciones + "\n\n";
			
			Files.addToFile (fich_extension, cad_aux);
		
		System.out.println ("\nProgram finished!\n");	
		

	}
	
	
	// - oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
	// - oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
	
	
	 private static void Update_MissingValues (example_set E, myDataset dataset){
		 for (int cont1=0; cont1<E.n_example; cont1++){
			 for (int cont2=0; cont2<E.n_variable-1;cont2++){
				 if (dataset.isMissing (cont1, cont2)){
					 E.data[cont1][cont2] = MISSING;
				 }
			 }
		 }
		 
	 }
	
	

	
	
	// - oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
	// - oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
	
	
	private static boolean Changes (double[] m_act, double[] m_old, int cols){
		int i = 0;
		while (i<cols && m_act[i]==m_old[i])
			i++;

		return (i != cols);
	}


	
	/**
	 * <p>
	 * Selects "n_individuos" individuals from the examples
	 * </p>
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param clase int A selected class
	 * @param n_individuos in The total number of individuals
	 * @param Tabla double[][][] The adaptation table
	 * @param n_ejemplos_por_clase int[] Vector containing the number of examples per class
	 * @return int[][] The selected individuals
	 */
	
	static int[][] Find_Individuals_given_examples (vectorvar V, example_set E, int clase, int n_individuos, double[][][] Tabla, int[] n_ejemplos_por_clase){
		int a = Select_Random_Individual (E.N_Examples(), -1, 0);

		int[][] matriz = new int[n_individuos][];
		for (int i=0; i< n_individuos; i++)
			matriz[i] = new int[V.N_Antecedent()];

		for (int i=0; i<n_individuos; i++){
			// Elegir un ejemplo de la clase i%n_clases
			if (n_ejemplos_por_clase[clase] == 0){
				for (int j=0; j<V.N_Antecedent(); j++)
					matriz[i][j] = -1;
			}
			else {
				while (E.Is_Covered (a) || T_Adaptation_Variable_Label (V, Tabla, a, V.Consequent(), clase)==0){
					a++;
					a = a % E.N_Examples();
				}
				for (int j=0; j<V.N_Antecedent(); j++){
					matriz[i][j] = BetterEtiqueta (V, Tabla, j, a);
				}
			}
		}

		return matriz;
	}
		
		
	private static int Select_Random_Individual (int n, int menos_este, int eli){
		int nd = n;
		//Random rand = new Random();
		int a = Randomize.Randint (0, nd);

		while (a==menos_este || a<eli)
			a = Randomize.Randint (0, nd);

		return a;
	}
	
	
	/**
	 * <p>
	 * The Genetic Algorithm: extracts the best rule for a given class (clase)
	 * </p>
	 * @param G multipopulation Binary, integer and real information of each individual of the population
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param R ruleset Set of learned rules
	 * @param I double[][] Information measures
	 * @param clase int A selected class
	 * @param Total_ejemplos int The total number of examples
	 * @param Tabla double[][][] The adaptation table
	 * @return int Number of iterations
	 */
	
	static int GA (multipopulation G, vectorvar V, example_set E, ruleset R, double[][] I, int clase, int Total_ejemplos, double[][][] Tabla){

		double peso;
		int NFitness = G.N_Val();
		double[] valor = new double [NFitness];
		double[] valor_anterior = new double[NFitness];
		//int n = G.N_individuos ();
		int n_clases = V.SizeDomain (V.Consequent());
		int[] n_ejemplos_por_clase = new int[n_clases];
		
		int sin_cambios = 0;
		//int z;
		//double aciertos;
		Int_t evaluaciones = new Int_t (0);

		E.Examples_per_Class (V.Consequent(), 0, n_clases, n_ejemplos_por_clase);

		int[][] sujetos = Find_Individuals_given_examples (V, E, clase, G.N_individuals(), Tabla, n_ejemplos_por_clase);
		int[] lista_tamano = new int[V.N_Antecedent()];
		for (int i=0; i<V.N_Antecedent(); i++)
			lista_tamano[i] = V.SizeDomain (i);
		      
		G.InitialPopulation (I, n_clases, E.N_Examples(), clase, sujetos, V.N_Antecedent(), lista_tamano); // Revisar

		fitness (G, V, E, Tabla, clase, evaluaciones);
		G.Sort();

		for (int i=0; i<G.N_Val(); i++)
			valor_anterior[i] = G.ValueFitness (0, i);
		
		//int t = 0;

		while (sin_cambios <= Parameters.iter_no_changes){
			// Selection operator
			G.Selection();
			G.Put_NotModified();
			// Crossover operator
			G.GenerationalCrossover();
			// Uniform mutation operator
			G.UniformMutation();
			fitness (G, V, E, Tabla, clase, evaluaciones);
			G.Sort();
			
			for (int i=0; i<G.N_Val(); i++)
				valor[i] = G.ValueFitness (0, i);
			
			//t++;


			if (!Changes (valor, valor_anterior, G.N_Val()))
				sin_cambios++;
			else {
				sin_cambios = 0;
				for (int i=0; i<G.N_Val(); i++)
					valor_anterior[i] = valor[i];

			}

		}
		
		

		int[] nn1;
		Int_t n1 = new Int_t (0);

		nn1 = G.Subpopulation_Integer (0, 0, n1);
		nn1[0] = clase;
		genetcode code = new genetcode ();
		G.Code (0, code);
		peso = Goodness (G, V, E, valor, NFitness, 0, Tabla, clase);
		R.Add (code, peso);
		double[] nr2;
		Int_t r2 = new Int_t (0);

		if (G.ValueFitness (0, 0) > 0){
			nr2 = G.Subpopulation_Real (1, 0, r2);
			E.MarkClase (nr2);
		}

		return evaluaciones.value;
	}


	/**
	 * <p>
	 * The evaluation function: measures the goodness of an individual ("i") and stores the values of
	 * its fitness function in "valor"
	 * </p>
	 * @param G multipopulation Binary, integer and real information of each individual of the population
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param Tabla double[][][] The adaptation table
	 * @param clase int A selected class
	 * @param evaluaciones Int_t Number of iterations
	 */

	static void fitness (multipopulation G, vectorvar V, example_set E, double[][][] Tabla, int clase, Int_t evaluaciones){

		int n = G.N_individuals();
		int n_fitnes_componentes = G.N_Val();
		double[] valor = new double[n_fitnes_componentes];

		for (int i=0; i<n; i++){
			if (G.Modified (i)){
				Goodness (G, V, E, valor, n_fitnes_componentes, i, Tabla, clase);
				G.Assessment (i, valor);
				evaluaciones.value++;
			}
		}

	}
	
	
	private static boolean Understandable_Assignment (char[] s, int tama){
		int sec_unos = 0, sec_ceros = 0, n_ceros = 0;
	    boolean act_unos = (s[0] == '1');

	    if (act_unos)
	    	sec_unos = 1;
	    else{
	    	sec_ceros = 1;
	    	n_ceros = 1;
	    }

	    for (int i=1; i<tama; i++){
	        if (!act_unos && s[i]=='1'){
	            act_unos = true;
	            sec_unos++;
	        }
	        else{
	        	if (!act_unos && s[i]=='0'){
	        		n_ceros++;
	        	}
	        	else{
	        		if (act_unos && s[i]=='0'){
	        			sec_ceros++;
	        			n_ceros++;
	        			act_unos = false;
	        		}
	        	}
	        }	
	    }

	    return ( (sec_unos==1) || (n_ceros==1) );
	}
	
	/**
	 * <p>
	 * Calculates the values of the fitness function of "elemento_i" and returns its weight ("peso")
	 * </p>
	 * @param G multipopulation Binary, integer and real information of each individual of the population
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param valor double[] Stores the fitness information
	 * @param N_valor int Number of components of the fitness function
	 * @param elemento_i int An individual
	 * @param Tabla double[][][] The adaptation table
	 * @param clase int A selected class
	 * @return double The weight of the rule
	 */
	
	static double Goodness (multipopulation G, vectorvar V, example_set E, double[] valor, int N_valor, int elemento_i, double[][][] Tabla, int clase){

		char[] nb;
		double[] nr2;
		double[] nr1;
		int[] nn1;
		int[] nn2;
		int r;
		String regla;
		int ne = E.N_Examples();
		vectordouble w = new vectordouble (V.N_Antecedent());
		double positivos = 0, negativos = 0, positivos_peso = 0;
		int j;
		Int_t b = new Int_t (0);
		Int_t r2 = new Int_t (0);
		Int_t r1 = new Int_t (0);
		Int_t n1 = new Int_t (0);
		Int_t n2 = new Int_t (0);


		nb = G.Subpopulation_Binary (0, elemento_i, b);
		char[] s = new char[b.value];
		for (j=0; j<b.value; j++){
			s[j] = nb[j];
		}

		regla = String.valueOf (s);

		nn1=G.Subpopulation_Integer(0,elemento_i,n1);
		nn2=G.Subpopulation_Integer(1,elemento_i,n2);
		nr1=G.Subpopulation_Real(0,elemento_i,r1);
		nr2=G.Subpopulation_Real(1,elemento_i,r2);

		int pos=0,tamanio;
		valor[1]=0; 
		valor[2]=0; 
		for (j=0; j<r1.value-1; j++){
			tamanio = V.Variable(j).N_labels();

			if (nr1[j]<nr1[r1.value-1])
				valor[1]++;
			else{
				if (Understandable_Assignment (nb, tamanio))
					valor[2]++;
			}	

		   pos = pos + tamanio;
		 }

		double[] aux_p = new double[ne];
		double[] aux_n = new double[ne];
		double peso;
		Double_t kk = new Double_t (0);
		
		// Checks if it is a valid rule
		boolean esta_cubierto;
		boolean regla_valida = V.Is_Valid (regla, kk);
		double calculo_pos;

		// Needed for calculating the weight of the rule
		if (regla_valida){
			for (j=0; j<ne; j++){
				w = E.Data (j);
				T_AdaptationC (V, Tabla, j, clase , aux_p, aux_n);
				esta_cubierto = E.Is_Covered(j);
				//esta_cubierto = false;
				if (!esta_cubierto){
					nr2[j] = T_Adaptation_Antecedent (V, Tabla, j, regla, nr1, nr1[r1.value-1]);

					calculo_pos = nr2[j] * aux_p[j];
					positivos_peso = positivos_peso + (calculo_pos);
					negativos = negativos + (nr2[j]*aux_n[j]);
	
					if (calculo_pos >= E.Grade_Is_Negative_Covered (j))
						positivos = positivos + (calculo_pos);
				
					if (aux_n[j] > aux_p[j])
						nr2[j] = -nr2[j];
				}
			}
		}
		peso = (positivos_peso+1) / (positivos_peso+negativos+1);

		
		if (regla_valida){
			valor[0] = (positivos - negativos); // Primera componente fitness.
		}
		
		else {
			valor[0] = -ne;
		}

		return peso;
	}
	
	
	/**
	 * <p>
	 * Considering the set of learned rules, it returns the number of successes over the total number of examples.
	 * </p>
	 * @param R ruleset Set of learned rules
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @return double Number of successes weighted through the total number of examples
	 */
	
	static double Successes (ruleset R, vectorvar V, example_set E){

		int n = E.N_Examples();
		int clase;
		vectordouble w;
		int[] marcar = new int[n];
		int tama1 = 0;
		double[] gmarcar = new double[n];
		int[] desmarcar = new int[n];
		int tama2 = 0;
		double[] gdesmarcar = new double[n];
		double bien = 0, mal = 0, nosesabe = 0;
		int conse = V.Consequent();
		Int_t regla_disparada = new Int_t (0);
		Double_t grado = new Double_t (0);
		int[] bien_clas = new int[R.N_rule()];
		int[] mal_clas = new int[R.N_rule()];

		for (int i=0; i<R.N_rule(); i++){
			bien_clas[i] = 0;
			mal_clas[i] = 0;
		}

		for (int i=0; i<n; i++){
			w = E.Data (i);
			clase = R.InferenceC (w, grado, regla_disparada);
			if (clase == -1){
				nosesabe++;
			}
			else{
				if (clase == w.At (conse)) {
					bien++;
					marcar[tama1] = i;
					gmarcar[tama1] = grado.value;
					tama1++;
					bien_clas[regla_disparada.value]++;
				}
				else {
					mal++;
		            desmarcar[tama2] = i;
		            gdesmarcar[tama2] = grado.value;
		            tama2++;
		            mal_clas[regla_disparada.value]++;
		        }
			}	
		}

		return bien/n;

	}
	

	
	private static int Return_Different_Class (int clase){
		int clase_dist = -1;
		
		if (clase == 0)
			clase_dist = clase + 1;
		else
			clase_dist = clase - 1;
		
		return clase_dist;
	}
	
	
	/**
	 * <p>
	 * Considering the set of learned rules, it returns the number of successes over the total number of examples.
	 * </p>
	 * @param R ruleset Set of learned rules
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param out String Name of an output file
	 * @param out2 String Name of an output file
	 * @param vname String[] Names of the variables
	 * @param dataset myDataset class containing methods for reading classification datasets
	 * @return double Number of successes weighted through the total number of examples
	 */

	static double Successes (ruleset R, vectorvar V, example_set E, String out, String out2, String[] vname, myDataset dataset) throws IOException{

		int n = E.N_Examples();
		int clase;
		vectordouble w;
		int[] marcar = new int[n];
		int tama1 = 0;
		double[] gmarcar = new double[n];
		int[] desmarcar = new int[n];
		int tama2 = 0;
		double[] gdesmarcar = new double[n];
		double bien = 0, mal = 0, nosesabe = 0;
		int conse = V.Consequent();
		Int_t regla_disparada = new Int_t (0);
		Double_t grado = new Double_t (0);
		int[] bien_clas = new int[R.N_rule()];
		int[] mal_clas = new int[R.N_rule()];
		
		String cad_out2 = new String ("");
		cad_out2 = dataset.copyHeader ();

		for (int i=0; i<R.N_rule(); i++){
			bien_clas[i] = 0;
			mal_clas[i] = 0;
		}

		for (int i=0; i<n; i++){
			w = E.Data (i);
			clase = R.InferenceC (w, grado, regla_disparada);
			if (clase == -1){
				nosesabe++;
				
				clase = Return_Different_Class ((int) w.At (conse));
				cad_out2 = cad_out2 + dataset.getOutputAsString(i) + " " + vname[clase] + "\n";
			}
			else{
				if (clase == w.At (conse)) {
					bien++;
					marcar[tama1] = i;
					gmarcar[tama1] = grado.value;
					tama1++;
					bien_clas[regla_disparada.value]++;
				}
		        else {
		            mal++;
		            desmarcar[tama2] = i;
		            gdesmarcar[tama2] = grado.value;
		            tama2++;
		            mal_clas[regla_disparada.value]++;
		        }
				cad_out2 = cad_out2 + dataset.getOutputAsString(i) + " " + vname[clase] + "\n";
			}

		}
		
		Files.writeFile (out2, cad_out2);
		
		String cad_aux = new String ("");
		

		cad_aux = cad_aux + "Successes: " + bien + "\n";

		cad_aux = cad_aux + "Errors:  " + mal + "\n";
		
		cad_aux = cad_aux + "Not classified: " + nosesabe + "\n";

		for (int i=0; i<R.N_rule(); i++){
			cad_aux = cad_aux + "\tRule " + i + ": " + bien_clas[i] + " / " + mal_clas[i] + "\n";
		}
		
		Files.addToFile (out, cad_aux);

		return bien/n;

	}
	
	
	private static double[][] ReserveForInformationMeasures (vectorvar V){
		int conse = V.Consequent();
		int tam_dom_conse = V.SizeDomain (conse);
		int nv;
		int m = tam_dom_conse + 1;


		nv = V.N_Antecedent();
		

		double[][] matriz = new double[nv][];
		for (int i=0; i<nv; i++)
			matriz[i] = new double [m];

		return matriz;
	}

	

	private static void ProbabilitySimpleVariable (vectorvar V, example_set E, int variable, int n_casos, double[] p, double[][][] Tabla){
		int ne=0;
		double suma=0;
		double[] aux = new double[n_casos];

		for (int j=0; j<n_casos; j++)
			p[j] = 0;

		for (int i=0; i<E.N_Examples(); i++){
			if (!E.Is_Covered (i)){
				suma = 0;
				for (int j=0; j<n_casos; j++){
					aux[j] = T_Adaptation_Variable_Label (V, Tabla, i, variable, j);
					suma += aux[j];
				}
				
				if (suma > 0){
					for (int j=0; j<n_casos; j++)
						p[j] = p[j] + (aux[j]/suma);

					ne++;
				}
			}
		}

		if (ne > 0){
			for (int j=0; j<n_casos; j++)
				p[j] = p[j] / ne;
		}

	}


	private static void JoinedProbability2Variables (vectorvar V, example_set E, int var1, int var2, int n_casos1, int n_casos2, double[][] m, double[][][] Tabla){
		
		int ne = 0;
		double suma;
		double[][] aux = new double[n_casos1][];
		
		for (int j=0;j<n_casos1;j++)
			aux[j] = new double[n_casos2];

		for (int j=0; j<n_casos1; j++){
			for (int k=0; k<n_casos2; k++)
				m[j][k] = 0;
		}	

		for (int i=0; i<E.N_Examples(); i++){
			if (!E.Is_Covered (i)){
				suma = 0;
				for (int j=0; j<n_casos1; j++){
					for (int k=0; k<n_casos2; k++){
						aux[j][k] = T_Adaptation_Variable_Label (V,Tabla,i,var1,j) * T_Adaptation_Variable_Label (V,Tabla,i,var2,k);
						suma = suma + aux[j][k];
					}
				}	

				if (suma > 0){
					for (int j=0; j<n_casos1; j++)
						for (int k=0; k<n_casos2; k++)
							m[j][k] = m[j][k] + (aux[j][k]/suma);

					ne++;
				}
			}

		}

		if (ne > 0){
			for (int j=0; j<n_casos1; j++){
				for (int k=0; k<n_casos2; k++)
					m[j][k] = m[j][k] / ne;
			}
		}	

	}

	
	

	/**
	 * <p>
	 * Calculates the information measures
	 * </p>
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @param I double[][] Information measures
	 * @param Tabla double[][][] The adaptation table
	 */
	
	static void InformationMeasures (vectorvar V, example_set E, double[][] I, double[][][] Tabla){
		int n = V.TotalVariables();
		int conse = V.Consequent();
		double[] px;
		double[] py;
		double[][] pxy;
		int nv = V.N_Antecedent();

		py = new double [V.SizeDomain(conse)];
		ProbabilitySimpleVariable (V, E, conse, V.SizeDomain (conse), py, Tabla);

		int i = 0, j = 0;
		double I1 = 0, H1 = 0, I2 = 0, H2 = 0, aux;
		
		while (i < nv){
			if (V.IsActive (j) && V.IsAntecedent (j)) {
				px = new double [V.SizeDomain (j)];
				pxy = new double[V.SizeDomain (j)][];
				
				for (int k=0; k<V.SizeDomain (j); k++)
					pxy[k] = new double[V.SizeDomain (conse)];
				
				ProbabilitySimpleVariable (V, E, j, V.SizeDomain (j), px, Tabla); 
				JoinedProbability2Variables (V, E, j, conse, V.SizeDomain (j), V.SizeDomain (conse), pxy, Tabla);
				I1 = 0; H1 = 0;
				
				for (int k=0; k<V.SizeDomain (conse); k++){
					I2 = 0; H2 = 0;
					for (int q=0; q<V.SizeDomain (j); q++){
						if (pxy[q][k] == 0)
							aux = 0;
						else
							aux = pxy[q][k] * Math.log ((px[q]*py[k])/pxy[q][k]);

						I2 = I2 - aux;
						I1 = I1 - aux;

						if (pxy[q][k] == 0)
							aux = 0;
						else
							aux = pxy[q][k] * Math.log (pxy[q][k]);

						H2 = H2 - aux;
						H1 = H1 - aux;
					}

					if  (H2 == 0) 
						I[i][k+1] = 0;
					else
						I[i][k+1] = (I2 / H2);
				}
				if (H1 == 0)
					I[i][0] = 0;
				else
					I[i][0] = (I1 / H1);

				i++;
			}
			j++;
		}

	}


	/**
	 * <p>
	 * It calculates the adaptation degree of each example (E) to each label
	 * of each variable (V)
	 * </p>
	 * @param V vectorvar Information regarding the variables
	 * @param E example_set The set of examples
	 * @preturn double[][][] The table with the degree of adaptation
	 */

	static double[][][] Create_Adaptation_Table (vectorvar V, example_set E){
		int n = V.TotalVariables();
		int m = E.N_Examples();
		int l;
		double valor;

		// Memory allocation
		double[][][] Tabla = new double[n][][];
		for (int i=0; i<n; i++){
			l = V.SizeDomain (i);
			Tabla[i] = new double [l][];
			for (int j=0; j<l; j++)
				Tabla[i][j] = new double[m];
		}

		// Calculating the adaptation...
		for (int e=0; e<m; e++){
			for (int v=0; v<n; v++){
				valor = E.Data (e, v);
				l = V.SizeDomain (v);
				for (int j=0; j<l; j++)
					Tabla[v][j][e] = V.Adaptation (valor, v, j);
			}
		}

		return Tabla;
	}


	private static double T_Adaptation_Variable_Label (vectorvar V, double[][][] Tabla, int ejemplo, int variable, int etiqueta){
		return Tabla[variable][etiqueta][ejemplo];
	}


	private static double T_Adaptation_Variable (vectorvar V, double[][][] Tabla, int ejemplo, int variable, String labels){
		int l = V.SizeDomain (variable);
		double max = 0;
		char[] labels_aux = labels.toCharArray();
				
		
		for (int etiqueta=0; etiqueta<l && max<1; etiqueta++){
			if ( labels_aux[etiqueta]=='1' && Tabla[variable][etiqueta][ejemplo]>max)
				max = Tabla[variable][etiqueta][ejemplo];
		}	
		return max;
	}


	private static double T_Adaptation_Antecedent (vectorvar V, double[][][] Tabla, int ejemplo, String regla, double[] nivel_variable, double umbral){
		int n = V.N_Antecedent();
		double max = 1, aux;
		String sub;
		int trozo = 0, tam;
		char[] cad_aux = regla.toCharArray();
		
		for (int v=0; v<n && max>0; v++){
			tam = V.SizeDomain (v);
			if (nivel_variable[v] >= umbral){
				sub = String.valueOf (cad_aux, trozo, tam);
				aux = T_Adaptation_Variable (V, Tabla, ejemplo, v, sub);
				if (aux < max)
					max = aux;
			}
			trozo += tam;
		}
		return max;
	}



	private static void T_AdaptationC (vectorvar V, double[][][] Tabla, int ejemplo, int etiq, double[] pos, double[] neg){
		int conse = V.Consequent();
		pos[ejemplo] = Tabla[conse][etiq][ejemplo];
		if (pos[ejemplo] == 1)
			neg[ejemplo] = 0;
		else
			neg[ejemplo] = 1;
	}


	private static int BetterEtiqueta (vectorvar V, double[][][] Tabla, int variable, int ejemplo){

		int l = V.SizeDomain (variable), et = 0;
		double max = 0;

		for (int etiqueta=0; etiqueta<l && max<1; etiqueta++){
			if (Tabla[variable][etiqueta][ejemplo] > max){
				max = Tabla[variable][etiqueta][ejemplo];
				et = etiqueta;
			}
		}
		return et;
	}
	
	
	
	
	
	
	
	
	
	
	
}
