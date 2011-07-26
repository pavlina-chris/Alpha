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
    String lhsV, rhsV, valueString;
    Type vtype, rtype;
    Comparison cmp;
    
    public CmpNum (Token token) {
        this.token = token;
    }

    /**
     * Set the left-hand operand. */
    public CmpNum lhs (String lhsV) {
        this.lhsV = lhsV;
        return this;
    }

    /**
     * Set the right-hand operand. */
    public CmpNum rhs (String rhsV) {
        this.rhsV = rhsV;
        return this;
    }

    /**
     * Set the comparison */
    public CmpNum cmp (Comparison cmp) {
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
            enc != Type.Encoding.FLOAT) {
            throw CError.at ("invalid types for division", token);
        }
        rtype = new Type (env, "bool", null);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        if (vtype.getEncoding () == SINT || vtype.getEncoding () == UINT)
            genLLVM_int (env, emitter, function);
        else
            genLLVM_flt (env, emitter, function);
    }

    public void genLLVM_int (Env env, LLVMEmitter emitter, Function function) {
        icmp.Icmp cmp;
        switch (vtype.getEncoding ()) {
        case SINT:
            switch (this.cmp) {
            case LT: cmp = icmp.Icmp.SLT; break;
            case GT: cmp = icmp.Icmp.SGT; break;
            case LE: cmp = icmp.Icmp.SLE; break;
            case GE: cmp = icmp.Icmp.SGE; break;
            case EQ: cmp = icmp.Icmp.EQ; break;
            case NE: cmp = icmp.Icmp.NE; break;
            default:
                throw new RuntimeException ("Invalid compare");
            } break;
        case UINT:
            switch (this.cmp) {
            case LT: cmp = icmp.Icmp.ULT; break;
            case GT: cmp = icmp.Icmp.UGT; break;
            case LE: cmp = icmp.Icmp.ULE; break;
            case GE: cmp = icmp.Icmp.UGE; break;
            case EQ: cmp = icmp.Icmp.EQ; break;
            case NE: cmp = icmp.Icmp.NE; break;
            default:
                throw new RuntimeException ("Invalid compare");
            } break;
        default:
            throw new RuntimeException ("Invalid compare");
        }

        String cmpResult = new icmp (emitter, function)
            .comparison (cmp)
            .type (LLVMType.getLLVMName (vtype))
            .operands (lhsV, rhsV)
            .build ();
        valueString = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.SEXT)
            .source ("i1", cmpResult)
            .dest ("i8")
            .build ();
    }

    public void genLLVM_flt (Env env, LLVMEmitter emitter, Function function) {
        fcmp.Fcmp cmp;
        switch (this.cmp) {
        case LT: cmp = fcmp.Fcmp.OLT; break;
        case GT: cmp = fcmp.Fcmp.OGT; break;
        case LE: cmp = fcmp.Fcmp.OLE; break;
        case GE: cmp = fcmp.Fcmp.OGE; break;
        case EQ: cmp = fcmp.Fcmp.OEQ; break;
        case NE: cmp = fcmp.Fcmp.ONE; break;
        default:
            throw new RuntimeException ("Invalid compare");
        }

        String cmpResult = new fcmp (emitter, function)
            .comparison (cmp)
            .type (LLVMType.getLLVMName (vtype))
            .operands (lhsV, rhsV)
            .build ();
        valueString = new Conversion (emitter, function)
            .operation (Conversion.ConvOp.SEXT)
            .source ("i1", cmpResult)
            .dest ("i8")
            .build ();
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return rtype;
    }


    public enum Comparison {
        EQ, NE, LE, GE, LT, GT;
    }
}
