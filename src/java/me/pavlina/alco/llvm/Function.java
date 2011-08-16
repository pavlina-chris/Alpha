// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Function. */
public class Function extends RootObject {

    String name, type, linkage, visibility, cconv;
    String[] retattrs, fattrs;

    List<String[]> paramAttrs;
    List<String> paramTypes, paramNames;
    List<Block> code;

    /**
     * Create a function. Parameters must be added with addParamenter().
     * A default block will be created, which can be retrieved with getBlock().
     * @param name Function name. Should start with @.
     * @param type Return type.
     * @param linkage Linkage type.
     * @param visibility Visibility type.
     * @param cconv Calling convention.
     * @param retattrs Return attributes.
     * @param fattrs Function attributes.
     */
    public Function (String name, String type, String linkage,
                     String visibility, String cconv, String[] retattrs,
                     String... fattrs) {
        this.name = name;
        this.type = type;
        this.linkage = linkage;
        this.visibility = visibility;
        this.cconv = cconv;
        this.retattrs = retattrs;
        this.fattrs = fattrs;
        paramAttrs = new ArrayList<String[]> ();
        paramTypes = new ArrayList<String> ();
        paramNames = new ArrayList<String> ();
        code = new LinkedList<Block> ();
        code.add (new Block ());
    }

    /**
     * Create a function. Parameters must be added with addParameter().
     * A default block will be created, which can be retrieved with getBlock().
     * @param name Function name. Should start with @.
     * @param type Return type.
     */
    public Function (String name, String type) {
        this.name = name;
        this.type = type;
        linkage = "";
        visibility = "";
        cconv = "";
        retattrs = null;
        fattrs = null;
        paramAttrs = new ArrayList<String[]> ();
        paramTypes = new ArrayList<String> ();
        paramNames = new ArrayList<String> ();
        code = new ArrayList<Block> ();
        code.add (new Block ());
    }

    /**
     * Add a function parameter.
     * @param type Parameter type
     * @param name Parameter name. Should start with %
     * @param attrs All parameter attributes */
    public void addParameter (String type, String name, String... attrs) {
        paramAttrs.add (attrs);
        paramTypes.add (type);
        paramNames.add (name);
    }

    /**
     * Add code intelligently, creating a new block if necessary. */
    public void add (Instruction i) {
        if (!code.get (code.size () - 1).add (i)) {
            code.add (new Block ());
            code.get (code.size () - 1).add (i);
        }
    }

    /**
     * Add a block */
    public void add (Block b) {
        code.add (b);
    }

    public int getLevel () { return RootObject.LEVEL_FUNCTION; }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("define");
        if (!("").equals (linkage))
            sb.append (' ').append (linkage);
        if (!("").equals (visibility))
            sb.append (' ').append (visibility);
        if (!("").equals (cconv))
            sb.append (' ').append (cconv);
        if (retattrs != null)
            for (String attr: retattrs)
                sb.append (' ').append (attr);
        sb.append (' ').append (type).append (' ').append (name)
            .append (" (");
        for (int i = 0; i < paramAttrs.size (); ++i) {
            String[] attrs = paramAttrs.get (i);
            String type = paramTypes.get (i);
            String name = paramNames.get (i);
            if (i != 0)
                sb.append (", ");
            for (String attr: attrs)
                sb.append (attr).append (' ');
            sb.append (type).append (' ').append (name);
        }
        sb.append (")");
        if (fattrs != null)
            for (String attr: fattrs)
                sb.append (' ').append (attr);
        sb.append (" {\n");
        
        // Number all instructions and blocks
        int n = -1;
        for (Block b: code)
            n = b.number (n);

        for (Block b: code)
            sb.append (b);

        sb.append ("}\n\n");
        return sb.toString ();
    }

    public boolean needsId () { return false; }
    public void setId (String id) {}
    public String getId () { throw new RuntimeException (); }
    public String getType () { return null; }

}
