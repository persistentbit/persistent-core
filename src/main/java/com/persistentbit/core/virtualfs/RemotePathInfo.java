package com.persistentbit.core.virtualfs;

import com.persistentbit.core.logging.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/01/2017
 */
public final class RemotePathInfo {
    private final RemotePath path;
    private final LocalDateTime   created;
    private final LocalDateTime   lastUpdated;
    private final long length;

    public RemotePathInfo(RemotePath path, LocalDateTime created, LocalDateTime lastUpdated, long length) {
        this.path = path;
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.length = length;
        if(path.isDir() && length !=0){
            throw new IllegalArgumentException("If path is a directory, then the length should be 0");
        }
    }

    public static RemotePathInfo forFile(RemotePath path, File file){
        return Log.function(path,file).code(l -> {
            BasicFileAttributes fa =  getBasicAttributes(file);
            if(file.isDirectory()){
                if(path.isDir() == false){
                    throw new RuntimeException("Expected a dir for " + file);
                }
                return new RemotePathInfo(
                        path,
                        LocalDateTime.ofInstant(fa.creationTime().toInstant(), ZoneId.systemDefault()),
                        LocalDateTime.ofInstant(fa.lastModifiedTime().toInstant(),ZoneId.systemDefault()),
                        0
                );
            } else {
                if(path.isFile() == false){
                    throw new RuntimeException("Expected a dir for " + file);
                }
                return new RemotePathInfo(
                        path,
                        LocalDateTime.ofInstant(fa.creationTime().toInstant(), ZoneId.systemDefault()),
                        LocalDateTime.ofInstant(fa.lastModifiedTime().toInstant(),ZoneId.systemDefault()),
                        file.length()
                );
            }
        });
    }
    private static BasicFileAttributes getBasicAttributes(File file){
        return Log.function(file).code(l ->
                Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes()
        );
    }

    public RemotePath getPath() {
        return path;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemotePathInfo that = (RemotePathInfo) o;

        if (length != that.length) return false;
        if (!path.equals(that.path)) return false;
        if (!created.equals(that.created)) return false;
        return lastUpdated.equals(that.lastUpdated);
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + created.hashCode();
        result = 31 * result + lastUpdated.hashCode();
        result = 31 * result + (int) (length ^ (length >>> 32));
        return result;
    }
}
