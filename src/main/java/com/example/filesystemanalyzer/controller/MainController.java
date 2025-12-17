package com.example.filesystemanalyzer.controller;

import com.example.filesystemanalyzer.model.FileNode;
import com.example.filesystemanalyzer.service.FileAnalyzer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML
    private TextField pathField;

    @FXML
    private Button analyzeButton;

    @FXML
    private TreeTableView<FileNode> treeTable;

    @FXML
    private TreeTableColumn<FileNode, String> nameColumn;
    @FXML
    private TreeTableColumn<FileNode, String> ownerColumn;
    @FXML
    private TreeTableColumn<FileNode, String> typeColumn;
    @FXML
    private TreeTableColumn<FileNode, String> sizeColumn;
    @FXML
    private TreeTableColumn<FileNode, String> dateColumn;

    @FXML
    private BorderPane rootPane;

    private final FileAnalyzer analyzer = new FileAnalyzer();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TreeItem<FileNode> currentRoot;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupEventHandlers();
        initializeEmptyTable();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getValue().getName()));

        nameColumn.setCellFactory(new Callback<>() {
            @Override
            public TreeTableCell<FileNode, String> call(TreeTableColumn<FileNode, String> param) {
                return new TreeTableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            FileNode node = getTreeTableRow().getItem();
                            setText(node.isDirectory() ? "📁 " + item : "📄 " + item);
                        }
                    }
                };
            }
        });

        ownerColumn.setCellValueFactory(param ->
                new SimpleStringProperty(
                        param.getValue().getValue().getOwner() != null
                                ? param.getValue().getValue().getOwner()
                                : "N/A"
                ));

        typeColumn.setCellValueFactory(param ->
                new SimpleStringProperty(
                        param.getValue().getValue().isDirectory() ? "Папка" : "Файл"
                ));

        sizeColumn.setCellValueFactory(param ->
                new SimpleStringProperty(
                        formatSize(param.getValue().getValue().getSize())
                ));

        dateColumn.setCellValueFactory(param ->
                new SimpleStringProperty(
                        formatDate(param.getValue().getValue().getCreationTime())
                ));
    }

    private void setupEventHandlers() {
        analyzeButton.setOnAction(e -> analyzePath());
        pathField.setOnAction(e -> analyzePath());
    }

    private void initializeEmptyTable() {
        TreeItem<FileNode> placeholder = new TreeItem<>(
                new FileNode("Загрузка...", 0, false, null, "N/A", null)
        );
        treeTable.setRoot(placeholder);
        treeTable.setShowRoot(false);
    }

    private void analyzePath() {
        String inputPath = pathField.getText().trim();

        if (inputPath.isEmpty()) {
            showErrorAlert("Ошибка ввода", "Пожалуйста, укажите путь к каталогу");
            return;
        }

        Path path = Paths.get(inputPath).toAbsolutePath();

        if (!java.nio.file.Files.exists(path)) {
            showErrorAlert("Ошибка пути", "Указанный путь не существует: " + path);
            return;
        }

        Task<TreeItem<FileNode>> analysisTask = new Task<>() {
            @Override
            protected TreeItem<FileNode> call() throws Exception {
                updateMessage("Анализ файловой системы...");
                return analyzer.analyze(path);
            }
        };

        analysisTask.setOnSucceeded(e -> {
            currentRoot = analysisTask.getValue();
            if (currentRoot != null) {
                Platform.runLater(() -> {
                    treeTable.setRoot(currentRoot);
                    treeTable.setShowRoot(true);
                    expandSignificantBranches(currentRoot, 3);
                });
            } else {
                showErrorAlert("Ошибка анализа", "Не удалось проанализировать указанный путь");
            }
        });

        analysisTask.setOnFailed(e -> {
            Throwable throwable = analysisTask.getException();
            String errorMessage = "Ошибка при анализе: ";

            if (throwable instanceof SecurityException) {
                errorMessage += "Недостаточно прав доступа";
            } else if (throwable instanceof IOException) {
                errorMessage += "Проблема с чтением файлов: " + throwable.getMessage();
            } else {
                errorMessage += throwable.getMessage();
            }

            String finalErrorMessage = errorMessage;
            Platform.runLater(() -> showErrorAlert("Критическая ошибка", finalErrorMessage));
        });

        analysisTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> analyzeButton.setText(newVal));
        });

        executor.execute(analysisTask);
    }

    private void expandSignificantBranches(TreeItem<FileNode> root, int depth) {
        if (depth <= 0 || root == null || !root.getValue().isDirectory()) return;

        root.setExpanded(true);

        for (TreeItem<FileNode> child : root.getChildren()) {
            if (child.getValue().isDirectory()) {
                expandSignificantBranches(child, depth - 1);
            }
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "iB";

        return String.format("%.1f %s", bytes / Math.pow(1024, exp), unit);
    }

    private String formatDate(java.nio.file.attribute.FileTime time) {
        if (time == null) return "N/A";

        try {
            Instant instant = time.toInstant();
            if (instant.getEpochSecond() < 0) return "N/A";

            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } catch (Exception e) {
            return "Ошибка даты";
        }
    }

    private void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.initOwner(rootPane.getScene().getWindow());
            alert.showAndWait();
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}