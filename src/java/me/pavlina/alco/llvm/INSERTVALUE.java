// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * insertvalue */
public class INSERTVALUE {

    String type, vtype, dest, value, id;
    int index;
    int[] indices;

    public INSERTVALUE () {}

    /**
     * Required: Set destination value */
    public INSERTVALUE dest (String type, String value) {
        this.type = type;
        this.dest = value;
        return this;
    }

    /**
     * Required: Set source value */
    public INSERTVALUE source (String type, String value) {
        this.vtype = type;
        this.value = value;
        return this;
    }

    /**
     * Required: Set indices
     * @param index First index
     * @param indices Further indices */
    public INSERTVALUE indices (int index, int... indices) {
        this.index = index;
        this.indices = indices;
        return this;
    }

    public String toString () {
        StringBuffer sb = new StringBuffer ();
        sb.append (id).append (" = insertvalue ").append (type)
            .append (' ').append (dest).append (", ")
            .append (vtype).append (' ').append (value).append (", ")
            .append (index);
        for (int i: indices)
            sb.append (", ").append (i);
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type; }
}
