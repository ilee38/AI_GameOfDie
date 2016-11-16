package dicegame;
import java.util.*;

// An inefficient implementation of factors for Bayesian networks.
public class Factor {
    private Variable[] vars;  // the variables of this factor
    private double[] vals;   // the values for each assignment to vars
    
    // Initialize a Factor with these variables.
    // All values are initialized to zero.
    public Factor(Variable... vars) {
        ArrayList<Variable> varSet = new ArrayList<Variable>();
        for (Variable var : vars) {
            if (varSet.contains(var)) {
                throw new RuntimeException("Duplicate Variables");
            }
            varSet.add(var);
        }
        this.vars = varSet.<Variable> toArray(new Variable[0]);
        int size = 1;
        for (Variable var : vars) {
            size *= var.getSize();
        }
        vals = new double[size];
    }
    
    // Returns a copy of the variables.
    public Variable[] getVariables() {
        return Arrays.copyOf(vars, vars.length);
    }
    
    // Returns true if the factor uses this variable.
    public boolean contains(Variable var) {
        for (int i = 0; i < vars.length; i++) {
            if (var.equals(vars[i])) return true;
        }
        return false;
    }

    // Set the value of the factor for an assignment to the variables.
    // indexes are indices to the values of the variable.
    // For example, if indexes is 0,3,1, that corresponds to
    // the index 0 value of variable 0
    // the index 3 value of variable 1
    // the index 1 value of variable 2
    // No bounds checking is done, so this code assumes you know
    // what you are doing.
    public void set(int[] indexes, double p) {
        int index = 0;
        for (int i = 0; i < vars.length; i++) {
            index = index * vars[i].getSize() + indexes[i];
        }
        vals[index] = p;
    }
    
    public void set(double p, int... indexes) {
        int index = 0;
        for (int i = 0; i < vars.length; i++) {
            index = index * vars[i].getSize() + indexes[i];
        }
        vals[index] = p;
    }

    // Get the value of the factor for an assignment to the variables.
    // indexes are indices to the values of the variable.
    public double get(int... indexes) {
        int index = 0;
        for (int i = 0; i < vars.length; i++) {
            index = index * vars[i].getSize() + indexes[i];
        }
        return vals[index];
    }

    // Returns an array of indexes into the values of variables.
    // All elements are set to 0.
    public int[] initialIndexes() {
        return new int[vars.length];
    }

    // Increments an array of indexes into the values of variables.
    // Returns false when the array has "turned over", when all
    // the variables are zero again.
    // Loop over all possible values to the factor's variables by:
    //    
    //     int[] indexes = factor.initialIndexes();
    //     do {
    //         // do stuff with indexes
    //     } while (factor.incrementIndexes(indexes));
    //
    public boolean incrementIndexes(int[] indexes) {
        for (int i = vars.length - 1; i >= 0; i--) {
            indexes[i]++;
            if (indexes[i] >= vars[i].getSize()) {
                indexes[i] = 0;
            } else {
                return true;
            }
        }
        return false;
    }

    // Return the factor that results from observing the variable
    // to be a particular value.
    public Factor observe(Variable var, String val) {
        String[] vals = var.getValues();
        for (int i = 0; i < vals.length; i++) {
            if (val.equals(vals[i]))
                return observe(var, i);
        }
        throw new RuntimeException(val + " is not a possible value for\n" + var);
    }
    
    // Return the factor that results from observing the variable
    // to be a particular value.  In this case, val is an index into
    // the values of the variable.
    public Factor observe(Variable var, int val) {
        int observedi = -1;
        Variable[] newvars = new Variable[vars.length - 1];
        int j = 0;
        for (int i = 0; i < vars.length; i++) {
            if (var.equals(vars[i])) {
                observedi = i;
            } else {
            	newvars[j] = vars[i];
            	j++;
            }
        }
        if (observedi == -1)
            throw new RuntimeException("Variable is not used by factor");
        Factor newfactor = new Factor(newvars);
        int[] indexes = this.initialIndexes();
        int[] newindexes = newfactor.initialIndexes();
        do {
            if (indexes[observedi] == val) {
                newfactor.set(newindexes, this.get(indexes));
                newfactor.incrementIndexes(newindexes);
            }
        } while (incrementIndexes(indexes));
        return newfactor;
    }

    // Return a factor in which a variable in the factor is summed out.
    public Factor sumout(Variable var) {
        int sumouti = -1;
        Variable[] newvars = new Variable[vars.length - 1];
        for (int i = 0; i < vars.length; i++) {
            if (var.equals(vars[i])) {
                sumouti = i;
            } else {
                if (sumouti == -1) {
                    newvars[i] = vars[i];
                } else {
                    newvars[i - 1] = vars[i];
                }
            }
        }
        if (sumouti == -1)
            throw new RuntimeException("Variable is not used by factor");
        Factor newfactor = new Factor(newvars);

        int[] oldIndexes = this.initialIndexes();
        int[] newIndexes = newfactor.initialIndexes();
        do {
            for (int i = 0; i < sumouti; i++) {
                newIndexes[i] = oldIndexes[i];
            }
            for (int i = sumouti; i < newvars.length; i++) {
                newIndexes[i] = oldIndexes[i + 1];
            }

            newfactor.set(newIndexes,
                    newfactor.get(newIndexes) + this.get(oldIndexes));
        } while (this.incrementIndexes(oldIndexes));

        return newfactor;
    }

    // Return a factor that is the product of this factor
    // and the other factor.
    public Factor multiply(Factor other) {
        ArrayList<Variable> newvars = new ArrayList<Variable>();

        int[] thismap = new int[vars.length];
        for (int i = 0; i < vars.length; i++) {
            if (!newvars.contains(vars[i])) {
                newvars.add(vars[i]);
            }
            thismap[i] = newvars.indexOf(vars[i]);
        }

        int[] othermap = new int[other.vars.length];
        for (int i = 0; i < other.vars.length; i++) {
            if (!newvars.contains(other.vars[i])) {
                newvars.add(other.vars[i]);
            }
            othermap[i] = newvars.indexOf(other.vars[i]);
        }

        Factor newfactor = new Factor(
                newvars.<Variable> toArray(new Variable[0]));

        int[] newIndexes = newfactor.initialIndexes();
        int[] oldIndexes1 = this.initialIndexes();
        int[] oldIndexes2 = other.initialIndexes();
        do {
            for (int i = 0; i < vars.length; i++) {
                oldIndexes1[i] = newIndexes[thismap[i]];
            }
            for (int i = 0; i < other.vars.length; i++) {
                oldIndexes2[i] = newIndexes[othermap[i]];
            }
            newfactor.set(newIndexes,
                    this.get(oldIndexes1) * other.get(oldIndexes2));
        } while (newfactor.incrementIndexes(newIndexes));

        return newfactor;
    }
    
    // Normalize the vals so they sum to 1.
    public void normalize() {
        double sum = 0;
        for (int i = 0; i < vals.length; i++) {
            sum += vals[i];
        }
        for (int i = 0; i < vals.length; i++) {
            vals[i] /= sum;
        }
    }

    // Return a nice String showing the factor as a table.
    public String toString() {
        String result = "";
        for (int i = 0; i < vars.length; i++) {
            result += vars[i] + "\n";
        }
        int[] indexes = initialIndexes();
        int j = 0;
        do {
            for (int i = 0; i < vars.length; i++) {
                result += indexes[i] + " ";
            }
            result += vals[j] + "\n";
            j++;
        } while (incrementIndexes(indexes));

        return result;

    }
}
