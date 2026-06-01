package ru.university.lab9.server;

import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;
import ru.university.lab9.protocol.ProtoStructCodec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AnonymousVotingServer implements AutoCloseable {
    private final AnonymousVotingService votingService;
    private final int port;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    public AnonymousVotingServer(AnonymousVotingService votingService, int port) {
        this.votingService = votingService;
        this.port = port;
    }

    public void start() throws IOException {
        if (running) {
            return;
        }
        running = true;
        serverSocket = new ServerSocket(port);
        System.out.println("Voting server started on port " + serverSocket.getLocalPort());
        try {
            while (running) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
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
        return serverSocket == null ? port : serverSocket.getLocalPort();
    }

    private void handleClient(Socket socket) {
        try (socket) {
            int preamble = socket.getInputStream().read();
            if (preamble == -1) {
                return;
            }

            Connection connection = preamble == 'B'
                    ? new ProtoConnection(socket)
                    : new TextConnection(socket);

            String currentUser = null;
            ProtocolRequest request;
            while ((request = connection.readRequest()) != null) {
                ProtocolResponse response;
                switch (request.command().toUpperCase()) {
                    case "REGISTER" -> {
                        currentUser = request.argument();
                        response = votingService.register(currentUser);
                    }
                    case "NOMINATE" -> response = votingService.nominate(currentUser, request.argument());
                    case "VOTE" -> response = votingService.vote(currentUser, request.argument());
                    case "RESULTS" -> response = votingService.results();
                    default -> response = new ProtocolResponse(false, "Unknown command: " + request.command());
                }
                connection.writeResponse(response);
            }
        } catch (IOException exception) {
            System.out.println("Client connection closed: " + exception.getMessage());
        }
    }

    private interface Connection extends Closeable {
        ProtocolRequest readRequest() throws IOException;

        void writeResponse(ProtocolResponse response) throws IOException;
    }

    private static final class TextConnection implements Connection {
        private final BufferedReader reader;
        private final PrintWriter writer;

        private TextConnection(Socket socket) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)),
                    true
            );
        }

        @Override
        public ProtocolRequest readRequest() throws IOException {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            String[] parts = line.split("\\|", 2);
            String argument = parts.length > 1 ? parts[1] : "";
            return new ProtocolRequest(parts[0], argument);
        }

        @Override
        public void writeResponse(ProtocolResponse response) {
            writer.println((response.success() ? "OK" : "ERROR") + "|" + response.message());
        }

        @Override
        public void close() {
        }
    }

    private static final class ProtoConnection implements Connection {
        private final DataInputStream inputStream;
        private final DataOutputStream outputStream;

        private ProtoConnection(Socket socket) throws IOException {
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public ProtocolRequest readRequest() throws IOException {
            return ProtoStructCodec.readRequest(inputStream);
        }

        @Override
        public void writeResponse(ProtocolResponse response) throws IOException {
            ProtoStructCodec.writeResponse(outputStream, response);
        }

        @Override
        public void close() {
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
