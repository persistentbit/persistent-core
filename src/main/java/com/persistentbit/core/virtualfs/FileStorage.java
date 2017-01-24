package com.persistentbit.core.virtualfs;

import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/01/2017
 */
public interface FileStorage {

    class NewFileToken{
        private final RemotePath.File   path;
        private final LocalDateTime created;
        private final LocalDateTime lastWrite;
        private final long lengthWritten;

        public NewFileToken(RemotePath.File path, LocalDateTime created, LocalDateTime lastWrite, long lengthWritten) {
            this.path = Objects.requireNonNull(path);
            this.created = Objects.requireNonNull(created);
            this.lastWrite = Objects.requireNonNull(lastWrite);
            this.lengthWritten = Objects.requireNonNull(lengthWritten);
        }
        public NewFileToken(RemotePath.File path){
            this(path,LocalDateTime.now(),LocalDateTime.now(),0L);
        }
        public NewFileToken addWrittenLength(long addedLength){
            return new NewFileToken(
                path,created,LocalDateTime.now(),lengthWritten+addedLength
            );
        }

        public RemotePath.File getPath() {
            return path;
        }

        public LocalDateTime getCreated() {
            return created;
        }

        public LocalDateTime getLastWrite() {
            return lastWrite;
        }

        public long getLengthWritten() {
            return lengthWritten;
        }
    }
    class ReadFileToken{

    }

    Result<PList<RemotePathInfo>>    dir(RemotePath.Dir path);
    Result<RemotePathInfo>  getInfo(RemotePath dirOrFile);

    Result<RemotePath>  mkdirs(RemotePath.Dir path);

    Result<NewFileToken>    createNewFile(RemotePath.File newFilePath);
    Result<NewFileToken>    append(NewFileToken newFileToken, PByteList data);
    Result<RemotePath.File>   closeNewFile(NewFileToken newFileToken);

    Result<RemotePathInfo> append(RemotePath.File existingFile, PByteList data);

    Result<OK>  deleteFile(RemotePath.File existingFile);
    Result<ReadFileToken> openForReading(RemotePath.File existingFile);
    Result<ReadFileToken> read(ReadFileToken fileToken, int length);


}
