// NAME "While" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 1
// POUT 2
// POUT 3
// POUT 4
// POUT 5

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let i = 0;
    while (i < 6) {
        putint (i);
        ++i;
    }
    return 0;

}
