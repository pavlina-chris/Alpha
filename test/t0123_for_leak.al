// NAME "For" statement scope leak
// COMPILE ["-o", "testout"]
// CEXIT 1
// CERR t0123_for_leak.al:13:5: error: cannot resolve name
// CERR     i := 2;
// CERR     ^      
/* Note the extra spaces in the above line */

package testout;

void f () {
    for (let i = 0; i < 5; ++i) {}
    i := 2;
}
