# Lab 9 - Anonymous Voting System

Variant 15:
Anonymous voting for the best employee.

Implemented parts:

- `VotingServerMain` - main server with anonymous vote storage
- `ProxyServerMain` - TCP proxy that hides the client IP from the server
- `VotingClientApp` - JavaFX client with protection levels 0-3
- plain text protocol for levels 0 and 1
- ProtoBuf binary framing for levels 2 and 3

## Protection levels

- `0` - direct plain TCP
- `1` - plain TCP through proxy
- `2` - ProtoBuf tunnel directly to server
- `3` - ProtoBuf tunnel through proxy

## Traffic visibility / Wireshark notes

- Level `0`: the server sees the real client IP and the traffic is readable as plain UTF-8 commands.
- Level `1`: the server sees the proxy IP instead of the client IP, but commands are still readable as plain text between client-proxy and proxy-server.
- Level `2`: the server sees the real client IP, but payloads are sent as length-prefixed ProtoBuf frames, so Wireshark does not show readable command lines.
- Level `3`: the server sees the proxy IP and the payload is transferred as ProtoBuf frames, combining IP hiding from the server with non-text payloads on the wire.

ProtoBuf here is a binary tunnel format, not cryptographic encryption. It hides plain command text from casual packet inspection, but it does not provide confidentiality against a determined analyst who knows the protocol.

## Commands in the project

- `REGISTER`
- `NOMINATE`
- `VOTE`
- `RESULTS`

## Run server

```powershell
mvn compile
mvn exec:java -Dexec.mainClass=ru.university.lab9.server.VotingServerMain
```

## Run proxy

```powershell
mvn exec:java -Dexec.mainClass=ru.university.lab9.proxy.ProxyServerMain -Dexec.args="7001 localhost 7000"
```

## Run GUI client

```powershell
mvn javafx:run
```
