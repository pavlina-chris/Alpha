// NAME Operator precedence
// COMPILE ["-pre-ast"]
// COUT Package testout
// COUT   void f ()
// COUT     (  (add (cast c (cast b a)) (cast e d))
// COUT        (add a (mult b c))
// COUT        (add (mult a b) c)
// COUT        (subt a (div b c))
// COUT        (subt (div a b) c)
// COUT        (bitw-or (bitw-or (bitw-and a b) c) d)
// COUT        (bitw-or (bitw-and a b) (bitw-or c d))
// COUT        (bitw-or (bitw-or a (bitw-and b c)) d)
// COUT        (bitw-or (bitw-or a b) (bitw-and c d))
// COUT        (bitw-or a (bitw-or (bitw-and b c) d))
// COUT        (bitw-or a (bitw-or b (bitw-and c d)))
// COUT        (veq? (lt? a b) c)
// COUT        (veq? a (lt? b c))
// COUT        (log-or (log-and a b) c)
// COUT        (log-or a (log-and b c))
// COUT        (conditional  a '(b c))
// COUT        (conditional  (log-and a b) '((log-or c d) (log-and e f)))
// COUT        (assign a (conditional  b '(c d)))
// COUT        '(a (assign b c) d)
// COUT        (assign '(a b) '(c d))
// COUT        )

package testout;
void f () {
    a as b as c + d as e;
    a + b * c;
    a * b + c;
    a - b / c;
    a / b - c;
    a & b ^ c | d;
    a & b | c ^ d;
    a ^ b & c | d;
    a ^ b | c & d;
    a | b & c ^ d;
    a | b ^ c & d;
    a < b == c;
    a == b < c;
    a && b || c;
    a || b && c;
    a ? b : c;
    a && b ? c || d : e && f;
    a := b ? c : d;
    a, b := c, d;
    (a, b) := (c, d);
}
