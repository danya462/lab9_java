package ru.university.lab9.proxy;

public final class ProxyServerMain {
    private ProxyServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int proxyPort = args.length > 0 ? Integer.parseInt(args[0]) : 7001;
        String targetHost = args.length > 1 ? args[1] : "localhost";
        int targetPort = args.length > 2 ? Integer.parseInt(args[2]) : 7000;
        SimpleProxyServer proxyServer = new SimpleProxyServer(proxyPort, targetHost, targetPort);
        proxyServer.start();
    }
}
