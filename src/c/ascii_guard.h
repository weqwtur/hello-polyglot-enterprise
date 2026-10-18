/* GENERATED FILE. Run tools/gen_c.py instead of editing. */
#ifndef ASCII_GUARD_H
#define ASCII_GUARD_H

/* Returns 1 if value is a printable ASCII code accepted by its dedicated validator. */
int ascii_guard_is_valid(int value);

/* Returns a human readable name for the code, or "UNKNOWN". */
const char *ascii_guard_name(int value);

/* Number of dedicated validators. */
int ascii_guard_count(void);

#endif /* ASCII_GUARD_H */
