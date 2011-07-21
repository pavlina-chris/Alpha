// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;

/**
 * Coercion of types. Performs default coercion - if you need anything else,
 * check for it first */
public class Coerce {
    Token token;
    String lhsV, rhsV;
    Type lhsT, rhsT, type;
    int castSide; // -1 L, 0 None, 1 R

    public Coerce (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand type.
     */
    public Coerce lhsT (Type lhsT) {
        this.lhsT = lhsT;
        return this;
    }

    /**
     * Set the right-hand type.
     */
    public Coerce rhsT (Type rhsT) {
        this.rhsT = rhsT;
        return this;
    }

    /**
     * Set the left-hand value.
     */
    public Coerce lhsV (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand value.
     */
    public Coerce rhsV (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        castSide = Type.arithCoerce (lhsT, rhsT, token);
        type = (castSide == 1 ? lhsT : rhsT).getNormalised ();
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (castSide == 1) {
            Cast c = new Cast (token)
                .value (rhsV).type (rhsT).dest (type);
            c.genLLVM (env, emitter, function);
            rhsV = c.getValueString ();
        } else if (castSide == -1) {
            Cast c = new Cast (token)
                .value (lhsV).type (lhsT).dest (type);
            c.genLLVM (env, emitter, function);
            lhsV = c.getValueString ();
        }
    }

    public Type getType () {
        return type;
    }

    public String getValueStringL () {
        return lhsV;
    }

    public String getValueStringR () {
        return rhsV;
    }
}
