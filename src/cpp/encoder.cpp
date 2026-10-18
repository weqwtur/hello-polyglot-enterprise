// Stage 2: reads character codes from stdin, XORs them with a compile-time key
// and prints "KEY <k>" followed by the encoded codes.
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

#include "encoder_core.hpp"
#include "generated_asserts.hpp"

namespace {

std::vector<int> readCodes(std::istream& input) {
    std::vector<int> codes;
    std::string line;
    while (std::getline(input, line)) {
        std::istringstream stream(line);
        int value;
        while (stream >> value) {
            codes.push_back(value);
        }
    }
    return codes;
}

}  // namespace

int main() {
    const std::vector<int> codes = readCodes(std::cin);
    if (codes.empty()) {
        std::cerr << "[cpp][ERROR] no input" << std::endl;
        return 1;
    }
    std::cout << "KEY " << hello::core::kKey << "\n";
    for (std::size_t index = 0; index < codes.size(); ++index) {
        if (index > 0) {
            std::cout << ' ';
        }
        std::cout << hello::core::encode(codes[index], hello::core::kKey);
    }
    std::cout << "\n";
    std::cerr << "[cpp][INFO ] encoded " << codes.size() << " codes with compile-time key "
              << hello::core::kKey << std::endl;
    return 0;
}
