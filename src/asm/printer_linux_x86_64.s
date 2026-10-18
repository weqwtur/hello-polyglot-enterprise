# Stage 6 (x86-64 Linux, GNU as, AT&T syntax).
# Reads all of stdin into a buffer, writes it to stdout and appends a newline.
# This is the one and only place where "Hello, World!" is actually printed.

    .section .bss
    .lcomm buffer, 4096

    .section .rodata
newline:
    .ascii "\n"

    .section .text
    .globl _start

_start:
    xorq %rbx, %rbx                 # rbx = total bytes read

read_loop:
    movq $0, %rax                   # sys_read
    movq $0, %rdi                   # fd 0 (stdin)
    leaq buffer(%rip), %rsi
    addq %rbx, %rsi                 # buffer + total
    movq $4096, %rdx
    subq %rbx, %rdx                 # remaining space
    jz   write_out                  # buffer full
    syscall
    cmpq $0, %rax
    jl   fail                       # read error
    je   write_out                  # EOF
    addq %rax, %rbx
    jmp  read_loop

write_out:
    cmpq $0, %rbx
    je   fail                       # nothing to print
    movq $1, %rax                   # sys_write
    movq $1, %rdi                   # fd 1 (stdout)
    leaq buffer(%rip), %rsi
    movq %rbx, %rdx
    syscall

    movq $1, %rax                   # sys_write
    movq $1, %rdi
    leaq newline(%rip), %rsi
    movq $1, %rdx
    syscall

    movq $60, %rax                  # sys_exit
    xorq %rdi, %rdi                 # status 0
    syscall

fail:
    movq $60, %rax                  # sys_exit
    movq $1, %rdi                   # status 1
    syscall
