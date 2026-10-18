// Compile-time machinery for the encoder stage.
// The XOR key is computed by template recursion, because a literal would be too easy.
#pragma once

namespace hello::core {

// Fibonacci modulo 251, evaluated entirely by the compiler.
template <int N>
struct FibMod {
    static constexpr int value = (FibMod<N - 1>::value + FibMod<N - 2>::value) % 251;
};

template <>
struct FibMod<0> {
    static constexpr int value = 0;
};

template <>
struct FibMod<1> {
    static constexpr int value = 1;
};

// Factorial, because every metaprogram needs one.
template <int N>
struct Factorial {
    static constexpr long long value = N * Factorial<N - 1>::value;
};

template <>
struct Factorial<0> {
    static constexpr long long value = 1;
};

static_assert(FibMod<40>::value == 200, "Fibonacci(40) mod 251 must be 200");
static_assert(Factorial<10>::value == 3628800LL, "10! must be 3628800");

constexpr int kKey = FibMod<40>::value;

constexpr int encode(int value, int key) {
    return value ^ key;
}

constexpr int decode(int value, int key) {
    return value ^ key;
}

}  // namespace hello::core
