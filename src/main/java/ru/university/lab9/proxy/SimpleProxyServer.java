package ru.university.lab9.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SimpleProxyServer implements AutoCloseable {
    private final int listenPort;
    private final String targetHost;
    private final int targetPort;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    public SimpleProxyServer(int listenPort, String targetHost, int targetPort) {
        this.listenPort = listenPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public void start() throws IOException {
        if (running) {
            return;
        }
        running = true;
        serverSocket = new ServerSocket(listenPort);
        try {
            System.out.println("Прокси-сервер запущен на порту " + serverSocket.getLocalPort()
                    + " -> " + targetHost + ":" + targetPort);
            while (running) {
                Socket client = serverSocket.accept();
                executor.submit(() -> handleClient(client));
            }
        } catch (IOException exception) {
            if (running) {
                throw exception;
            }
        } finally {
            close();
        }
    }

    public Future<?> startAsync() {
        return executor.submit(() -> {
            try {
                start();
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    public int getLocalPort() {
        return serverSocket == null ? listenPort : serverSocket.getLocalPort();
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket; Socket serverSocket = new Socket(targetHost, targetPort)) {
            InputStream clientInput = clientSocket.getInputStream();
            OutputStream clientOutput = clientSocket.getOutputStream();
            InputStream serverInput = serverSocket.getInputStream();
            OutputStream serverOutput = serverSocket.getOutputStream();

            executor.submit(() -> pipe(clientInput, serverOutput));
            pipe(serverInput, clientOutput);
        } catch (IOException exception) {
            System.out.println("Прокси-соединение закрыто: " + exception.getMessage());
        }
    }

    private void pipe(InputStream inputStream, OutputStream outputStream) {
        byte[] buffer = new byte[4096];
        int read;
        try {
            while ((read = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, read);
                outputStream.flush();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        executor.shutdownNow();
    }
}
