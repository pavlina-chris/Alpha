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
 * Bitwise XOR. */
public class BitXor {
    Token token;
    String lhsV, rhsV, valueString;
    Type type;
    
    public BitXor (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public BitXor lhs (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public BitXor rhs (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public BitXor type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT) {
            throw CError.at ("invalid types for bitwise operation", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        valueString = new Binary (emitter, function)
            .operation (Binary.BinOp.XOR)
            .type (LLVMType.getLLVMName (type))
            .operands (lhsV, rhsV)
            .build ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }
}
