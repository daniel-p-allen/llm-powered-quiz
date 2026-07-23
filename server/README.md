# Quiz server — Flask

Asks a language model for a multiple-choice question on a topic, parses the reply
into structured JSON, and returns it to the Android client in
[`../client`](../client).

For the whole system see the [root README](../README.md).

## How it works

```
  Android client
        │  GET /getQuiz?topic=photosynthesis
        ▼
  ┌──────────────────────────────────────────┐
  │  app.py  (Flask, port 5000)              │
  │                                          │
  │   fetchQuizFromLlama()                   │
  │     builds a prompt demanding a          │
  │     fixed output format, then POSTs ─────┼──► Groq
  │                                          │    llama-3.3-70b-versatile
  │   process_quiz()                    ◄────┼──── free-form text reply
  │     regex-parses that reply into         │
  │     question / options / answer          │
  │                                          │
  │   answer_to_index()                      │
  │     turns "B" into 1, the index the      │
  │     client actually stores               │
  └──────────────────┬───────────────────────┘
                     │  JSON
                     ▼
   { "quiz": [ { "question": ...,
                 "options": [A, B, C, D],
                 "correct_answer": "B",
                 "correct_index": 1,
                 "topic": ... } ] }
```

The interesting problem is the middle step. A language model returns prose, but an
Android client needs structured data. The prompt therefore specifies an exact
output shape, and `process_quiz()` parses it with a regular expression — if the
model deviates, parsing returns nothing and the endpoint answers `500` with the
raw text attached, rather than handing the client something malformed.

`answer_to_index()` is the second half of that problem, and it is where a real bug
lived: the server used to send only the answer *letter*, while the client reads an
*integer index*. See the root README for the full account.

## Endpoints

| Method | Path | Query | Returns |
|---|---|---|---|
| `GET` | `/getQuiz` | `topic` | A parsed question, options, answer and index as JSON |
| `GET` | `/test` | – | `{"quiz": "test"}`, for checking the server is reachable |

`/getQuiz` answers `400` if `topic` is missing, and `500` if the model call fails
or the reply cannot be parsed.

## Five ways to call a model

`variants/` keeps the same server written several ways. They exist because "how do
you actually call an LLM from a backend" has several answers with very different
trade-offs.

| File | Approach | Trade-off |
|---|---|---|
| `app.py` | HTTP POST to Groq, an OpenAI-compatible endpoint | **This is the one that runs.** Free tier, 30 requests/minute, no local hardware |
| `variants/main-huggingface-router.py` | The same, against the Hugging Face router | How this project began. Still correct, but Hugging Face's free allowance is now $0.10/month |
| `variants/main-inferenceclient.py` | `huggingface_hub.InferenceClient` | Hosted inference through an SDK rather than raw HTTP |
| `variants/main-pipeline.py` | `transformers.pipeline()`, model running locally | No key or network, but downloads the model and wants a GPU |
| `variants/main-directModel.py` | `AutoTokenizer` + `AutoModelForCausalLM` directly | Most control over generation, most code, same hardware cost |

**Why the default moved off Hugging Face.** The original endpoint pinned a
provider into the URL path (`.../novita/v3/openai/...`). That provider stopped
serving the model, so the URL began returning *"Model not supported by provider
novita"* — the server was dead through no fault of its own. Hugging Face also
replaced its free Inference API with metered Inference Providers: the old
`api-inference.huggingface.co` host no longer resolves, and free accounts get
$0.10 of credit a month. Groq speaks the same OpenAI-compatible dialect, so the
prompt, the parser and the routes are unchanged.

`hugginginfo.md` records the Hugging Face setup — access tokens, the licence
acknowledgement the Gemma model requires, and the router configuration — with
screenshots. It is kept for the variants that still use Hugging Face.

## Running it

```bash
cp .env.example .env              # add a free key from https://console.groq.com/keys
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
.venv/bin/python app.py           # http://127.0.0.1:5000
```

Or `make run` from the repository root, which does the same thing.

The two local variants need heavier dependencies (`transformers`, `torch`) and
several gigabytes of model weights, deliberately kept out of `requirements.txt`
because nothing needed to run the server should pull down a deep-learning stack.

## Layout

```
app.py                 the server that runs
requirements.txt       what app.py needs - flask, requests, python-dotenv
.env.example           template; copy to .env and add your key
tests/                 27 tests - no network, no key
variants/              the same server, four other ways
hugginginfo.md         Hugging Face setup notes and screenshots
```

## Testing

```bash
make test        # from the repository root
```

27 tests, run in well under a second. They drive the real Flask routes through
Flask's test client and stub the model call, so they need no key, make no network
request, and cannot flake on a provider being slow. Nothing in `app.py` was
reshaped to make it testable.

Every test was validated by reintroducing the defect it covers in a throwaway copy
and confirming it fails — eight mutations, all caught. See [`tests/README.md`](tests/README.md).

## Known limitations

- **One question per request.** The prompt asks for a single question; the format
  supports more, and the local variants ask for three.
- **Parsing is brittle by design.** The model is asked for an exact format and the
  reply is matched against it. A model that ignores the format produces a `500`
  rather than a guess — safer for the client, but output quality depends on the
  prompt holding. Questions whose answer cannot be identified are dropped rather
  than guessed at.
- **Flask's development server, single process.** Correct for a demo, wrong for
  production.
- **No authentication, rate limiting or caching.** The endpoints are open and every
  request hits the provider. Fine on localhost, not for deployment.
- **Not deployed.** It runs locally, against the Android emulator at `10.0.2.2:5000`.

## Security

`.env` holds the API key and is excluded by `.gitignore`. No secrets are tracked,
and `scripts/check-secrets.sh` fails the build if one reaches the tree.

The server previously printed its outbound headers — bearer token included — on
every request. It now logs only whether the key is set. A test asserts this, and a
CI step fails if header logging returns.

## License

MIT — see [`../LICENSE`](../LICENSE).

## Development notes

Built by me as part of a university applied-AI project. The July 2026 repair and
documentation pass was carried out with the assistance of an AI coding assistant
(Claude); the code, the design decisions, and the review of the result are my own.
