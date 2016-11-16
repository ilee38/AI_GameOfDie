package dicegame;

import java.io.*;
import java.util.*;

public class DiceGame implements Runnable {
	private Scanner in;
	private PrintStream out;
	public static final String[] SWITCHER = { "zero", "one" };
	public static final String[] EVIDENCE = { "zero", "one" };
	private Random random;
	private int numGames; // number of games to play
	private int seed; // used for random number seed
	private int die1, die2; // the value of the dice for a game
	private int evidence1, evidence2, evidence3; // evidence of dice's value
	private int guess1, guess2; // solver's guess for the dice

	public DiceGame(Scanner in, PrintStream out) {
		this.in = in;
		this.out = out;
		// A different seed will be used to test your lab.
		this.seed = 13;
	}

	public void run() {
		// first line is the number of games to play
		if (in.hasNextInt()) {
			numGames = in.nextInt();
			in.nextLine(); // need to scan newline
		} else {
			out.printf("quit\n");
			return;
		}
		double totalTries = 0;
		for (int game = 1; game <= numGames; game++) {
			random = new Random(seed + game);
			die1 = 1 + random.nextInt(6);
			die2 = 1 + random.nextInt(6);
			int tries = 0;
			do {
				// probability evidence1 = 1 is (die1 + die2 - 2) / 10
				int r = random.nextInt(10);
				evidence1 = (die1 + die2 - 2 > r) ? 1 : 0;
				// probability evidence2 = 1 is 0 if die1 > die2, 1 if die1 < die2, else 1/2
				r = random.nextInt(2);
				evidence2 = (die1 > die2) ? 0 : (die1 < die2) ? 1 : r;
				// probability evidence3 = 1 is 0 if both dice are even, 1 if both are odd, else 1/2
				r = random.nextInt(2);
				evidence3 = (r == 0) ? die1 % 2 : die2 % 2;
				// flip a coin to switch the bits to have more fun
				out.printf("%d %d %d\n", evidence1, evidence2, evidence3);
				// input guess from solver
				guess1 = guess2 = 0;
				if (in.hasNextInt()) {
					guess1 = in.nextInt();
					if (in.hasNextInt()) {
						guess2 = in.nextInt();
						in.nextLine(); // need to scan newline
					}
				} 
				if (guess1 == 0 || guess2 == 0) {
					out.printf("quit bad line from solver\n");
					return;
				}
				tries++;
				totalTries++;
				if (guess1 == die1 && guess2 == die2) {
					out.printf("right in %d tries, after %d games average = %f\n", 
							tries, game, totalTries / game );
				} else {
					out.printf("wrong try again\n");
				}
			} while (guess1 != die1 || guess2 != die2);
		} out.printf("quit with average = %f\n",totalTries/numGames);

	}

	// Let the user play a random game.
	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);
		DiceGame dg = new DiceGame(stdin, System.out);
		dg.seed = new java.security.SecureRandom().nextInt();
		dg.run();
	}

}
