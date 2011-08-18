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
 * Numeric division. Divides two values of the SAME TYPE */
public class DivNum {
    Token token;
    Instruction lhsV, rhsV, instruction;
    Type type;
    
    public DivNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public DivNum lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public DivNum rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public DivNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for division", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String op;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
            op = "sdiv";
            break;
        case UINT:
            op = "udiv";
            break;
        case FLOAT:
            op = "fdiv";
            break;
        default:
            assert false: enc;
            return;
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
