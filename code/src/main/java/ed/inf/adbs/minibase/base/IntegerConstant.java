package ed.inf.adbs.minibase.base;

/**
 * stores the value of a term whose value is a constant integer, contains functions for returning the value and checking
 * equality to another object
 */
public class IntegerConstant extends Constant {
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object object){
        if (!super.equals(object)) return false;
        else return ((IntegerConstant) object).getValue().equals(this.getValue());
    }
}
