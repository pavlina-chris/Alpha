// NAME Multiple assign (simple)
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT 2
// POUT 2
// POUT 1

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let x = 1, y = 2;
    putint (x); putint (y);
    (x, y) := (y, x);
    putint (x); putint (y);

    return 0;
}
