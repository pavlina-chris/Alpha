// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * getelementptr */
public class GETELEMENTPTR implements Instruction
{
    String rtype, type, value, id;
    Instruction iValue;
    boolean inbounds;
    List<String> types;
    List<String> indices;
    List<Instruction> iIndices;

    public GETELEMENTPTR () {
        types = new ArrayList<String> ();
        indices = new ArrayList<String> ();
        iIndices = new ArrayList<Instruction> ();
    }

    /**
     * Required: Set aggregate type */
    public GETELEMENTPTR type (String t) { type = t; return this; }

    /**
     * Required: Set return type */
    public GETELEMENTPTR rtype (String t) { rtype = t; return this; }

    /**
     * Required: Set value */
    public GETELEMENTPTR value (String v) { value = v; return this; }

    /**
     * Set value */
    public GETELEMENTPTR value (Instruction i) { iValue = i; return this; }

    /**
     * Set whether to trap out-of-bounds indices */
    public GETELEMENTPTR inbounds (boolean ib) { inbounds = ib; return this; }

    /**
     * Add a variable index */
    public GETELEMENTPTR addIndex (String type, String index) {
        types.add (type);
        indices.add (index);
        iIndices.add (null);
        return this;
    }

    /**
     * Add a variable index */
    public GETELEMENTPTR addIndex (Instruction i) {
        types.add (null);
        indices.add (null);
        iIndices.add (i);
        return this;
    }

    /**
     * Add a constant index */
    public GETELEMENTPTR addIndex (int i) {
        types.add ("i32");
        indices.add (Integer.toString (i));
        iIndices.add (null);
        return this;
    }

    public String toString () {
        if (iValue != null) {
            value = iValue.getId ();
            type = iValue.getType ();
        }
        StringBuilder sb = new StringBuilder ();
        sb.append (id).append (" = getelementptr ");
        if (inbounds) sb.append ("inbounds ");
        sb.append (type).append (' ').append (value);
        for (int i = 0; i < types.size (); ++i) {
            Instruction iIndex = iIndices.get (i);
            String type, index;
            if (iIndex == null) {
                type = types.get (i);
                index = indices.get (i);
            } else {
                type = iIndex.getType ();
                index = iIndex.getId ();
            }
            sb.append (", ").append (type).append (' ').append (index);
        }
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return rtype; }
}
