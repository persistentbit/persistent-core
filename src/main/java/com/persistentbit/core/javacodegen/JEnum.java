package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.exceptions.ToDo;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/06/17
 */
public class JEnum extends BaseValueClass{
	private final String        className;
	private final AccessLevel   accessLevel;

	private final PList<JEnumInstance> instances;
	private final PList<JMethod>       methods;
	@Nullable
	private final String               doc;
	private final PList<String>        annotations;
	private final PSet<JImport>        imports;
	private final PList<JField>		   fields;

	public JEnum(String className, AccessLevel accessLevel,
				  PList<JEnumInstance> instances,
				  PList<JMethod> methods,
				  String doc,
				  PList<String> annotations,
				  PSet<JImport> imports,
				  PList<JField> fields
	) {
		this.className = className;
		this.accessLevel = accessLevel;
		this.instances = instances;
		this.methods = methods;
		this.doc = doc;
		this.annotations = annotations;
		this.imports = imports;
		this.fields = fields;
	}


	public JEnum(String className){
		this(
			className,
			AccessLevel.Public,
			PList.empty(),
			PList.empty(),
			null,
			PList.empty(),
			PSet.empty(),
			PList.empty()
		);
	}
	public JEnum packagePrivate(){
		return copyWith("accessLevel",AccessLevel.Private);
	}
	public JEnum asFinal() {
		return copyWith("isFinal",true);
	}
	public JEnum asStatic() { return copyWith("isStatic",true);}
	public JEnum extendsDef(String extendsDef){
		return copyWith("extendsDef",extendsDef);
	}

	public JEnum withInstances(PList<JEnumInstance> instances){
		return copyWith("instances",instances);
	}
	public PList<JEnumInstance> getInstances() {
		return instances;
	}

	public JEnum addInstance(JEnumInstance field){
		return withInstances(instances.plus(field));
	}
	public JEnum addInstance(String name){
		return addInstance(new JEnumInstance(name));
	}

	public JEnum withFields(PList<JField> fields) {
		return copyWith("fields",fields);
	}
	public JEnum addField(JField field){
		return withFields(fields.plus(field));
	}


	public JEnum javaDoc(String javaDoc){
		return copyWith("doc",javaDoc);
	}

	public JEnum addMethod(JMethod method){
		return copyWith("methods",methods.plus(method));
	}
	public JEnum addMethod(String name, String typeDef, Function<JMethod,JMethod> builder){
		return addMethod(builder.apply(new JMethod(name,typeDef)));
	}

	public JEnum addAnnotation(String annotation){
		return copyWith("annotations",annotations.plus(annotation));
	}

	public boolean hasAnnotation(String name){
		return annotations.filter(str -> str.startsWith("@" + name)).isEmpty() == false;
	}

	public JEnum addImport(JImport imp){
		return copyWith("imports", imports.plus(imp));
	}


	public JEnum addImport(String name){
		return addImport(new JImport(name));
	}

	public JEnum addImport(Class cls){
		return addImport(cls.getName());
	}

	public PSet<JImport> getAllImports(){
		return imports;
	}


	public PrintableText printFields() {
		return out -> {
			instances.forEach(f -> out.print(f.print()));
		};
	}
	public PrintableText printMethods() {
		return out -> {
			methods
				.filter(m -> m.isConstructor() == false)
				.forEach(m -> out.print(m.print()));
		};
	}
	public PrintableText printConstructors() {
		return out -> {
			methods
				.filter(m -> m.isConstructor())
				.forEach(m -> out.print(m.print()));
		};
	}




	public PrintableText print() {
		return out -> {
			/*String res = accessLevel.label();
			if(res.isEmpty() == false){
				res += " ";
			}
			res += "enum " + className;
			res += extendsDef == null ? "" : " extends " + extendsDef;
			if(implementsDef.isEmpty() == false){
				res += " implements " + implementsDef.toString(", ");
			}
			res += " {";
			if(doc != null){
				out.print(doc);
			}
			for(String ann :annotations){
				out.println(ann);
			}
			out.println(res);
			out.indent(printClassContent());
			out.println("}");*/
			throw new ToDo("print enum");
		};
	}

}
