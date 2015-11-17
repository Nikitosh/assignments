package ru.spbau.mit;

import java.util.*;


public class SumTwoNumbersGame implements Game {
    private final static String RIGHT = "Right";
    private final static String WON = " won";
    private final static String WRONG = "Wrong";
    private Integer firstInt, secondInt;
    private GameServer server;

    private void generateIntegers() {
        firstInt = Math.abs(new Random().nextInt());
        secondInt = Math.abs(new Random().nextInt());
    }

    public SumTwoNumbersGame(GameServer server) {
        this.server = server;
        generateIntegers();
    }

    @Override
    public void onPlayerConnected(String id) {
        server.sendTo(id, firstInt.toString() + " " + secondInt.toString());
    }

    @Override
    public void onPlayerSentMsg(String id, String msg) {
        try {
            Integer answer = Integer.parseInt(msg);
            if (answer.equals(firstInt + secondInt)) {
                server.sendTo(id, RIGHT);
                server.broadcast(id + WON);
                generateIntegers();
                server.broadcast(firstInt.toString() + " " + secondInt.toString());
            }
            else {
                server.sendTo(id, WRONG);
            }
        } catch (NumberFormatException e) {}
    }
}
