#!/usr/bin/env bash
# Refuse to ship if anything resembling a real credential is in the tree.
#
# This repository holds two kinds of secret and one kind of signing material:
#
#   - a Groq API key (gsk_...), which the server needs to generate quizzes
#   - a Hugging Face token (hf_...), for variants/main-huggingface-router.py
#   - Android signing material (.jks, .keystore, local.properties)
#
# There is deliberately no MongoDB or AWS rule here. Neither this client nor this
# server touches a database or a cloud host, and a check that looks for things
# the project cannot contain is noise that trains you to ignore the output.
#
# The server did once print its bearer token on every request, so the leak this
# guards against is not hypothetical.
#
# Usage: make check   (or ./scripts/check-secrets.sh)

set -uo pipefail
cd "$(dirname "$0")/.."

status=0

# This script contains the patterns it searches for, so it always excludes
# itself. -I skips binary files so the screenshots are never scanned byte by
# byte and cannot produce a false failure.
tracked_files () {
    git ls-files -z | grep -zZv -e '^scripts/check-secrets.sh$'
}

# Files that legitimately hold real credentials at runtime. They must never be
# tracked. .env.example is the committed template and is fine.
echo "Checking for credential files that should not be tracked..."
creds=$(git ls-files \
    | grep -E '(^|/)(\.env(\..*)?|.*\.pem|.*\.key|.*\.p12|.*\.jks|.*\.keystore|local\.properties|keystore\.properties|signing\.properties|google-services\.json)$' \
    | grep -v '\.env\.example$')

if [ -n "$creds" ]; then
    echo "  FAIL  these are tracked by git and can hold real credentials:"
    echo "$creds" | sed 's/^/          /'
    status=1
fi

# Groq keys have a fixed prefix and are the key this project actually uses.
echo "Checking for Groq API keys..."
groq=$(tracked_files | xargs -0 grep -IhoE '\bgsk_[A-Za-z0-9]{20,}' 2>/dev/null | sort -u)

if [ -n "$groq" ]; then
    echo "  FAIL  found what look like Groq API keys:"
    echo "$groq" | sed 's/^/          /' | cut -c1-28
    status=1
fi

# Hugging Face tokens, for the variant that still calls Hugging Face.
echo "Checking for Hugging Face tokens..."
hf=$(tracked_files | xargs -0 grep -IhoE '\bhf_[A-Za-z0-9]{30,}' 2>/dev/null | sort -u)

if [ -n "$hf" ]; then
    echo "  FAIL  found what look like Hugging Face tokens:"
    echo "$hf" | sed 's/^/          /' | cut -c1-28
    status=1
fi

# A bearer token written straight into source rather than read from the
# environment. This is how the key would most plausibly get committed.
echo "Checking for hard-coded bearer tokens..."
bearer=$(tracked_files \
    | xargs -0 grep -IhoE 'Bearer[[:space:]]+[A-Za-z0-9_\-]{20,}' 2>/dev/null \
    | grep -vE 'Bearer[[:space:]]+\{|Bearer[[:space:]]+\$' \
    | sort -u)

if [ -n "$bearer" ]; then
    echo "  FAIL  found hard-coded bearer tokens:"
    echo "$bearer" | sed 's/^/          /' | cut -c1-32
    status=1
fi

# Private keys are unambiguous: the PEM header is the whole tell.
echo "Checking for private key material..."
pem=$(tracked_files | xargs -0 grep -Il 'BEGIN .*PRIVATE KEY' 2>/dev/null | sort -u)

if [ -n "$pem" ]; then
    echo "  FAIL  these files contain private key material:"
    echo "$pem" | sed 's/^/          /'
    status=1
fi

if [ "$status" -eq 0 ]; then
    echo
    echo "OK — nothing that looks like a real credential."
else
    echo
    echo "Secret check FAILED. Do not commit."
fi

exit "$status"
