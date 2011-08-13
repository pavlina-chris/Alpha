// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * _switch */
public class _switch
{
    private Function function;
    private String type;
    private String value;
    private String defdest;
    private List<String> values;
    private List<String> dests;

    public _switch (Counter counter, Function function) {
        this.function = function;
        values = new ArrayList<String> ();
        dests = new ArrayList<String> ();
    }

    /**
     * Required: Set the given value */
    public _switch value (String type, String value) {
        this.type = type;
        this.value = value;
        return this;
    }

    /**
     * Required: Set the default destination */
    public _switch dest (String dest) {
        defdest = dest;
        return this;
    }

    /**
     * Add a value and destination pair */
    public _switch addDest (String value, String dest) {
        values.add (value);
        dests.add (dest);
        return this;
    }

    public void build () {
        StringBuilder sb = new StringBuilder
            (String.format
             ("switch %s %s, label %s [", type, value, defdest));
        for (int i = 0; i < values.size (); ++i) {
            sb.append (String.format
                       (" %s %s, label %s", type, values.get (i),
                        dests.get (i)));
        }
        sb.append (" ]\n");
        function.add (sb.toString ());
    }
}
