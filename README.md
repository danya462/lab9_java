# Лаба 9 - анонимное голосование

Вариант 15: анонимное голосование за лучшего сотрудника.

## Возможности

- команды `REGISTER`, `NOMINATE`, `VOTE`, `RESULTS`;
- уровни защиты 0-3;
- прямое TCP-соединение или прокси;
- текстовый протокол или ProtoBuf-туннель;
- JavaFX-клиент.

## Запуск

Сервер:

```powershell
mvn exec:java -Dexec.mainClass=ru.university.lab9.server.VotingServerMain
```

Прокси:

```powershell
mvn exec:java -Dexec.mainClass=ru.university.lab9.proxy.ProxyServerMain
```

GUI-клиент:

```powershell
mvn javafx:run
```

Тесты:

```powershell
mvn test
```
