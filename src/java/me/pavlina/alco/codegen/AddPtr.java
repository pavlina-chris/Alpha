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
    Instruction ptrV, intV, value;
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
    public AddPtr pointerV (Instruction ptrV) {
        this.ptrV = ptrV;
        return this;
    }

    /**
     * Set the integer value */
    public AddPtr integerV (Instruction intV) {
        this.intV = intV;
        return this;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding ptrE = ptrT.getEncoding ();
        Type.Encoding intE = intT.getEncoding ();
        assert ptrE == Type.Encoding.POINTER : ptrE;
        assert intE == Type.Encoding.SINT || intE == Type.Encoding.UINT : intE;
        if (intT.getSize () > (env.getBits () / 8)) {
            throw CError.at ("cannot add pointer to wider integer", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        value = new GETELEMENTPTR ()
            .type (LLVMType.getLLVMName (ptrT))
            .value (ptrV)
            .addIndex (intV);
        function.add (value);
    }

    public Instruction getInstruction () {
        return value;
    }
}
