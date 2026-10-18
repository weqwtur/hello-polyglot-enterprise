# Everything needed to build and run all ten stages, including the optional Go and Rust ones.
FROM ubuntu:24.04

RUN apt-get update && apt-get install -y --no-install-recommends \
    bash binutils build-essential golang-go make nodejs openjdk-21-jdk-headless perl python3 rustc zip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . /app
RUN ./scripts/build.sh

CMD ["./scripts/run.sh"]
