// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import me.pavlina.alco.codegen.Cast;
import me.pavlina.alco.codegen.NewArray;
import me.pavlina.alco.codegen.NewBackedArray;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.IntLimits;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.parse.ExpressionParser;
import me.pavlina.alco.parse.TypeParser;
import static me.pavlina.alco.language.Type.Encoding;

/**
 * Array literals */
public class ArrayValue extends Expression
{
    Token token;
    Type type, subtype;
    Instruction instruction;
    List<Expression> items;
    List<Boolean> isLiteral;

    public ArrayValue (Env env, TokenStream stream, Method method) throws CError
    {
        token = stream.next ();
        assert token.is (Token.OPER, "{");
        items = new ArrayList<Expression> ();
        isLiteral = new ArrayList<Boolean> ();
        for (;;) {
            Token temp = stream.peek ();
            if (temp.is (Token.OPER, ",") || temp.is (Token.OPER, "}"))
                throw Unexpected.at ("expression", stream.last ());
            Expression exp = ExpressionParser.parse (env, stream, method, ",}");
            items.add (exp);
            temp = stream.next ();
            if (temp.is (Token.OPER, "}"))
                break;
            else if (!temp.is (Token.OPER, ","))
                throw Unexpected.at (", or }", stream.last ());
        }
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) items;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        // Usually, all expressions in an array literal must be of the same
        // type. However, there are a few cases where multiple types are
        // allowed. The array type, in these cases, is always the least upper
        // bound.
        //   Mixed integer literals
        //   Pointers and 'null'
        //   Arrays and 'null'
        //   Object references and 'null'

        Type lub = null;
        Type nullType = Type.getNull ();
        Encoding lubE = null;
        Encoding nullE = nullType.getEncoding ();
        BigInteger glbInt = null, lubInt = null;

        for (Expression i: items) {
            i.checkTypes (env, resolver);
            Type type = i.getType ();
            Encoding enc = type.getEncoding ();
            if (lub == null || lub.equals (nullType)) {
                lub = type;
                lubE = type.getEncoding ();
                if (type.getValue () != null)
                    glbInt = lubInt = type.getValue ();
            } else if (lubE == Encoding.POINTER && enc == Encoding.NULL) {
                // Do nothing
            } else if (lubE == Encoding.POINTER && enc == Encoding.POINTER) {
                if (!lub.getSubtype ().equals (type.getSubtype ())) {
                    throw CError.at
                        ("array elements must match in type (expected "
                         + lub.getType () + ")", i.getToken ());
                }
            } else if (lubE == Encoding.ARRAY && enc == Encoding.NULL) {
                // Do nothing
            } else if (lubE == Encoding.ARRAY && enc == Encoding.ARRAY) {
                if (!lub.getSubtype ().equals (type.getSubtype ())) {
                    throw CError.at
                        ("array elements must match in type (expected "
                         + lub.getType () + ")", i.getToken ());
                }
            } else if (lub.getValue () != null && type.getValue () != null) {
                // Mixed integer literals
                if (type.getValue ().compareTo (lubInt) > 0)
                    lubInt = type.getValue ();
                if (type.getValue ().compareTo (glbInt) < 0)
                    glbInt = type.getValue ();
            } else if (!lub.equals (type)) {
                throw CError.at
                    ("array elements must match in type (expected "
                     + lub.getType () + ")", i.getToken ());
            }
        }
        if (lubInt == null)
            subtype = lub;
        else {
            if (lubInt.compareTo (IntLimits.INT_MIN) >= 0 &&
                lubInt.compareTo (IntLimits.INT_MAX) <= 0 &&
                glbInt.compareTo (IntLimits.INT_MIN) >= 0 &&
                glbInt.compareTo (IntLimits.INT_MAX) <= 0)
                subtype = new Type (env, "int", null);
            else if (lubInt.compareTo (IntLimits.I64_MIN) >= 0 &&
                     lubInt.compareTo (IntLimits.I64_MAX) <= 0 &&
                     glbInt.compareTo (IntLimits.I64_MIN) <= 0 &&
                     glbInt.compareTo (IntLimits.I64_MAX) >= 0)
                subtype = new Type (env, "i64", null);
            else if (lubInt.compareTo (IntLimits.U64_MIN) >= 0 &&
                     lubInt.compareTo (IntLimits.U64_MAX) <= 0 &&
                     glbInt.compareTo (IntLimits.U64_MIN) >= 0 &&
                     glbInt.compareTo (IntLimits.U64_MAX) <= 0)
                subtype = new Type (env, "u64", null);
            else
                throw CError.at ("integer magnitude too high", token);

        }
        type = subtype.getArray (env).getConst ();
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // If everything is a literal, create a true constant. Otherwise,
        // create a threadlocal with the non-literals as zeros, then generate
        // code to fill them in.

        // This isn't the most efficient code (we traverse the array multiple
        // times), but honestly, array literals aren't going to be _that_ long.

        StringBuilder value = new StringBuilder ();
        String ty = LLVMType.getLLVMName (subtype);
        value.append ("[ ");
        boolean first = true;
        boolean foundNonLiteral = false;
        for (Expression i: items) {
            if (first) first = false;
            else value.append (", ");
            value.append (ty).append (' ');
            if (IntValue.class.isInstance (i)) {
                BigInteger val = ((IntValue) i).getValue ();
                value.append (val);
                isLiteral.add (true);
            } else if (RealValue.class.isInstance (i)) {
                double val = ((RealValue) i).getValue ();
                value.append (String.format
                           ("0x%016x", Double.doubleToRawLongBits (val)));
                isLiteral.add (true);
            } else if (BoolValue.class.isInstance (i)) {
                boolean val = ((BoolValue) i).getValue ();
                value.append (val ? "-1" : "0");
                isLiteral.add (true);
            } else {
                value.append ("0");
                isLiteral.add (false);
                foundNonLiteral = true;
            }
        }
        value.append (" ]");

        String arrTy = "[" + isLiteral.size () + " x "
            + LLVMType.getLLVMName (subtype) + "]";
        String size_t = "i" + env.getBits ();
        String strTy = "{" + size_t + ", "
            + arrTy + "*}";
        if (foundNonLiteral) {
            Global g = new Global
                (arrTy,
                 value.toString (),
                 "thread_local");
            emitter.add (g);
            Instruction arrStruct = new ALLOCA ().type (strTy);
            Instruction arrStructSz = new GETELEMENTPTR ()
                .type (strTy + "*").rtype (size_t).value (arrStruct)
                .addIndex (0).addIndex (0);
            function.add (arrStruct);
            function.add (arrStructSz);
            function.add (new STORE ().pointer (arrStructSz)
                    .type (size_t)
                    .value (Integer.toString (isLiteral.size ())));
            Instruction arrStructPtr = new GETELEMENTPTR ()
                .type (strTy + "*").rtype (arrTy + "*").value (arrStruct)
                .addIndex (0).addIndex (1);
            function.add (arrStructPtr);
            function.add (new STORE ().pointer (arrStructPtr).value (g));
            instruction = new CONVERT ()
                .op ("bitcast").stype (strTy + "*").dtype ("%.nonprim")
                .value (arrStruct);
            function.add (instruction);

            for (int i = 0; i < isLiteral.size (); ++i) {
                if (isLiteral.get (i))
                    continue;
                items.get (i).genLLVM (env, emitter, function);
                Instruction ptr = new GETELEMENTPTR ()
                    .type (arrTy + "*")
                    .rtype (ty + "*")
                    .value (g)
                    .addIndex (0).addIndex (i);
                function.add (ptr);
                function.add (new STORE ().pointer (ptr)
                              .value (items.get (i).getInstruction ()));
            }
        } else {
            Constant c = new Constant
                (arrTy,
                 value.toString (),
                 "internal");
            emitter.add (c);
            Instruction arrStruct = new ALLOCA ().type (strTy);
            Instruction arrStructSz = new GETELEMENTPTR ()
                .type (strTy + "*").rtype (size_t).value (arrStruct)
                .addIndex (0).addIndex (0);
            function.add (arrStruct);
            function.add (arrStructSz);
            function.add (new STORE ().pointer (arrStructSz)
                    .type (size_t)
                    .value (Integer.toString (isLiteral.size ())));
            Instruction arrStructPtr = new GETELEMENTPTR ()
                .type (strTy + "*").rtype (arrTy + "*").value (arrStruct)
                .addIndex (0).addIndex (1);
            function.add (arrStructPtr);
            function.add (new STORE ().pointer (arrStructPtr).value (c));
            instruction = new CONVERT ()
                .op ("bitcast").stype (strTy + "*").dtype ("%.nonprim")
                .value (arrStruct);
            function.add (instruction);
        }
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("array literal has no address; " +
                         "assign to variable first", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }

    public void print (PrintStream out) {
        StringBuilder sb = new StringBuilder ();
        sb.append ("(array");
        for (Expression i: items) {
            sb.append (' ');
            i.print (out);
        }
        sb.append (')');
        out.print (sb);
    }

}
