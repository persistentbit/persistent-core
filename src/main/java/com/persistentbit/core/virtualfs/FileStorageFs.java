package com.persistentbit.core.virtualfs;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.OK;
import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.utils.IO;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/01/2017
 */
public class FileStorageFs implements FileStorage{
    private final File    root;
    static private final String suffixNotFinished = "._not_finished";

    public FileStorageFs(File root) {
        this.root = root;
        if(root.exists() == false){
            if(root.mkdirs() == false){
                throw new RuntimeException("Can't create FileStorageFs root: "+ root.getAbsolutePath());
            }
        }
    }

    private File toFs(RemotePath path){
        File res = root;
        RemotePath.Dir pathDir = path.match(f -> f.getParent().get(),d -> d);
        for(RemotePath.Dir part : pathDir.pathParts().tail()){
            res = new File(res,part.getName());
        }
        File finalRes = res;
        return path.match(f -> new File(finalRes,f.getName()),d -> finalRes);
    }


    private RemotePathInfo createInfo(RemotePath.Dir parent, File file){
        return Log.function(parent,file).code(l -> {
            Objects.requireNonNull(parent,"parent");
            Objects.requireNonNull(file,"file");

            if(file.isDirectory()){
                return RemotePathInfo.forFile(parent.dir(file.getName()),file);
            } else {
                return RemotePathInfo.forFile(parent.file(file.getName()),file);
            }
        });

    }

    @Override
    public Result<PList<RemotePathInfo>> dir(RemotePath.Dir path) {
        return Result.function(path).code(l -> {
            if(path == null){
                return Result.failure("path is null");
            }
            File fDir = toFs(path);
            if(fDir.exists() == false){
                return Result.empty("Path doesn't exist: " + path);
            }
            if(fDir.isDirectory() == false){
                return Result.failure("Not a directory:" + path);
            }
            PList<RemotePathInfo> res = PList.empty();
            File[] files = fDir.listFiles();
            if(files == null){
                return Result.failure("Can't get list of files");
            }
            for(File f : files){
                if(f.getName().endsWith(suffixNotFinished)){
                    continue;
                }
                res = res.plus(createInfo(path,f));
            }
            return Result.success(res);
        });

    }

    @Override
    public Result<RemotePathInfo> getInfo(RemotePath dirOrFile) {
        return Result.function(dirOrFile).code(l->
            Result.success(RemotePathInfo.forFile(dirOrFile,toFs(dirOrFile)))
        );

    }

    @Override
    public Result<RemotePath> mkdirs(RemotePath.Dir path) {
        return Result.function(path).code(l -> {
            toFs(path).mkdirs();
            return Result.success(path);
        });
    }

    @Override
    public Result<NewFileToken> createNewFile(RemotePath.File newFilePath) {
        return Result.function(newFilePath).code(l -> {
            if(toFs(newFilePath).createNewFile() == false){
                return Result.failure("File already exists:" + newFilePath);
            }
            toFs(newFilePath.mapName(n -> n +suffixNotFinished));

            return Result.success(new NewFileToken(newFilePath));
        });
    }

    @Override
    public Result<NewFileToken> append(NewFileToken newFileToken, PByteList data) {
        return Result.function(newFileToken,"<data>").code(l -> {
            if(newFileToken == null){
                return Result.failure("newFileToken is null");
            }
            if(data == null){
                return Result.failure("data is null");
            }
            File f = toFs(newFileToken.getPath());
            if(toFs(newFileToken.getPath().mapName(n -> n +suffixNotFinished)).exists() == false){
                return Result.failure("File is not marked as new");
            }
            if(f.length() != newFileToken.getLengthWritten()){
                return Result.failure("File size " + f.length() + " is different from " + newFileToken.getLengthWritten());
            }
            return IO.fileToOutputStream(f)
                    .flatMap(out -> {
                        try {
                            return IO.copy(data.getInputStream(), out);
                        } finally {
                            try {
                                out.close();
                            } catch (IOException e) {
                                return Result.failure(e);
                            }
                        }
                    })
                    .map(out -> newFileToken.addWrittenLength(data.size()));
        });
    }

    @Override
    public Result<RemotePath.File> closeNewFile(NewFileToken newFileToken) {
        return Result.function(newFileToken).code(l-> {
            if(newFileToken == null){
                return Result.failure("newFileToken is null");
            }
            File fe = toFs(newFileToken.getPath().mapName(n -> n +suffixNotFinished));
            if(fe.exists() == false){
                return Result.failure("File is not marked as new");
            }
            if(fe.delete() == false){
                return Result.failure("Could not delete " + fe);
            }
            return Result.success(newFileToken.getPath());
        });
    }

    @Override
    public Result<RemotePathInfo> append(RemotePath.File existingFile, PByteList data) {
        throw new RuntimeException("FileStorageFs.append TODO: Not yet implemented");
    }

    @Override
    public Result<OK> deleteFile(RemotePath.File existingFile) {
        throw new RuntimeException("FileStorageFs.deleteFile TODO: Not yet implemented");
    }

    @Override
    public Result<ReadFileToken> openForReading(RemotePath.File existingFile) {
        throw new RuntimeException("FileStorageFs.openForReading TODO: Not yet implemented");
    }

    @Override
    public Result<ReadFileToken> read(ReadFileToken fileToken, int length) {
        throw new RuntimeException("FileStorageFs.read TODO: Not yet implemented");
    }

    static final TestCase test = TestCase.name("FileStorageFs").code(tr -> {
        FileStorage fs = new FileStorageFs(new File("d:\\fstest"));
        fs.dir(RemotePath.Dir.root()).orElseThrow().forEach(rpi -> System.out.println(rpi));
        tr.isEmpty(fs.dir(RemotePath.root().dir("nonExisting")));
        RemotePath.Dir sub = RemotePath.Dir.root().dir("sub1");
        tr.isSuccess(fs.mkdirs(sub));
        tr.isSuccess(fs.dir(RemotePath.root()));
    });

    public static void main(String... args) throws Exception {
        LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true));
        lp.registerAsGlobalHandler();
        TestRunner.runAndPrint(lp,FileStorageFs.class);
    }
}
