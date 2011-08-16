// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Conversion operator */
public class CONVERT implements Instruction {

    String stype, dtype, value, op;
    String id;
    Instruction iValue;

    public CONVERT () { }

    /**
     * Required: Source type */
    public CONVERT stype (String t) { stype = t; return this; }

    /**
     * Required: Destination type */
    public CONVERT dtype (String t) { dtype = t; return this; }

    /**
     * Required: Operation. Choices are like "trunc", "zext", etc. See LLVM
     * documentation */
    public CONVERT op (String o) { op = o; return this; }

    /**
     * Required: Set value */
    public CONVERT value (String v) { value = v; return this; }
    
    /**
     * Set value and source type */
    public CONVERT value (Instruction i) { iValue = i; return this; }


    public String toString () {
        if (iValue != null) {
            value = iValue.getId ();
            stype = iValue.getType ();
        }
        StringBuilder sb = new StringBuilder (id);
        sb.append (" = ").append (op).append (" ").append (stype)
            .append (" ").append (value).append (" to ").append (dtype)
            .append ('\n');

        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return dtype; }

}
