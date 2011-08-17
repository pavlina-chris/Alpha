// NAME Allowed implicit casts
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 2
// POUT 3
// POUT 0
// POUT -1
// POUT 3.600000
// POUT -1
// POUT 0
// POUT 0
// POUT 0

executable testout;

extern void putint (int);
extern void putdbl (double);

int main (int argc, u8** argv) nomangle {

    let a i8 = 2;
    let b = 0;
    b := a;
    putint (b);

    let c u8 = 3;
    let d unsigned = 0;
    d := c;
    putint (d as int);

    let e = true;
    e := 0;
    putint (e as int);

    e := (1 as unsigned);
    putint (e as int);

    let f = 3.4;
    f := (3.6 as float);
    putdbl (f);

    let g = false;
    g := argv;
    putint (g as int);

    let h = 1;
    h := null;
    putint (h);

    let i unsigned = 1;
    i := null;
    putint (i as int);

    let j = true;
    j := null;
    putint (j as int);

    return 0;
}
