// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;

/**
 * Forward or external function declaration */
public class FDeclare extends RootObject {

    String name, type, linkage, visibility, cconv;
    String[] retattrs, fattrs;
    List<String[]> paramattrs;
    List<String> paramtypes;

    /**
     * Declare a function. Parameters must be added with addParameter().
     * @param name Function name. Should start with @.
     * @param type Return type
     * @param linkage Linkage. See LLVM documentation.
     * @param visibility Visibility. See LLVM documentation.
     * @param cconv Calling convention. May be "". See LLVM documentation.
     * @param retattrs Return attributes.
     * @param fattrs Function attributes. */
    public FDeclare (String name, String type, String linkage,
                     String visibility, String cconv, String[] retattrs,
                     String... fattrs) {
        this.name = name;
        this.type = type;
        this.linkage = linkage;
        this.visibility = visibility;
        this.cconv = cconv;
        this.retattrs = retattrs;
        this.fattrs = fattrs;
        paramattrs = new ArrayList<String[]> ();
        paramtypes = new ArrayList<String> ();
    }

    /**
     * Declare a function. Parameters must be added with addParameter().
     * @param name Function name. Should start with @.
     * @param type Return type */
    public FDeclare (String name, String type) {
        this.name = name;
        this.type = type;
        linkage = "";
        visibility = "";
        cconv = "";
        retattrs = null;
        fattrs = null;
        paramattrs = new ArrayList<String[]> ();
        paramtypes = new ArrayList<String> ();
    }

    /**
     * Add a function parameter.
     * @param type Parameter type.
     * @param attrs All parameter attributes */
    public void addParameter (String type, String... attrs) {
        paramattrs.add (attrs);
        paramtypes.add (type);
    }

    public int getLevel () { return RootObject.LEVEL_GLOBAL; }

    public String toString () {
        StringBuffer sb = new StringBuffer ();
        sb.append ("declare");
        if (!linkage.equals (""))
            sb.append (' ').append (linkage);
        if (!visibility.equals (""))
            sb.append (' ').append (visibility);
        if (!cconv.equals (""))
            sb.append (' ').append (cconv);
        if (retattrs != null)
            for (String i: retattrs)
                sb.append (' ').append (i);
        sb.append (' ').append (type) .append (' ').append (name)
            .append (" (");
        for (int i = 0; i < paramattrs.size (); ++i) {
            String[] attrs = paramattrs.get (i);
            String type = paramtypes.get (i);
            if (i != 0)
                sb.append (", ");
            for (String attr: attrs)
                sb.append (attr).append (' ');
            sb.append (type);
        }
        sb.append (')');
        if (fattrs != null)
            for (String attr: fattrs)
                sb.append (' ').append (attr);
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }

}
