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
 * Bitwise AND. */
public class BitAnd {
    Token token;
    Instruction lhsV, rhsV, instruction;
    Type type;
    
    public BitAnd (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public BitAnd lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public BitAnd rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public BitAnd type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT) {
            throw CError.at ("invalid types for bitwise op", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        instruction = new BINARY ()
            .op ("and")
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
