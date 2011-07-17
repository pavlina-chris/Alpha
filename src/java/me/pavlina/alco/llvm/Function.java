// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import me.pavlina.alco.llvm.FHead.*;

/**
 * Function. */
public class Function extends RootCodeObj
{

    String name;
    String type;
    Linkage linkage;
    Visibility visibility;
    CallingConvention cconv;
    ParamAttribute[] retattrs;
    FunctionAttribute[] fattrs;

    List<ParamAttribute[]> paramAttrs;
    List<String> paramTypes;
    List<String> paramNames;
    List<String> code;

    /**
     * Create a function. Parameters must be added with addParameter(), and
     * code with add().
     * @param name Function name. Do not include the @ prefix.
     * @param type Return type.
     * @param linkage Linkage type.
     * @param visibility Visibility type.
     * @param cconv Calling convention.
     * @param retattrs Return attributes.
     * @param fattrs Function attributes. */
    public Function (String name, String type, Linkage linkage,
                     Visibility visibility,
                     CallingConvention cconv, ParamAttribute[] retattrs,
                     FunctionAttribute... fattrs) {
        this.name = name;
        this.type = type;
        this.linkage = linkage;
        this.visibility = visibility;
        this.cconv = cconv;
        this.retattrs = retattrs;
        this.fattrs = fattrs;

        paramAttrs = new ArrayList<ParamAttribute[]> ();
        paramTypes = new ArrayList<String> ();
        paramNames = new ArrayList<String> ();
        code = new ArrayList<String> ();
    }

    /**
     * Create a function. Parameters must be added with addParameter(), and
     * code with add(). Linkage defaults to EXTERNALLY_VISIBLE, visibility to
     * DEFAULT, calling convention to CCC, and all attributes empty.
     * @param name Function name. Do not include the @ prefix.
     * @param type Return type
     */
    public Function (String name, String type) {
        this.name = name;
        this.type = type;
        linkage = Linkage.EXTERNALLY_VISIBLE;
        visibility = Visibility.DEFAULT;
        cconv = CallingConvention.CCC;
        retattrs = null;
        fattrs = null;

        paramAttrs = new ArrayList<ParamAttribute[]> ();
        paramTypes = new ArrayList<String> ();
        paramNames = new ArrayList<String> ();
        code = new ArrayList<String> ();
    }

    /**
     * Add a function parameter.
     * @param type Parameter type
     * @param name Parameter name. Should start with %
     * @param attrs All parameter attributes */
    public void addParameter (String type, String name, ParamAttribute... attrs)
    {
        paramAttrs.add (attrs);
        paramTypes.add (type);
        paramNames.add (name);
    }

    /**
     * Add code. */
    public void add (String o) {
        code.add (o);
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_FUNCTION;
    }

    public void write (PrintStream out) {
        out.printf ("define %s %s %s", linkage, visibility, cconv);
        if (retattrs != null)
            for (ParamAttribute i: retattrs) {
                out.printf (" %s", i);
            }
        out.printf (" %s @%s (", type, name);
        for (int i = 0; i < paramAttrs.size (); ++i) {
            ParamAttribute[] attrs = paramAttrs.get (i);
            String type = paramTypes.get (i);
            String name = paramNames.get (i);

            if (i != 0)
                out.print (", ");

            for (ParamAttribute j: attrs) {
                out.printf ("%s ", j);
            }

            out.printf ("%s %s", type, name);
        }
        out.print (") ");

        if (fattrs != null)
            for (FunctionAttribute i: fattrs) {
                out.printf ("%s ", i);
            }

        out.print ("{\n");

        for (String i: code) {
            out.print ("  ");
            out.print (i);
        }

        if (this.type.equals ("void")) {
            out.println ("  ret void");
        } else if (this.type.equals ("float") || this.type.equals ("double")) {
            out.println ("  ret " + this.type + " 0.0");
        } else if (this.type.endsWith ("*")) {
            out.println ("  ret null");
        } else
            out.println ("  ret " + this.type + " 0");

        out.print ("}\n\n");
    }
}
