package com.persistentbit.core.experiments.shell;

import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.exceptions.ToDo;

import java.util.function.Predicate;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 17/03/2017
 */
@FunctionalInterface
public interface VFileSet {

    PStream<VFile>  children();

    default VFileSet recursive(){
        throw new ToDo();
    }

    default VFileSet filter(Predicate<VFile>file){
        throw new ToDo();
    }

    default VFileSet filterName(String nameRegEx){
        return filter(vf -> vf.getName().matches(nameRegEx));
    }
    default VFileSet onlyFiles() {
        return filter(vf -> vf.isFolder() == false);
    }
    default VFileSet onlyFolders(){
        return filter(vf -> vf.isFolder());
    }
    default VFileSet filterExtension(Predicate<String> extension){
        throw new ToDo();
    }

}
