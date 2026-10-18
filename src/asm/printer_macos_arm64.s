// Stage 6 (Apple Silicon, macOS). Assemble and link with: cc -o printer printer_macos_arm64.s
// Reads all of stdin into a stack buffer, writes it to stdout and appends a newline.
// Raw BSD syscalls: x16 = number, svc #0x80, carry flag set on error.
// Stack layout: [sp] = newline byte, [sp+16 .. sp+16+4096) = buffer.

    .text
    .globl _main
    .p2align 2

_main:
    sub  sp, sp, #1, lsl #12        // 4096 bytes
    sub  sp, sp, #16                // + 16 for the newline slot
    mov  x19, #0                    // x19 = total bytes read

Lread_loop:
    mov  x2, #4096
    subs x2, x2, x19                // remaining space
    b.eq Lwrite_out                 // buffer full
    mov  x0, #0                     // fd 0 (stdin)
    add  x1, sp, #16
    add  x1, x1, x19                // buffer + total
    mov  x16, #3                    // SYS_read
    svc  #0x80
    b.cs Lfail                      // carry set: error
    cbz  x0, Lwrite_out             // EOF
    add  x19, x19, x0
    b    Lread_loop

Lwrite_out:
    cbz  x19, Lfail                 // nothing to print
    mov  x0, #1                     // fd 1 (stdout)
    add  x1, sp, #16
    mov  x2, x19
    mov  x16, #4                    // SYS_write
    svc  #0x80

    mov  w9, #10
    strb w9, [sp]
    mov  x0, #1
    mov  x1, sp
    mov  x2, #1
    mov  x16, #4                    // SYS_write
    svc  #0x80

    mov  x0, #0
    mov  x16, #1                    // SYS_exit
    svc  #0x80

Lfail:
    mov  x0, #1
    mov  x16, #1                    // SYS_exit
    svc  #0x80
