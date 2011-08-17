// NAME Allowed casts
// COMPILE ["-o", "testout"]
// RUN ./testout
// DELETE testout
// POUT 1
// POUT 1
// POUT 2
// POUT 3
// POUT 3
// POUT 4
// POUT 5
// POUT 5
// POUT -1
// POUT 255
// POUT 0
// POUT -1
// POUT 0
// POUT 3.400000
// POUT 3.400000

executable testout;

extern void putint (int);
extern void putdbl (double);

int main (int argc, u8** argv) nomangle {

    let a = 1;
    putint (a);

    let b = a as int;
    putint (b);

    let c unsigned = 2;
    let d int = c;
    putint (d);

    let e int = 3;
    let f unsigned = e;
    putint (e as int);

    let g i64 = 3;
    let h int = g;
    putint (h);

    let i = 4;
    let j i64 = i;
    putint (j as int);

    let k unsigned = 5;
    let l u64 = k;
    putint (l as unsigned as int);

    let m u64 = 5;
    let n unsigned = m;
    putint (n as int);

    let o = true;
    putint (o as int);
    putint (o as u8 as unsigned as int);
    o := false;
    putint (o as int);

    let p = 2;
    putint (p as bool as int);
    p := 0;
    putint (p as bool as int);

    let q = 3.4;
    let r float = q;
    putdbl (r as double);

    let s float = 3.4;
    let t double = s;
    putdbl (t);

    return 0;
}
