from dotenv import load_dotenv
load_dotenv()  # load variables from .env into os.environ

import os
import re
import requests
from flask import Flask, request, jsonify
# working fine
app = Flask(__name__)

# ── API setup ───────────────────────────────────────────────────────────────
# The router used to be addressed with the provider pinned into the path
# (.../novita/v3/openai/...). Novita stopped serving this model, so that URL now
# returns "Model not supported by provider novita" and the server was dead through
# no fault of its own. Asking the router to choose a provider keeps working when
# any single one drops the model.
API_URL      = "https://router.huggingface.co/v1/chat/completions"
HF_API_TOKEN = os.getenv('HF_API_TOKEN', '').strip()
HEADERS      = {"Authorization": f"Bearer {HF_API_TOKEN}"}

# ── MODEL choices (router-side) ──────────────────────────────────────────────
MODEL = "google/gemma-3-27b-it"

def fetchQuizFromLlama(student_topic):
    # The headers used to be printed in full, which put the bearer token in the
    # log on every single request. Whether the token is present is the only part
    # that is ever useful for debugging; the token itself never is.
    print(">>> HITTING URL:", API_URL)
    print(">>> TOKEN:", "set" if HF_API_TOKEN else "MISSING")
    print("Fetching quiz for topic:", student_topic)
    payload = {
        "model": MODEL,
        "messages": [
            {"role":"user","content":(
                f"Generate a quiz with 1 question on “{student_topic}”. "
                "Each question should have 4 options (A–D) and exactly one correct answer. "
                "Format:\n"
                "**QUESTION 1:** ...\n"
                "**OPTION A:** ...\n"
                "**OPTION B:** ...\n"
                "**OPTION C:** ...\n"
                "**OPTION D:** ...\n"
                "**ANS:** A\n\n"
                # repeat for Q2 and Q3 if desired
            )}
        ],
        "max_tokens": 500,
        "temperature": 0.7,
        "top_p": 0.9
    }
    resp = requests.post(API_URL, headers=HEADERS, json=payload, timeout=30)
    if resp.status_code != 200:
        raise Exception(f"API request failed: {resp.status_code} - {resp.text}")
    return resp.json()["choices"][0]["message"]["content"]

def answer_to_index(answer, options):
    """Turn the model's answer into the 0-based option index the client needs.

    The Android client stores `correct_index`, an int. This server used to send
    only `correct_answer`, a letter. Gson silently dropped the field it did not
    recognise and left correct_index at its default of 0, so every question was
    graded as if option A were correct. Sending the index here fixes that without
    the client changing at all.

    The model is not trusted to be tidy about the letter: "B", " b ", "**B**",
    "B)" and "B. Paris" have all been seen. Where no letter can be found the
    answer is compared against the option text, which the model sometimes returns
    instead. Returns None when neither works.
    """
    cleaned = answer.strip()

    # A leading letter A-D, ignoring any decoration around it. The word boundary
    # stops "Deakin" being read as option D.
    match = re.match(r'^\W*([A-Da-d])\b', cleaned)
    if match:
        return ord(match.group(1).upper()) - ord('A')

    # Fall back to matching the answer against the options themselves.
    for i, option in enumerate(options):
        if option.strip().lower() == cleaned.lower():
            return i

    return None

def process_quiz(quiz_text, topic=None):
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n|$)', re.DOTALL
    )
    questions = []
    for q,a,b,c,d,ans in pattern.findall(quiz_text):
        options = [a.strip(), b.strip(), c.strip(), d.strip()]
        index = answer_to_index(ans, options)

        # A question whose answer cannot be identified is worse than no question:
        # the client would grade it against a guess. Drop it. If every question is
        # dropped the caller sees the existing "Parse failed" path.
        if index is None:
            continue

        questions.append({
            "question": q.strip(),
            "options": options,
            "correct_answer": ans.strip(),
            "correct_index": index,
            "topic": topic
        })
    return questions

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    topic = request.args.get('topic')
    if not topic:
        return jsonify(error="Missing topic"), 400
    try:
        raw = fetchQuizFromLlama(topic)
        parsed = process_quiz(raw, topic)
        if not parsed:
            return jsonify(error="Parse failed", raw=raw), 500
        return jsonify(quiz=parsed)
    except Exception as e:
        return jsonify(error=str(e)), 500

@app.route('/test', methods=['GET'])
def test():
    return jsonify(quiz="test")

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
