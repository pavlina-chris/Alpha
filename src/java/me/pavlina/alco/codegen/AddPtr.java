// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;

/**
 * Pointer addition. Adds (T* + integer). */
public class AddPtr {
    Token token;
    String ptrV, intV, valueString;
    Type ptrT, intT, type;

    public AddPtr (Token token) {
        this.token = token;
    }

    /**
     * Set the pointer type. */
    public AddPtr pointerT (Type ptrT) {
        this.ptrT = ptrT;
        type = ptrT.getNormalised ();
        return this;
    }

    /**
     * Set the integer type. */
    public AddPtr integerT (Type intT) {
        this.intT = intT;
        return this;
    }

    /**
     * Set the pointer value */
    public AddPtr pointerV (String ptrV) {
        this.ptrV = ptrV;
        return this;
    }

    /**
     * Set the integer value */
    public AddPtr integerV (String intV) {
        this.intV = intV;
        return this;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding ptrE = ptrT.getEncoding ();
        Type.Encoding intE = intT.getEncoding ();
        if (ptrE != Type.Encoding.POINTER) {
            throw new RuntimeException ("Passed non-pointer to AddPtr.pointer");
        }
        if (intE != Type.Encoding.SINT && intE != Type.Encoding.UINT) {
            throw new RuntimeException ("Passed non-integer to AddPtr.integer");
        }
        if (intT.getSize () > (env.getBits () / 8)) {
            throw CError.at ("cannot add pointer to wider integer", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        Type.Encoding ptrE = ptrT.getEncoding ();
        Type.Encoding intE = intT.getEncoding ();
        valueString = new getelementptr (emitter, function)
            .type (LLVMType.getLLVMName (ptrT))
            .pointer (ptrV)
            .addIndex (LLVMType.getLLVMName (intT), intV)
            .build ();
    }

    public String getValueString () {
        return valueString;
    }
}
