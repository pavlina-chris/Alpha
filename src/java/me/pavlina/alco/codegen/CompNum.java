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
 * Bitwise complement */
public class CompNum {
    Token token;
    String opndV, valueString;
    Type type;

    public CompNum (Token token) {
        this.token = token;
    }

    /**
     * Set the operand */
    public CompNum operand (String opnd) {
        this.opndV = opnd;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public CompNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT) {
            throw CError.at ("invalid type for complement", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        valueString = new Binary (emitter, function)
            .operation (Binary.BinOp.XOR)
            .type (LLVMType.getLLVMName (getType ()))
            .operands (opndV, "-1")
            .build ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }
}
