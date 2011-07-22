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
 * Pointer/int subtraction. Evaluates (T* - integer). */
public class Sub1Ptr {
    Token token;
    String ptrV, intV, valueString;
    Type ptrT, intT, type;

    public Sub1Ptr (Token token) {
        this.token = token;
    }

    /**
     * Set the pointer type. */
    public Sub1Ptr pointerT (Type ptrT) {
        this.ptrT = ptrT;
        type = ptrT.getNormalised ();
        return this;
    }

    /**
     * Set the integer type. */
    public Sub1Ptr integerT (Type intT) {
        this.intT = intT;
        return this;
    }

    /**
     * Set the pointer value */
    public Sub1Ptr pointerV (String ptrV) {
        this.ptrV = ptrV;
        return this;
    }

    /**
     * Set the integer value */
    public Sub1Ptr integerV (String intV) {
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
            throw new RuntimeException("Passed non-pointer to Sub1Ptr.pointer");
        }
        if (intE != Type.Encoding.SINT && intE != Type.Encoding.UINT) {
            throw new RuntimeException("Passed non-integer to Sub1Ptr.integer");
        }
        if (intT.getSize () > (env.getBits () / 8)) {
            throw CError.at ("cannot add pointer to wider integer", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        Type.Encoding ptrE = ptrT.getEncoding ();
        Type.Encoding intE = intT.getEncoding ();
        String neg = new Binary (emitter, function)
            .operation (Binary.BinOp.SUB)
            .type (LLVMType.getLLVMName (intT))
            .operands ("0", intV)
            .build ();
        valueString = new getelementptr (emitter, function)
            .type (LLVMType.getLLVMName (ptrT))
            .pointer (ptrV)
            .addIndex (LLVMType.getLLVMName (intT), neg)
            .build ();
    }

    public String getValueString () {
        return valueString;
    }
}
