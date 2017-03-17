package com.persistentbit.core.experiments.shell;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 17/03/2017
 */
public interface VFile extends VFileSet{
    String getName();
    long getLength();
    VFile getPath();
    boolean isFolder();
    void copy(VFile dest);
    void move(VFile dest);


}
