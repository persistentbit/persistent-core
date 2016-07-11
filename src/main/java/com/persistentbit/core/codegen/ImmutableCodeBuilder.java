package com.persistentbit.core.codegen;

import com.persistentbit.core.Immutable;
import com.persistentbit.core.Pair;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.ImTools;


import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Optional;

/**
 * Class to auto generate code for Immutable classes that are marked with {@link Immutable}. <br>
 * Start The codebuilder with <code>ImmutableCodeBuilder.build(sourcePathFile);</code>
 * This will iterate over all the *.java files in the filesystem and for each found file,<br>
 * Load the class and add the generated code to the .java file if the method is not found in the class.<br>
 * Code that can be generated: per property: Lens, with, get functions.<br>
 * and equals/hashcode for the class<br>
 * Typical use:<br>
 * <code>static public void main(String...args){<br>
 * ImmutableCodeBuilder.build(findSourcePath(ImmutableCodeBuilder.class,"resource-marker.txt"));<br>
 * }<br></code>
 *
 * @see GenNoEquals
 * @see GenNoGetter
 * @see GenNoLens
 * @see GenNoWith
 * @see com.persistentbit.core.Nullable
 * @see com.persistentbit.core.NotNullable
 * @author Peter Muys
 * @since 11/07/2016
 */
public class ImmutableCodeBuilder {
    private final Class<?>    cls;
    private final ImTools<?> im;
    private final File        source;

    public ImmutableCodeBuilder(Class<?> cls, File source) {
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
                    genField(out,f);
                }
            }
            String gen = bout.toString();
            if(gen.trim().isEmpty() == false){
                gen = "\t//Generated by " + getClass().getName() + "\r\n\r\n" + gen;
                String org =readFile(source).get();
                int i = org.lastIndexOf('}');
                org = org.substring(0,i) + gen + org.substring(i);
                System.out.println(org);
                writeFile(org,source);
            }
        }

    }


    static public Optional<String> readFile(File f){
        if(f.exists() == false || f.isFile() == false || f.canRead() == false){
            return Optional.empty();
        }
        try(Reader r = new FileReader(f)){
            return readStream(r);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private static Optional<String> readStream(Reader fin) throws IOException {

        char[]  buffer  =   new char[1024];
        StringBuffer stringBuffer=   new StringBuffer();
        int c = 0;
        do {
            c = fin.read(buffer);
            if(c != -1){
                stringBuffer.append(buffer,0,c);
            }
        }while(c != -1);
        return Optional.of(stringBuffer.toString());
    }


    static public void writeFile(String text, File f){
        try{
            FileWriter fout = new FileWriter(f);
            try{
                fout.write(text);
            }finally {
                fout.close();
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    public void genField(PrintStream out,Field f){
        // Generate With methods
        if(im.getWithMethod(f).isPresent() == false && f.getAnnotation(GenNoWith.class) == null){
            out.println("\tpublic " + name(cls) + "\t with" + firstCharUppercase(f.getName())+ "(" + name(f.getGenericType()) + " value){");
            out.println("\t\treturn new " + cls.getSimpleName() + (hasParams(cls)? "<>" : "")  + "(" +
                    im.getConstructorProperties().map(g -> g.propertyName).map(n -> n.equals(f.getName())? "value" : "this." + n).join((a,b)->a+", " + b).get()
                    + ");");
            out.println("\t} ");
            out.println();
        }

        //Generate getters

        ImTools.Getter getter = im.getFieldGetters().find(fg-> fg.propertyName.equals(f.getName())).get();
        if(im.getGetterMethod(f).isPresent() == false && f.getAnnotation(GenNoGetter.class) == null){

            String rt = getter.isNullable ? "Optional<" + name(f.getGenericType()) + ">" : name(f.getGenericType());
            String of = getter.isNullable ? "Optional.ofNullable(" +  getter.propertyName + ")" : getter.propertyName;
            out.println("\tpublic " + rt + " get" + firstCharUppercase(f.getName()) + "(){ return "+ of + "; }");
            out.println();
        }




        //Generate Lens code


        try {
            if(f.getAnnotation(GenNoLens.class) == null && f.getAnnotation(GenNoWith.class) == null && hasParams(cls) == false) {
                cls.getDeclaredField("_" + f.getName());
            }
            //new LensImpl<>((obj) -> obj.get...,(obj,value)->with(value))
        } catch (NoSuchFieldException e) {

            String g = "obj-> obj.get" + firstCharUppercase(f.getName()) + "()";
            if(Modifier.isPublic(f.getModifiers())){
                g = "obj-> obj." + f.getName();
            } else {
                if (getter.isNullable) {
                    g += ".orElse(null)";
                }
            }
            String s = "(obj,value)-> obj.with" + firstCharUppercase(f.getName()) +"(value)";
            String gen = "<" + cls.getSimpleName()+ "," + f.getType().getSimpleName() + ">";
            out.println("\tstatic public final Lens" + gen + " _" + f.getName() + " = new LensImpl" + gen + "("+ g + "," + s + ");");
            out.println("");
        }
    }

    private String simpleName(Type t){
        Class<?> cls = classFromType(t);;
        String name = cls.getSimpleName();
        int i =name.lastIndexOf(".");
        name = name.substring(i+1);
        return name;
    }


    private String params(Type t){
        Class  cls = classFromType(t);
        if(cls.getTypeParameters().length == 0){
            return "";
        }
        return "<" + PStream.from(cls.getTypeParameters()).map(m -> m.toString()).join((a,b)-> a + "," + b).orElse("") + ">";

    }

    boolean hasParams(Type t){
        return params(t).isEmpty() == false;
    }

    String name(Type t){
        if(t instanceof TypeVariable){
            TypeVariable tv = (TypeVariable)t;
            return tv.getName();
        }
        Class<?> cls = classFromType(t);
        String p = params(t);

        return classFromType(t).getSimpleName() + params(t);
    }
    public static  Class<?> classFromType(Type t){
        if(t instanceof Class){
            return (Class<?>)t;
        }
        if(t instanceof ParameterizedType){
            return classFromType(((ParameterizedType)t).getRawType());
        }
        if(t instanceof GenericArrayType){
            GenericArrayType gat = (GenericArrayType)t;
            throw new RuntimeException(gat.getTypeName());
        }
        if(t instanceof WildcardType){
            WildcardType wct = (WildcardType)t;
            return classFromType(wct.getUpperBounds()[0]);
        }
        if(t instanceof TypeVariable){
            return Object.class;
        }
        throw new RuntimeException("Don't know how to handle " + t);
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
         build(sourceRoot,ImmutableCodeBuilder.class.getClassLoader());
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
            ImmutableCodeBuilder b = new ImmutableCodeBuilder(p._2,p._1);
            b.generate();
        });
    }
    static public void main(String...args){
        ImmutableCodeBuilder.build(findSourcePath(ImmutableCodeBuilder.class,"resource-marker.txt"));
    }
}
