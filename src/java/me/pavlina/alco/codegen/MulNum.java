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
 * Numeric multiplication. Multiplies two values of the SAME TYPE */
public class MulNum {
    Token token;
    Instruction lhsV, rhsV, instruction;
    Type type;
    
    public MulNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public MulNum lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public MulNum rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public MulNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for multiplication", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String op;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
        case UINT:
            op = "mul";
            break;
        case FLOAT:
            op = "fmul";
            break;
        default:
            throw new RuntimeException ("Multiplying unsupported items");
        }

        instruction = new BINARY ()
            .op (op)
            .type (LLVMType.getLLVMName (type))
            .lhs (lhsV).rhs (rhsV);
        function.add (instruction);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }
}
