package ru.university.lab9.server;

public final class VotingServerMain {
    private VotingServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 7000;
        AnonymousVotingServer server = new AnonymousVotingServer(new AnonymousVotingService(), port);
        server.start();
    }
}
