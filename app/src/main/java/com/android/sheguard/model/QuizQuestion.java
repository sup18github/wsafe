package com.android.sheguard.model;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;

    public QuizQuestion(String question, List<String> options, int correctOptionIndex, String explanation) {
        this.question = question;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }
}
