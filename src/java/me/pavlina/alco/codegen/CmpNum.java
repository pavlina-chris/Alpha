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
 * Numeric comparison. Compares two values of the SAME TYPE */
public class CmpNum {
    Token token;
    Instruction lhsV, rhsV, instruction;
    Type vtype, rtype;
    String cmp;
    
    public CmpNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public CmpNum lhs (Instruction lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public CmpNum rhs (Instruction rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the comparison */
    public CmpNum cmp (String cmp) {
        this.cmp = cmp;
        return this;
    }

    /**
     * Set the (non-normalised) type */
    public CmpNum type (Type type) {
        this.vtype = type.getNormalised ();
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        Type.Encoding enc = vtype.getEncoding ();
        if (enc != Type.Encoding.SINT &&
            enc != Type.Encoding.UINT &&
            enc != Type.Encoding.FLOAT &&
            enc != Type.Encoding.BOOL) {
            throw CError.at ("invalid types for compare", token);
        }
        if (enc == Type.Encoding.BOOL &&
            (!cmp.equals ("eq") && !cmp.equals ("ne"))) {
            throw CError.at ("invalid comparison for type 'bool'", token);
        }
        rtype = new Type (env, "bool", null);
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (vtype.getEncoding () == SINT || vtype.getEncoding () == UINT)
            genLLVM_int (env, emitter, function);
        else if (vtype.getEncoding () == BOOL)
            genLLVM_bool (env, emitter, function);
        else
            genLLVM_flt (env, emitter, function);
    }

    public void genLLVM_bool (Env env, Emitter emitter, Function function) {

        Instruction lhsTrue = new BINARY ()
            .op ("icmp ne").type (LLVMType.getLLVMName (vtype))
            .lhs (lhsV).rhs ("0");
        Instruction rhsTrue = new BINARY ()
            .op ("icmp ne").type (LLVMType.getLLVMName (vtype))
            .lhs (rhsV).rhs ("0");
        Instruction cmpResult = new BINARY ()
            .op ("icmp " + cmp).type ("i1")
            .lhs (lhsTrue).rhs (rhsTrue);
        instruction = new CONVERT ()
            .op ("sext").stype ("i1").dtype ("i8").value (cmpResult);
        function.add (lhsTrue);
        function.add (rhsTrue);
        function.add (cmpResult);
        function.add (instruction);
    }

    public void genLLVM_int (Env env, Emitter emitter, Function function) {
        String oper;
        switch (vtype.getEncoding ()) {
        case SINT:
            if (cmp.equals ("lt")) oper = "icmp slt";
            else if (cmp.equals ("gt")) oper = "icmp sgt";
            else if (cmp.equals ("le")) oper = "icmp sle";
            else if (cmp.equals ("ge")) oper = "icmp sge";
            else if (cmp.equals ("eq")) oper = "icmp eq";
            else if (cmp.equals ("ne")) oper = "icmp ne";
            else throw new RuntimeException ("Invalid compare");
            break;
        case UINT:
            if (cmp.equals ("lt")) oper = "icmp ult";
            else if (cmp.equals ("gt")) oper = "icmp ugt";
            else if (cmp.equals ("le")) oper = "icmp ule";
            else if (cmp.equals ("ge")) oper = "icmp uge";
            else if (cmp.equals ("eq")) oper = "icmp eq";
            else if (cmp.equals ("ne")) oper = "icmp ne";
            else throw new RuntimeException ("Invalid compare");
            break;
        default:
            throw new RuntimeException ("Invalid compare");
        }

        Instruction cmpResult = new BINARY ()
            .op (oper).type (LLVMType.getLLVMName (vtype))
            .lhs (lhsV).rhs (rhsV);
        instruction = new CONVERT ()
            .op ("sext").stype ("i1").dtype ("i8").value (cmpResult);
        function.add (cmpResult);
        function.add (instruction);
    }

    public void genLLVM_flt (Env env, Emitter emitter, Function function) {
        String oper;
        if (cmp.equals ("lt")) oper = "fcmp olt";
        else if (cmp.equals ("gt")) oper = "fcmp ogt";
        else if (cmp.equals ("le")) oper = "fcmp ole";
        else if (cmp.equals ("ge")) oper = "fcmp oge";
        else if (cmp.equals ("eq")) oper = "fcmp oeq";
        else if (cmp.equals ("ne")) oper = "fcmp one";
        else throw new RuntimeException ("Invalid compare");

        Instruction cmpResult = new BINARY ()
            .op (oper).type (LLVMType.getLLVMName (vtype))
            .lhs (lhsV).rhs (rhsV);
        instruction = new CONVERT ()
            .op ("sext").stype ("i1").dtype ("i8").value (cmpResult);
        function.add (instruction);
        function.add (cmpResult);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return rtype;
    }
}
