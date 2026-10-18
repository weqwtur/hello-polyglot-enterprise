// Optional stage (Rust): verifies every code is printable ASCII and relays the data unchanged.
// Skipped automatically when rustc is not installed.
use std::io::{self, Read, Write};
use std::process;

fn main() {
    let mut input = String::new();
    if io::stdin().read_to_string(&mut input).is_err() {
        eprintln!("[rust][ERROR] cannot read stdin");
        process::exit(1);
    }

    let mut count: usize = 0;
    for token in input.split_whitespace() {
        match token.parse::<u32>() {
            Ok(value) if (32..=126).contains(&value) => count += 1,
            Ok(value) => {
                eprintln!("[rust][ERROR] code outside printable ASCII: {}", value);
                process::exit(2);
            }
            Err(_) => {
                eprintln!("[rust][ERROR] not a number: {}", token);
                process::exit(3);
            }
        }
    }

    eprintln!("[rust][INFO ] validated {} codes", count);
    io::stdout()
        .write_all(input.as_bytes())
        .expect("failed to write to stdout");
}
