package converter;

public class OldConvertationEx {

    public static float rubleToEuro(float value) {
        return value / 70;
    }

    public static float rubleToDollar(float value) {
        return value / 60;
    }

    public static double roundedTill2DigitsRubleToEuro(double value) {
        int divisor = 70;
        return Math.round(value * 100.0 / divisor) / 100.0;
    }

    public static double preciseRubleToEuro(double value) {
        int divisor = 70;
        long totalCent = Math.round(value * 100.0 / divisor);
        long euro = totalCent / 100;
        long cents = totalCent % 100;
        return euro + cents / 100.0;
    }

    public static void main(String[] args) {
        float in = 140;
        float expected = 2;
        float out = OldConvertationEx.rubleToEuro(in);
        boolean passed = expected == out;
        System.out.println("140 rubles are 2 euros. Test result : " + passed);

        expected = 2.3333333F;
        out = rubleToDollar(in);
        passed = expected == out;
        System.out.println("140 rubles are " + expected + " dollars. Test result : " + passed);
    }
}