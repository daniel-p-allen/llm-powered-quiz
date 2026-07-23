# LLM-Powered Quiz — repository tasks
#
# Two halves in one repository, each with its own toolchain, so there is no single
# package manager to hang scripts off. This Makefile is the entry point that knows
# where each half lives.
#
#   client/    Android app (Java, Gradle) — the quiz UI
#   server/    Flask API (Python) — prompts the model, parses the quiz
#   scripts/   repository tooling
#
# Requires: JDK 17+ and the Android SDK for the client, Python 3.9+ for the server.

SERVER := server
CLIENT := client
VENV   := $(SERVER)/.venv
PY     := $(VENV)/bin/python

.PHONY: check test run install apk clean help

help:
	@echo "make check    refuse to ship if a credential is committed (fast, no install)"
	@echo "make test     run the server test suite"
	@echo "make run      start the quiz server on port 5000"
	@echo "make apk      build the Android debug APK"
	@echo "make install  create the server virtualenv and install dependencies"
	@echo "make clean    remove build output and the virtualenv"

# Refuse to ship if anything resembling a real credential is in the tree. The
# server needs an API key to do anything at all, so the repository has to check
# itself rather than rely on someone remembering.
#
# Kept separate from `test`: this needs nothing installed and runs in a second,
# which is what makes it usable as a habit before every push.
check:
	@./scripts/check-secrets.sh

$(VENV):
	python3 -m venv $(VENV)

install: $(VENV)
	$(VENV)/bin/pip install -q -r $(SERVER)/requirements.txt
	$(VENV)/bin/pip install -q -r $(SERVER)/tests/requirements.txt

# The suite drives the real Flask routes through the test client and stubs the
# model call, so it needs no API key, makes no network request and cannot flake
# on an upstream provider being slow. Nothing under server/ was reshaped to make
# it testable.
test: install
	cd $(SERVER) && .venv/bin/python -m pytest tests/ -q

# Needs server/.env — copy server/.env.example and add a free Groq key.
run: install
	cd $(SERVER) && .venv/bin/python app.py

# The emulator reaches this server at 10.0.2.2:5000, which the client already
# expects, so no configuration is needed on the Android side.
apk:
	cd $(CLIENT) && ./gradlew assembleDebug

clean:
	rm -rf $(VENV) $(SERVER)/__pycache__ $(SERVER)/tests/__pycache__ $(SERVER)/.pytest_cache
	cd $(CLIENT) && ./gradlew clean || true
