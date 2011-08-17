// NAME "Do-while" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 0
// POUT 1
// POUT 2
// POUT 3
// POUT 4
// POUT 5
// POUT 6
// POUT 7
// POUT 8
// POUT 9

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let i = 0;
    do
        putint (i);
    while (i > 0);

    i := 0;
    do {
        putint (i);
        ++i;
    } while (i < 10);

}
