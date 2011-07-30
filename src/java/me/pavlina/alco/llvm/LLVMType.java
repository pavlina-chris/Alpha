// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import me.pavlina.alco.language.Type;

/**
 * Get an LLVM type name from a Type */
public class LLVMType {

    private LLVMType () {}

    /**
     * Get the LLVM equivalent name to an Alpha type */
    public static String getLLVMName (Type type) {
        
        if (type == null) return "void";

        Type.Encoding enc = type.getEncoding ();

        if (enc == Type.Encoding.OBJECT) {
            return "%.nonprim";
        } else if (enc == Type.Encoding.ARRAY) {
            return "%.nonprim";
        } else if (enc == Type.Encoding.UINT || enc == Type.Encoding.SINT) {
            return "i" + Integer.toString (type.getSize () * 8);
        } else if (enc == Type.Encoding.FLOAT) {
            if (type.getSize () == 4)
                return "float";
            else
                return "double";
        } else if (enc == Type.Encoding.POINTER) {
            return getLLVMName (type.getSubtype ()) + "*";
        } else if (enc == Type.Encoding.BOOL) {
            return "i8";
        } else if (enc == Type.Encoding.NULL) {
            return "%.nonprim";
        } else
            throw new RuntimeException ("Missed an encoding!");
    }

    /**
     * Same as getLLVMName(), but returns "void" for the null type */
    public static String getLLVMNameV (Type type) {
        if (type.getEncoding () == Type.Encoding.NULL)
            return "void";
        return getLLVMName (type);
    }
}
