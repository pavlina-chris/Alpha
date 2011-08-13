// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * branch */
public class branch
{
    private Function function;
    private String cond;
    private String ifTrue;
    private String ifFalse;

    public branch (Counter counter, Function function) {
        this.function = function;
    }

    /**
     * Required: Set destination if true (or if no condition) */
    public branch ifTrue (String ifTrue) {
        this.ifTrue = ifTrue;
        return this;
    }

    /**
     * Set destination if false */
    public branch ifFalse (String ifFalse) {
        this.ifFalse = ifFalse;
        return this;
    }

    /**
     * Set condition */
    public branch cond (String cond) {
        this.cond = cond;
        return this;
    }

    public void build () {
        String text;
        if (cond == null)
            text = String.format ("br label %s\n", ifTrue);
        else
            text = String.format ("br i1 %s, label %s, label %s\n",
                                  cond, ifTrue, ifFalse);
        function.add (text);
    }
}
