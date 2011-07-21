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
 * Numeric subtraction. Subtracts two values of the SAME TYPE */
public class SubNum {
    Token token;
    String lhsV, rhsV, valueString;
    Type type;
    
    public SubNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public SubNum lhs (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public SubNum rhs (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public SubNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for subtraction", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        Binary.BinOp operation;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
        case UINT:
            operation = Binary.BinOp.SUB;
            break;
        case FLOAT:
            operation = Binary.BinOp.FSUB;
            break;
        default:
            throw new RuntimeException ("Subtracting unsupported items");
        }

        valueString = new Binary (emitter, function)
            .operation (operation)
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
