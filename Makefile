.PHONY: all build run test generate stats clean zip

all: run

build:
	./scripts/build.sh

run:
	./scripts/run.sh

test:
	./scripts/test.sh

generate:
	python3 tools/generate_all.py

stats:
	./scripts/stats.sh

clean:
	rm -rf build/* src/**/__pycache__ orchestrator/__pycache__ tests/__pycache__

zip: clean
	cd .. && zip -r hello-polyglot-enterprise.zip hello-polyglot-enterprise -x '*/__pycache__/*'
