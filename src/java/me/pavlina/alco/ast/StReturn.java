// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Return (duh). Syntax:
 * return ;
 * return {expression} ;
 */
public class StReturn extends Statement
{
    private Token token;
    private Expression[] value;
    List<Expression> values;
    private Method method;

    public StReturn (Env env, TokenStream stream, Method method) throws CError {
        this.method = method;
        token = stream.next ();
        if (!token.is (Token.WORD, "return"))
            throw new RuntimeException ("StReturn instantiated without kwd");

        value = new Expression[]
            {Expression.parse (env, stream, method, ";")}; // Allow null

        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (";", stream.last ());
        else if (!temp.is (Token.OPER, ";"))
            throw Unexpected.after (";", stream.last ());
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (value);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        if (value[0] == null) {
            if (method.getType () != null) {
                throw CError.at ("length of return list does not match " +
                                 "function declaration", token);
            }
            return;
        }

        // Unpack tuples
        values = new ArrayList<Expression> ();
        if (OpComma.class.isInstance (value[0])) {
            ((OpComma) value[0]).unpack (values);
        } else {
            values.add (value[0]);
        }

        for (Expression i: values)
            i.checkTypes (env, resolver);

        List<Type> methodTypes = method.getTypes ();

        if (values.size () != methodTypes.size ())
            throw CError.at ("length of return list does not match function" +
                             " declaration", token);

        for (int i = 0; i < values.size (); ++i) {
            Type.checkCoerce (values.get (i), methodTypes.get (i), token);
        }
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (value[0] == null) {
            // Starting a new block. Increment temp counter to avoid
            // "instruction expected to be numbered '%blah'" errors from LLC
            emitter.getTemporary ("%");
            new ret (emitter, function).build ();
        } else {
            List<Type> methodTypes = method.getTypes ();
            for (int i = 1; i < values.size (); ++i) {
                values.get (i).genLLVM (env, emitter, function);
                String valueString = values.get (i).getValueString ();
                Cast c = new Cast (token)
                    .value (valueString).type (values.get (i).getType ())
                    .dest (methodTypes.get (i));
                c.genLLVM (env, emitter, function);

                new store (emitter, function)
                    .pointer ("%.R" + Integer.toString (i))
                    .value (LLVMType.getLLVMName (values.get (i).getType ()),
                            c.getValueString ())
                    .build ();
            }
            values.get (0).genLLVM (env, emitter, function);
            String valueString = values.get (0).getValueString ();
            Cast c = new Cast (token)
                .value (valueString).type (values.get (0).getType ())
                .dest (methodTypes.get (0));
            c.genLLVM (env, emitter, function);

            // Starting a new block. Increment temp counter to avoid
            // "instruction expected to be numbered '%blah'" errors from LLC
            emitter.getTemporary ("%");
            new ret (emitter, function)
                .value (LLVMType.getLLVMName (values.get (0).getType ()),
                        c.getValueString ())
                .build ();
        }
    }

    public void print (java.io.PrintStream out) {
        if (value[0] == null)
            out.print ("(return)");
        else {
            out.print ("(return ");
            value[0].print (out);
            out.print (")");
        }
    }

    public static Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method)
                    throws CError
                {
                    return new StReturn (env, stream, method);
                }
            };
    }
}
