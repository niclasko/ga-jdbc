package org.sebson.SQL;

public class NumberReference {

    private Number value;

    public NumberReference(String numericToken) {
        if(toInteger(numericToken)) {
            return;
        } else if(toDouble(numericToken)) {
            return;
        }
        this.value = null;
    }

    private boolean toInteger(String numericToken) {
        try {
            this.value = Integer.parseInt(numericToken);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    private boolean toDouble(String numericToken) {
        try {
            this.value = Double.parseDouble(numericToken);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public Number value() {
        return this.value;
    }
}
