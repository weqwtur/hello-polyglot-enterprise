// Stage 6 (AArch64 Linux, GNU as).
// Reads all of stdin into a stack buffer, writes it to stdout and appends a newline.
// Stack layout: [sp] = newline byte, [sp+16 .. sp+16+4096) = buffer.

    .text
    .globl _start

_start:
    sub  sp, sp, #1, lsl #12        // 4096 bytes
    sub  sp, sp, #16                // + 16 for the newline slot
    mov  x19, #0                    // x19 = total bytes read

read_loop:
    mov  x2, #4096
    subs x2, x2, x19                // remaining space
    b.eq write_out                  // buffer full
    mov  x0, #0                     // fd 0 (stdin)
    add  x1, sp, #16
    add  x1, x1, x19                // buffer + total
    mov  x8, #63                    // sys_read
    svc  #0
    cmp  x0, #0
    b.lt fail                       // read error
    b.eq write_out                  // EOF
    add  x19, x19, x0
    b    read_loop

write_out:
    cbz  x19, fail                  // nothing to print
    mov  x0, #1                     // fd 1 (stdout)
    add  x1, sp, #16
    mov  x2, x19
    mov  x8, #64                    // sys_write
    svc  #0

    mov  w9, #10
    strb w9, [sp]
    mov  x0, #1
    mov  x1, sp
    mov  x2, #1
    mov  x8, #64                    // sys_write
    svc  #0

    mov  x0, #0
    mov  x8, #94                    // sys_exit_group
    svc  #0

fail:
    mov  x0, #1
    mov  x8, #94                    // sys_exit_group
    svc  #0
