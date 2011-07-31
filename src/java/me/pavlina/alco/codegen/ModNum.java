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
 * Numeric modulo. Returns the true modulo of the operands (-3 % 2 is 1, not
 * -1). */
public class ModNum {
    Token token;
    String lhsV, rhsV, valueString;
    Type type;
    
    public ModNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public ModNum lhs (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public ModNum rhs (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public ModNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for modulo", token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        Binary.BinOp remop, addop;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
            remop = Binary.BinOp.SREM;
            addop = Binary.BinOp.ADD;
            break;
        case UINT:
            remop = Binary.BinOp.UREM;
            addop = Binary.BinOp.ADD;
            break;
        case FLOAT:
            remop = Binary.BinOp.FREM;
            addop = Binary.BinOp.FADD;
            break;
        default:
            throw new RuntimeException ("Modulo of unsupported items");
        }

        // ((x REM y) + y) REM y

        String xREMy = new Binary (emitter, function)
            .operation (remop)
            .type (LLVMType.getLLVMName (type))
            .operands (lhsV, rhsV)
            .build ();

        String pPLUSy = new Binary (emitter, function)
            .operation (addop)
            .type (LLVMType.getLLVMName (type))
            .operands (xREMy, rhsV)
            .build ();

        valueString = new Binary (emitter, function)
            .operation (remop)
            .type (LLVMType.getLLVMName (type))
            .operands (pPLUSy, rhsV)
            .build ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }
}
