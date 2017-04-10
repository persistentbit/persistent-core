package com.persistentbit.core.virtualfs;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.utils.UOS;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 24/01/2017
 */
public abstract class RemotePath {


    private RemotePath() {
    }

    public abstract <T> T match(
            Function<File,T> aFile,
            Function<Dir, T> aDir
    );

    public boolean isDir() {
        return match(
                aFile -> false,
                aDir -> true
        );
    }
    public boolean isFile() {
        return match(
                aFile -> true,
                aDir -> false
        );
    }

    static public Dir root() {
        return new Dir();
    }
    public abstract Optional<Dir> getParent();

    static final class File extends RemotePath {
        private final Dir parent;
        private final String name;

        public File(Dir parent, String name) {
            this.parent = Objects.requireNonNull(parent);
            this.name = Objects.requireNonNull(name);
        }

        public static File from(String path){
            Dir full = Dir.from(path);
            return full.parent.file(full.name);
        }

        @Override
        public Optional<Dir> getParent() {
            return Optional.of(parent);
        }

        public String getName() {
            return name;
        }

        public File rename(String newFileName){
            return new File(parent,newFileName);
        }
        public File mapName(Function<String,String> nameMapper){
            return rename(nameMapper.apply(name));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            File file = (File) o;

            if (!parent.equals(file.parent)) return false;
            return name.equals(file.name);
        }



        @Override
        public int hashCode() {
            int result = parent.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return parent.toString() + "/" + name;
        }

        @Override
        public <T> T match(Function<File, T> aFile, Function<Dir, T> aDir) {
            return aFile.apply(this);
        }
    }
    static final class Dir extends RemotePath {
        private final Dir parent;
        private final String name;

        Dir(Dir parent, String name) {
            this.parent = parent;
            this.name = Objects.requireNonNull(name);
            if(name.isEmpty() && parent != null){
                throw new IllegalArgumentException("Sub directory name can't be empty for " + parent);
            }
        }
        public Dir(){
            this(null,"");
        }


        public static Dir from(String path){
            path = path.startsWith("/") ? path.substring(1) : path;
            if(path.isEmpty()){
                return root();
            }
            return root().dir(path.split("/"));
        }


        public PList<Dir> pathParts(){
            PList<Dir> parentParts = getParent().map(p -> p.pathParts()).orElse(PList.empty());
            return parentParts.plus(this);
        }

        @Override
        public String toString() {
            String ps = getParent().map(p -> p.toString()).orElse("") + "/";
            if(ps.equals("//")){
                ps = "/";
            }
            return ps + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dir dir = (Dir) o;

            if (parent != null ? !parent.equals(dir.parent) : dir.parent != null) return false;
            return name.equals(dir.name);
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public <T> T match(Function<File, T> aFile, Function<Dir, T> aDir) {
            return aDir.apply(this);
        }
        public Dir dir(String...dirNames){
            Dir res = this;
            for(String sub : dirNames){
                res = new Dir(res,sub);
            }
            return res;
        }

        public Dir dir(Dir subDir){
            return subDir.pathParts().tail().fold(this,(a,s)-> new Dir(a,s.getName()));
        }
        public File file(String fileName){
            return new File(this,fileName);
        }
        @Override
        public Optional<Dir> getParent() {
            return Optional.ofNullable(parent);
        }

        public String getName() {
            return name;
        }
    }

    static final TestCase testPaths = TestCase.name("Test Remote Paths").code(tr -> {
        Dir p = RemotePath.root();
        tr.isEquals(p.toString(),"/");

        tr.isEquals(p.dir("sub1","sub2","sub3","sub4").getParent().get().toString(),"/sub1/sub2/sub3");
        tr.isEquals(p,Dir.from("/"));
        tr.isEquals(p.dir("sub1"),Dir.from("/sub1"));
        tr.isEquals(p.dir("sub1","sub2"),Dir.from("/sub1/sub2"));
        tr.isEquals(p.dir("sub1","sub2"),Dir.root().dir(Dir.root().dir("sub1","sub2")));
        tr.isEquals(p.dir("sub1","sub2","sub3"),Dir.root().dir("sub1").dir(Dir.root().dir("sub2","sub3")));

        File f = Dir.from("/sub1/sub2/sub3").file("fileName");
        tr.isEquals(Dir.root().dir("sub1","sub2","sub3").file("fileName"),f);
        tr.isEquals(f.toString(),"/sub1/sub2/sub3/fileName");
    });

    public static void main(String... args) throws Exception {
        LogPrint lp = LogPrintStream.sysOut(ModuleCore.createLogFormatter(UOS.hasAnsiColor()));
        lp.registerAsGlobalHandler();
        TestRunner.runAndPrint(lp,RemotePath.class);
    }
}
