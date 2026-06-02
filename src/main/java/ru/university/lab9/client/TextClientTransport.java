package ru.university.lab9.client;

import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TextClientTransport implements ClientTransport {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    @Override
    public void connect(String host, int port) throws IOException {
        close();
        socket = new Socket(host, port);
        socket.getOutputStream().write('T');
        socket.getOutputStream().flush();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
    }

    @Override
    public ProtocolResponse send(ProtocolRequest request) throws IOException {
        writer.println(request.command() + "|" + request.argument());
        String response = reader.readLine();
        if (response == null) {
            throw new IOException("Сервер закрыл соединение.");
        }
        String[] parts = response.split("\\|", 2);
        boolean success = "OK".equalsIgnoreCase(parts[0]);
        return new ProtocolResponse(success, parts.length > 1 ? parts[1] : "");
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
