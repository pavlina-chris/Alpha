// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Global string constants. Two formats: LLVM array and C-style pointer to
 * char */
public class StringConstant extends RootObject implements Instruction {
    String id;
    byte[] value;
    boolean pointer;

    /**
     * Get an array-type string constant.
     * @param name Name of the constant. Should start with @
     * @param value String value. Will be encoded in UTF-8. */
    public static StringConstant getArrayConst (String name, String value) {
        StringConstant sc;
        try {
            sc = new StringConstant
                (name, value.getBytes ("UTF-8"), false);
        } catch (java.io.UnsupportedEncodingException e) {
            // Everything is valid in UTF-8
            throw new RuntimeException ("UnsupportedEncodingException in UTF8");
        }
        return sc;
    }

    /**
     * Get an array-type string constant with a sequential name.
     * @param value String value. Will be encoded in UTF-8.
     */
    public static StringConstant getArrayConst (String value) {
        StringConstant sc;
        try {
            sc = new StringConstant
                (null, value.getBytes ("UTF-8"), false);
        } catch (java.io.UnsupportedEncodingException e) {
            // Everything is valid in UTF-8
            throw new RuntimeException ("UnsupportedEncodingException in UTF8");
        }
        return sc;
    }

    /**
     * Get a pointer-type string constant.
     * @param name Name of the constant. Should start with @
     * @param value String value. Will be encoded in UTF-8. */
    public static StringConstant getPointerConst (String name, String value) {
        StringConstant sc;
        try {
            sc = new StringConstant
                (name, value.getBytes ("UTF-8"), true);
        } catch (java.io.UnsupportedEncodingException e) {
            // Everything is valid in UTF-8
            throw new RuntimeException ("UnsupportedEncodingException in UTF8");
        }
        return sc;
    }
    /**
     * Get a pointer-type string constant with a sequential name.
     * @param value String value. Will be encoded in UTF-8.
     */
    public static StringConstant getPointerConst (String value)
    {
        StringConstant sc;
        try {
            sc = new StringConstant
                (null, value.getBytes ("UTF-8"), true);
        } catch (java.io.UnsupportedEncodingException e) {
            // Everything is valid in UTF-8
            throw new RuntimeException ("UnsupportedEncodingException in UTF8");
        }
        return sc;
    }

    private StringConstant (String id, byte[] value, boolean pointer) {
        this.id = id;
        this.value = value;
        this.pointer = pointer;
    }

    public int getLevel () {
        return RootObject.LEVEL_GLOBAL;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        String arrname;

        if (pointer) {
            // Insert a dot after the @
            arrname = "@." + id.substring (1);
        } else
            arrname = id;

        sb.append (arrname).append (" = internal constant [ ")
            .append (value.length + 1).append (" x i8 ] c\"");
        for (byte i: value) {
            // Printable ASCII
            if (i >= ' ' && i <= '~' && i != '"' && i != '\\')
                sb.append ((char) i);
            else
                sb.append (String.format ("\\%02x", i));
        }
        sb.append ("\\00\"\n");

        if (pointer) {
            sb.append (id).append
                (" = linkonce global i8* getelementptr inbounds ").append
                ("([ ").append (value.length + 1).append
                (" x i8 ] * ").append (arrname).append
                (", i64 0, i64 0)\n");
        }
        return sb.toString ();
    }

    public boolean needsId () { return id == null; }
    public void setId (String id) { if (this.id == null) this.id = id; }
    public String getId () { return id; }
    public String getType () {
        return pointer
            ? "i8*"
            : String.format ("[ %d x i8 ]", value.length + 1);
    }

}
