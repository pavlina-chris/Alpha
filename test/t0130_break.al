// NAME "Break" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 0
// POUT 1
// POUT 2
// POUT 3
// POUT 4
// POUT 0
// POUT 1
// POUT 2

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let i = 0;
    while (i < 6) {
        if (i == 5) break;
        putint (i);
        ++i;
    }

    i := 0;
    while (i < 6) {
        while (i < 4) {
            if (i == 3) break 2;
            putint (i);
            ++i;
        }
        ++i;
    }

    return 0;

}
