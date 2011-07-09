// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * getelementptr */
public class getelementptr
{
    private Counter counter;
    private Function function;
    private String result;
    private String type;
    private String pointer;
    private boolean inbounds;
    private List<String> types;
    private List<String> indices;

    public getelementptr (Counter counter, Function function) {
        this.counter = counter;
        this.function = function;
        inbounds = false;
        types = new ArrayList<String> ();
        indices = new ArrayList<String> ();
    }

    /**
     * Required: Set aggregate type */
    public getelementptr type (String type) {
        this.type = type;
        return this;
    }

    /**
     * Required: Set pointer */
    public getelementptr pointer (String pointer) {
        this.pointer = pointer;
        return this;
    }

    /**
     * Set whether to trap out-of-bounds indices */
    public getelementptr inbounds (boolean inbounds) {
        this.inbounds = inbounds;
        return this;
    }

    /**
     * Add a variable index */
    public getelementptr addIndex (String type, String index) {
        this.types.add (type);
        this.indices.add (index);
        return this;
    }

    /**
     * Add a constant index */
    public getelementptr addIndex (int index) {
        this.types.add ("i32");
        this.indices.add (Integer.toString (index));
        return this;
    }

    public String build () {
        if (result == null) {
            result = "%" + counter.getTemporary ("%");
        }

        StringBuilder sb = new StringBuilder (result);
        sb.append (" = getelementptr");
        if (inbounds)
            sb.append (" inbounds");
        sb.append (' ');
        sb.append (type);
        sb.append (' ');
        sb.append (pointer);
        for (int i = 0; i < types.size (); ++i) {
            sb.append (", ");
            sb.append (types.get (i));
            sb.append (' ');
            sb.append (indices.get (i));
        }
        sb.append ('\n');
        function.add (sb.toString ());

        return result;
    }
}
