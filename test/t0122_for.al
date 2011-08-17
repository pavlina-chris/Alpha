// NAME "For" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 1
// POUT 2
// POUT 0
// POUT 1
// POUT 2
// POUT 3
// POUT 4

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let i = 0;
    for (;;) {
        if (i > 2) break;
        putint (i);
        ++i;
    }

    for (let j = 0; j < 5; ++j) {
        putint (j);
    }

}
