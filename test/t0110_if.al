// NAME "If" statement
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT 4
// POUT 6
// POUT 7

executable testout;
extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let x = true, y = false;

    if (x)
        putint (1);
    else
        putint (2);

    if (y)
        putint (3);
    else
        putint (4);

    if (y)
        putint (5);
    else if (x)
        putint (6);
    
    if (x)
        putint (7);
    if (y)
        putint (8);

    return 0;
}
