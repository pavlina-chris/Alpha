// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * Function call */
public class CALL implements Instruction {

    String type, ftype, pointer, cconv, id;
    boolean tail;
    List<String> retattrs;
    List<String> fattrs;
    List<String[]> argAttrs;
    List<String> argTypes;
    List<String> argValues;
    List<Instruction> iArgValues;

    public CALL () {
        cconv = "";
        retattrs = new ArrayList<String> ();
        fattrs = new ArrayList<String> ();
        argAttrs = new ArrayList<String[]> ();
        argTypes = new ArrayList<String> ();
        argValues = new ArrayList<String> ();
        iArgValues = new ArrayList<Instruction> ();
    }

    /**
     * Required: Set return type. May be "void" */
    public CALL type (String t) { type = t; return this; }

    /**
     * Set function type */
    public CALL ftype (String t) { ftype = t; return this; }

    /**
     * Required: Set function pointer */
    public CALL fun (String f) { pointer = f; return this; }

    /**
     * Set whether this is a tail call */
    public CALL tail (boolean t) { tail = t; return this; }

    /**
     * Set the calling convention. See LLVM documentation. May be "" */
    public CALL cconv (String c) { cconv = c; return this; }

    /**
     * Add a return attribute */
    public CALL retattr (String a) { retattrs.add (a); return this; }

    /**
     * Add a function attribute */
    public CALL fattr (String a) { fattrs.add (a); return this; }

    /**
     * Add an argument */
    public CALL arg (String type, String value, String... attrs) {
        argTypes.add (type);
        argValues.add (value);
        iArgValues.add (null);
        argAttrs.add (attrs);
        return this;
    }

    /**
     * Add an argument */
    public CALL arg (Instruction value, String... attrs) {
        argTypes.add (null);
        argValues.add (null);
        iArgValues.add (value);
        argAttrs.add (attrs);
        return this;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();

        if (!type.equals ("void")) {
            sb.append (id).append (" = ");
        }

        if (tail)
            sb.append ("tail ");
        sb.append ("call");

        if (cconv != null && !cconv.equals (""))
            sb.append (' ').append (cconv);

        for (String i: retattrs)
            sb.append (' ').append (i);

        if (ftype != null)
            sb.append (' ').append (ftype);

        sb.append (' ').append (pointer).append (" (");

        for (int i = 0; i < argAttrs.size (); ++i) {
            String[] attrs = argAttrs.get (i);
            String type = argTypes.get (i);
            String value = argValues.get (i);
            if (iArgValues.get (i) != null) {
                type = iArgValues.get (i).getType ();
                value = iArgValues.get (i).getId ();
            }

            if (i != 0) sb.append (", ");

            for (String attr: attrs)
                sb.append (attr).append (' ');

            sb.append (type).append (' ').append (value);
        }
        
        sb.append (")");
        for (String i: fattrs)
            sb.append (' ').append (i);
        sb.append ('\n');

        return sb.toString ();
    }

    public boolean needsId () { return !("void").equals (type); }
    public void setId (String id) {
        if (!("void").equals (type))
            this.id = id;
    }
    public String getId () {
        if (("void").equals (type))
            throw new RuntimeException ();
        return id;
    }
    public String getType () { return type; }
}
