import os
from flask import Flask, request, jsonify
import re
from transformers import pipeline
import torch

app = Flask(__name__)

# Determine device (GPU if available, else CPU)
device = 0 if torch.cuda.is_available() else -1
print(f"Using device: {'GPU' if device >= 0 else 'CPU'}")

# Initialize the pipeline with the specified model
# MODEL = "meta-llama/Llama-3.2-1B"
MODEL = "google/gemma-3-1b-it"

pipe = pipeline(
    "text-generation",
    model=MODEL,
    device=device,  # Dynamically set to GPU (0) or CPU (-1)
    framework="pt",  # Explicitly use PyTorch
    # torch_dtype="auto",  # Uncomment for optimized precision (e.g., float16 on GPU)
)

def fetchQuizFromLlama(student_topic):
    print(f"Generating quiz for topic: {student_topic} using {MODEL}")
    prompt = (
        f"Generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"Format your response as follows:\n"
        f"**QUESTION 1:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 2:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 3:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
        f"Follow this pattern for all questions. "
        f"Here is the student topic:\n{student_topic}"
    )

    try:
        # Generate text using the pipeline
        result = pipe(
            prompt,
            max_new_tokens=500,
            temperature=0.7,
            top_p=0.9,
            do_sample=True,
            return_full_text=False,  #True for Full
        )
        # Extract the generated text
        generated_text = result[0]["generated_text"]
        # Extract the quiz part from the full output
        quiz_start = generated_text.find("**QUESTION 1:**")
        if quiz_start == -1:
            raise Exception("Failed to generate a properly formatted quiz")
        quiz_text = generated_text[quiz_start:]
        return quiz_text
    except Exception as e:
        raise Exception(f"Failed to generate quiz with pipeline: {str(e)}")

def process_quiz(quiz_text):
    questions = []
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n|$)',
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans = match[5].strip()

        # Skip questions with placeholder text
        if "[Your question here]" in question or any("[First option]" in opt for opt in options):
            continue

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans
        }
        questions.append(question_data)

    return questions

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if not student_topic:
        return jsonify({'error': 'Missing topic parameter'}), 400
    try:
        quiz = fetchQuizFromLlama(student_topic)
        print(quiz)
        processed_quiz = process_quiz(quiz)
        if not processed_quiz:
            return jsonify({'error': 'Failed to parse quiz data', 'raw_response': quiz}), 500
        return jsonify({'quiz': processed_quiz}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

if __name__ == '__main__':
    port_num = 5000
    print(f"App running on port {port_num}")
    app.run(port=port_num, host="0.0.0.0")
