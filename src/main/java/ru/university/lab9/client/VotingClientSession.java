package ru.university.lab9.client;

import ru.university.lab9.model.ProtectionLevel;
import ru.university.lab9.model.ProtocolRequest;
import ru.university.lab9.model.ProtocolResponse;

import java.io.IOException;

public class VotingClientSession implements AutoCloseable {
    private ClientTransport transport;
    private String nickname;

    public ProtocolResponse register(String nickname,
                                     String serverHost,
                                     int serverPort,
                                     String proxyHost,
                                     int proxyPort,
                                     ProtectionLevel level) throws IOException {
        close();
        this.transport = level.useProto() ? new ProtoClientTransport() : new TextClientTransport();
        this.nickname = nickname;

        String host = level.useProxy() ? proxyHost : serverHost;
        int port = level.useProxy() ? proxyPort : serverPort;

        transport.connect(host, port);
        return transport.send(new ProtocolRequest("REGISTER", nickname));
    }

    public ProtocolResponse nominate(String candidate) throws IOException {
        ensureConnected();
        return transport.send(new ProtocolRequest("NOMINATE", candidate));
    }

    public ProtocolResponse vote(String candidate) throws IOException {
        ensureConnected();
        return transport.send(new ProtocolRequest("VOTE", candidate));
    }

    public ProtocolResponse results() throws IOException {
        ensureConnected();
        return transport.send(new ProtocolRequest("RESULTS", ""));
    }

    public String nickname() {
        return nickname;
    }

    public void close() throws IOException {
        if (transport != null) {
            transport.close();
            transport = null;
        }
    }

    private void ensureConnected() throws IOException {
        if (transport == null) {
            throw new IOException("Сначала зарегистрируйтесь.");
        }
    }
}
