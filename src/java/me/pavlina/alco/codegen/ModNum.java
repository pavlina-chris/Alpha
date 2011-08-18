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
    Instruction lhsV, rhsV, instruction;
    Type type;
    
    public ModNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public ModNum lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public ModNum rhs (Instruction rhsV) {
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

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String remop, addop;
        Type.Encoding enc = type.getEncoding ();
        switch (enc) {
        case SINT:
            remop = "srem";
            addop = "add";
            break;
        case UINT:
            remop = "urem";
            addop = "add";
            break;
        case FLOAT:
            remop = "frem";
            addop = "fadd";
            break;
        default:
            assert false : enc;
            return;
        }

        // ((x REM y) + y) REM y

        Instruction xREMy = new BINARY ()
            .op (remop)
            .type (LLVMType.getLLVMName (type))
            .lhs (lhsV).rhs (rhsV);

        Instruction pPLUSy = new BINARY ()
            .op (addop)
            .type (LLVMType.getLLVMName (type))
            .lhs (xREMy).rhs (rhsV);

        instruction = new BINARY ()
            .op (remop)
            .type (LLVMType.getLLVMName (type))
            .lhs (pPLUSy).rhs (rhsV);

        function.add (xREMy);
        function.add (pPLUSy);
        function.add (instruction);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }
}
