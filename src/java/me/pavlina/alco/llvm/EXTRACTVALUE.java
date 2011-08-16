// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * extractvalue */
public class EXTRACTVALUE
{
    String rtype, type, value;
    String id;
    int index;
    int[] indices;

    public EXTRACTVALUE () {}

    /**
     * Required: Set value */
    public EXTRACTVALUE value (String type, String value) {
        this.type = type;
        this.value = value;
        return this;
    }

    /**
     * Required: Set return type */
    public EXTRACTVALUE rtype (String t) { rtype = t; return this; }

    /**
     * Required: Set indices
     * @param index First index
     * @param indices Further indices into the type */
    public EXTRACTVALUE indices (int index, int... indices) {
        this.index = index;
        this.indices = indices;
        return this;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append (id).append (" = extractvalue ").append (type)
            .append (' ').append (value).append (", ").append (index);
        if (indices != null)
            for (int i: indices)
                sb.append (", ").append (i);
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return rtype; }

}
