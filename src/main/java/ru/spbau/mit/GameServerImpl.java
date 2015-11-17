package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.RunnableFuture;


public class GameServerImpl implements GameServer {
    private int connectionNumber = 0;
    private Game game;
    private HashMap <String, Connection> connections = new HashMap<String, Connection>();

    public GameServerImpl(String gameClassName, Properties properties) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class <?> gameClass = Class.forName(gameClassName);
        game = (Game) gameClass.getConstructor(GameServer.class).newInstance(this);
        for (String property : properties.stringPropertyNames()) {
            String methodName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
            try {
                Integer number = Integer.parseInt(properties.getProperty(property));
                gameClass.getMethod(methodName, Integer.class).invoke(game, number);
            } catch (NumberFormatException e) {
                gameClass.getMethod(methodName, String.class).invoke(game, properties.getProperty(property));
            }
        }
    }

    @Override
    public void accept(final Connection connection) {
        final String id = Integer.toString(connectionNumber++);
        connections.put(id, connection);
        connection.send(id);
        new Thread(new Runnable() {
            private static final int TIMEOUT = 1000;

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
                    } catch (InterruptedException e) {}
                }
            }
        }).start();
    }

    @Override
    public void broadcast(String message) {
        for (Connection connection : connections.values()) {
            if (!connection.isClosed()) {
                connection.send(message);
            }
        }
    }

    @Override
    public void sendTo(String id, String message) {
        Connection connection = connections.get(id);
        if (!connection.isClosed()) {
            connection.send(message);
        }
    }
}
