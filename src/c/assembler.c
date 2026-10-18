/* Stage 5 (C): reads character codes from stdin, validates each one with its dedicated
 * generated validator and writes the assembled string to stdout (no trailing newline). */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "ascii_guard.h"

#define MAX_CODES 4096

int main(void) {
    char *text = malloc(65536);
    if (text == NULL) {
        fprintf(stderr, "[c][ERROR] out of memory\n");
        return 1;
    }
    size_t length = fread(text, 1, 65535, stdin);
    text[length] = '\0';

    static char result[MAX_CODES + 1];
    size_t count = 0;
    char *cursor = text;
    char *end = NULL;

    for (;;) {
        long value = strtol(cursor, &end, 10);
        if (end == cursor) {
            break;
        }
        if (count >= MAX_CODES) {
            fprintf(stderr, "[c][ERROR] too many codes\n");
            free(text);
            return 2;
        }
        if (!ascii_guard_is_valid((int)value)) {
            fprintf(stderr, "[c][ERROR] invalid code: %ld\n", value);
            free(text);
            return 3;
        }
        result[count++] = (char)value;
        cursor = end;
    }
    result[count] = '\0';
    free(text);

    if (count == 0) {
        fprintf(stderr, "[c][ERROR] no codes\n");
        return 4;
    }
    fprintf(stderr, "[c][INFO ] assembled %zu characters using %d validators\n", count, ascii_guard_count());
    fwrite(result, 1, count, stdout);
    return 0;
}
