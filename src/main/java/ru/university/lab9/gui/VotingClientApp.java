package ru.university.lab9.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.university.lab9.client.VotingClientSession;
import ru.university.lab9.model.ProtectionLevel;
import ru.university.lab9.model.ProtocolResponse;

import java.io.IOException;

public class VotingClientApp extends Application {
    private final VotingClientSession session = new VotingClientSession();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        TextField nicknameField = new TextField("Алиса");
        TextField serverHostField = new TextField("127.0.0.1");
        TextField serverPortField = new TextField("7000");
        TextField proxyHostField = new TextField("127.0.0.1");
        TextField proxyPortField = new TextField("7001");
        TextField candidateField = new TextField("Боб");

        ComboBox<ProtectionLevel> levelBox = new ComboBox<>(FXCollections.observableArrayList(ProtectionLevel.values()));
        levelBox.getSelectionModel().select(ProtectionLevel.LEVEL_0);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);

        Button registerButton = new Button("Зарегистрироваться");
        Button nominateButton = new Button("Выдвинуть");
        Button voteButton = new Button("Голосовать");
        Button resultsButton = new Button("Результаты");

        registerButton.setOnAction(event -> {
            try {
                ProtocolResponse response = session.register(
                        nicknameField.getText().trim(),
                        serverHostField.getText().trim(),
                        Integer.parseInt(serverPortField.getText().trim()),
                        proxyHostField.getText().trim(),
                        Integer.parseInt(proxyPortField.getText().trim()),
                        levelBox.getValue()
                );
                append(logArea, "Регистрация -> " + response.message());
            } catch (IOException | NumberFormatException exception) {
                append(logArea, "Регистрация не удалась: " + exception.getMessage());
            }
        });

        nominateButton.setOnAction(event -> execute(logArea, "Выдвижение", () -> session.nominate(candidateField.getText().trim())));
        voteButton.setOnAction(event -> execute(logArea, "Голосование", () -> session.vote(candidateField.getText().trim())));
        resultsButton.setOnAction(event -> execute(logArea, "Результаты", session::results));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Имя участника"), nicknameField);
        form.addRow(1, new Label("Хост сервера"), serverHostField);
        form.addRow(2, new Label("Порт сервера"), serverPortField);
        form.addRow(3, new Label("Хост прокси"), proxyHostField);
        form.addRow(4, new Label("Порт прокси"), proxyPortField);
        form.addRow(5, new Label("Уровень защиты"), levelBox);
        form.addRow(6, new Label("Кандидат"), candidateField);

        VBox root = new VBox(10,
                form,
                registerButton,
                nominateButton,
                voteButton,
                resultsButton,
                new Label("Журнал клиента"),
                logArea
        );
        root.setPadding(new Insets(16));
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Scene scene = new Scene(root, 760, 600);
        stage.setTitle("Лаба 9 - анонимное голосование");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        session.close();
        super.stop();
    }

    private void execute(TextArea logArea, String title, ThrowingResponseSupplier supplier) {
        try {
            ProtocolResponse response = supplier.get();
            append(logArea, title + " -> " + response.message());
        } catch (IOException exception) {
            append(logArea, title + " не удалось выполнить: " + exception.getMessage());
        }
    }

    private void append(TextArea logArea, String message) {
        logArea.appendText(message + System.lineSeparator());
    }

    @FunctionalInterface
    private interface ThrowingResponseSupplier {
        ProtocolResponse get() throws IOException;
    }
}
