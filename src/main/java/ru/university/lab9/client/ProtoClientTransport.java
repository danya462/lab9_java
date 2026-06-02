package ru.university.lab9.client;

import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;
import ru.university.lab9.protocol.ProtoStructCodec;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ProtoClientTransport implements ClientTransport {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    @Override
    public void connect(String host, int port) throws IOException {
        close();
        socket = new Socket(host, port);
        socket.getOutputStream().write('B');
        socket.getOutputStream().flush();
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public ProtocolResponse send(ProtocolRequest request) throws IOException {
        ProtoStructCodec.writeRequest(outputStream, request);
        ProtocolResponse response = ProtoStructCodec.readResponse(inputStream);
        if (response == null) {
            throw new IOException("Сервер закрыл соединение.");
        }
        return response;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
