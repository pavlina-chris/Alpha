// NAME Stripping of "const" from pure values
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 42
// POUT 1337

executable testout;

extern void putint (int);

int main (int argc, u8** argv) nomangle {

    const x = 23, y = 19;
    let z = x + y;
    putint (z);
    z := 1337;
    putint (z);

    return 0;
}
