package ru.university.lab9;

import org.junit.jupiter.api.Test;
import ru.university.lab9.client.VotingClientSession;
import ru.university.lab9.model.ProtectionLevel;
import ru.university.lab9.model.ProtocolResponse;
import ru.university.lab9.proxy.SimpleProxyServer;
import ru.university.lab9.server.AnonymousVotingServer;
import ru.university.lab9.server.AnonymousVotingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VotingIntegrationTest {
    @Test
    void shouldWorkOverDirectTextConnection() throws Exception {
        runScenario(ProtectionLevel.LEVEL_0);
    }

    @Test
    void shouldWorkOverDirectProtoConnection() throws Exception {
        runScenario(ProtectionLevel.LEVEL_2);
    }

    @Test
    void shouldWorkOverProxyTextConnection() throws Exception {
        runScenario(ProtectionLevel.LEVEL_1);
    }

    @Test
    void shouldWorkOverProxyProtoConnection() throws Exception {
        runScenario(ProtectionLevel.LEVEL_3);
    }

    private void runScenario(ProtectionLevel level) throws Exception {
        int serverPort = findFreePort();
        int proxyPort = findFreePort();

        AnonymousVotingServer server = new AnonymousVotingServer(new AnonymousVotingService(), serverPort);
        server.startAsync();

        SimpleProxyServer proxy = null;
        if (level.useProxy()) {
            proxy = new SimpleProxyServer(proxyPort, "127.0.0.1", serverPort);
            proxy.startAsync();
        }

        Thread.sleep(Duration.ofMillis(300).toMillis());

        try (VotingClientSession alice = new VotingClientSession();
             VotingClientSession bob = new VotingClientSession()) {
            ProtocolResponse registerAlice = alice.register("Alice", "127.0.0.1", serverPort, "127.0.0.1", proxyPort, level);
            ProtocolResponse registerBob = bob.register("Bob", "127.0.0.1", serverPort, "127.0.0.1", proxyPort, level);
            ProtocolResponse nominate = alice.nominate("Bob");
            ProtocolResponse vote = alice.vote("Bob");
            ProtocolResponse results = bob.results();

            assertTrue(registerAlice.success());
            assertTrue(registerBob.success());
            assertTrue(nominate.success());
            assertTrue(vote.success());
            assertTrue(results.message().contains("Bob = 1"));
        } finally {
            if (proxy != null) {
                proxy.close();
            }
            server.close();
        }
    }

    private int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
