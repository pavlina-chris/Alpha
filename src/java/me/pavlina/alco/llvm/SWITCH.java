// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * switch */
public class SWITCH implements Terminator {

    String type, value, defdest;
    List<String> values, dests;
    Instruction iValue;
    List<Instruction> iValues;
    Block bDefdest;
    List<Block> bDests;

    public SWITCH () {
        values = new ArrayList<String> ();
        dests = new ArrayList<String> ();
        iValues = new ArrayList<Instruction> ();
        bDests = new ArrayList<Block> ();
    }

    /**
     * Required: Set the given value */
    public SWITCH value (String v) { value = v; return this; }

    /**
     * Required: Set the type of the value */
    public SWITCH type (String t) { type = t; return this; }

    /**
     * Set the value and type */
    public SWITCH value (Instruction i) { iValue = i; return this; }

    /**
     * Required: Set the default destination */
    public SWITCH dest (String d) { defdest = d; return this; }

    /**
     * Set the default destination */
    public SWITCH dest (Block b) { bDefdest = b; return this; }

    /**
     * Add a value and destination pair */
    public SWITCH addDest (String value, String dest) {
        values.add (value);
        dests.add (dest);
        iValues.add (null);
        bDests.add (null);
        return this;
    }

    /**
     * Add a value and destination pair */
    public SWITCH addDest (Instruction value, String dest) {
        values.add (null);
        dests.add (dest);
        iValues.add (value);
        bDests.add (null);
        return this;
    }

    /**
     * Add a value and destination pair */
    public SWITCH addDest (String value, Block dest) {
        values.add (value);
        dests.add (null);
        iValues.add (null);
        bDests.add (dest);
        return this;
    }

    /**
     * Add a value and destination pair */
    public SWITCH addDest (Instruction value, Block dest) {
        values.add (null);
        dests.add (null);
        iValues.add (value);
        bDests.add (dest);
        return this;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        if (iValue != null) {
            value = iValue.getId ();
            type = iValue.getType ();
        }
        if (bDefdest != null)
            defdest = bDefdest.getId ();
        sb.append ("switch ").append (type).append (' ').append (value)
            .append (", label ").append (defdest);
        sb.append (" [");
        for (int i = 0; i < values.size (); ++i) {
            String v, d;
            v = (iValues.get (i) != null)
                ? iValues.get (i).getId ()
                : values.get (i);
            d = (bDests.get (i) != null)
                ? bDests.get (i).getId ()
                : dests.get (i);
            sb.append (' ').append (type).append (' ').append (v)
                .append (", label ").append (d);
        }
        sb.append (" ]\n");
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }
}
