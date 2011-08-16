// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Branch instruction */
public class BRANCH implements Terminator {

    String cond;
    String ifTrue;
    String ifFalse;
    Instruction iCond;
    Block bIfTrue, bIfFalse;

    public BRANCH () {
    }

    /**
     * Required for conditional: Set condition. */
    public BRANCH cond (String c) { cond = c; return this; }

    /**
     * For conditional: Set condition. */
    public BRANCH cond (Instruction i) { iCond = i; return this; }

    /**
     * Required for conditional: Destination if true. */
    public BRANCH T (String d) { ifTrue = d; return this; }

    /**
     * For conditional: Destination if true. */
    public BRANCH T (Block b) { bIfTrue = b; return this; }

    /**
     * Required for conditional: Destination if false. */
    public BRANCH F (String d) { ifFalse = d; return this; }

    /**
     * For conditional: Destination if false. */
    public BRANCH F (Block b) { bIfFalse = b; return this; }

    /**
     * Required for nonconditional: Destination */
    public BRANCH dest (String d) { ifTrue = d; return this; }

    /**
     * For nonconditional: Destination */
    public BRANCH dest (Block b) { bIfTrue = b; return this; }

    public String toString () {
        if (bIfTrue != null)
            ifTrue = bIfTrue.getId ();
        if (bIfFalse != null)
            ifFalse = bIfFalse.getId ();
        if (iCond != null) cond = iCond.getId ();
        if (cond == null) {
            return String.format ("br label %s\n", ifTrue);
        } else {
            return String.format ("br i1 %s, label %s, label %s\n",
                                  cond, ifTrue, ifFalse);
        }
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }

}
