#!/usr/bin/env python
# Also compatible with Python 3
# Copyright (c) 2011, Christopher Pavlina. All rights reserved.

# This is the build script for alco. None of the Java build tools were flexible
# enough, and using Make was getting to be quite annoying.

import os
import sys
import subprocess
import shutil

def whine (msg):
    """
    Python 2/3-compatible version of print (msg, file=sys.stderr)
    """
    sys.stderr.write (msg)
    sys.stderr.write ("\n")
    sys.stderr.flush ()

def needs_compile (source, source_ext, out_ext):
    """
    Checks whether a source file needs to be compiled. Returns True if the
    output file does not exist, or is older than the source file.

    source: Source file
    source_ext: Source file extension (".java" for example)
    out_ext: Output file extension (".class" for example)
    """

    if not source.endswith (source_ext):
        raise Exception ("source file %s does not end with %s" %
                (source, source_ext))

    if source_ext == '':
        out = source + out_ext
    else:
        out = source[:-len(source_ext)] + out_ext

    if not os.path.exists (out):
        return True

    return os.path.getmtime (source) > os.path.getmtime (out)

def do_java_compile (source, classpath, verbose):
    """
    Compile a Java file.
    
    source: Source file
    classpath: List of classpaths (jar files, etc)
    """
    if "DEBUG" in os.environ:
        cmdline = ["javac", "-g", "-classpath", ":".join(classpath), source]
    else:
        cmdline = ["javac", "-classpath", ":".join(classpath), source]
    if verbose:
        print (" ".join (cmdline))
    else:
        print (source)
    javac = subprocess.Popen (cmdline)
    if javac.wait ():
        sys.exit (1)

def do_c_compile (source, verbose):
    """
    Compile a C file.

    source: Source file
    """
    cmdline = ["cc", "-o", source[:-2], source]
    if verbose:
        print (" ".join (cmdline))
    else:
        print (source)
    cc = subprocess.Popen (cmdline)
    if cc.wait ():
        sys.exit (1)

def compile_if_needed (cf, source, source_ext, out_ext, *v):
    """
    Compile 'source' using compile function 'cf' if needed
    (see needs_compile).
    """

    if needs_compile (source, source_ext, out_ext):
        cf (source, *v)
        return True
    return False

def recurse (path, ext, f, pre, post):
    """
    Run f(pre..., file, post...) for every file, recursively in 'path', ending
    with 'ext'. Returns the number of times f() returned True.
    """
    count = 0
    for dirpath, dirnames, filenames in os.walk (path):
        for file in filenames:
            if file.endswith (ext):
                args = pre + [os.path.join (dirpath, file)] + post
                if f (*args):
                    count += 1
    return count

def findall (path, ext):
    """
    Return a list of all files in 'path' ending with 'ext', recursively.
    """
    L = []
    for dirpath, dirnames, filenames in os.walk (path):
        for file in filenames:
            if file.endswith (ext):
                L.append (os.path.join (dirpath, file))
    return L

def link_file (root, path, dest):
    """
    Move root/path to dest/path, creating all necessary subdirectories.
    """
    current = dest
    for i in path.split('/')[:-1]:
        current = current + '/' + i
        if os.path.exists (current) and not os.path.isdir (current):
            whine ("Error: %s is not a directory" % current)
            sys.exit (1)
        if not os.path.exists (current):
            os.mkdir (current)

    link = dest + '/' + path
    target = os.path.abspath (root + '/' + path)
    if os.path.islink (link):
        os.unlink (link)
    os.symlink (target, link)


def prep_jar_directory (dest, roots, jars, verbose):
    """
    Merge all of 'roots' (directories) and 'jars' (existing jar files) into
    one 'dest' directory to be made into a jar file.
    """
    roots = roots[:]
    for i in jars:
        target = i + ".d"
        if not os.path.isdir (target):
            os.mkdir (target)
            cwd = os.getcwd ()
            os.chdir (target)
            cmdline = ['jar', 'xf', '../' + os.path.basename (i)]
            if verbose:
                print (" ".join (cmdline))
            else:
                print (os.path.basename (i))
            jar = subprocess.Popen (cmdline)
            if jar.wait ():
                sys.exit (1)
            os.chdir (cwd)
        roots.append (target)

    for i in roots:
        # Make sure every root path ends with a single /
        i = i.rstrip ('/') + '/'
        
        # Walk the root, finding all '.class' files, and move them into 'dest'
        for dirpath, dirnames, filenames in os.walk (i):
            for file in filenames:
                if file.endswith (".class"):
                    # Get the path relative to the root
                    path = os.path.join (dirpath[len (i):], file)

                    # Link it into dest
                    link_file (i, path, dest)

def build_jar_file (root, inputs, mainclass, filename, verbose):
    cwd = os.getcwd ()
    os.chdir (root)
    cmdline = ['jar', 'cef', mainclass, filename] + inputs
    if verbose:
        print (" ".join (cmdline))
    else:
        print (filename)
    jar = subprocess.Popen (cmdline)
    if jar.wait ():
        sys.exit (1)
    os.chdir (cwd)

def main ():
    if "--help" in sys.argv:
        print ("Compiles AlCo. Options:")
        print ("  --help")
        print ("  --verbose")
        print ("Note: will not re-unpack a jar file, so to change an included")
        print ("jar file, make sure to \"make clean\" first.")
        return
    verbose = ("--verbose" in sys.argv)

    # Find jar files
    jars = findall ("jars", ".jar")

    # Compile Java files
    java_files = recurse (
            "src/java",
            ".java",
            compile_if_needed,
            [do_java_compile],
            [".java", ".class", jars + ["src/java"], verbose]
            )

    # Compile C tools
    c_files = recurse (
            "src/c",
            ".c",
            compile_if_needed,
            [do_c_compile],
            [".c", "", verbose]
            )

    if java_files:
        # Prepare the jar directory
        try:
            os.mkdir ("build")
            os.mkdir ("build/jar")
        except:
            pass
        prep_jar_directory ("build/jar", ["src/java"], jars, verbose)

        # Build the jar file
        build_jar_file ("build/jar", ["me", "com"],
                "me.pavlina.alco.compiler.Compiler",
                "alco.jar", verbose)

    # Move/copy files out
    if java_files:
        os.rename ("build/jar/alco.jar", "alco.jar")
    if c_files:
        shutil.copyfile ("src/c/alco", "alco")
        os.chmod ("alco", 0o755)

if __name__ == "__main__":
    main ()
