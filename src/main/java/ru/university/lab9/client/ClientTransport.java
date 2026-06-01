package ru.university.lab9.client;

import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;

import java.io.Closeable;
import java.io.IOException;

public interface ClientTransport extends Closeable {
    void connect(String host, int port) throws IOException;

    ProtocolResponse send(ProtocolRequest request) throws IOException;
}
