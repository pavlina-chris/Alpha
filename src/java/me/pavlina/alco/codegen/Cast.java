// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import java.math.BigInteger;
import static me.pavlina.alco.language.IntLimits.*;

public class Cast {

    Type srcT, dstT;
    Instruction instruction, sval;
    Token token;

    public Cast (Token token) {
        this.token = token;
    }

    /**
     * Required: Set value to cast. This is an LLVM value string. */
    public Cast value (Instruction value) {
        this.sval = value;
        return this;
    }

    /**
     * Required: Set type of value. */
    public Cast type (Type type) {
        this.srcT = type;
        return this;
    }

    /**
     * Required: Set destination type. */
    public Cast dest (Type dest) {
        this.dstT = dest;
        return this;
    }

    /**
     * Check types. */
    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding srcE = srcT.getEncoding ();
        Type.Encoding dstE = dstT.getEncoding ();

        // See Standard:Types:Casting:AllowedCasts

        // Integer literal cast
        if (((srcE == Type.Encoding.SINT) || (srcE == Type.Encoding.UINT)) &&
            ((dstE == Type.Encoding.SINT) || (dstE == Type.Encoding.UINT)) &&
            (srcT.getValue () != null)) {

            BigInteger intVal = srcT.getValue ();
            BigInteger min, max;

            int size = dstT.getSize ();
            if (dstE == Type.Encoding.SINT) {
                if (size == 8) {
                    min = I64_MIN;
                    max = I64_MAX;
                } else if (size == 4) {
                    min = I32_MIN;
                    max = I32_MAX;
                } else if (size == 2) {
                    min = I16_MIN;
                    max = I16_MAX;
                } else {
                    min = I8_MIN;
                    max = I8_MAX;
                }
            } else {
                min = BigInteger.ZERO;
                if (size == 8)
                    max = U64_MAX;
                else if (size == 4)
                    max = U32_MAX;
                else if (size == 2)
                    max = U16_MAX;
                else
                    max = U8_MAX;
            }
            if (min.compareTo (intVal) > 0 ||
                max.compareTo (intVal) < 0) {
                throw CError.at ("integer literal outside range for type",
                                 token);
            }

            return;
        }

        else if (srcT.equalsNoQual (dstT)) {
            // T to T
            // OK
   
        } else if (((srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT)||
                    (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT))
                   && (srcT.getSize () != dstT.getSize ())) {
            throw CError.at ("cannot cast integer in both sign and width;\n" +
                             "sign and width casts are not commutative",
                             token);

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
            // SI to UI
            // OK

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
            // UI to SI
            // OK

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                   && srcT.getSize () < dstT.getSize ()) {
            // SI to SI
            // OK

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                   && srcT.getSize () > dstT.getSize ()) {
            // SI to SI
            // OK

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                   && srcT.getSize () < dstT.getSize ()) {
            // UI to UI
            // OK

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                   && srcT.getSize () > dstT.getSize ()) {
            // UI to UI
            // OK

        } else if ((srcE == Type.Encoding.UINT || srcE == Type.Encoding.SINT) &&
                   dstE == Type.Encoding.BOOL) {
            // SI/UI to B
            // OK

        } else if ((dstE == Type.Encoding.UINT || dstE == Type.Encoding.SINT) &&
                   srcE == Type.Encoding.BOOL) {
            // B to SI/UI
            // OK

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
            // SI to FP
            // OK

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
            // FP to SI
            // OK
        
        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
            // UI to FP
            // OK

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
            // FP to UI
            // OK

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                   && srcT.getSize () < dstT.getSize ()) {
            // FP to FP
            // OK

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                   && srcT.getSize () > dstT.getSize ()) {
            // FP to FP
            // OK

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.POINTER) {
            // T* to U*
            // OK

        } else if (srcE == Type.Encoding.POINTER && dstE == Type.Encoding.BOOL){
            // T* to B
            // OK

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.UINT &&
                   dstT.getSize () >= (env.getBits () / 8)) {
            // T* to UI
            // OK

        } else if (srcE == Type.Encoding.UINT &&
                   dstE == Type.Encoding.POINTER &&
                   srcT.getSize () >= (env.getBits () / 8)) {
            // UI to T*
            // OK

        } else if (srcE == Type.Encoding.UINT &&
                   dstE == Type.Encoding.POINTER) {
            throw CError.at ("invalid cast: narrow integer to pointer", token);

        } else if (srcE == Type.Encoding.SINT &&
                   dstE == Type.Encoding.POINTER) {
            throw CError.at ("invalid cast: signed integer to pointer", token);

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.UINT) {
            throw CError.at ("invalid cast: pointer to narrow integer", token);

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.SINT) {
            throw CError.at ("invalid cast: pointer to signed integer", token);

        } else if (srcE == Type.Encoding.ARRAY &&
                   dstE == Type.Encoding.POINTER &&
                   srcT.getSubtype ().equals (dstT.getSubtype ())) {
            // T[] to T*
            throw new RuntimeException ("NOT IMPLEMENTED YET");

        } else if (srcE == Type.Encoding.ARRAY &&
                   dstE == Type.Encoding.BOOL) {
            // T[] to B
            throw new RuntimeException ("NOT IMPLEMENTED YET");

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.SINT) {
            // null to SI
            // OK

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.UINT) {
            // null to UI
            // OK

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.OBJECT) {
            // null to class
            // OK

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.ARRAY) {
            // null to T[]
            // OK

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.POINTER) {
            // null to T*
            // OK

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.BOOL) {
            // null to B
            // OK

        } else {
            throw CError.at ("invalid cast: " + srcT.toString ()
                             + " to " + dstT.toString (), token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String sty = LLVMType.getLLVMName (srcT);
        String dty = LLVMType.getLLVMName (dstT);
        Type.Encoding srcE = srcT.getEncoding ();
        Type.Encoding dstE = dstT.getEncoding ();

        // See Standard:Types:Casting:AllowedCasts

        // Integer literal cast
        if ((srcE == Type.Encoding.SINT || srcE == Type.Encoding.UINT) &&
            (dstE == Type.Encoding.SINT || dstE == Type.Encoding.UINT) &&
            srcT.getValue () != null) {
            BigInteger intVal = srcT.getValue ();
            instruction = new Placeholder (intVal.toString (), dty);
            return;
        }

        else if (srcT.equalsNoQual (dstT)) {
            // T to T
            instruction = sval;
   
        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
            // SI to UI
            instruction = sval;

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
            // UI to SI
            instruction = sval;

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                   && srcT.getSize () < dstT.getSize ()) {
            // SI to SI
            instruction = new CONVERT ()
                .op ("sext")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                   && srcT.getSize () > dstT.getSize ()) {
            // SI to SI
            instruction = new CONVERT ()
                .op ("trunc")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                   && srcT.getSize () < dstT.getSize ()) {
            // UI to UI
            instruction = new CONVERT ()
                .op ("zext")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                   && srcT.getSize () > dstT.getSize ()) {
            // UI to UI
            instruction = new CONVERT ()
                .op ("trunc")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if ((srcE == Type.Encoding.UINT || srcE == Type.Encoding.SINT) &&
                   dstE == Type.Encoding.BOOL) {
            // SI/UI to B
            Instruction isZero = new BINARY ()
                .op ("icmp eq")
                .type (sty).lhs (sval).rhs ("0");
            instruction = new SELECT ()
                .cond (isZero).type (dty).T ("0").F ("-1");
            function.add (isZero);
            function.add (instruction);

        } else if ((dstE == Type.Encoding.UINT || dstE == Type.Encoding.SINT) &&
                   srcE == Type.Encoding.BOOL) {
            // B to SI/UI
            if (srcT.getSize () < dstT.getSize ()) {
                instruction = new CONVERT ()
                    .op ("sext")
                    .stype (sty).dtype (dty).value (sval);
                function.add (instruction);
            } else {
                instruction = sval;
            }

        } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
            // SI to FP
            instruction = new CONVERT ()
                .op ("sitofp")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
            // FP to SI
            instruction = new CONVERT ()
                .op ("fptosi")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);
        
        } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
            // UI to FP
            instruction = new CONVERT ()
                .op ("uitofp")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
            // FP to UI
            instruction = new CONVERT ()
                .op ("fptoui")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                   && srcT.getSize () < dstT.getSize ()) {
            // FP to FP
            instruction = new CONVERT ()
                .op ("fpext")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                   && srcT.getSize () > dstT.getSize ()) {
            // FP to FP
            if (Placeholder.class.isInstance (sval) &&
                sval.getId ().startsWith ("0x")) {
                // Raw double value, casting to float. We can convert this here,
                // rather than emitting an fptrunc. I checked, and no, LLVM does
                // not optimise it out into the assembly. (I haven't checked
                // the different options for OPT)


                // FUCK YOU JAVA
                try {
                    String half1 = sval.getId ().substring (2, 10);
                    String half2 = sval.getId ().substring (10, 18);
                    long bits = (Long.parseLong (half1, 16) << 32) |
                        Long.parseLong (half2, 16);
                    double valD = Double.longBitsToDouble (bits);
                    bits = Double.doubleToRawLongBits ((float) valD);
                    instruction = new Placeholder
                        (String.format ("0x%016x", bits), dty);
                    return;
                } catch (NumberFormatException e) {}
                catch (IndexOutOfBoundsException e) {}
            }
            instruction = new CONVERT ()
                .op ("fptrunc")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.POINTER) {
            // T* to U*
            instruction = new CONVERT ()
                .op ("bitcast")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.BOOL) {
            // T* to B
            String intermedT = "i" + Integer.toString (env.getBits ());
            Instruction ptrAsInt = new CONVERT ()
                .op ("ptrtoint")
                .stype (sty).dtype (intermedT).value (sval);
            Instruction isZero = new BINARY ()
                .op ("icmp eq")
                .type (intermedT).lhs (ptrAsInt).rhs ("0");
            instruction = new SELECT ()
                .cond (isZero).type (dty).T ("0").F ("-1");
            function.add (ptrAsInt);
            function.add (isZero);
            function.add (instruction);

        } else if (srcE == Type.Encoding.POINTER &&
                   dstE == Type.Encoding.UINT &&
                   dstT.getSize () >= (env.getBits () / 8)) {
            // T* to UI
            instruction = new CONVERT ()
                .op ("ptrtoint")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.UINT &&
                   dstE == Type.Encoding.POINTER &&
                   srcT.getSize () >= (env.getBits () / 8)) {
            // UI to T*
            instruction = new CONVERT ()
                .op ("inttoptr")
                .stype (sty).dtype (dty).value (sval);
            function.add (instruction);

        } else if (srcE == Type.Encoding.ARRAY &&
                   dstE == Type.Encoding.POINTER &&
                   srcT.getSubtype ().equals (dstT.getSubtype ())) {
            // T[] to T*
            throw new RuntimeException ("NOT IMPLEMENTED YET");

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.SINT) {
            // null to SI
            instruction = new Placeholder ("0", dty);

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.UINT) {
            // null to UI
            instruction = new Placeholder ("0", dty);

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.OBJECT) {
            // null to class
            instruction = sval;

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.ARRAY) {
            // null to T[]
            instruction = sval;

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.POINTER) {
            // null to T*
            instruction = new Placeholder ("null", dty);

        } else if (srcE == Type.Encoding.NULL &&
                   dstE == Type.Encoding.BOOL) {
            instruction = new Placeholder ("0", dty);

        } else {
            throw new RuntimeException ("Invalid cast in genLLVM");
        }
    }

    public Instruction getInstruction () {
        return instruction;
    }
}
