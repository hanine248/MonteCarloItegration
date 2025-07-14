package montecarlo;

@FunctionalInterface
public interface Function {
    double evaluate(double x);
}