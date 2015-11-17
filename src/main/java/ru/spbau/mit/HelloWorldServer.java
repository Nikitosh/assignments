package ru.spbau.mit;


public class HelloWorldServer implements Server {

    @Override
    public void accept(final Connection connection) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.send("Hello world");
                connection.close();
            }
        }).start();
    }
}
