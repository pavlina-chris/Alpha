// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.ast.IntValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigInteger;
import static me.pavlina.alco.language.IntLimits.*;

/**
 * Alpha type. */
public class Type implements HasType {

    String name;
    /* This ivar has a few uses, depending on the encoding:
     *  - ARRAY: Array type (for int[], it will contain int)
     *  - POINTER: Pointer type (for int*, it will contain int)
     *  - OBJECT: Arguments (for map<string, int>, it will contain string
     *      and int.
     */
    List<Type> subtypes;
    int size;
    Encoding encoding;
    boolean isConst;
    boolean isVolatile;
    BigInteger intVal;

    /**
     * Initialise a type. This is a quick constructor that will build a nested
     * type from specification. The param documentation provides example
     * values; these are for building the type map&lt;string, int&gt;[]*[]
     * (array of pointers to arrays of maps(string to int). Any of these params
     * (except name) may be null, including mods.
     * @param env Compile environment
     * @param name Base name of the type: "map"
     * @param args Array arguments: {Type("string"), Type("int")}
     * @param mods Modifiers: {ARRAY, POINTER, ARRAY} */
    public Type (Env env, String name, List<Type> args, Modifier... mods) {
        
        int modsLength = mods.length;
        isConst = false;
        // Eat any qualifier keywords
        while (modsLength != 0) {
            if (mods[modsLength - 1] == Modifier.CONST) {
                isConst = true;
            } else if (mods[modsLength - 1] == Modifier.VOLATILE) {
                isVolatile = true;
            } else break;
            --modsLength;
        }

        // If there are no mods, type creation is simple.
        if (modsLength == 0) {
            this.baseType (env, name, args);
        }

        // Otherwise, the root type is the last mod, and recurse.
        else if (mods[modsLength - 1] == Modifier.ARRAY) {
            this.name = "";
            this.size = Type.OBJECT_SIZE;
            this.encoding = Encoding.ARRAY;
            this.subtypes = new ArrayList<Type> (1);
            this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                         (mods, modsLength - 1)));
        } else /* Modifier.POINTER */ {
            this.name = "";
            this.size = env.getBits () / 8;
            this.encoding = Encoding.POINTER;
            this.subtypes = new ArrayList<Type> (1);
            this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                         (mods, modsLength - 1)));
        }
    }

    /**
     * Set the integer value of a type. Integers for which the value is known
     * at compile-time (literals) have special semantics. */
    public void setValue (BigInteger intVal) {
        this.intVal = intVal;
    }

    /**
     * Get the integer value of a type.
     * @return Integer value, or null if there is none */
    public BigInteger getValue () {
        return intVal;
    }

    /**
     * Get the unnamed null type. */
    public static Type getNull () {
        return nullType;
    }

    private static Type nullType;
    static {
        nullType = new Type ();
        nullType.name = "";
        nullType.size = 0;
        nullType.encoding = Encoding.NULL;
        nullType.subtypes = null;
    }

    /**
     * Basic type initialiser */
    private void baseType (Env env, String name, List<Type> args) {
        this.name = name;
        Encoding enc = this.PRIMITIVE_ENCODINGS.get (name);
        if (enc == null) {
            // Object type
            this.size = Type.OBJECT_SIZE;
            this.encoding = Encoding.OBJECT;
            if (args == null)
                this.subtypes = null;
            else
                this.subtypes = new ArrayList<Type> (args);
        } else {
            this.size = Type.PRIMITIVE_SIZES.get (name);
            if (this.size == -1)
                this.size = env.getBits () / 8;
            this.encoding = enc;
            this.subtypes = null;
        }
    }

    private Type () {}

    /**
     * Get the basic name of the type. Arrays and pointers will return an
     * empty string. */
    public String getName () {
        return name;
    }
    
    /**
     * Returns this. */
    public Type getType () {
        return this;
    }

    /**
     * Cheat: returns null. */
    public Token getToken () {
        return null;
    }

    /**
     * Get the type arguments. Returns null for non-object types. */
    public List<Type> getArguments () {
        if (encoding == Encoding.OBJECT)
            return Collections.unmodifiableList (subtypes);
        else
            return null;
    }

    /**
     * Get the subtype. Returns null for types other than array and pointer. */
    public Type getSubtype () {
        if (encoding == Encoding.ARRAY || encoding == Encoding.POINTER)
            return subtypes.get (0);
        else
            return null;
    }

    /**
     * Get a pointer type to this type. */
    public Type getPointer (Env env) {
        Type t = new Type ();
        t.name = "";
        t.size = env.getBits () / 8;
        t.encoding = Encoding.POINTER;
        t.subtypes = new ArrayList<Type> (1);
        t.subtypes.add (this);
        return t;
    }

    /**
     * Get the size in bytes. */
    public int getSize () {
        return size;
    }

    /**
     * Get the encoding. */
    public Encoding getEncoding () {
        return encoding;
    }

    /**
     * Get whether the type is constant */
    public boolean isConst () {
        return isConst;
    }

    /**
     * Get whether the type is volatile */
    public boolean isVolatile () {
        return isVolatile;
    }

    /**
     * Return a copy of this which is constant. */
    public Type getConst () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.intVal = intVal;
        t.isConst = true;
        t.isVolatile = isVolatile;
        return t;
    }

    /**
     * Return a copy of this which is volatile */
    public Type getVolatile () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.intVal = intVal;
        t.isConst = isConst;
        t.isVolatile = true;
        return t;
    }

    /**
     * Return a copy of this which is not constant. */
    public Type getNotConst () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.intVal = intVal;
        t.isConst = false;
        t.isVolatile = isVolatile;
        return t;
    }

    /**
     * Return a copy of this which is not volatile. */
    public Type getNotVolatile () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.intVal = intVal;
        t.isConst = isConst;
        t.isVolatile = false;
        return t;
    }

    /**
     * Return a normalised copy of this type. This is the type with the const
     * and value qualifiers removed. The type of (a op b) is usually the
     * normalised type of the operands, once they have been coerced. */
    public Type getNormalised () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.intVal = null;
        t.isConst = false;
        t.isVolatile = false;
        return t;
    }

    /**
     * Return a non-literal copy of this type. This removes the value
     * qualifier. */
    public Type getNonLiteral () {
        Type t = new Type ();
        t.name = name;
        t.subtypes = subtypes;
        t.size = size;
        t.encoding = encoding;
        t.isConst = isConst;
        t.isVolatile = isVolatile;
        t.intVal = null;
        return t;
    }

    /**
     * Get the encoded type name. This is used in code, both in the dynamic
     * type system and in method name mangling. It is fully reversible
     * (see fromEncodedName()). See Standard:CallingConvention:NameMangling */
    public String getEncodedName () {
        // Standard:CallingConvention:NameMangling
        String prefix = (isConst ? "K" : "");
        if (encoding == Encoding.SINT) {
            switch (size) {
            case 1: return prefix + "A";
            case 2: return prefix + "B";
            case 4: return prefix + "C";
            case 8: return prefix + "D";
            }
        } else if (encoding == Encoding.UINT) {
            switch (size) {
            case 1: return prefix + "E";
            case 2: return prefix + "F";
            case 4: return prefix + "G";
            case 5: return prefix + "H";
            }
        } else if (encoding == Encoding.FLOAT) {
            switch (size) {
            case 4: return prefix + "I";
            case 8: return prefix + "J";
            }
        } else if (encoding == Encoding.BOOL) {
            return prefix + "T";
        } else if (encoding == Encoding.POINTER) {
            return prefix + "P" + subtypes.get (0).getEncodedName ();
        } else if (encoding == Encoding.ARRAY) {
            return prefix + "Q" + subtypes.get (0).getEncodedName ();
        } else if (encoding == Encoding.OBJECT) {
            return prefix + "M" + Integer.toString (name.length ()) + name;
        }
        throw new RuntimeException ("getEncodedName() on invalid type");
    }

    /**
     * Get a Type from an encoded type name. Returns null on error.
     * Create a MangleReader to read from the encoded name. A single
     * MangleReader can be used to get multiple types from one string. */
    public static Type fromEncodedName (Env env, MangleReader reader) {
        try {
            char ch = reader.nextChar ();
            switch (ch) {
            case 'A': return new Type (env, "i8", null);
            case 'B': return new Type (env, "i16", null);
            case 'C': return new Type (env, "int", null);
            case 'D': return new Type (env, "i64", null);
            case 'E': return new Type (env, "u8", null);
            case 'F': return new Type (env, "u16", null);
            case 'G': return new Type (env, "unsigned", null);
            case 'H': return new Type (env, "u64", null);
            case 'I': return new Type (env, "float", null);
            case 'J': return new Type (env, "double", null);
            case 'T': return new Type (env, "bool", null);
            case 'K':
                {
                    Type baseType = Type.fromEncodedName (env, reader);
                    return baseType.getConst ();
                }
            case 'P':
                {
                    Type baseType = Type.fromEncodedName (env, reader);
                    if (baseType == null) return null;
                    Type ty = new Type ();
                    ty.name = "";
                    ty.subtypes = new ArrayList<Type> (1);
                    ty.subtypes.add (baseType);
                    ty.size = env.getBits () / 8;
                    ty.encoding = Encoding.POINTER;
                    return ty;
                }
            case 'Q':
                {
                    Type baseType = Type.fromEncodedName (env, reader);
                    if (baseType == null) return null;
                    Type ty = new Type ();
                    ty.name = "";
                    ty.subtypes = new ArrayList<Type> (1);
                    ty.subtypes.add (baseType);
                    ty.size = Type.OBJECT_SIZE;
                    ty.encoding = Encoding.ARRAY;
                    return ty;
                }
            case 'M':
                {
                    int len = reader.nextInt ();
                    String name = reader.nextString (len);
                    Type ty = new Type ();
                    ty.name = name;
                    ty.subtypes = null;
                    ty.size = Type.OBJECT_SIZE;
                    ty.encoding = Encoding.OBJECT;
                    return ty;
                }
            default:
                return null;
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Try an arithmetic coercion. This applies standard rules
     * (Standard:Types:Casting:Coercion) to the two types, and returns which
     * of the two must be converted.
     * @return -1: left, 0: neither, 1: right
     */
    public static int arithCoerce (HasType left, HasType right, Token token)
        throws CError
    {
        int leftRank = arithCoerceRank (left.getType ());
        int rightRank = arithCoerceRank (right.getType ());

        if (leftRank == -1 || rightRank == -1)
            throw CError.at ("invalid type for operation", token);

        if (leftRank < rightRank) {
            checkCoerce (right, left, token);
            return 1;
        } else if (rightRank < leftRank) {
            checkCoerce (left, right, token);
            return -1;
        } else
            return 0;
    }

    /**
     * Return the arithmetic coercion type rank
     * (Standard:Types:Casting:Coercion), or -1 for non-coercible types */
    private static int arithCoerceRank (Type t)
    {
        Encoding enc = t.getEncoding ();
        int size = t.getSize ();
        if (enc == Encoding.BOOL) {
            return 5;
        } else if (enc == Encoding.FLOAT) {
            if (size == 8)      return 10;
            else                return 15;
        } else if (enc == Encoding.UINT) {
            if (size == 8)      return 20;
            else if (size == 4) return 25;
            else if (size == 2) return 30;
            else                return 35;
        } else if (enc == Encoding.SINT) {
            if (size == 8)      return 40;
            else if (size == 4) return 45;
            else if (size == 2) return 50;
            else                return 55;
        } else
            return -1;
    }

    /**
     * Check whether a value can be coerced to a type, and give a standard
     * error if not. */
    public static void checkCoerce (HasType value, HasType destination,
                                    Token token) throws CError
    {
        if (!canCoerce (value, destination)) {
            Type vtype = value.getType ();
            Type dtype = destination.getType ();
            throw CError.at ("invalid implicit cast: " +
                             vtype.toString () + " to " +
                             dtype.toString (), token);
        }
    }

    /**
     * Check whether a value can be coerced to a type.
     * @returns Whether the coercion is possible
     */
    public static boolean canCoerce (HasType value, HasType destination)
    {
        Type vtype = value.getType ();
        Type dtype = destination.getType ();

        if (vtype.equals (dtype)) {
            // Same type - no cast
            return true;

        } else if (vtype.equalsNoQual (dtype) && !vtype.isConst () &&
                   dtype.isConst ()) {
            // T to T const
            return true;

        } else if (vtype.encoding == Encoding.SINT
            && dtype.encoding == vtype.encoding
            && vtype.size <= dtype.size) {
            // SIa to SIb where b >= a (signed upcast)
            return true;

        } else if (vtype.encoding == Encoding.UINT
                   && dtype.encoding == vtype.encoding
                   && vtype.size <= dtype.size) {
            // UIa to UIb where b >= a (unsigned upcast)
            return true;
        
        } else if (vtype.encoding == Encoding.SINT
                   && dtype.encoding == Encoding.BOOL) {
            // SIa to B
            return true;

        } else if (vtype.encoding == Encoding.UINT
                   && dtype.encoding == Encoding.BOOL) {
            // UIa to B
            return true;

        } else if (vtype.getValue () != null
                   && dtype.encoding == Encoding.UINT) {
            // The standard mentions both:
            //    IntValue within SIa to SIa
            //    IntValue within UIa to UIa
            // Because IntValues are implicitly i32/i64, the first one will
            // be done by "SIa to SIb". However, "SIa to UIb" is normally
            // illegal, so we have to specifically check for it.
            
            BigInteger val = vtype.getValue (), min, max;

            min = BigInteger.ZERO;
            if (dtype.size == 1)
                max = U8_MAX;
            else if (dtype.size == 2)
                max = U16_MAX;
            else if (dtype.size == 4)
                max = U32_MAX;
            else if (dtype.size == 8)
                max = U64_MAX;
            else
                throw new RuntimeException ("Bad type size");
            if (min.compareTo (val) <= 0
                && max.compareTo (val) >= 0) {
                return true;
            }

        } else if (vtype.getValue () != null
                   && dtype.encoding == Encoding.SINT) {
            // The standard mentions both:
            //    IntValue within SIa to SIa
            //    IntValue within UIa to UIa
            // Because IntValues are implicitly i32/i64, the first one will
            // be done by "SIa to SIb". However, "SIa to UIb" is normally
            // illegal, so we have to specifically check for it.
            
            BigInteger val = vtype.getValue (), min, max;

            if (dtype.size == 1) {
                min = I8_MIN;
                max = I8_MAX;
            } else if (dtype.size == 2) {
                min = I16_MIN;
                max = I16_MAX;
            } else if (dtype.size == 4) {
                min = I32_MIN;
                max = I32_MAX;
            } else if (dtype.size == 8) {
                min = I64_MIN;
                max = I64_MAX;
            } else
                throw new RuntimeException ("Bad type size");
            if (min.compareTo (val) <= 0
                && max.compareTo (val) >= 0) {
                return true;
            }

        } else if (vtype.encoding == Encoding.FLOAT
                   && dtype.encoding == Encoding.FLOAT
                   && vtype.size <= dtype.size) {
            // FPa to FPb where b >= a (floating point upcast)
            return true;

        } else if (vtype.encoding == Encoding.POINTER
                   && dtype.encoding == Encoding.BOOL) {
            // T* to B
            return true;

        } else if (vtype.encoding == Encoding.ARRAY
                   && dtype.encoding == Encoding.POINTER
                   && vtype.getSubtype ().equals (dtype.getSubtype ())) {
            // T[] to T*
            throw new RuntimeException ("T[] to T* cast not implemented yet");

        } else if (vtype.encoding == Encoding.NULL
                   && (dtype.encoding == Encoding.SINT ||
                       dtype.encoding == Encoding.UINT ||
                       dtype.encoding == Encoding.OBJECT ||
                       dtype.encoding == Encoding.ARRAY ||
                       dtype.encoding == Encoding.POINTER ||
                       dtype.encoding == Encoding.BOOL))
            // Null to SI, UI, class, T[], T*, B
            return true;

        return false;
    }

    /**
     * Return whether two types are equivalent.
     *  - Integer: size, sign, const and volatile are equal
     *  - Float: size, const and volatile are equal
     *  - Array: subtype, const and volatile are equal
     *  - Pointer: subtype, const and volatile are equal
     *  - Object: name, arguments, const and volatile are equal
     *  - Boolean: const and volatile are equal
     *
     * Therefore: i32 == int, i32* == int*, list&lt;i32&gt; == list&lt;int&gt;
     *  i32 const != i32
     */
    public boolean equals (Object other) {
        if (!Type.class.isInstance (other)) return false;
        Type type = (Type) other;
        if (type.encoding != encoding) return false;
        if (encoding == Encoding.UINT ||
            encoding == Encoding.SINT ||
            encoding == Encoding.FLOAT) {
            return size == type.size && isConst == type.isConst
                && isVolatile == type.isVolatile;
        }
        else if (encoding == Encoding.ARRAY ||
                 encoding == Encoding.POINTER) {
            return subtypes.get (0).equals (type.subtypes.get (0))
                && isConst == type.isConst
                && isVolatile == type.isVolatile;
        }
        else if (encoding == Encoding.OBJECT) {
            if (!name.equals (type.name)) return false;
            if (subtypes.size () != type.subtypes.size ()) return false;
            if (isConst != type.isConst) return false;
            if (isVolatile != type.isVolatile) return false;
            for (int i = 0; i < subtypes.size (); ++i) {
                if (! subtypes.get (i).equals (type.subtypes.get (i)))
                    return false;
            }
            return true;
        }
        else if (encoding == Encoding.BOOL) {
            return isConst == type.isConst && isVolatile == type.isVolatile;
        }
        else if (encoding == Encoding.NULL) {
            return true;
        }
        return false;
    }

    /**
     * Return whether two types are equivalent, ignoring const and volatile. */
    public boolean equalsNoQual (Type type) {
        if (type.encoding != encoding) return false;
        if (encoding == Encoding.UINT ||
            encoding == Encoding.SINT ||
            encoding == Encoding.FLOAT) {
            return size == type.size;
        }
        else if (encoding == Encoding.ARRAY ||
                 encoding == Encoding.POINTER) {
            return subtypes.get (0).equals (type.subtypes.get (0));
        }
        else if (encoding == Encoding.OBJECT) {
            if (!name.equals (type.name)) return false;
            if (subtypes.size () != type.subtypes.size ()) return false;
            for (int i = 0; i < subtypes.size (); ++i) {
                if (! subtypes.get (i).equals (type.subtypes.get (i)))
                    return false;
            }
            return true;
        }
        else if (encoding == Encoding.BOOL)
            return true;
        return false;
    }

    /**
     * Dummy hashCode () */
    public int hashCode () {
        assert false : "No hashCode() written";
        return 1;
    }

    /**
     * Return a string representing the type.
     *  - Integer, Float: base name
     *  - Array: subtype string + "[]"
     *  - Pointer: subtype string + "*"
     *  - Object: base name + args in &lt;&gt;
     */
    public String toString () {
        String suffix;
        suffix = (isConst ? " const" : "");
        if (isVolatile)
            suffix = suffix + " volatile";

        if (encoding == Encoding.UINT ||
            encoding == Encoding.SINT ||
            encoding == Encoding.FLOAT)
            return name + suffix;
        else if (encoding == Encoding.ARRAY)
            return subtypes.get (0).toString () + "[]"
                + suffix;
        else if (encoding == Encoding.POINTER)
            return subtypes.get (0).toString () + "*"
                + suffix;
        else if (encoding == Encoding.NULL)
            return "<null>" + suffix;
        else if (encoding == Encoding.BOOL)
            return "bool" + suffix;
        else if (encoding == Encoding.OBJECT) {
            if (subtypes == null) return name;
            StringBuilder sb = new StringBuilder (name);
            sb.append ('<');
            boolean first = true;
            for (Type i: subtypes) {
                if (first) first = false;
                else sb.append (", ");
                sb.append (i.toString ());
            }
            sb.append ('>');
            sb.append (suffix);
            return sb.toString ();
        }
        throw new RuntimeException ("Invalid type");
    }

    /**
     * All possible type encodings. */
    public enum Encoding { UINT, SINT, FLOAT, ARRAY, POINTER, OBJECT, NULL,
            BOOL }

    /**
     * Type modifiers */
    public enum Modifier { ARRAY, POINTER, CONST, VOLATILE }

    private static Map<String, Encoding> PRIMITIVE_ENCODINGS;
    private static Map<String, Integer> PRIMITIVE_SIZES;
    public static final int OBJECT_SIZE = 16;
    static {
        PRIMITIVE_ENCODINGS = new HashMap<String, Encoding> ();
        PRIMITIVE_SIZES = new HashMap<String, Integer> ();

        PRIMITIVE_ENCODINGS.put ("i8",       Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("i16",      Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("i32",      Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("i64",      Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("u8",       Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("u16",      Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("u32",      Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("u64",      Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("int",      Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("unsigned", Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("size",     Encoding.UINT);
        PRIMITIVE_ENCODINGS.put ("ssize",    Encoding.SINT);
        PRIMITIVE_ENCODINGS.put ("bool",     Encoding.BOOL);
        PRIMITIVE_ENCODINGS.put ("float",    Encoding.FLOAT);
        PRIMITIVE_ENCODINGS.put ("double",   Encoding.FLOAT);

        PRIMITIVE_SIZES.put ("i8",       1);
        PRIMITIVE_SIZES.put ("i16",      2);
        PRIMITIVE_SIZES.put ("i32",      4);
        PRIMITIVE_SIZES.put ("i64",      8);
        PRIMITIVE_SIZES.put ("u8",       1);
        PRIMITIVE_SIZES.put ("u16",      2);
        PRIMITIVE_SIZES.put ("u32",      4);
        PRIMITIVE_SIZES.put ("u64",      8);
        PRIMITIVE_SIZES.put ("int",      4);
        PRIMITIVE_SIZES.put ("unsigned", 4);
        PRIMITIVE_SIZES.put ("size",    -1);
        PRIMITIVE_SIZES.put ("ssize",   -1);
        PRIMITIVE_SIZES.put ("bool",     1);
        PRIMITIVE_SIZES.put ("float",    4);
        PRIMITIVE_SIZES.put ("double",   8);
    }
}

