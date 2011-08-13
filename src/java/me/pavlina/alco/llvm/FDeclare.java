// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import me.pavlina.alco.llvm.FHead.*;

/**
 * Forward declaration of function. */
public class FDeclare extends RootCodeObj
{

    private String name;
    private String type;
    private Linkage linkage;
    private Visibility visibility;
    private CallingConvention cconv;
    private ParamAttribute[] retattrs;
    private FunctionAttribute[] fattrs;

    private List<ParamAttribute[]> paramAttrs;
    private List<String> paramTypes;

    /**
     * Declare a function. Parameters must be added with addParameter().
     * @param name Function name. Do not include the @ prefix.
     * @param type Return type.
     * @param linkage Linkage type.
     * @param visibility Visibility type.
     * @param cconv Calling convention.
     * @param retattrs Return attributes.
     * @param fattrs Function attributes. */
    public FDeclare (String name, String type, Linkage linkage,
                     Visibility visibility,
                     CallingConvention cconv, ParamAttribute[] retattrs,
                     FunctionAttribute... fattrs) {
        this.name = name;
        this.type = type;
        this.linkage = linkage;
        this.visibility = visibility;
        this.cconv = cconv;
        this.retattrs = Arrays.copyOf (retattrs, retattrs.length);
        this.fattrs = fattrs;

        paramAttrs = new ArrayList<ParamAttribute[]> ();
        paramTypes = new ArrayList<String> ();
    }

    /**
     * Declare a function. Parameters must be added with addParameter().
     * Linkage defaults to EXTERNALLY_VISIBLE, visibility to
     * DEFAULT, calling convention to CCC, and all attributes empty.
     * @param name Function name. Do not include the @ prefix.
     * @param type Return type
     */
    public FDeclare (String name, String type) {
        this.name = name;
        this.type = type;
        linkage = Linkage.EXTERNALLY_VISIBLE;
        visibility = Visibility.DEFAULT;
        cconv = CallingConvention.CCC;
        retattrs = null;
        fattrs = null;

        paramAttrs = new ArrayList<ParamAttribute[]> ();
        paramTypes = new ArrayList<String> ();
    }

    /**
     * Add a function parameter.
     * @param type Parameter type
     * @param attrs All parameter attributes */
    public void addParameter (String type, ParamAttribute... attrs)
    {
        paramAttrs.add (attrs);
        paramTypes.add (type);
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_GLOBAL;
    }

    public void write (PrintStream out) {
        out.printf ("declare %s %s %s", linkage, visibility, cconv);
        if (retattrs != null)
            for (ParamAttribute i: retattrs)
                out.printf (" %s", i);
        out.printf (" %s @%s (", type, name);
        for (int i = 0; i < paramAttrs.size (); ++i) {
            ParamAttribute[] attrs = paramAttrs.get (i);
            String type = paramTypes.get (i);

            if (i != 0)
                out.print (", ");

            for (ParamAttribute j: attrs) {
                out.printf ("%s ", j);
            }

            out.print (type);
        }
        out.print (") ");

        if (fattrs != null)
            for (FunctionAttribute i: fattrs) {
                out.printf ("%s ", i);
            }

        out.print ("\n");
    }
}
