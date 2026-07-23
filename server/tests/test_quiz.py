"""Tests for the quiz server.

The server is driven through its real routes with Flask's test client, and the
model call is stubbed. Nothing here needs a key, a network or a running server,
so the suite is deterministic and safe to run on any pull request.

The bug these tests exist for: the server sent the answer as a letter
("correct_answer": "B") while the Android client reads an int field
("correct_index"). Gson dropped the field it did not recognise and left the int
at its default of 0, so every question was graded as if option A were correct.
The tests that matter most are the ones asserting a non-zero index — an
A-answered fixture would have passed against the broken code too.
"""

import conftest
import pytest
from conftest import WELL_FORMED

import app as server


# ── answer_to_index ─────────────────────────────────────────────────────────

@pytest.mark.parametrize("letter,expected", [("A", 0), ("B", 1), ("C", 2), ("D", 3)])
def test_each_letter_maps_to_its_index(letter, expected):
    assert server.answer_to_index(letter, ["w", "x", "y", "z"]) == expected


@pytest.mark.parametrize("answer", ["b", " B ", "**B**", "B)", "B.", "B. Paris", "(b)"])
def test_untidy_letters_still_map(answer):
    """The model is not consistent about decoration, so the parser must not be."""
    assert server.answer_to_index(answer, ["w", "x", "y", "z"]) == 1


def test_answer_given_as_option_text_is_matched():
    """Sometimes the model answers with the option itself rather than a letter."""
    options = ["London", "Paris", "Berlin", "Madrid"]
    assert server.answer_to_index("Paris", options) == 1
    assert server.answer_to_index("  paris  ", options) == 1


def test_word_beginning_with_a_letter_is_not_read_as_that_letter():
    """'Deakin' must not be read as option D. The word boundary is load-bearing.

    The option deliberately sits at index 0 while 'D' would mean index 3, so
    dropping the \\b from the pattern makes this test fail. An earlier version put
    the option at index 3 and passed either way, proving nothing.
    """
    options = ["Deakin University", "Melbourne", "Perth", "Sydney"]
    assert server.answer_to_index("Deakin University", options) == 0


def test_unmappable_answer_returns_none():
    assert server.answer_to_index("Zebra", ["w", "x", "y", "z"]) is None
    assert server.answer_to_index("", ["w", "x", "y", "z"]) is None


# ── process_quiz ────────────────────────────────────────────────────────────

def test_well_formed_response_parses_into_the_expected_shape():
    parsed = server.process_quiz(WELL_FORMED, topic="Geography")
    assert len(parsed) == 1
    q = parsed[0]
    assert q["question"] == "What is the capital of France?"
    assert q["options"] == ["London", "Paris", "Berlin", "Madrid"]


def test_correct_index_is_derived_from_the_answer_letter():
    """THE regression test. Answer is B, so the index must be 1 — never 0."""
    q = server.process_quiz(WELL_FORMED, topic="Geography")[0]
    assert q["correct_index"] == 1
    assert q["options"][q["correct_index"]] == "Paris"


def test_correct_answer_is_still_sent():
    """The fix is additive: the original field stays so nothing downstream breaks."""
    q = server.process_quiz(WELL_FORMED, topic="Geography")[0]
    assert q["correct_answer"] == "B"


def test_topic_is_included_in_each_question():
    """QuizItem has a topic field the server never used to populate."""
    q = server.process_quiz(WELL_FORMED, topic="Geography")[0]
    assert q["topic"] == "Geography"


def test_questions_with_an_unusable_answer_are_dropped():
    """A question graded against a guess is worse than no question at all."""
    bad = WELL_FORMED.replace("**ANS:** B", "**ANS:** Zebra")
    assert server.process_quiz(bad, topic="Geography") == []


def test_several_questions_are_all_parsed():
    three = (
        WELL_FORMED
        + WELL_FORMED.replace("QUESTION 1", "QUESTION 2").replace("**ANS:** B", "**ANS:** D")
        + WELL_FORMED.replace("QUESTION 1", "QUESTION 3").replace("**ANS:** B", "**ANS:** C")
    )
    parsed = server.process_quiz(three, topic="Geography")
    assert [q["correct_index"] for q in parsed] == [1, 3, 2]


def test_unparseable_text_yields_no_questions():
    assert server.process_quiz("the model said something else entirely", "x") == []


# ── routes ──────────────────────────────────────────────────────────────────

def test_test_route_responds(client):
    r = client.get("/test")
    assert r.status_code == 200
    assert r.get_json() == {"quiz": "test"}


def test_missing_topic_is_rejected(client):
    r = client.get("/getQuiz")
    assert r.status_code == 400
    assert "error" in r.get_json()


def test_getquiz_returns_a_parsed_quiz(client, stub_model):
    stub_model()
    r = client.get("/getQuiz?topic=Geography")
    assert r.status_code == 200
    quiz = r.get_json()["quiz"]
    assert len(quiz) == 1
    assert quiz[0]["correct_index"] == 1
    assert quiz[0]["topic"] == "Geography"


def test_unparseable_model_output_is_a_500_carrying_the_raw_text(client, stub_model):
    """Existing behaviour worth keeping: the raw response aids debugging."""
    stub_model(response="nothing resembling a quiz")
    r = client.get("/getQuiz?topic=Geography")
    assert r.status_code == 500
    assert "nothing resembling a quiz" in r.get_json()["raw"]


def test_upstream_failure_is_reported_not_crashed(client, stub_model):
    stub_model(error=RuntimeError("API request failed: 402 - out of credits"))
    r = client.get("/getQuiz?topic=Geography")
    assert r.status_code == 500
    assert "402" in r.get_json()["error"]


def test_the_api_key_is_never_printed(monkeypatch, capsys):
    """The headers used to be printed in full on every request, key included.

    This stubs requests.post rather than fetchQuizFromLlama, because the logging
    being tested lives *inside* fetchQuizFromLlama. An earlier version of this
    test stubbed the function itself, so the print never ran and the test passed
    no matter what the code did — mutation testing caught that.
    """
    # Assembled at runtime rather than written as a literal. scripts/check-secrets.sh
    # scans for anything key-shaped and cannot tell a fixture from the real thing —
    # correctly, so the fixture avoids looking like one.
    secret = "gsk_" + "fakeTestKeyNeverReal" + "0" * 12

    class FakeResponse:
        status_code = 200

        @staticmethod
        def json():
            return {"choices": [{"message": {"content": WELL_FORMED}}]}

    monkeypatch.setattr(server, "GROQ_API_KEY", secret)
    monkeypatch.setattr(server, "HEADERS", {"Authorization": f"Bearer {secret}"})
    monkeypatch.setattr(server.requests, "post", lambda *a, **kw: FakeResponse())

    server.fetchQuizFromLlama("Geography")

    printed = capsys.readouterr().out
    assert secret not in printed
    assert "Bearer" not in printed
