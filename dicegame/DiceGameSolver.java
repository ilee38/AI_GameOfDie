package dicegame;
import java.io.*;
import java.util.*;

public class DiceGameSolver implements Runnable {
	private Scanner in;
	private PrintStream out;
	public static final String[] DIE_VALUES = { "one", "two", "three", "four", "five", "six"};
	public static final String[] EVIDENCE_VALUES = {"0", "1"};	//possible truth values for the evidence bits
	private int numGames; // number of games to play

	public DiceGameSolver(Scanner in, PrintStream out) {
		this.in = in;
		this.out = out;
		numGames = 1000;  // Change to 1000 for final version
	}

	public void run() {
		// This specifies how many games to play.
		// Change to 1000 for final version.
		out.printf("%d\n", numGames);

		for (int game = 1; game <= numGames; game++) {
			String line = null;
			int evidence1 = 0, evidence2 = 0, evidence3 = 0;
			List<String> guessList = new ArrayList<String>();  // remember previous guesses
			List<Integer> evidenceList = new ArrayList<Integer>();  // remember evidence
			BayesianNetwork bn = new BayesianNetwork();
			
			Variable die1 = new Variable("Die1", DIE_VALUES);
			bn.addVariable(die1);
	        Factor dieFactor1 = new Factor(die1);
	        bn.addFactor(dieFactor1);
	        
	        dieFactor1.set(1.0 / 6.0, 0);
	        dieFactor1.set(1.0 / 6.0, 1);
	        dieFactor1.set(1.0 / 6.0, 2);
	        dieFactor1.set(1.0 / 6.0, 3);
	        dieFactor1.set(1.0 / 6.0, 4);
	        dieFactor1.set(1.0 / 6.0, 5);
	        
	        Variable die2 = new Variable("Die2", DIE_VALUES);
			bn.addVariable(die2);
	        Factor dieFactor2 = new Factor(die2);
	        bn.addFactor(dieFactor2);
	        
	        dieFactor2.set(1.0 / 6.0, 0);
	        dieFactor2.set(1.0 / 6.0, 1);
	        dieFactor2.set(1.0 / 6.0, 2);
	        dieFactor2.set(1.0 / 6.0, 3);
	        dieFactor2.set(1.0 / 6.0, 4);
	        dieFactor2.set(1.0 / 6.0, 5);
	        
			do {
				// Expecting three ints on this line.
				line = in.nextLine();
				if (line.contains("quit"))
					break;
				Scanner scan = new Scanner(line);
				if (scan.hasNextInt()) {
					evidence1 = scan.nextInt();
				} else {
					out.printf("quit\n");
					scan.close();
					return;
				}
				if (scan.hasNextInt()) {
					evidence2 = scan.nextInt();
				} else {
					out.printf("quit\n");
					scan.close();
					return;
				}
				if (scan.hasNextInt()) {
					evidence3 = scan.nextInt();
				} else {
					out.printf("quit\n");
					scan.close();
					return;
				}
				scan.close();
				
				// Add evidence to evidenceList
				evidenceList.add(evidence1);
				evidenceList.add(evidence2);
				evidenceList.add(evidence3);
				
				// run Bayesian network to get next guess
				int[] guesses = diceNetwork(evidenceList, guessList, bn, die1, die2);
				int guess1 = guesses[0];
				int guess2 = guesses[1];

				// remember guess and print it out
				guessList.add("" + guess1 + guess2);
				out.printf("%d %d\n", guess1, guess2);

				// Expecting a string on this line (right or wrong)
				line = in.nextLine();
				if (line.contains("quit")) {
					break;
				} 
			} while (line.contains("wrong"));
		}

		out.print("quit\n");
	}
	
	// This returns the next guess.
	// evidenceList is all the evidence bits that have been read
	// guessList indicates previous guesses.
	private int[] diceNetwork(List<Integer> evidenceList, List<String> guessList, BayesianNetwork bn, Variable die1, Variable die2) {
		
        // You need to write the code to completely define the Bayesian network.
        // For example, if evidenceList.size() is n, then you will need n
        // evidence Variables and n Factors.
        // You will also need to call bn.observe n times to set the values
        // of the evidence variables.
        //
        // See BayesianNetworkTest.java for examples.
        
        createNewVarsandFacts(evidenceList, bn, die1, die2);
        observeEvidence(evidenceList, bn);
       
        Factor result1 = bn.eliminateVariables(die1);
        Factor result2 = bn.eliminateVariables(die2);
        
        // result1.get(i-1) should be P(die1 = i | evidence)
        int guess1 = -1; // need to add one later
        int guess2 = -1; // need to add one later
        for (int i = 1; i <= 6; i++) {
        	for (int j = 1; j <= 6; j++) {
        		if (! guessList.contains("" + i + j)) {
        			if (guess1 == -1 ||
        				result1.get(i-1) * result2.get(j-1) > 
        				result1.get(guess1-1) * result2.get(guess2-1)) {
        				guess1 = i;
        				guess2 = j;
        			}
        		}
        	}
        }
        	
        int[] guesses = new int[2];
        guesses[0] = guess1;
        guesses[1] = guess2;
		return guesses;
	}
	
	// Creates new variables and factors for each additional evidence bit
	// on the evidenceList. Each new variable gets its own name.
	private void  createNewVarsandFacts(List<Integer >evidenceList, BayesianNetwork bn, Variable die1, Variable die2){
		int round = evidenceList.size() / 3;
		int j = 1;
		String newVarName = getName(j, round);
		bn.addVariable(new Variable(newVarName, EVIDENCE_VALUES));
		Factor newFactorOne = new Factor(die2, die1, bn.findVariable(newVarName));
		bn.addFactor(newFactorOne);
		setFactorOne(newFactorOne);
		j++;
		newVarName = getName(j, round);
		bn.addVariable(new Variable(newVarName, EVIDENCE_VALUES));
		Factor newFactorTwo = new Factor(die2, die1, bn.findVariable(newVarName));
		bn.addFactor(newFactorTwo);
		setFactorTwo(newFactorTwo);
		j++;
		newVarName = getName(j, round);
		bn.addVariable(new Variable(newVarName, EVIDENCE_VALUES));
		Factor newFactorThree = new Factor(die2, die1, bn.findVariable(newVarName));
		bn.addFactor(newFactorThree);
		setFactorThree(newFactorThree);
	}
	
	//Tells the Bayesian network the evidence that has been given
	//i.e. sets the value of a variable to the given evidence provided of that variable
	private void observeEvidence(List<Integer>evidenceList, BayesianNetwork bn){
		Integer round = 1, listIndex = 0;
		for(int i = 0; i < evidenceList.size(); i+=3){
			int j = 1;
			String newVarName = getName(j, round);
			bn.observe(bn.findVariable(newVarName), evidenceList.get(listIndex).toString());
			listIndex++;
			j++;
			newVarName = getName(j, round);
			bn.observe(bn.findVariable(newVarName), evidenceList.get(listIndex).toString());
			listIndex++;
			j++;
			newVarName = getName(j, round);
			bn.observe(bn.findVariable(newVarName), evidenceList.get(listIndex).toString());
			listIndex++;
			round++;
		}
	}
	
	//Returns a new evidence variable name
	private String getName(int eNum, Integer round){
		return "Evidence" + eNum + round;
	}
	
	//Sets the factor tables for the evidence bits. Uses the probability
	//tables given in the lab description
	private void setFactorOne(Factor evidenceFactor1){
		evidenceFactor1.set(1.0, 0, 0, 0);
        evidenceFactor1.set(0.0, 0, 0, 1);
        evidenceFactor1.set(0.9, 1, 0, 0);
        evidenceFactor1.set(0.1, 1, 0, 1);
        evidenceFactor1.set(0.8, 2, 0, 0);
        evidenceFactor1.set(0.2, 2, 0, 1);
        evidenceFactor1.set(0.7, 3, 0, 0);
        evidenceFactor1.set(0.3, 3, 0, 1);
        evidenceFactor1.set(0.6, 4, 0, 0);
        evidenceFactor1.set(0.4, 4, 0, 1);
        evidenceFactor1.set(0.5, 5, 0, 0);
        evidenceFactor1.set(0.5, 5, 0, 1);
        evidenceFactor1.set(0.9, 0, 1, 0);
        evidenceFactor1.set(0.1, 0, 1, 1);
        evidenceFactor1.set(0.8, 1, 1, 0);
        evidenceFactor1.set(0.2, 1, 1, 1);
        evidenceFactor1.set(0.7, 2, 1, 0);
        evidenceFactor1.set(0.3, 2, 1, 1);
        evidenceFactor1.set(0.6, 3, 1, 0);
        evidenceFactor1.set(0.4, 3, 1, 1);
        evidenceFactor1.set(0.5, 4, 1, 0);
        evidenceFactor1.set(0.5, 4, 1, 1);
        evidenceFactor1.set(0.4, 5, 1, 0);
        evidenceFactor1.set(0.6, 5, 1, 1);
        evidenceFactor1.set(0.8, 0, 2, 0);
        evidenceFactor1.set(0.2, 0, 2, 1);
        evidenceFactor1.set(0.7, 1, 2, 0);
        evidenceFactor1.set(0.3, 1, 2, 1);
        evidenceFactor1.set(0.6, 2, 2, 0);
        evidenceFactor1.set(0.4, 2, 2, 1);
        evidenceFactor1.set(0.5, 3, 2, 0);
        evidenceFactor1.set(0.5, 3, 2, 1);
        evidenceFactor1.set(0.4, 4, 2, 0);
        evidenceFactor1.set(0.6, 4, 2, 1);
        evidenceFactor1.set(0.3, 5, 2, 0);
        evidenceFactor1.set(0.7, 5, 2, 1);
        evidenceFactor1.set(0.7, 0, 3, 0);
        evidenceFactor1.set(0.3, 0, 3, 1);
        evidenceFactor1.set(0.6, 1, 3, 0);
        evidenceFactor1.set(0.4, 1, 3, 1);
        evidenceFactor1.set(0.5, 2, 3, 0);
        evidenceFactor1.set(0.5, 2, 3, 1);
        evidenceFactor1.set(0.4, 3, 3, 0);
        evidenceFactor1.set(0.6, 3, 3, 1);
        evidenceFactor1.set(0.3, 4, 3, 0);
        evidenceFactor1.set(0.7, 4, 3, 1);
        evidenceFactor1.set(0.2, 5, 3, 0);
        evidenceFactor1.set(0.8, 5, 3, 1);
        evidenceFactor1.set(0.6, 0, 4, 0);
        evidenceFactor1.set(0.4, 0, 4, 1);
        evidenceFactor1.set(0.5, 1, 4, 0);
        evidenceFactor1.set(0.5, 1, 4, 1);
        evidenceFactor1.set(0.4, 2, 4, 0);
        evidenceFactor1.set(0.6, 2, 4, 1);
        evidenceFactor1.set(0.3, 3, 4, 0);
        evidenceFactor1.set(0.7, 3, 4, 1);
        evidenceFactor1.set(0.2, 4, 4, 0);
        evidenceFactor1.set(0.8, 4, 4, 1);
        evidenceFactor1.set(0.1, 5, 4, 0);
        evidenceFactor1.set(0.9, 5, 4, 1);
        evidenceFactor1.set(0.5, 0, 5, 0);
        evidenceFactor1.set(0.5, 0, 5, 1);
        evidenceFactor1.set(0.4, 1, 5, 0);
        evidenceFactor1.set(0.6, 1, 5, 1);
        evidenceFactor1.set(0.3, 2, 5, 0);
        evidenceFactor1.set(0.7, 2, 5, 1);
        evidenceFactor1.set(0.2, 3, 5, 0);
        evidenceFactor1.set(0.8, 3, 5, 1);
        evidenceFactor1.set(0.1, 4, 5, 0);
        evidenceFactor1.set(0.9, 4, 5, 1);
        evidenceFactor1.set(0.0, 5, 5, 0);
        evidenceFactor1.set(1.0, 5, 5, 1);
	}
	
	//Sets the factor values for evidence bit 2
	private void setFactorTwo(Factor evidenceFactor2){
		evidenceFactor2.set(0.5, 0, 0, 0);
        evidenceFactor2.set(0.5, 0, 0, 1);
        evidenceFactor2.set(0.0, 1, 0, 0);
        evidenceFactor2.set(1.0, 1, 0, 1);
        evidenceFactor2.set(0.0, 2, 0, 0);
        evidenceFactor2.set(1.0, 2, 0, 1);
        evidenceFactor2.set(0.0, 3, 0, 0);
        evidenceFactor2.set(1.0, 3, 0, 1);
        evidenceFactor2.set(0.0, 4, 0, 0);
        evidenceFactor2.set(1.0, 4, 0, 1);
        evidenceFactor2.set(0.0, 5, 0, 0);
        evidenceFactor2.set(1.0, 5, 0, 1);
        evidenceFactor2.set(1.0, 0, 1, 0);
        evidenceFactor2.set(0.0, 0, 1, 1);
        evidenceFactor2.set(0.5, 1, 1, 0);
        evidenceFactor2.set(0.5, 1, 1, 1);
        evidenceFactor2.set(0.0, 2, 1, 0);
        evidenceFactor2.set(1.0, 2, 1, 1);
        evidenceFactor2.set(0.0, 3, 1, 0);
        evidenceFactor2.set(1.0, 3, 1, 1);
        evidenceFactor2.set(0.0, 4, 1, 0);
        evidenceFactor2.set(1.0, 4, 1, 1);
        evidenceFactor2.set(0.0, 5, 1, 0);
        evidenceFactor2.set(1.0, 5, 1, 1);
        evidenceFactor2.set(1.0, 0, 2, 0);
        evidenceFactor2.set(0.0, 0, 2, 1);
        evidenceFactor2.set(1.0, 1, 2, 0);
        evidenceFactor2.set(0.0, 1, 2, 1);
        evidenceFactor2.set(0.5, 2, 2, 0);
        evidenceFactor2.set(0.5, 2, 2, 1);
        evidenceFactor2.set(0.0, 3, 2, 0);
        evidenceFactor2.set(1.0, 3, 2, 1);
        evidenceFactor2.set(0.0, 4, 2, 0);
        evidenceFactor2.set(1.0, 4, 2, 1);
        evidenceFactor2.set(0.0, 5, 2, 0);
        evidenceFactor2.set(1.0, 5, 2, 1);
        evidenceFactor2.set(1.0, 0, 3, 0);
        evidenceFactor2.set(0.0, 0, 3, 1);
        evidenceFactor2.set(1.0, 1, 3, 0);
        evidenceFactor2.set(0.0, 1, 3, 1);
        evidenceFactor2.set(1.0, 2, 3, 0);
        evidenceFactor2.set(0.0, 2, 3, 1);
        evidenceFactor2.set(0.5, 3, 3, 0);
        evidenceFactor2.set(0.5, 3, 3, 1);
        evidenceFactor2.set(0.0, 4, 3, 0);
        evidenceFactor2.set(1.0, 4, 3, 1);
        evidenceFactor2.set(0.0, 5, 3, 0);
        evidenceFactor2.set(1.0, 5, 3, 1);
        evidenceFactor2.set(1.0, 0, 4, 0);
        evidenceFactor2.set(0.0, 0, 4, 1);
        evidenceFactor2.set(1.0, 1, 4, 0);
        evidenceFactor2.set(0.0, 1, 4, 1);
        evidenceFactor2.set(1.0, 2, 4, 0);
        evidenceFactor2.set(0.0, 2, 4, 1);
        evidenceFactor2.set(1.0, 3, 4, 0);
        evidenceFactor2.set(0.0, 3, 4, 1);
        evidenceFactor2.set(0.5, 4, 4, 0);
        evidenceFactor2.set(0.5, 4, 4, 1);
        evidenceFactor2.set(0.0, 5, 4, 0);
        evidenceFactor2.set(1.0, 5, 4, 1);
        evidenceFactor2.set(1.0, 0, 5, 0);
        evidenceFactor2.set(0.0, 0, 5, 1);
        evidenceFactor2.set(1.0, 1, 5, 0);
        evidenceFactor2.set(0.0, 1, 5, 1);
        evidenceFactor2.set(1.0, 2, 5, 0);
        evidenceFactor2.set(0.0, 2, 5, 1);
        evidenceFactor2.set(1.0, 3, 5, 0);
        evidenceFactor2.set(0.0, 3, 5, 1);
        evidenceFactor2.set(1.0, 4, 5, 0);
        evidenceFactor2.set(0.0, 4, 5, 1);
        evidenceFactor2.set(0.5, 5, 5, 0);
        evidenceFactor2.set(0.5, 5, 5, 1);
	}
	
	//Sets the factor values for evidence bit 3
	private void setFactorThree(Factor evidenceFactor3){
		evidenceFactor3.set(0.0, 0, 0, 0);
        evidenceFactor3.set(1.0, 0, 0, 1);
        evidenceFactor3.set(0.5, 1, 0, 0);
        evidenceFactor3.set(0.5, 1, 0, 1);
        evidenceFactor3.set(0.0, 2, 0, 0);
        evidenceFactor3.set(1.0, 2, 0, 1);
        evidenceFactor3.set(0.5, 3, 0, 0);
        evidenceFactor3.set(0.5, 3, 0, 1);
        evidenceFactor3.set(0.0, 4, 0, 0);
        evidenceFactor3.set(1.0, 4, 0, 1);
        evidenceFactor3.set(0.5, 5, 0, 0);
        evidenceFactor3.set(0.5, 5, 0, 1);
        evidenceFactor3.set(0.5, 0, 1, 0);
        evidenceFactor3.set(0.5, 0, 1, 1);
        evidenceFactor3.set(1.0, 1, 1, 0);
        evidenceFactor3.set(0.0, 1, 1, 1);
        evidenceFactor3.set(0.5, 2, 1, 0);
        evidenceFactor3.set(0.5, 2, 1, 1);
        evidenceFactor3.set(1.0, 3, 1, 0);
        evidenceFactor3.set(0.0, 3, 1, 1);
        evidenceFactor3.set(0.5, 4, 1, 0);
        evidenceFactor3.set(0.5, 4, 1, 1);
        evidenceFactor3.set(1.0, 5, 1, 0);
        evidenceFactor3.set(0.0, 5, 1, 1);
        evidenceFactor3.set(0.0, 0, 2, 0);
        evidenceFactor3.set(1.0, 0, 2, 1);
        evidenceFactor3.set(0.5, 1, 2, 0);
        evidenceFactor3.set(0.5, 1, 2, 1);
        evidenceFactor3.set(0.0, 2, 2, 0);
        evidenceFactor3.set(1.0, 2, 2, 1);
        evidenceFactor3.set(0.5, 3, 2, 0);
        evidenceFactor3.set(0.5, 3, 2, 1);
        evidenceFactor3.set(0.0, 4, 2, 0);
        evidenceFactor3.set(1.0, 4, 2, 1);
        evidenceFactor3.set(0.5, 5, 2, 0);
        evidenceFactor3.set(0.5, 5, 2, 1);
        evidenceFactor3.set(0.5, 0, 3, 0);
        evidenceFactor3.set(0.5, 0, 3, 1);
        evidenceFactor3.set(1.0, 1, 3, 0);
        evidenceFactor3.set(0.0, 1, 3, 1);
        evidenceFactor3.set(0.5, 2, 3, 0);
        evidenceFactor3.set(0.5, 2, 3, 1);
        evidenceFactor3.set(1.0, 3, 3, 0);
        evidenceFactor3.set(0.0, 3, 3, 1);
        evidenceFactor3.set(0.5, 4, 3, 0);
        evidenceFactor3.set(0.5, 4, 3, 1);
        evidenceFactor3.set(1.0, 5, 3, 0);
        evidenceFactor3.set(0.0, 5, 3, 1);
        evidenceFactor3.set(0.0, 0, 4, 0);
        evidenceFactor3.set(1.0, 0, 4, 1);
        evidenceFactor3.set(0.5, 1, 4, 0);
        evidenceFactor3.set(0.5, 1, 4, 1);
        evidenceFactor3.set(0.0, 2, 4, 0);
        evidenceFactor3.set(1.0, 2, 4, 1);
        evidenceFactor3.set(0.5, 3, 4, 0);
        evidenceFactor3.set(0.5, 3, 4, 1);
        evidenceFactor3.set(0.0, 4, 4, 0);
        evidenceFactor3.set(1.0, 4, 4, 1);
        evidenceFactor3.set(0.5, 5, 4, 0);
        evidenceFactor3.set(0.5, 5, 4, 1);
        evidenceFactor3.set(0.5, 0, 5, 0);
        evidenceFactor3.set(0.5, 0, 5, 1);
        evidenceFactor3.set(1.0, 1, 5, 0);
        evidenceFactor3.set(0.0, 1, 5, 1);
        evidenceFactor3.set(0.5, 2, 5, 0);
        evidenceFactor3.set(0.5, 2, 5, 1);
        evidenceFactor3.set(1.0, 3, 5, 0);
        evidenceFactor3.set(0.0, 3, 5, 1);
        evidenceFactor3.set(0.5, 4, 5, 0);
        evidenceFactor3.set(0.5, 4, 5, 1);
        evidenceFactor3.set(1.0, 5, 5, 0);
        evidenceFactor3.set(0.0, 5, 5, 1);
	}
}
