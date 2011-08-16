// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.lex.Token;

/**
 * Subtraction of pointers. Subtracts two pointers of the same type. */
public class Sub2Ptr {
    Instruction lhsV, rhsV, instruction;
    Type type, ptrT;

    public Sub2Ptr (Token token) {
    }

    /**
     * Set the left-hand operand. */
    public Sub2Ptr lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public Sub2Ptr rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public Sub2Ptr type (Type type) {
        ptrT = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        type = new Type (env, "ssize", null);
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        String intermedT = "i" + Integer.toString (env.getBits ());

        Instruction lhsAsInt = new CONVERT ()
            .op ("ptrtoint")
            .stype (LLVMType.getLLVMName (ptrT)).dtype (intermedT)
            .value (lhsV);
        Instruction rhsAsInt = new CONVERT ()
            .op ("ptrtoint")
            .stype (LLVMType.getLLVMName (ptrT)).dtype (intermedT)
            .value (rhsV);
        Instruction diff = new BINARY ()
            .op ("sub").type (intermedT).lhs (lhsAsInt).rhs (rhsAsInt);
        instruction = new BINARY ()
            .op ("sdiv").type (intermedT).lhs (diff)
            .rhs (Integer.toString (ptrT.getSubtype ().getSize ()));

        function.add (lhsAsInt);
        function.add (rhsAsInt);
        function.add (diff);
        function.add (instruction);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

}
