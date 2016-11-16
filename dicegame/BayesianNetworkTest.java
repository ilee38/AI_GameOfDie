package dicegame;
import java.util.Arrays;

public class BayesianNetworkTest {
    public static final String[] TRUTH_VALUES = {"false", "true"}; 
    
    public static void main(String[] args) {
        BayesianNetwork bn = exampleSixTen();
        exampleSixThirteen(bn);
    }

    public static BayesianNetwork exampleSixTen() {
     //Create Bayesian network and variables (with name and truth vals   
    	BayesianNetwork bn = new BayesianNetwork();
        Variable tampering = new Variable("Tampering", TRUTH_VALUES);
        Variable fire = new Variable("Fire", TRUTH_VALUES);
        Variable alarm = new Variable("Alarm", TRUTH_VALUES);
        Variable smoke = new Variable("Smoke", TRUTH_VALUES);
        Variable leaving = new Variable("Leaving", TRUTH_VALUES);
        Variable report = new Variable("Report", TRUTH_VALUES);
  
    //Add variables to network
        bn.addVariable(tampering);
        bn.addVariable(fire);
        bn.addVariable(alarm);
        bn.addVariable(smoke);
        bn.addVariable(leaving);
        bn.addVariable(report);
  
    //Create factors
        // P(tampering) = 0.02
        Factor f1 = new Factor(tampering);
        f1.set(0.98, 0);
        f1.set(0.02, 1);
        
        // P(fire) = 0.01
        Factor f2 = new Factor(fire);
        f2.set(0.99, 0);
        f2.set(0.01, 1);
        
        // P(alarm | fire & tampering) = 0.5
        // P(alarm | fire & !tampering) = 0.99
        // P(alarm | !fire & tampering) = 0.85
        // P(alarm | !fire & !tampering) = 0.0001
        Factor f3 = new Factor(tampering, fire, alarm);
        f3.set(0.9999, 0, 0, 0);
        f3.set(0.0001, 0, 0, 1);
        f3.set(0.01, 0, 1, 0);
        f3.set(0.99, 0, 1, 1);
        f3.set(0.15, 1, 0, 0);
        f3.set(0.85, 1, 0, 1);
        f3.set(0.5, 1, 1, 0);
        f3.set(0.5, 1, 1, 1);
        
       
        // P(smoke | fire ) = 0.9
        // P(smoke | ~fire ) = 0.01
        Factor f4 = new Factor(fire, smoke);
        f4.set(0.99, 0, 0);
        f4.set(0.01, 0, 1);
        f4.set(0.1, 1, 0);
        f4.set(0.9, 1, 1);
        
        // P(leaving | alarm) = 0.88
        // P(leaving | ~alarm ) = 0.001
        Factor f5 = new Factor(alarm, leaving);
        f5.set(0.999, 0, 0);
        f5.set(0.001, 0, 1);
        f5.set(0.12, 1, 0);
        f5.set(0.88, 1, 1);
        
        // P(report | leaving ) = 0.75
        // P(report | ~leaving ) = 0.01 
        Factor f6 = new Factor(leaving, report);
        f6.set(0.99, 0, 0);
        f6.set(0.01, 0, 1);
        f6.set(0.25, 1, 0);
        f6.set(0.75, 1, 1);
        
    //Add factors to network   
        bn.addFactor(f1);
        bn.addFactor(f2);
        bn.addFactor(f3);
        bn.addFactor(f4);
        bn.addFactor(f5);
        bn.addFactor(f6);  
        
        return bn;
    }
    
    public static void exampleSixThirteen(BayesianNetwork bn) {
        System.out.println("Prior probabilities\n");
        // P(tampering) = 0.02
        // P(fire) = 0.01
        // P(report) = 0.028
        // P(smoke) = 0.0189 
        Factor result = bn.eliminateVariables(bn.findVariable("Tampering"));
        printResult(result, "");
        result = bn.eliminateVariables(bn.findVariable("Fire"));
        printResult(result, "");
        result = bn.eliminateVariables(bn.findVariable("Report"));
        printResult(result, "");
        result = bn.eliminateVariables(bn.findVariable("Smoke"));
        printResult(result, "");
        
        System.out.println("Probabilities given Report=true\n");
        // P(tampering | report) = 0.399
        // P(fire | report)= 0.2305
        // P(smoke | report) = 0.215 
        // need to make copy because observe replaces a factor
        BayesianNetwork copybn = bn.copy();
        copybn.observe(bn.findVariable("Report"), "true");
        result = copybn.eliminateVariables(bn.findVariable("Tampering"));
        printResult(result, " | Report=true");
        result = copybn.eliminateVariables(bn.findVariable("Fire"));
        printResult(result, " | Report=true");
        result = copybn.eliminateVariables(bn.findVariable("Smoke"));
        printResult(result, " | Report=true");
        
        System.out.println("Probabilities given Smoke=true\n");
        // P(tampering | smoke) = 0.02
        // P(fire | smoke) = 0.476
        // P(report | smoke) = 0.320 
        copybn = bn.copy();
        copybn.observe(bn.findVariable("Smoke"), "true");
        result = copybn.eliminateVariables(bn.findVariable("Tampering"));
        printResult(result, " | Smoke=true");
        result = copybn.eliminateVariables(bn.findVariable("Fire"));
        printResult(result, " | Smoke=true");
        result = copybn.eliminateVariables(bn.findVariable("Report"));
        printResult(result, " | Smoke=true");
        
        System.out.println("Probabilities given Report=true and Smoke=true\n");
        // P(tampering | report & smoke) = 0.0284
        // P(fire | report & smoke) = 0.964 
        copybn = bn.copy();
        copybn.observe(bn.findVariable("Report"), "true");
        copybn.observe(bn.findVariable("Smoke"), "true");
        result = copybn.eliminateVariables(bn.findVariable("Tampering"));
        printResult(result, " | Report=true & Smoke=true");
        result = copybn.eliminateVariables(bn.findVariable("Fire"));
        printResult(result, " | Report=true & Smoke=true");
        
        System.out.println("Probabilities given Report=true and Smoke=false\n");
        // P(tampering | report & !smoke) = 0.501
        // P(fire | report & !smoke) = 0.0294
        copybn = bn.copy();
        copybn.observe(bn.findVariable("Report"), "true");
        copybn.observe(bn.findVariable("Smoke"), "false");
        result = copybn.eliminateVariables(bn.findVariable("Tampering"));
        printResult(result, " | Report=true & Smoke=false");
        result = copybn.eliminateVariables(bn.findVariable("Fire"));
        printResult(result, " | Report=true & Smoke=false");   
    }
    
    // This assumes the factor has just one variable
    public static void printResult(Factor factor, String evidenceString) {
    	// get the variable
    	Variable variable = factor.getVariables()[0];
    	// get name and values
    	String name = variable.getName();
    	String[] values = variable.getValues();
    	// print heading
    	System.out.printf("Variable %s: %s\n", name, Arrays.toString(values));
    	// print probability of each value
    	for (int i = 0; i < values.length; i++) {
    		System.out.printf("P(%s=%s%s) = %s\n", 
    				name, values[i], evidenceString, factor.get(i));
    	}
    	System.out.printf("\n");
    }
}
