package com.example.filesystemanalyzer.model;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class FileNode {

    private final String name;
    private final long size;
    private final boolean directory;
    private final FileTime creationTime;
    private final String owner;
    private final Path path;

    public FileNode(String name, long size, boolean directory,
                    FileTime creationTime, String owner, Path path) {
        this.name = name;
        this.size = size;
        this.directory = directory;
        this.creationTime = creationTime;
        this.owner = owner;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public FileTime getCreationTime() {
        return creationTime;
    }

    public String getOwner() {
        return owner;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (directory) {
            sb.append(" [DIR]");
        } else {
            sb.append(" (").append(size).append(" bytes)");
        }

        String timeStr = (creationTime != null)
                ? creationTime.toString().replace('T', ' ').substring(0, 19)
                : "N/A";

        sb.append(" | Owner: ").append(owner != null ? owner : "???");
        sb.append(" | Created: ").append(timeStr);
        return sb.toString();
    }
}