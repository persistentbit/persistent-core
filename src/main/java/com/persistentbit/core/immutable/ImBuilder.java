package com.persistentbit.core.immutable;

import com.persistentbit.core.Pair;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.ImTools;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;

/**
 * @author Peter Muys
 * @since 11/07/2016
 */
public class ImBuilder {
    private final Class<?>    cls;
    private final ImTools<?> im;
    private final File        source;

    public ImBuilder(Class<?> cls, File source) {
        this.cls = cls;
        this.source = source;
        this.im = ImTools.get(cls);
    }

    public void generate(){
        System.out.println("Generate for " + cls);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(PrintStream out = new PrintStream(bout,true)){
            for(Field f : cls.getDeclaredFields()){
                if(Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())==false && Modifier.isTransient(f.getModifiers())==false){
                    genField(System.out,f);
                }
            }

        }

    }
    public void genField(PrintStream out,Field f){
        if(im.getWithMethod(f).isPresent() == false){
            out.println("\t" + cls.getSimpleName() + "\t with" + firstCharUppercase(f.getName())+ "(" + f.getType().getSimpleName() + " value){");
            out.println("\t\treturn new " + cls.getSimpleName() + "(" +
                    im.getConstructorProperties().map(g -> g.propertyName).map(n -> n.equals(f.getName())? "value" : "this." + n).join((a,b)->a+", " + b).get()
                    + ");");
            out.println("\t} ");
            out.println();
        }
        ImTools.Getter getter = im.getFieldGetters().find(fg-> fg.propertyName.equals(f.getName())).get();
        if(im.getGetterMethod(f).isPresent() == false){

            String rt = getter.isNullable ? "Optional<" + f.getType().getSimpleName() + ">" : f.getType().getSimpleName();
            String of = getter.isNullable ? "Optional.ofNullable(" +  getter.propertyName + ")" : getter.propertyName;
            out.println("\tpublic " + rt + " get" + firstCharUppercase(f.getName()) + "(){ return "+ of + "; }");
            out.println();
        }
        try {
            cls.getDeclaredField("_" + f.getName());
            //new LensImpl<>((obj) -> obj.get...,(obj,value)->with(value))
        } catch (NoSuchFieldException e) {

            String g = "obj-> obj.get" + firstCharUppercase(f.getName()) + "()";
            if(getter.isNullable){
                g += ".orElse(null)";
            }
            String s = "(obj,value)-> obj.with" + firstCharUppercase(f.getName()) +"(value)";
            out.println("\tstatic public Lens<" + cls.getSimpleName() + "," + f.getType().getSimpleName() + "> _" + f.getName() + " = new LensImpl<>("+ g + "," + s + ");");
        }
    }

    static public File findSourcePath(Class<?>cls, String resourceName) {
        URL url = cls.getClassLoader().getResource(resourceName);
        if(url == null){
            throw new IllegalArgumentException("Can't find resouce '" + resourceName + "' using classloader for "+ cls.getName());
        }
        File f = new File(url.getFile());
        while(f.getName().equals("target")== false){
            f = f.getParentFile();
        }
        f = new File(f.getParentFile(),"src/main/java");

        return f;
    }
    static private String firstCharUppercase(String str){
        return "" + Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    static private PStream<File> findSources(File root) {
        if(root.isDirectory()){
            return PStream.from(root.listFiles())
                    .map(f-> findSources(f))
                    .flatten();
        }
        if(root.getName().toLowerCase().endsWith(".java")){
            return PStream.val(root);
        }
        return PStream.val();
    }
    static public void build(File sourceRoot){
         build(sourceRoot,ImBuilder.class.getClassLoader());
    }

    static public void build(File sourceRoot,ClassLoader classLoader){

        System.out.println(sourceRoot);
        int subLength = sourceRoot.getAbsolutePath().length()+1;
        PStream<Pair<File,Class>> cf = findSources(sourceRoot).map(f -> {
            String clsName = f.getAbsolutePath().substring(subLength);
            clsName = clsName.substring(0,clsName.length()-".java".length());
            clsName = clsName.replace('/','.').replace('\\','.');
            try {
                return new Pair<File,Class>(f,classLoader.loadClass(clsName));
            } catch (NoClassDefFoundError|ClassNotFoundException e) {
                System.err.println(e.getMessage());
                return null;
            }
        }).filter(p -> {
            if(p == null){
                return false;
            }
            Class<?> cls = p._2;
            return cls.getAnnotation(Immutable.class) != null;
        });
        cf.forEach(p -> {
            ImBuilder b = new ImBuilder(p._2,p._1);
            b.generate();
        });
    }
    static public void main(String...args){
        ImBuilder.build(findSourcePath(ImBuilder.class,"resource-marker.txt"));
    }
}
