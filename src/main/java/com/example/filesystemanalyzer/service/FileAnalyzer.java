package com.example.filesystemanalyzer.service;

import com.example.filesystemanalyzer.model.FileNode;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileAnalyzer {

    private static final Logger logger = LogManager.getLogger(FileAnalyzer.class);

    public TreeItem<FileNode> analyze(Path path) {
        logger.info("Начат анализ пути: {}", path);

        try {
            FileNode rootNode = createNode(path);
            TreeItem<FileNode> rootItem = new TreeItem<>(rootNode);
            rootItem.setExpanded(true);

            if (Files.isDirectory(path)) {
                buildTree(path, rootItem);
            }

            return rootItem;
        } catch (IOException e) {
            logger.error("Ошибка анализа пути", e);
            return null;
        }
    }

    private void buildTree(Path directory, TreeItem<FileNode> parent) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                FileNode node = createNode(entry);
                TreeItem<FileNode> item = new TreeItem<>(node);
                parent.getChildren().add(item);

                if (Files.isDirectory(entry)) {
                    buildTree(entry, item);
                }
            }
        }
    }

    private FileNode createNode(Path path) throws IOException {
        BasicFileAttributes attrs =
                Files.readAttributes(path, BasicFileAttributes.class);

        String owner = Files.getOwner(path).getName();

        return new FileNode(
                path.getFileName().toString(),
                attrs.size(),
                attrs.isDirectory(),
                attrs.creationTime(),
                owner,
                path
        );
    }
}