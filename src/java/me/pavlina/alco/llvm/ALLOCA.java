// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * alloca */
public class ALLOCA implements Instruction {

    String type;
    String numElementsType, sNumElements;
    Instruction iNumElements;
    int alignment;
    String id;

    public ALLOCA () {
        alignment = 0;
    }

    /**
     * Required: Type of value */
    public ALLOCA type (String t) { type = t; return this; }

    /**
     * Allocate an array.
     * @param type Type of 'number'
     * @param number Number of elements */
    public ALLOCA elements (String type, String number)
    { numElementsType = type; sNumElements = number; return this; }

    /**
     * Allocate an array. */
    public ALLOCA elements (Instruction number)
    { iNumElements = number; return this; }

    /**
     * Set alignment */
    public ALLOCA align (int a) { alignment = a; return this; }

    public String toString () {
        if (iNumElements != null) {
            sNumElements = iNumElements.getId ();
            numElementsType = iNumElements.getType ();
        }
        StringBuilder sb = new StringBuilder (id);
        sb.append (" = alloca ").append (type);
        if (numElementsType != null)
            sb.append (", ").append (numElementsType).append (" ")
                .append (sNumElements);
        if (alignment != 0)
            sb.append (", align ").append (alignment);
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return this.id == null; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type + "*"; }

}
