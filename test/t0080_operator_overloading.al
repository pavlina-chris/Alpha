// NAME Operator overloading
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT -1
// POUT 42

executable testout;

extern void putint (int);

int main (int argc, u8** argv) nomangle {

    let x = true;
    x := x + false;
    putint (x as int);

    let y = 39;
    y += 3.2;
    putint (y);

    return 0;
}

bool + (bool a, bool b) {
    putint (1);
    return true;
}

int += (int *a, double b) {
    return (*a += b as int);
}
