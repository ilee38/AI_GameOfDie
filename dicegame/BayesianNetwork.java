package dicegame;
import java.util.*;

// an inefficient implementation of Bayesian networks
// using variables with a finite set of possible values
public class BayesianNetwork {
    ArrayList<Factor> factors; // the factors of the Bayesian network
    ArrayList<Variable> vars; // the variables in the Bayesian network

    // Creates an empty Bayesian network.
    public BayesianNetwork() {
        factors = new ArrayList<Factor>();
        vars = new ArrayList<Variable>();
    }

    // Returns a shallow copy of this network.
    public BayesianNetwork copy() {
        BayesianNetwork newBN = new BayesianNetwork();
        for (Variable v : vars)
            newBN.addVariable(v);
        for (Factor f : factors)
            newBN.addFactor(f);
        return newBN;
    }

    // Adds a Variable to this network.
    public void addVariable(Variable v) {
        String name = v.getName();
        for (Variable var : vars) {
            if (name.equals(var.getName())) {
                throw new RuntimeException("Duplicate variables named " + name);
            }
        }
        vars.add(v);
    }

    // Returns the variable with the given name.
    public Variable findVariable(String varName) {
        for (Variable var : vars) {
            if (varName.equals(var.getName())) {
                return var;
            }
        }
        throw new RuntimeException("Variable " + varName + " not found");
    }

    // Adds the factor to the network.
    public void addFactor(Factor f) {
        Variable[] fvars = f.getVariables();
        for (Variable v : fvars) {
            if (!vars.contains(v)) {
                throw new RuntimeException(
                        "Bayesian network does not contain\n" + v);
            }
        }
        factors.add(f);
    }

    // Incorporate observation that var = val.
    public void observe(Variable var, String val) {
        for (int i = 0; i < factors.size(); i++) {
            Factor f = factors.get(i);
            if (f.contains(var)) {
                factors.set(i, f.observe(var, val));
            }
        }
    }

    // Run the eliminate variable algorithm on this network and return a factor
    // containing the probabilities of the selected variable.
    // This is performed on a copy to avoid changing this network
    public Factor eliminateVariables(Variable focus) {
        BayesianNetwork tempBN = this.copy();
        for (Variable elim : vars) {
            if (!elim.equals(focus)) {
                Factor product = null;
                ArrayList<Factor> toBeRemoved = new ArrayList<Factor>();
                for (Factor f : tempBN.factors) {
                    if (f.contains(elim)) {
                        toBeRemoved.add(f);
                        if (product == null)
                            product = f;
                        else
                            product = product.multiply(f);
                    }
                }
                if (product != null) {
                	for (Factor f : toBeRemoved)
                		tempBN.factors.remove(f);
                	product = product.sumout(elim);
                	tempBN.factors.add(product);
                }
            }
        }
        Factor product = null;
        for (Factor f : tempBN.factors) {
            if (product == null)
                product = f;
            else
                product = product.multiply(f);
        }
        product.normalize();
        return product;
    }
}
