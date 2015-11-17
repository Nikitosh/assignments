package ru.spbau.mit;

import java.io.*;
import java.util.*;


public class QuizGame implements Game {
    private static final String START = "!start";
    private static final String STOP = "!stop";
    private static final String GAME_STOPPED = "Game has been stopped by ";
    private static final String WINNER_IS = "The winner is ";
    private static final String WRONG_TRY = "Wrong try";
    private static final String PREFIX = "Current prefix is ";
    private static final String NO_ANSWER = "Nobody guessed, the word was ";
    private static final String NEW_ROUND = "New round started: ";

    private int delayUntilNextLetter;
    private int maxLettersToOpen;
    private String dictionaryFilename;
    private GameServer server;
    private ArrayList<Question> questionList = new ArrayList<Question>();
    private int currentQuestionIndex = 0;
    private boolean isStopped = false;
    private Thread currentQuestionThread;

    private static class Question {
        private String question;
        private String answer;
        Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    public QuizGame(GameServer server) {
        this.server = server;
    }

    public void setDelayUntilNextLetter(Integer delayUntilNextLetter) {
        this.delayUntilNextLetter = delayUntilNextLetter;
    }

    public void setMaxLettersToOpen(Integer maxLettersToOpen) {
        this.maxLettersToOpen = maxLettersToOpen;
    }

    public void setDictionaryFilename(String dictionaryFilename) {
        this.dictionaryFilename = dictionaryFilename;
    }

    @Override
    public void onPlayerConnected(String id) {
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        synchronized (this) {
            if (msg == START) {
                isStopped = false;
                startGame();
                startRound();
                return;
            }
            if (isStopped) {
                return;
            }
            if (msg == STOP) {
                isStopped = true;
                currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                server.broadcast(GAME_STOPPED + id);
                if (currentQuestionThread != null) {
                    currentQuestionThread.interrupt();
                }
            } else {
                if (msg != null && msg.equals(questionList.get(currentQuestionIndex).answer)) {
                    server.broadcast(WINNER_IS + id);
                    currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
                    currentQuestionThread.interrupt();
                    startRound();
                } else {
                    server.sendTo(id, WRONG_TRY);
                }
            }
        }
    }

    private void startGame() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                String [] parts = line.split(";");
                if (parts.length != 2) {
                    throw new RuntimeException("Wrong structure of question");
                }
                questionList.add(new Question(parts[0], parts[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error during reading questions");
        }
    }

    private void stopGame() {
        questionList.clear();
        isStopped = true;
    }

    private void startRound() {
        final Question currentQuestion = questionList.get(currentQuestionIndex);
        server.broadcast(NEW_ROUND + currentQuestion.question + " (" + currentQuestion.answer.length() + " letters)");
        currentQuestionThread = new Thread(new Runnable() {
            private int openLettersNumber = 0;

            @Override
            public void run() {
                synchronized (this) {
                    for (int i = 0; i < maxLettersToOpen; i++) {
                        try {
                            Thread.sleep(delayUntilNextLetter);
                            server.broadcast(PREFIX + currentQuestion.answer.substring(0, openLettersNumber++ + 1));
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    try {
                        Thread.sleep(delayUntilNextLetter);
                        server.broadcast(NO_ANSWER + currentQuestion.answer);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        currentQuestionThread.start();
    }
}
