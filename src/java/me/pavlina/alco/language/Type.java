// Copyright (c) 2011, Chris Pavlina. All rights reserved.

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

    private String name;
    /* This ivar has a few uses, depending on the encoding:
     *  - ARRAY: Array type (for int[], it will contain int)
     *  - POINTER: Pointer type (for int*, it will contain int)
     *  - OBJECT: Arguments (for map<string, int>, it will contain string
     *      and int.
     */
    private List<Type> subtypes;
    private int size;
    private Encoding encoding;


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
        // If there are no mods, type creation is simple.
        if (mods == null || mods.length == 0) {
            this.baseType (env, name, args);
        }

        // Otherwise, the root type is the last mod, and recurse.
        else if (mods[mods.length - 1] == Modifier.ARRAY) {
            this.name = "";
            this.size = Type.OBJECT_SIZE;
            this.encoding = Encoding.ARRAY;
            this.subtypes = new ArrayList<Type> (1);
            this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                         (mods, mods.length - 1)));
        } else /* Modifier.POINTER */ {
            this.name = "";
            this.size = env.getBits () / 8;
            this.encoding = Encoding.POINTER;
            this.subtypes = new ArrayList<Type> (1);
            this.subtypes.add (new Type (env, name, args, Arrays.copyOf
                                         (mods, mods.length - 1)));
        }
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
     * Get the encoded type name. This is used in code, both in the dynamic
     * type system and in method name mangling. It is fully reversible
     * (see fromEncodedName()). */
    public String getEncodedName () {
        // This table gives encodings for various types. See ENCODED_NAMES
        // for the table as used in the code.
        //         1  2  4  8
        // OBJECT        O  O
        // POINTER       p  p
        // ARRAY         q  q
        // FLOAT         F  f
        // SINT    A  B  C  D
        // UINT    a  b  c  d
        //
        // Arrays and pointers take their subtype after their name. int*[] is
        // encoded as qpC.
        // Objects are encoded in a special way:
        // O {objectname} $ {arg} {arg} {arg}... Z
        // Therefore, the encoded name of map<string, int> is:
        // Omap$Ostring$ZCZ
        if (encoding == Encoding.OBJECT) {
            if (subtypes == null) {
                return "O" + name + "$Z";
            }
            String encName = "O" + name + "$";
            for (Type i: subtypes) {
                encName = encName + i.getEncodedName ();
            }
            encName = encName + "Z";
            return encName;
        } else {
            int sizeLog2;
            switch (size) {
            case 1: sizeLog2 = 0; break;
            case 2: sizeLog2 = 1; break;
            case 4: sizeLog2 = 2; break;
            case 8: sizeLog2 = 3; break;
            default: throw new RuntimeException ("invalid size");
            }
            if (encoding == Encoding.POINTER || encoding == Encoding.ARRAY)
                return ENCODED_NAMES.get (encoding)[sizeLog2] +
                    subtypes.get (0).getEncodedName ();
            else
                return ENCODED_NAMES.get (encoding)[sizeLog2];
        }
    }

    /**
     * Used internally by fromEncodedName(String). int[] pos is a cheap hack
     * for pointers in Java. Yeah yeah, I know, it's bad programming practice.
     * Sue me. */
    private static Type fromEncodedName (Env env, String name, int[] pos) {
        Type type, subtype;
        if (pos[0] >= name.length ()) return null;
        ++pos[0];
        switch (name.charAt (pos[0])) {
        case 'A':
            type = new Type (env, "i8", null, (Modifier[]) null); break;
        case 'B':
            type = new Type (env, "i16", null, (Modifier[]) null); break;
        case 'C':
            type = new Type (env, "int", null, (Modifier[]) null); break;
        case 'D':
            type = new Type (env, "i64", null, (Modifier[]) null); break;
        case 'a':
            type = new Type (env, "u8", null, (Modifier[]) null); break;
        case 'b':
            type = new Type (env, "u16", null, (Modifier[]) null); break;
        case 'c':
            type = new Type (env, "unsigned", null, (Modifier[]) null); break;
        case 'd':
            type = new Type (env, "u64", null, (Modifier[]) null); break;
        case 'F':
            type = new Type (env, "float", null, (Modifier[]) null); break;
        case 'f':
            type = new Type (env, "double", null, (Modifier[]) null); break;
        case 'q':
            type = new Type ();
            type.size = Type.OBJECT_SIZE;
            type.encoding = Encoding.ARRAY;
            type.subtypes = new ArrayList<Type> (1);
            subtype = fromEncodedName (env, name, pos);
            if (subtype == null) return null;
            type.subtypes.add (subtype);
            return type;
        case 'p':
            type = new Type ();
            type.size = env.getBits () / 8;
            type.encoding = Encoding.POINTER;
            type.subtypes = new ArrayList<Type> (1);
            subtype = fromEncodedName (env, name, pos);
            if (subtype == null) return null;
            type.subtypes.add (subtype);
            return type;
        case 'O':
            {
                String basename;
                List<Type> args;
                int dollarSign = name.indexOf ('$', pos[0]);
                if (dollarSign == -1) return null;
                basename = name.substring (pos[0], dollarSign);
                pos[0] = dollarSign + 1;
                if (name.charAt (pos[0]) == 'Z') {
                    args = null;
                } else {
                    args = new ArrayList<Type> ();
                    boolean foundEnd = false;
                    while (pos[0] < name.length ()) {
                        if (name.charAt (pos[0]) == 'Z') {
                            foundEnd = true;
                            break;
                        }
                        subtype = fromEncodedName (env, name, pos);
                        if (subtype == null) return null;
                        args.add (subtype);
                    }
                    if (!foundEnd) return null;
                }
                type = new Type ();
                type.size = Type.OBJECT_SIZE;
                type.name = basename;
                type.encoding = Encoding.OBJECT;
                type.subtypes = args;
                return type;
            }
        default:
            return null;
        }
        return type;
    }

    /**
     * Get a Type from an encoded type name. Returns null on error. */
    public static Type fromEncodedName (Env env, String name) {
        int[] pos = new int[1];
        pos[0] = 0;
        Type t = fromEncodedName (env, name, pos);
        if (t == null) return null;
        if (pos[0] != (name.length () - 1)) return null;
        return t;
    }

    /**
     * Coerce type for assignment. See Standard:Types:Casting:ImplicitCasts
     * @throws CError on coercion error
     * @returns Coerced value */
    public static HasType coerce (HasType value, HasType destination,
                                  CastCreator creator, Env env) throws CError
    {
        Type vtype = value.getType ();
        Type dtype = destination.getType ();

        if (vtype.equals (dtype)) {
            // Same type - no cast
            return value;

        } else if (vtype.encoding == Encoding.SINT
            && dtype.encoding == vtype.encoding
            && vtype.size <= dtype.size) {
            // SIa to SIb where b >= a (signed upcast)
            return creator.cast (value, dtype, env);

        } else if (vtype.encoding == Encoding.UINT
                   && dtype.encoding == vtype.encoding
                   && vtype.size <= dtype.size) {
            // UIa to UIb where b >= a (unsigned upcast)
            return creator.cast (value, dtype, env);
        
        } else if (IntValue.class.isInstance (value)
                   && dtype.encoding == Encoding.UINT) {
            // The standard mentions both:
            //    IntValue within SIa to SIa
            //    IntValue within UIa to UIa
            // Because IntValues are implicitly i32/i64, the first one will
            // be done by "SIa to SIb". However, "SIa to UIb" is normally
            // illegal, so we have to specifically check for it.
            
            IntValue iv = (IntValue) value;
            BigInteger val = iv.getValue (), min, max;

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
                iv.setType (dtype);
                return value;
            }
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
                       dtype.encoding == Encoding.POINTER))
            // Null to SI, UI, class, T[], T*
            return creator.cast (value, dtype, env);

        throw CError.at ("invalid implicit cast: " + vtype.toString ()
                         + " to " + dtype.toString (), value.getToken ());
    }

    /**
     * Coerce pure type (no value) for assignment. See
     * Standard:Types:Casting:ImplicitCasts
     * @returns Type, or null on error */
    public static Type coerce (Type vtype, Type dtype)
    {
        if (vtype.equals (dtype)) {
            // Same type - no cast
            return dtype;

        } else if (vtype.encoding == Encoding.SINT
            && dtype.encoding == vtype.encoding
            && vtype.size <= dtype.size) {
            // SIa to SIb where b >= a (signed upcast)
            return dtype;

        } else if (vtype.encoding == Encoding.UINT
                   && dtype.encoding == vtype.encoding
                   && vtype.size <= dtype.size) {
            // UIa to UIb where b >= a (unsigned upcast)
            return dtype;
        
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
                       dtype.encoding == Encoding.POINTER))
            // Null to SI, UI, class, T[], T*
            return dtype;

        return null;
    }

    /**
     * Return whether two types are equivalent.
     *  - Integer: size and sign are equal
     *  - Float: size is equal
     *  - Array: subtype is equal
     *  - Pointer: subtype is equal
     *  - Object: name is equal and arguments are equal
     *
     * Therefore: i32 == int, i32* == int*, list&lt;i32&gt; == list&lt;int&gt;
     */
    public boolean equals (Type type) {
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
        return false;
    }

    /**
     * Return a string representing the type.
     *  - Integer, Float: base name
     *  - Array: subtype string + "[]"
     *  - Pointer: subtype string + "*"
     *  - Object: base name + args in &lt;&gt;
     */
    public String toString () {
        if (encoding == Encoding.UINT ||
            encoding == Encoding.SINT ||
            encoding == Encoding.FLOAT)
            return name;
        else if (encoding == Encoding.ARRAY)
            return subtypes.get (0).toString () + "[]";
        else if (encoding == Encoding.POINTER)
            return subtypes.get (0).toString () + "*";
        else if (encoding == Encoding.NULL)
            return "null";
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
            return sb.toString ();
        }
        return super.toString ();
    }

    /**
     * Interface for a cast creator. This takes a value and a desired type, and
     * wraps the value in a cast to it. */
    public static interface CastCreator {
        public HasType cast (HasType value, Type type, Env env);
    }

    /**
     * All possible type encodings. */
    public enum Encoding { UINT, SINT, FLOAT, ARRAY, POINTER, OBJECT, NULL }

    /**
     * Type modifiers */
    public enum Modifier { ARRAY, POINTER }

    private static Map<String, Encoding> PRIMITIVE_ENCODINGS;
    private static Map<String, Integer> PRIMITIVE_SIZES;
    private static Map<Encoding, String[]> ENCODED_NAMES;
    public static final int OBJECT_SIZE = 16;
    static {
        PRIMITIVE_ENCODINGS = new HashMap<String, Encoding> ();
        PRIMITIVE_SIZES = new HashMap<String, Integer> ();
        ENCODED_NAMES = new HashMap<Encoding, String[]> ();

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
        PRIMITIVE_SIZES.put ("float",    4);
        PRIMITIVE_SIZES.put ("double",   8);

        ENCODED_NAMES.put (Encoding.POINTER, new String[] {"",  "",  "p", "p"});
        ENCODED_NAMES.put (Encoding.ARRAY,   new String[] {"",  "",  "q", "q"});
        ENCODED_NAMES.put (Encoding.FLOAT,   new String[] {"",  "",  "F", "f"});
        ENCODED_NAMES.put (Encoding.SINT,    new String[] {"A", "B", "C", "D"});
        ENCODED_NAMES.put (Encoding.UINT,    new String[] {"a", "b", "c", "d"});
    }
}
