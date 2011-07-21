// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import static me.pavlina.alco.language.Type.Encoding.*;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.lex.Token;

/**
 * Numeric negation */
public class NegNum {
    Token token;
    String opndV, valueString;
    Type type;

    public NegNum (Token token) {
        this.token = token;
    }

    /**
     * Set the operand */
    public NegNum operand (String opnd) {
        this.opndV = opnd;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public NegNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc == Type.Encoding.UINT)
            throw CError.at ("negation of unsigned integer", token);
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid type for negation", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (type.getEncoding () == Type.Encoding.FLOAT) {
            valueString = new Binary (emitter, function)
                .operation (Binary.BinOp.FSUB)
                .type (LLVMType.getLLVMName (getType ()))
                .operands ("0.0", opndV)
                .build ();
        } else { // SINT
            valueString = new Binary (emitter, function)
                .operation (Binary.BinOp.SUB)
                .type (LLVMType.getLLVMName (getType ()))
                .operands ("0", opndV)
                .build ();
        }
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }
}
