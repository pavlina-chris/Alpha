// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * phi */
public class phi
{
    private Counter counter;
    private Function function;
    private String result;
    private String type;
    private String value;
    private String label;
    private String[] values;
    private String[] labels;

    public phi (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
    }

    /**
     * Required: Set value type */
    public phi type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set values and labels
     * @param value First value
     * @param label First label
     * @param pairs... Additional values and labels
     * @throws IllegalArgumentException If pairs has an odd number of elements
     */
    public phi pairs (String value, String label, String... pairs) {
        this.value = value;
        this.label = label;
        if (pairs.length % 2 != 0)
            throw new IllegalArgumentException ("'pairs' does not have pairs");
        values = new String[pairs.length / 2];
        labels = new String[pairs.length / 2];
        for (int i = 0; i < pairs.length; i += 2) {
            values[i / 2] = pairs[i];
            labels[i / 2] = pairs[i + 1];
        }
        return this;
    }

    /**
     * Set result register */
    public phi result (String result) {
        this.result = result;
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }
        StringBuilder sb = new StringBuilder
            (String.format
             ("%s = phi %s [ %s, %s ]",
              result, type, value, label));
        for (int i = 0; i < values.length; ++i) {
            sb.append (String.format (", [ %s, %s ]", values[i], labels[i]));
        }
        sb.append ('\n');
            function.add (sb.toString ());

        return result;
    }
}
