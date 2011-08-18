// NAME "Continue" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 1
// POUT 2
// POUT 4
// POUT 5
// POUT 0
// POUT 1
// POUT 2
// POUT 4
// POUT 5
// POUT 6
// POUT 0
// POUT 1
// POUT 2
// POUT 4
// POUT 5
// POUT 6
// POUT 0
// POUT 1
// POUT 2

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    for (let i = 0; i < 6; ++i) {
        if (i == 3) continue;
        putint (i);
    }

    let i = -1;
    while (i < 6) {
        ++i;
        if (i == 3) continue;
        putint (i);
    }

    let k = -1;
    do {
        ++k;
        if (k == 3) continue;
        putint (k);
    } while (k < 6);

    for (let a = 0; a < 3; ++a) {
        for (let b = 0; b < 3; ++b) {
            if (b == 1) continue 2;
            putint (a + b);
        }
    }

    return 0;

}
