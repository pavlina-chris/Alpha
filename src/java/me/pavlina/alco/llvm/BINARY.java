// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Binary operator */
public class BINARY implements Instruction {

    String type;
    String op, lhs, rhs;
    String id;
    Instruction iLhs, iRhs;

    public BINARY () { }

    /**
     * Required: Type of values */
    public BINARY type (String t) { type = t; return this; }

    /**
     * Required: Operation. Choices are like "add", "add nuw", etc. See LLVM
     * documentation */
    public BINARY op (String o) { op = o; return this; }

    /**
     * Required: Set left-hand operand */
    public BINARY lhs (String o) { lhs = o; return this; }
    
    /**
     * Set left-hand operand */
    public BINARY lhs (Instruction i) { iLhs = i; return this; }

    /**
     * Required: Set right-hand operand */
    public BINARY rhs (String o) { rhs = o; return this; }

    /**
     * Set right-hand operand */
    public BINARY rhs (Instruction i) { iRhs = i; return this; }

    public String toString () {
        if (iLhs != null)
            lhs = iLhs.getId ();
        if (iRhs != null)
            rhs = iRhs.getId ();
        StringBuilder sb = new StringBuilder (id);
        sb.append (" = ").append (op).append (" ").append (type)
            .append (" ").append (lhs).append (", ").append (rhs).append ('\n');

        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () {
        if (op.startsWith ("icmp") || op.startsWith ("fcmp"))
            return "i1";
        else
            return type;
    }

}
