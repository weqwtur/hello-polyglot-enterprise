/* Stage 6 fallback (portable C): used when there is no native assembly variant for the
 * current OS/CPU, or when assembling it fails. Same contract as the assembly printers:
 * copy stdin to stdout, append a newline, exit 1 on empty input. */
#include <stdio.h>

int main(void) {
    static char buffer[4096];
    size_t total = fread(buffer, 1, sizeof buffer, stdin);
    if (total == 0) {
        fprintf(stderr, "[c-fallback][ERROR] nothing to print\n");
        return 1;
    }
    fwrite(buffer, 1, total, stdout);
    fputc('\n', stdout);
    fprintf(stderr, "[c-fallback][INFO ] printed %zu characters (portable C instead of assembly)\n", total);
    return 0;
}
