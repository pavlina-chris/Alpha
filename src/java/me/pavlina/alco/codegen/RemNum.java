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
 * Numeric remainder. (-3 %% 2 is -1)
 */
public class RemNum {
    Token token;
    Instruction lhsV, rhsV, instruction;
    Type type;
    
    public RemNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public RemNum lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public RemNum rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public RemNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for remainder", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String remop;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
            remop = "srem";
            break;
        case UINT:
            remop = "urem";
            break;
        case FLOAT:
            remop = "frem";
            break;
        default:
            throw new RuntimeException ("Remainder of unsupported items");
        }

        instruction = new BINARY ()
            .op (remop)
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
