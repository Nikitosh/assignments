package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.RunnableFuture;


public class GameServerImpl implements GameServer {
    private int connectionNumber = 0;
    private Game game;
    private HashMap <String, Connection> connections = new HashMap<String, Connection>();

    private Integer getInteger(String s) {
        try {
            int value = Integer.parseInt(s);
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> gameClass = Class.forName(gameClassName);
        game = (Game) gameClass.getConstructor(GameServer.class).newInstance(this);
        for (String property : properties.stringPropertyNames()) {
            String methodName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            String propertyValue = properties.getProperty(property);
            Integer number = getInteger(propertyValue);
            if (number == null) {
                gameClass.getMethod(methodName, String.class).invoke(game, propertyValue);
            } else {
                gameClass.getMethod(methodName, int.class).invoke(game, number);
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        final String id = Integer.toString(connectionNumber++);
        synchronized (connections) {
            connections.put(id, connection);
        }
        connection.send(id);
        new Thread(new Runnable() {
            private static final int TIMEOUT = 100;

            @Override
            public void run() {
                game.onPlayerConnected(id);
                while (!connection.isClosed()) {
                    try {
                        synchronized (connection) {
                            if (!connection.isClosed()) {
                                String message = connection.receive(TIMEOUT);
                                if (message != null) {
                                    game.onPlayerSentMsg(id, message);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }).start();
    }

    @Override
    public void broadcast(String message) {
        synchronized (connections) {
            for (Connection connection : connections.values()) {
                if (!connection.isClosed()) {
                    connection.send(message);
                }
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        synchronized (connections) {
            Connection connection = connections.get(id);
            if (!connection.isClosed()) {
                connection.send(message);
            }
        }
    }
}
