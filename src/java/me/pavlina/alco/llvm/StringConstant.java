// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.io.PrintStream;

/**
 * Global string constants. Two formats: LLVM array and C-style pointer to
 * char. */
public class StringConstant extends RootCodeObj
{

    String name;
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
     * @param counter Counter object (LLVMEmitter) to get the name from */
    public static StringConstant getArrayConst (String value, Counter counter) {
        int n = counter.getTemporary ("@$");
        StringConstant sc;
        try {
            sc = new StringConstant
                ("@$" + Integer.toString (n), value.getBytes ("UTF-8"), false);
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
     * @param counter Counter object (LLVMEmitter) to get the name from */
    public static StringConstant getPointerConst (String value, Counter counter)
    {
        int n = counter.getTemporary ("@$");
        StringConstant sc;
        try {
            sc = new StringConstant
                ("@$" + Integer.toString (n), value.getBytes ("UTF-8"), true);
        } catch (java.io.UnsupportedEncodingException e) {
            // Everything is valid in UTF-8
            throw new RuntimeException ("UnsupportedEncodingException in UTF8");
        }
        return sc;
    }

    private StringConstant (String name, byte[] value, boolean pointer) {
        this.name = name;
        this.value = value;
        this.pointer = pointer;
    }

    public int getLevel () {
        return RootCodeObj.LEVEL_GLOBAL;
    }

    public void write (PrintStream out) {
        String arrname;

        if (pointer) {
            // Insert a dot after the @
            arrname = "@." + name.substring (1);
        } else {
            arrname = name;
        }

        out.printf ("%s = internal constant [ %d x i8 ] c\"",
                    arrname, value.length + 1);
        for (byte i: value) {
            // Printable ASCII characters:
            //       space          ~            "            \ 
            if (i >= 0x20 && i <= 0x7e && i != 0x22 && i != 0x5c)
                out.write (i);
            else
                out.printf ("\\%02x", i);
        }
        out.print ("\\00\"\n");

        if (pointer) {
            out.printf ("%s = linkonce global i8* getelementptr inbounds " +
                        "([ %d x i8 ] * %s, i64 0, i64 0)\n",
                        name, value.length + 1, arrname);
        }
    }

    /**
     * Get the globally accessible name of the constant */
    public String getName () {
        return name;
    }

}
