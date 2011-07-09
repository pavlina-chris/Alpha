/* It's a freakin' launcher. No copyright claimed here. */

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <alloca.h>

#define JAVA "java"
#define ALCOJAR_INSTALLED "/usr/lib/alco.jar"
#define ALCOJAR_LOCAL     "./alco.jar"

int main (int argc, char **argv) {
    
    struct stat sbuf;
    char const *alco;
    char **new_argv;
    size_t i;

    /* Find alco */
    alco = stat (ALCOJAR_LOCAL, &sbuf) ? ALCOJAR_INSTALLED : ALCOJAR_LOCAL;

    /* Make a new argv. We need to add two arguments plus a NULL, so make it
     * argc+3 */
    new_argv = alloca ((argc + 3) * sizeof (*new_argv));
    new_argv[0] = JAVA;
    new_argv[1] = "-jar";
    new_argv[2] = (char *) alco;
    for (i = 1; i < argc; ++i) {
        new_argv[2 + i] = argv[i];
    }
    new_argv[argc + 2] = NULL;

    /* Execute alco */
    execvp (JAVA, new_argv);

    /* We failed if we made it here. */
    perror ("execvp");
    return 1;
}
