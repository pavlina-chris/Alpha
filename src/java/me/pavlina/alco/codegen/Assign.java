// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.ast.OpComma;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.lex.Token;
import java.util.List;
import java.util.ArrayList;

/**
 * Assignment. Assigns a sequence of values to a sequence of destinations.
 * Do NOT use for call assignment. */
public class Assign {
    Token token;
    Instruction instruction;
    List<Expression> sources, dests;

    public Assign (Token token) {
        this.token = token;
    }

    /**
     * Required: Set sources
     * @param sources Expression. Can be one source or an OpComma linking them.
     */
    public Assign sources (Expression sources) {
        this.sources = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (sources))
            ((OpComma) sources).unpack (this.sources);
        else
            this.sources.add (sources);
        return this;
    }

    /**
     * Required: Set destinations
     * @param dests Expression. Can be one destination or an OpComma
     * linking them.
     */
    public Assign dests (Expression dests) {
        this.dests = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (dests))
            ((OpComma) dests).unpack (this.dests);
        else
            this.dests.add (dests);
        return this;
    }

    /**
     * Check types. */
    public void checkTypes (Env env, Resolver resolver) throws CError {
        // Recursion
        for (Expression i: sources) {
            i.checkTypes (env, resolver);
        }
        for (Expression i: dests) {
            i.checkTypes (env, resolver);
            i.checkPointer (true, token);
        }

        // Implicit casts
        int limit = (sources.size () < dests.size ())
            ? sources.size ()
            : dests.size ();
        for (int i = 0; i < limit; ++i) {
            if (!Type.canCoerce (sources.get (i), dests.get (i)))
                throw CError.at ("invalid implicit cast: " +
                                 sources.get (i).getType ().toString () +
                                 " to " +
                                 dests.get (i).getType ().toString (),
                                 token);
        }

        // Symmetry
        if (sources.size () != dests.size ()) {
            env.warning_at ("multiple assign is not symmetric; only matching "
                            + "pairs will be assigned", token);
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        int limit = (sources.size () < dests.size ())
            ? sources.size ()
            : dests.size ();

        // Generate all expressions
        Instruction[] values = new Instruction[limit];
        for (int i = 0; i < limit; ++i) {
            sources.get (i).genLLVM (env, emitter, function);
            Cast c = new Cast (token)
                .value (sources.get (i).getInstruction ())
                .type (sources.get (i).getType ())
                .dest (dests.get (i).getType ());
            c.genLLVM (env, emitter, function);
            values[i] = c.getInstruction ();
        }
        instruction = values[0];

        // Generate all destinations
        Instruction[] pointers = new Instruction[limit];
        for (int i = 0; i < limit; ++i) {
            pointers[i] = dests.get (i).getPointer (env, emitter, function);
        }

        // Store all values
        for (int i = 0; i < limit; ++i) {
            function.add (new STORE ()
                          .pointer (pointers[i])
                          .value (values[i])
                          ._volatile (dests.get (i).getType ().isVolatile ()));
        }
    }

    public Instruction getInstruction () {
        return instruction;
    }
}
