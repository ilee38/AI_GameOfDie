package dicegame;
import java.util.*;

public class Variable {
    // the name of the variable
    private String name;

    // The initial list of all possible values for this variable.
    private String[] values;

    // values includes all the possible values of the variable
    public Variable(String name, String[] values) {
        this.name = name;
        this.values = Arrays.copyOf(values, values.length);
    }
    
    public String getName() { return name; }
    
    public String[] getValues() { return Arrays.copyOf(values, values.length); }
    
    public int getSize() { return values.length; }
    
    public String toString() {
        return "Variable " + name + ": " + Arrays.toString(values);
    }
}
