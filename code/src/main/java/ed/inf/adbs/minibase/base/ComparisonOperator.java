package ed.inf.adbs.minibase.base;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Comparison operator class holds comparison operator objects to be used in comparing terms
 */
public enum ComparisonOperator {
    EQ("="),
    NEQ("!="),
    GT(">"),
    GEQ(">="),
    LT("<"),
    LEQ("<=");

    private final String text;

    ComparisonOperator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     *
     * @param s string operator
     * @return ComparisonOperator object represnting the string provided
     * @throws NoSuchElementException if there isn't an operator for that string
     */
    public static ComparisonOperator fromString(String s) throws NoSuchElementException {
        return Arrays.stream(values())
                .filter(op -> op.text.equalsIgnoreCase(s))
                .findFirst().get();
    }

}
