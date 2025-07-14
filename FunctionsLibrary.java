package montecarlo;

public class FunctionsLibrary {
    public static Function sinFunction() {
        return Math::sin;
    }

    public static Function squareFunction() {
        return x -> x * x;
    }

    public static Function expNegSquare() {
        return x -> Math.exp(-x * x);
    }
}
