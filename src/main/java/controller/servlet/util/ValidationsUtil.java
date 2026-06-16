package controller.servlet.util;

public final class ValidationsUtil {
    private ValidationsUtil() {
    }

    public static boolean hasMissingRequiredFields(String first,
                                                    String second,
                                                    String third) {
        return first == null || first.isBlank()
                || second == null || second.isBlank()
                || third == null || third.isBlank();
    }

    public static boolean hasLengthNotEqualToExpected(String incomingString, int expectedLength) {
        return incomingString.length() != expectedLength;
    }

    public static boolean hasMissingPathInfo(String pathInfo) {
        return pathInfo == null || "/".equals(pathInfo);
    }

    public static boolean hasLengthMoreThanExpected(String incomingString, int maxLength) {
        return incomingString.length() > maxLength;
    }
}
