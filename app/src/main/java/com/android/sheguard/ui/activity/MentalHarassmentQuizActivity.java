package com.android.sheguard.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.model.QuizQuestion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MentalHarassmentQuizActivity extends AppCompatActivity {

    private TextView tvProgress, tvQuestion, tvExplanation;
    private RadioGroup rgOptions;
    private Button btnNext;

    private List<QuizQuestion> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mental_harassment_quiz);

        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvExplanation = findViewById(R.id.tvExplanation);
        rgOptions = findViewById(R.id.rgOptions);
        btnNext = findViewById(R.id.btnNext);

        loadQuestions();
        showQuestion();

        btnNext.setOnClickListener(v -> {
            if (!isAnswered) {
                if (rgOptions.getCheckedRadioButtonId() != -1) {
                    checkAnswer();
                } else {
                    Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                }
            } else {
                currentQuestionIndex++;
                if (currentQuestionIndex < questionList.size()) {
                    showQuestion();
                } else {
                    finishQuiz();
                }
            }
        });
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();

        questionList.add(new QuizQuestion(
                "Do you feel unsafe walking alone in your neighborhood, especially at night?",
                Arrays.asList("Yes", "No"),
                0,
                "Fear of public spaces restricts freedom and is a common result of societal harassment."
        ));

        questionList.add(new QuizQuestion(
                "Have you ever been followed or stalked by a stranger in a public place?",
                Arrays.asList("Yes", "No"),
                0,
                "Stalking is a serious crime and a form of intimidation that violates your safety."
        ));

        questionList.add(new QuizQuestion(
                "Do strangers frequently make inappropriate comments about your appearance (catcalling)?",
                Arrays.asList("Yes", "No"),
                0,
                "Street harassment creates a hostile environment and objectifies women in public spaces."
        ));

        questionList.add(new QuizQuestion(
                "Have you experienced unwanted touching or groping on public transport?",
                Arrays.asList("Yes", "No"),
                0,
                "Inappropriate touching is sexual assault and a violation of your bodily autonomy."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel pressured by family or society to dress in a way you don't want to?",
                Arrays.asList("Yes", "No"),
                0,
                "Policing women's clothing is a form of control and restricts personal expression."
        ));

        questionList.add(new QuizQuestion(
                "Have you been denied education or career opportunities simply because you are a woman?",
                Arrays.asList("Yes", "No"),
                0,
                "Gender discrimination blocks access to equal rights and financial independence."
        ));

        questionList.add(new QuizQuestion(
                "Do you receive unsolicited inappropriate messages or photos on social media?",
                Arrays.asList("Yes", "No"),
                0,
                "Online harassment (cyber-flashing) is a digital violation of your privacy and consent."
        ));

        questionList.add(new QuizQuestion(
                "Are your ideas constantly interrupted or dismissed in meetings (work/community)?",
                Arrays.asList("Yes", "No"),
                0,
                "Being silenced or talked over creates an environment where women's voices are undervalued."
        ));

        questionList.add(new QuizQuestion(
                "Do you constantly check your surroundings or carry items for self-defense?",
                Arrays.asList("Yes", "No"),
                0,
                "The constant need for hyper-vigilance is a symptom of living in an unsafe society."
        ));

        questionList.add(new QuizQuestion(
                "Have you been blamed for harassment you faced because of your clothes or behavior?",
                Arrays.asList("Yes", "No"),
                0,
                "Victim-blaming shifts responsibility from the abuser to the survivor, invalidating their experience."
        ));

        questionList.add(new QuizQuestion(
                "Are you pressured to get married or have children against your personal timeline?",
                Arrays.asList("Yes", "No"),
                0,
                "Societal pressure on reproductive choices disregards a woman's right to choose her own path."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel judged or shamed for being independent, ambitious, or outspoken?",
                Arrays.asList("Yes", "No"),
                0,
                "Shaming women for ambition is a tactic to maintain traditional gender roles and limit power."
        ));

        questionList.add(new QuizQuestion(
                "Have you been told that 'boys will be boys' to excuse bad behavior towards you?",
                Arrays.asList("Yes", "No"),
                0,
                "This phrase normalizes harassment and prevents accountability for harmful actions."
        ));

        questionList.add(new QuizQuestion(
                "Do you avoid certain streets or areas in your city due to fear of harassment?",
                Arrays.asList("Yes", "No"),
                0,
                "Restricting movement due to fear limits access to public life and opportunities."
        ));

        questionList.add(new QuizQuestion(
                "Do people explain things to you in a condescending way, assuming you don't know?",
                Arrays.asList("Yes", "No"),
                0,
                "Condescension based on gender (mansplaining) undermines your competence and intelligence."
        ));

        questionList.add(new QuizQuestion(
                "Are you expected to do the majority of household chores solely because of your gender?",
                Arrays.asList("Yes", "No"),
                0,
                "Imposing gendered roles creates unequal burdens and limits time for personal or professional growth."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel you have to smile or be overly polite to strangers to avoid aggression?",
                Arrays.asList("Yes", "No"),
                0,
                "Forced compliance serves to placate potential aggressors but is an unfair emotional burden."
        ));

        questionList.add(new QuizQuestion(
                "Has anyone ever shared your private photos or information without your consent?",
                Arrays.asList("Yes", "No"),
                0,
                "Non-consensual sharing of private data is a violation of trust and can be used for blackmail."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel you are paid less than male colleagues for doing the same work?",
                Arrays.asList("Yes", "No"),
                0,
                "Unequal pay is financial discrimination and undervalues your contribution."
        ));

        questionList.add(new QuizQuestion(
                "Have you been asked inappropriate questions about marriage/kids during job interviews?",
                Arrays.asList("Yes", "No"),
                0,
                "These questions are discriminatory and irrelevant to professional capability."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel the need to share your live location with others just to feel safe?",
                Arrays.asList("Yes", "No"),
                0,
                "Reliability on tracking for safety highlights the lack of security women feel in public."
        ));

        questionList.add(new QuizQuestion(
                "Have you faced restrictions on your freedom of movement or who you can meet?",
                Arrays.asList("Yes", "No"),
                0,
                "Controlling social interactions is a form of abuse aimed at isolation."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel constantly watched or judged by neighbors or your community?",
                Arrays.asList("Yes", "No"),
                0,
                "Moral policing by the community restricts privacy and personal freedom."
        ));

        questionList.add(new QuizQuestion(
                "Have you been ridiculed for emotional expression (e.g., called 'hysterical' or 'too sensitive')?",
                Arrays.asList("Yes", "No"),
                0,
                "Dismissing emotions with gendered slurs invalidates your feelings and gaslights your experience."
        ));

        questionList.add(new QuizQuestion(
                "Do you feel you cannot report harassment because you won't be believed?",
                Arrays.asList("Yes", "No"),
                0,
                "The fear of not being believed is a major barrier to justice and perpetuates the cycle of harassment."
        ));
    }

    private void showQuestion() {
        QuizQuestion q = questionList.get(currentQuestionIndex);

        tvProgress.setText("Question " + (currentQuestionIndex + 1) + "/" + questionList.size());
        tvQuestion.setText(q.getQuestion());
        tvExplanation.setVisibility(View.GONE);
        rgOptions.removeAllViews();
        rgOptions.clearCheck();

        for (int i = 0; i < q.getOptions().size(); i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(q.getOptions().get(i));
            rb.setId(i);
            rb.setTextSize(16);
            rb.setPadding(16, 16, 16, 16);
            rgOptions.addView(rb);
        }

        btnNext.setText(currentQuestionIndex == questionList.size() - 1 ? "Finish" : "Next");
        isAnswered = false;
        
        // Enable RadioGroup
        for (int i = 0; i < rgOptions.getChildCount(); i++) {
            rgOptions.getChildAt(i).setEnabled(true);
        }
    }

    private void checkAnswer() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        QuizQuestion q = questionList.get(currentQuestionIndex);

        boolean isCorrect = (selectedId == q.getCorrectOptionIndex());

        if (isCorrect) {
            score++;
        }
        
        tvExplanation.setBackgroundColor(getResources().getColor(android.R.color.white));

        tvExplanation.setText(q.getExplanation());
        tvExplanation.setVisibility(View.VISIBLE);
        
        // Disable RadioGroup to prevent changing answer
        for (int i = 0; i < rgOptions.getChildCount(); i++) {
            rgOptions.getChildAt(i).setEnabled(false);
        }

        isAnswered = true;
    }

    private void finishQuiz() {
        String resultText;
        int color;
        String riskLevel;

        if (score <= 5) {
            riskLevel = "Low Risk";
            resultText = "Safe / Low Risk\n\nYou seem to be in a relatively safe environment. However, always trust your instincts and stay aware of your rights.";
            color = getResources().getColor(android.R.color.holo_green_dark);
        } else if (score <= 10) {
            riskLevel = "Moderate Risk";
            resultText = "Moderate Risk\n\nYou are facing some forms of harassment. It is important to set boundaries and seek support from trusted friends or family.";
            color = getResources().getColor(android.R.color.holo_orange_light);
        } else if (score <= 15) {
            riskLevel = "High Risk";
            resultText = "High Risk\n\nYou are experiencing significant harassment. Please consider reaching out to a professional or a helpline for guidance.";
            color = getResources().getColor(android.R.color.holo_orange_dark);
        } else {
            riskLevel = "Severe Risk";
            resultText = "Severe Risk / Danger\n\nYou are in a hostile environment with clear signs of severe harassment or abuse. Please prioritize your safety and contact emergency services or a helpline immediately.";
            color = getResources().getColor(android.R.color.holo_red_dark);
        }

        tvQuestion.setText("Risk Analysis Report");
        tvExplanation.setText(resultText);
        tvExplanation.setTextColor(getResources().getColor(android.R.color.white));
        tvExplanation.setBackgroundColor(color);
        tvExplanation.setTextSize(18);
        tvExplanation.setPadding(32, 32, 32, 32);
        tvExplanation.setVisibility(View.VISIBLE);
        
        rgOptions.setVisibility(View.GONE);
        saveQuizResultToDatabase(riskLevel);
        
        btnNext.setText("Close");
        btnNext.setOnClickListener(v -> finish());
    }

    private void saveQuizResultToDatabase(String riskLevel) {
        Map<String, Object> quizResult = new HashMap<>();
        quizResult.put("score", score);
        quizResult.put("max_score", questionList.size());
        quizResult.put("risk_level", riskLevel);
        quizResult.put("timestamp", new Date());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            quizResult.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            quizResult.put("user_id", "anonymous");
        }

        FirebaseFirestore.getInstance().collection("quiz_assessments")
                .add(quizResult)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("MentalHarassmentQuiz", "Quiz result saved to database successfully.");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MentalHarassmentQuiz", "Failed to save quiz result", e);
                });
    }
}
