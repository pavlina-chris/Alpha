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
 * Numeric negation */
public class NegNum {
    Token token;
    Instruction opndV, instruction;
    Type type;

    public NegNum (Token token) {
        this.token = token;
    }

    /**
     * Set the operand */
    public NegNum operand (Instruction opnd) {
        this.opndV = opnd;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public NegNum type (Type type) {
        this.type = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = type.getEncoding ();
        if (enc == Type.Encoding.UINT)
            throw CError.at ("negation of unsigned integer", token);
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid type for negation", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (type.getEncoding () == Type.Encoding.FLOAT) {
            instruction = new BINARY ()
                .op ("fsub")
                .type (LLVMType.getLLVMName (getType ()))
                .lhs ("0.0").rhs (opndV);
        } else { // SINT
            instruction = new BINARY ()
                .op ("sub")
                .type (LLVMType.getLLVMName (getType ()))
                .lhs ("0").rhs (opndV);
        }
        function.add (instruction);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }
}
