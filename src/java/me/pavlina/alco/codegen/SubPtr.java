// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.lex.Token;

/**
 * Subtraction of pointers. Subtracts two pointers of the same type. */
public class SubPtr {
    Token token;
    String lhsV, rhsV, valueString;
    Type type, ptrT;

    public SubPtr (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public SubPtr lhs (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public SubPtr rhs (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public SubPtr type (Type type) {
        ptrT = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        type = new Type (env, "ssize", null);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        String intermedT = "i" + Integer.toString (env.getBits ());

        String lhsAsInt = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.PTRTOINT)
            .source (LLVMType.getLLVMName (ptrT), lhsV)
            .dest (intermedT)
            .build ();

        String rhsAsInt = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.PTRTOINT)
            .source (LLVMType.getLLVMName (ptrT), rhsV)
            .dest (intermedT)
            .build ();

        String diff = new Binary (emitter, function)
            .operation (Binary.BinOp.SUB)
            .type (intermedT)
            .operands (lhsAsInt, rhsAsInt)
            .build ();

        valueString = new Binary (emitter, function)
            .operation (Binary.BinOp.SDIV)
            .type (intermedT)
            .operands (diff,
                       Integer.toString (ptrT.getSubtype ().getSize ()))
            .build ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

}
