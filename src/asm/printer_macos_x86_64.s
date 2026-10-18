# Stage 6 (Intel Mac, macOS). Assemble and link with: cc -o printer printer_macos_x86_64.s
# Reads all of stdin into a stack buffer, writes it to stdout and appends a newline.
# BSD syscalls on x86-64: number = 0x2000000 + n in eax, carry flag set on error.
# Stack layout: (%rsp) = newline byte, 16(%rsp) .. 16+4096 = buffer.

    .text
    .globl _main

_main:
    subq $4112, %rsp
    xorq %rbx, %rbx                 # rbx = total bytes read

Lread_loop:
    movq $4096, %rdx
    subq %rbx, %rdx                 # remaining space
    jz   Lwrite_out                 # buffer full
    movl $0x2000003, %eax           # SYS_read
    xorl %edi, %edi                 # fd 0 (stdin)
    leaq 16(%rsp,%rbx), %rsi        # buffer + total
    syscall
    jc   Lfail                      # carry set: error
    testq %rax, %rax
    jz   Lwrite_out                 # EOF
    addq %rax, %rbx
    jmp  Lread_loop

Lwrite_out:
    testq %rbx, %rbx
    jz   Lfail                      # nothing to print
    movl $0x2000004, %eax           # SYS_write
    movl $1, %edi                   # fd 1 (stdout)
    leaq 16(%rsp), %rsi
    movq %rbx, %rdx
    syscall

    movb $10, (%rsp)
    movl $0x2000004, %eax           # SYS_write
    movl $1, %edi
    movq %rsp, %rsi
    movl $1, %edx
    syscall

    movl $0x2000001, %eax           # SYS_exit
    xorl %edi, %edi
    syscall

Lfail:
    movl $0x2000001, %eax           # SYS_exit
    movl $1, %edi
    syscall
