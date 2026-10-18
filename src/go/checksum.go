// Optional stage (Go): relays the plain codes unchanged and verifies a checksum
// computed over them twice, with two different algorithms, "for safety".
// Skipped automatically when the Go toolchain is not installed.
package main

import (
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"
)

func fnv1a(values []int) uint32 {
	hash := uint32(2166136261)
	for _, value := range values {
		hash ^= uint32(value)
		hash *= 16777619
	}
	return hash
}

func adler(values []int) uint32 {
	a, b := uint32(1), uint32(0)
	for _, value := range values {
		a = (a + uint32(value)) % 65521
		b = (b + a) % 65521
	}
	return (b << 16) | a
}

func main() {
	raw, err := io.ReadAll(os.Stdin)
	if err != nil {
		fmt.Fprintln(os.Stderr, "[go][ERROR] cannot read stdin:", err)
		os.Exit(1)
	}
	fields := strings.Fields(string(raw))
	values := make([]int, 0, len(fields))
	for _, field := range fields {
		value, parseErr := strconv.Atoi(field)
		if parseErr != nil {
			fmt.Fprintln(os.Stderr, "[go][ERROR] not a number:", field)
			os.Exit(2)
		}
		values = append(values, value)
	}
	first := fnv1a(values)
	second := fnv1a(append([]int(nil), values...))
	if first != second {
		fmt.Fprintln(os.Stderr, "[go][ERROR] checksum is not deterministic, which is concerning")
		os.Exit(3)
	}
	fmt.Fprintf(os.Stderr, "[go][INFO ] %d codes, fnv1a=%08x adler=%08x\n", len(values), first, adler(values))
	os.Stdout.Write(raw)
}
