package ed.inf.adbs.minibase.base;

/**
 * String constant class holds the value of a term that is a string constant, has functions to return the value and
 * check equality
 */
public class StringConstant extends Constant {
    private final String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object object){
        if (!super.equals(object)) return false;
        else return ((StringConstant) object).getValue().equals(this.getValue());
    }
}