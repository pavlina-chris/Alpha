// NAME Multiple return
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 2
// POUT 1
// POUT 1
// POUT 1

executable testout;
extern void putint (int);

(int, int) swapped (int a, int b) {
    return (b, a);
}

int main (int argc, u8** argv) nomangle {

    let x = 1, y = 2;
    (x, y) := swapped (x, y);
    putint (x); putint (y);

    x := swapped (x, y);
    putint (x); putint (y);

    return 0;
}
