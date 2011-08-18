// NAME Instantiation/allocation of array
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT 2
// POUT 1
// POUT 2
// POUT 3
// POUT 1
// POUT 2
// POUT 3

executable testout;
extern int puts (u8*);
extern int putint (int);

void put (u8 c) {
    putint (c as unsigned as int);
}

int main (int argc, u8** argv) nomangle {
    let test1 = new u8[] (2);
    test1[0] := 1;
    test1[1] := 2;
    put (test1[0]);
    put (test1[1]);

    let test2_backing = new u8 (3);
    let test2 = new u8[] (test2_backing, 2);
    test2[0] := 1;
    test2[1] := 2;
    test2[2] := 3;
    put (test2[0]);
    put (test2[1]);
    put (test2[2]);
    put (*(test2_backing));
    put (*(test2_backing + 1));
    put (*(test2_backing + 2));

    return 0;
}
