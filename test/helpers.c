#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

typedef unsigned char bool;
void * $$new (size_t n, unsigned int line, unsigned int col,
              bool (*oom) (size_t, unsigned int, unsigned int),
              void *(*alloc) (size_t)) {
  void *p = alloc (n);
  if (!p)
    if (oom ? oom (n, line, col) : 1) {
      fputs ("Error: Out of memory.\n", stderr);
      abort ();
    }
  return p;
}

void $$oobmsg (unsigned int line, unsigned int col,
               void (*bounds) (unsigned int, unsigned int)) {
  if (bounds) bounds (line, col);
  fputs ("Error: Invalid array access.\n", stderr);
  abort ();
}

void putint (int i) {
  printf ("%d\n", i);
}

void putssize (ssize_t i) {
  printf ("%zd\n", i);
}

void putdbl (double d) {
  printf ("%f\n", d);
}

void putptr (void *p) {
  printf ("%p\n", p);
}
