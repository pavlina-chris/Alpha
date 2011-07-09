// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Various enums for a function header. These are used by Function,
 * FDeclare and Call. */
public class FHead
{
    /**
     * Linkage types. EXTERNALLY_VISIBLE is the default. See the LLVM
     * documentation for the definitions. */
    public enum Linkage {
        PRIVATE, LINKER_PRIVATE, LINKER_PRIVATE_WEAK,
            LINKER_PRIVATE_WEAK_DEF_AUTO, INTERNAL,
            AVAILABLE_EXTERNALLY, LINKONCE, WEAK, COMMON, APPENDING,
            EXTERN_WEAK, LINKONCE_ODR, WEAK_ODR, EXTERNALLY_VISIBLE;

        public String toString () {
            if (this == EXTERNALLY_VISIBLE) return "";
            return super.toString ().toLowerCase ();
        }
    }

    /**
     * Visibility types. DEFAULT is the default (duh). See the LLVM
     * documentation for the definitions. */
    public enum Visibility {
        DEFAULT, HIDDEN, PROTECTED;

        public String toString () {
            if (this == DEFAULT) return "";
            return super.toString ().toLowerCase ();
        }
    }

    /**
     * Calling conventions. CCC is the default. See the LLVM documentation
     * for the definitions. */
    public enum CallingConvention {
        CCC, FASTCC, COLDCC, CC10;

        public String toString () {
            if (this == CCC) return "";
            if (this == CC10) return "cc 10";
            return super.toString ().toLowerCase ();
        }
    }

    /**
     * Parameter attributes. */
    public enum ParamAttribute {
        ZEROEXT, SIGNEXT, INREG, BYVAL, SRET, NOALIAS, NOCAPTURE, NEST;

        public String toString () {
            return super.toString ().toLowerCase ();
        }
    }

    /**
     * Function attributes. */
    public enum FunctionAttribute {
        ALWAYSINLINE, INLINEHINT, HOTPATCH, NONLAZYBIND, NAKED, NOIMPLICITFLOAT,
            NOINLINE, NOREDZONE, NORETURN, NOUNWIND, OPTSIZE, READNONE,
            READONLY, SSP, SSPREQ;

        public String toString () {
            return super.toString ().toLowerCase ();
        }
    }
}
