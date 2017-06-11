package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.javacodegen.annotations.CaseClass;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.Objects;
import java.util.Optional;


/**
 * TODOC
 *
 * @author petermuys
 * @since 10/06/17
 */
@CaseClass
public class JJavaFile extends BaseValueClass{
	private  final	String	packageName;
	@Nullable
	private  final	String	doc;
	private  final	PList<JClass>	classes;
	private  final PSet<JImport> imports;



	public JJavaFile(String packageName, @Nullable String doc, PList<JClass> classes,PSet<JImport> imports){
		this.packageName = Objects.requireNonNull(packageName, "packageName can not be null");
		this.doc = doc;
		this.classes = Objects.requireNonNull(classes, "classes can not be null");
		this.imports = imports;
	}

	public JJavaFile(String packageName){
		this(packageName,null,PList.empty(),PSet.empty());
	}

	public PSet<JImport> getAllImports() {
		return imports.plusAll(classes.map(cls -> cls.getAllImports()).flatten());
	}
	public PrintableText printImports() {
		return out -> {
			getAllImports()
				.filter(imp -> imp.includeForPackage(packageName))
				.distinct()
				.forEach(imp -> out.print(imp.print()));
		};
	}

	public JJavaFile addClass(JClass cls){
		return withClasses(classes.plus(cls));
	}

	public JJavaFile addImport(JImport imp){
		return withImports(imports.plus(imp));
	}

	public PrintableText print(){
		return out -> {
			if(doc != null){
				out.println(doc);
			}
			out.println("package " + packageName + ";");
			out.println();
			out.print(printImports());
			out.println();
			classes.forEach(cls -> out.print(cls.printClass()));
		};
	}



	public  String	getPackageName(){
		return this.packageName;
	}
	public  JJavaFile	withPackageName(String packageName){
		return new JJavaFile(packageName, doc, classes,imports);
	}
	public  Optional<String>	getDoc(){
		return Optional.ofNullable(this.doc);
	}
	public  JJavaFile	withDoc(@Nullable String doc){
		return new JJavaFile(packageName, doc, classes,imports);
	}
	public  PList<JClass>	getClasses(){
		return this.classes;
	}
	public  JJavaFile	withClasses(PList<JClass> classes){
		return new JJavaFile(packageName, doc, classes,imports);
	}
	public  JJavaFile	withImports(PSet<JImport> imports){
		return new JJavaFile(packageName, doc, classes,imports);
	}
}
