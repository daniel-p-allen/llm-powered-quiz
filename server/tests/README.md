# Server tests

27 tests. They run in well under a second, need no API key, and make no network
request.

```bash
make test                                   # from the repository root
cd server && .venv/bin/python -m pytest tests/ -q
```

## How they are built

**The tests adapt to the code, not the other way round.** `app.py` is a plain
Flask script and was not restructured to make it importable, injectable or
mockable. `conftest.py` puts its directory on `sys.path` and the tests drive the
real routes through Flask's `test_client()`.

**The model call is stubbed**, so the suite is deterministic. It cannot fail
because Groq is slow, because a key expired, or because a monthly allowance ran
out — all of which happened while this project was being repaired. That also means
CI needs no secret, so the suite runs on pull requests from anywhere.

One test deliberately stubs `requests.post` instead of `fetchQuizFromLlama`,
because the behaviour under test — that the API key is never printed — lives
*inside* that function. Stubbing the function would have skipped the code being
tested.

## What is covered

| Area | Tests |
|---|---|
| `answer_to_index` | every letter A–D; untidy forms (`b`, `**B**`, `B)`, `B. Paris`); answers given as option text; words beginning with A–D not misread; unmappable answers |
| `process_quiz` | shape of a parsed question; `correct_index` derived from the letter; `correct_answer` still sent; `topic` populated; unusable questions dropped; several questions in one reply; unparseable text |
| Routes | `/test`; missing `topic` → 400; a full quiz; unparseable model output → 500 with the raw text; upstream failure reported not crashed |
| Security | the API key is never printed |

## Why you can trust them

**A suite that has only ever passed proves nothing.** Every test here was
validated by reintroducing the defect it covers into a *throwaway copy* of the
server — never this repository — and confirming the suite fails.

Eight mutations, all caught:

| Mutation | Caught by |
|---|---|
| `correct_index` always 0 — the original bug | 4 tests |
| `correct_index` removed entirely | 3 tests |
| `topic` no longer sent | 2 tests |
| `correct_answer` dropped, breaking additivity | 1 test |
| word boundary removed from the letter regex | 1 test |
| unusable answers kept and graded as A | 1 test |
| API key logged in the outbound headers | 1 test |
| lowercase answer letters no longer accepted | 2 tests |

That exercise found two worthless tests on its first run, both since fixed:

- The word-boundary test asserted an index that was correct *either way*, so
  removing the boundary changed nothing. The option was moved to a different
  index so the two outcomes differ.
- The key-leak test stubbed the very function containing the logging, so the
  print never ran and the test passed regardless of the code. It now stubs the
  HTTP call instead.

Neither would have been found by running the suite and seeing green.
