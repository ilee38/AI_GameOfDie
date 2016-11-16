package dicegame;

public class FactorTest {

    public static void main(String[] args) {
        figure6point5();
        figure6point6();
        figure6point7();
    }
    
    public static void figure6point5() {
        System.out.println("Example from Figure 6.5\n");
        
        String[] values = {"false", "true"};  // so false is 0 and true is 1
 
        Variable x = new Variable("X", values);
        Variable y = new Variable("Y", values);
        Variable z = new Variable("Z", values);
        
        Factor factor0 = new Factor(x, y , z);
        factor0.set(0.1, 1, 1, 1);
        factor0.set(0.9, 1, 1, 0);
        factor0.set(0.2, 1, 0, 1);
        factor0.set(0.8, 1, 0, 0);
        factor0.set(0.4, 0, 1, 1);
        factor0.set(0.6, 0, 1, 0);
        factor0.set(0.3, 0, 0, 1);
        factor0.set(0.7, 0, 0, 0);
        System.out.println("factor = \n" + factor0);
        
        Factor factor1 = factor0.observe(x, "true");
        // or Factor factor1 = factor0.observe(x, 1);
        System.out.println("observe(X = true) = \n" + factor1);
        
        Factor factor2 = factor0.observe(x, 1).observe(z, "false");
        // or Factor factor2 = factor0.observe(x, 1).observe(z, 0);
        // or Factor factor2 = factor0.observe(x, "true").observe(z, 0);
        // or Factor factor2 = factor0.observe(x, "true").observe(z, "false");
        // or Factor factor2 = factor1.observe(z, 0);
        // or Factor factor2 = factor1.observe(z, "false");
        System.out.println("observe(X = true, Z = false) = \n" + factor2);
    }
    
    public static void figure6point6() {
        System.out.println("Example from Figure 6.6\n");
        
        String[] values = {"false", "true"};  // so false is 0 and true is 1

        Variable a = new Variable("A", values);
        Variable b = new Variable("B", values);
        Variable c = new Variable("C", values);

        Factor factor1 = new Factor(a, b);
        factor1.set(0.1, 1, 1);
        factor1.set(0.9, 1, 0);
        factor1.set(0.2, 0, 1);
        factor1.set(0.8, 0, 0);
        
        System.out.println("factor1 = \n" + factor1);
        
        Factor factor2 = new Factor(b, c);
        factor2.set(0.3, 1, 1);
        factor2.set(0.7, 1, 0);
        factor2.set(0.6, 0, 1);
        factor2.set(0.4, 0, 0);
        
        System.out.println("factor2 = \n" + factor2);
        
        Factor factor3 = factor1.multiply(factor2);
        // or Factor factor3 = factor2.multiply(factor1); with variables ordered differently
        
        System.out.println("factor1 * factor2 = \n" + factor3);
    }
    
    public static void figure6point7() {
        System.out.println("Example from Figure 6.7\n");
        
        String[] values = {"false", "true"};  // so false is 0 and true is 1

        Variable a = new Variable("A", values);
        Variable b = new Variable("B", values);
        Variable c = new Variable("C", values);
        
        Factor factor3 = new Factor(a, b, c);
        factor3.set(0.03, 1, 1, 1);
        factor3.set(0.07, 1, 1, 0);
        factor3.set(0.54, 1, 0, 1);
        factor3.set(0.36, 1, 0, 0);
        factor3.set(0.06, 0, 1, 1);
        factor3.set(0.14, 0, 1, 0);
        factor3.set(0.48, 0, 0, 1);
        factor3.set(0.32, 0, 0, 0);
        System.out.println("factor3 = \n" + factor3);
        
        Factor factor4 = factor3.sumout(b);
        System.out.println("sumout(B) = \n" + factor4);
    }
}
