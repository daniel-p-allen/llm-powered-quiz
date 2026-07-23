"""Test fixtures.

app.py sits one directory up and is a plain script, not an installed package.
Rather than restructure the server so a test can import it, the path is adjusted
here — the tests adapt to the code, not the other way round.
"""

import os
import sys

import pytest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import app as server  # noqa: E402


# A well-formed model response in the exact format the prompt asks for.
# The correct answer is deliberately B, not A: with the old code every question
# graded as A, so an A-answer fixture would have passed against the bug.
WELL_FORMED = """**QUESTION 1:** What is the capital of France?
**OPTION A:** London
**OPTION B:** Paris
**OPTION C:** Berlin
**OPTION D:** Madrid
**ANS:** B
"""


@pytest.fixture
def client():
    """Flask's test client, driving the real routes."""
    server.app.config.update(TESTING=True)
    return server.app.test_client()


@pytest.fixture
def stub_model(monkeypatch):
    """Replace the Hugging Face call.

    Every test runs through this, so the suite makes no network call and needs no
    token. CI can therefore run it on a pull request from anywhere, and a slow or
    unreachable Hugging Face can never turn into a red build.
    """

    def _stub(response=WELL_FORMED, error=None):
        def fake_fetch(topic):
            if error is not None:
                raise error
            return response

        monkeypatch.setattr(server, "fetchQuizFromLlama", fake_fetch)

    return _stub
