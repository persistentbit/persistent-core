package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.javacodegen.annotations.Generated;
import com.persistentbit.core.javacodegen.annotations.NoGet;
import com.persistentbit.core.javacodegen.annotations.NoWith;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoToString;
import com.persistentbit.core.utils.UString;
import com.persistentbit.core.utils.builders.NOT;
import com.persistentbit.core.utils.builders.SET;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JClass extends BaseValueClass{
	//private final String packageName;
	private final String className;
	private final AccessLevel accessLevel;
	private final String extendsDef;
	private final PList<String> implementsDef;
	private final boolean isFinal;

	private final PList<JField> fields;
	private final PList<JMethod> methods;
	@Nullable
	private final String doc;
	private final PList<String> annotations;
	private final PList<JClass> internalClasses;
	private final boolean isStatic;
	private final PSet<JImport> imports;

	public JClass(String className, AccessLevel accessLevel, String extendsDef,
				  PList<String> implementsDef,
				  boolean isFinal,
				  PList<JField> fields,
				  PList<JMethod> methods,
				  String doc,
				  PList<String> annotations,
				  PSet<JImport> imports,
				  PList<JClass> internalClasses,
				  boolean isStatic
	) {
		//this.packageName = packageName;
		this.className = className;
		this.accessLevel = accessLevel;
		this.extendsDef = extendsDef;
		this.implementsDef = implementsDef;
		this.isFinal = isFinal;
		this.fields = fields;
		this.methods = methods;
		this.doc = doc;
		this.annotations = annotations;
		this.imports = imports;
		this.internalClasses = internalClasses;
		this.isStatic = isStatic;
	}


	public JClass(String className){
		this(
			className,
			AccessLevel.Public,
			null,
			PList.<String>empty(),
			false,
			PList.<JField>empty(),
			PList.<JMethod>empty(),
			null,
			PList.<String>empty(),
			PSet.empty(),
			PList.empty(),
				false
		);
	}
	public JClass packagePrivate(){
		return copyWith("accessLevel",AccessLevel.Private);
	}
	public JClass asFinal() {
		return copyWith("isFinal",true);
	}
	public JClass asStatic() { return copyWith("isStatic",true);}
	public JClass extendsDef(String extendsDef){
		return copyWith("extendsDef",extendsDef);
	}
	public JClass addImplements(String implementsDef){
		return copyWith("implementsDef",this.implementsDef.plus(implementsDef));
	}
	public JClass withFields(PList<JField> fields){
		return copyWith("fields",fields);
	}
	public PList<JField> getFields() {
		return fields;
	}

	public JClass addField(JField field){
		return withFields(fields.plus(field));
	}
	public JClass addField(String name, String def, Function<JField,JField> builder){
		return addField(builder.apply(new JField(name,def)));
	}

	public JClass javaDoc(String javaDoc){
		return copyWith("doc",javaDoc);
	}

	public JClass addMethod(JMethod method){
		return copyWith("methods",methods.plus(method));
	}
	public JClass addMethod(String name, String typeDef, Function<JMethod,JMethod> builder){
		return addMethod(builder.apply(new JMethod(name,typeDef)));
	}

	public JClass addAnnotation(String annotation){
		return copyWith("annotations",annotations.plus(annotation));
	}

	public boolean hasAnnotation(String name){
		return annotations.filter(str -> str.startsWith("@" + name)).isEmpty() == false;
	}

	public JClass addImport(JImport imp){
		return copyWith("imports", imports.plus(imp));
	}

	public JClass addInternalClass(JClass cls) {
		return copyWith("internalClasses",internalClasses.plus(cls));
	}

	public JClass addImport(String name){
		return addImport(new JImport(name));
	}

	public JClass addImport(Class cls){
		return addImport(cls.getName());
	}

	public PSet<JImport> getAllImports(){
		return imports
			.plusAll(fields.map(JField::getAllImports).flatten())
			.plusAll(internalClasses.map(JClass::getAllImports).flatten())
			.plusAll(methods.map(JMethod::getAllImports).flatten());
	}


	private PList<JField> getConstructorFields() {
		return fields
				.filter(f -> f.isStatic() == false)
				.filter(f -> f.isFinal() == false || f.getInitValue().isPresent() == false);
	}

	private PList<JField> getNotNullableConstructorFields() {
		return getConstructorFields().filter(f -> f.isRequired());
	}

	public JClass addMainConstructor(AccessLevel level) {
		JMethod m = new JMethod(className).withAccessLevel(level);
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		PList<JField> constFields = getConstructorFields();
		for(JField f : constFields){
			m = m.addArg(f.asArgument());
		}
		m = m.withCode(out -> {
			for(JField f: constFields){
				out.indent(f.printConstructAssign(f.getName()));
			}
		});
		constFields.find(f -> f.isNullable()==false).ifPresent(field -> {
			addImport(Objects.class);
		});

		return hasMethodWithSignature(m) == false ? addMethod(m) : this;
	}

	public JClass addRequiredFieldsConstructor(AccessLevel level){

		JMethod m = new JMethod(className).withAccessLevel(level);
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		PList<JField> constFields = getNotNullableConstructorFields();
		for(JField f : constFields){
			m = m.addArg(f.asArgument());
		}
		m = m.withCode(out -> {
			for(JField f: constFields){
				out.indent(f.printConstructAssign());
			}
		});
		constFields.find(f -> f.isNullable()==false).ifPresent(field -> {
			addImport(Objects.class);
		});

		return hasMethodWithSignature(m) == false ? addMethod(m) : this;
	}
	}


	public PrintableText printFields() {
		return out -> {
			fields.forEach(f -> out.print(f.printDef()));
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

	public boolean isGenGetter() {
		return hasAnnotation(NoGet.class.getSimpleName()) == false;
	}

	public boolean isGenWith() {
		return hasAnnotation(NoWith.class.getSimpleName()) == false;
	}

	public JClass addGettersAndSetters() {
		JClass res = this;
		for(JField f : fields){
			if(f.isGenGetter()){
				if(f.isGenGetter() && isGenGetter()){
					JMethod m = f.createGetter();
					m = m.doc(out -> {
						out.println("/**");
						out.println(" * Get the value of field {@link #" + f.getName() + "}.<br>");
						out.println(" * @return {@link #" + f.getName() + "}");
						out.println(" *" + "/");
					});
					res = hasMethodWithSignature(m)? res : res.addMethod(m);
				}
			}

			if(f.isGenWith() && isGenWith()){
				res = res.addWithMethod(f);
			}
		}
		return res;
	}


	public JClass addWithMethod(JField field){
		JMethod m = new JMethod("with" + UString.firstUpperCase(field.getName()),className)
			.addArg(field.asArgument());
		m = m.withCode(out -> {
			String args = getConstructorFields().map(f -> f.getName()).toString(", ");
			out.println("return new " + className + "(" + args + ");");
		});
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		m = m.doc(out -> {
			out.println("/**");
			out.println(" * Create a copy of this " + className + " object with a new value for field {@link #" + field.getName() + "}.<br>");
			out.println(" * @param " + field.getName() + " The new value for field {@link #" + field.getName() + "}");
			out.println(" * @return A new instance of {@link " + className + "}");
			out.println(" */");
		});
		return hasMethodWithSignature(m) == false ? addMethod(m) : this;
	}



	public PrintableText printInternalClasses() {
		return out -> {
			internalClasses.forEach(cls -> out.print(cls.printClass()));
		};
	}

	public PrintableText printClassContent() {
		return out -> {
			out.print(printFields());
			out.println();
			out.print(printInternalClasses());
			out.println();
			out.print(printConstructors());
			//out.print(printGettersSetters());
			out.print(printMethods());
		};
	}



	public PrintableText printClass() {
		return out -> {
			String res = isStatic ? "static " : "";

			res += accessLevel.label();
			if(res.isEmpty() == false){
				res += " ";
			}
			res += "class " + className;
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
			out.println("}");
		};
	}

	private PList<JField> getBuilderRequiredFields(){
		return fields
			.filter(f -> f.isFinal() == false || f.getInitValue().isPresent() == false)
			.filter(f -> f.isStatic() == false)
			.filter(f -> f.isNullable() == false && f.getDefaultValue().isPresent() == false);
	}

	private JClass addBuilderClass() {
		if(hasAnnotation("NoBuilder")){
			return this;
		}
		PList<JField> reqFields = getBuilderRequiredFields();
		String clsGenerics = reqFields.zipWithIndex().map(t-> "_T" + (t._1+1)).toString(", ");
		String clsName = reqFields.isEmpty() ? "Builder" : "Builder<" + clsGenerics + ">";
		JClass bcls = new JClass(clsName).asStatic();
		bcls = bcls.addAnnotation("@Generated");
		bcls = bcls.addAnnotation("@SuppressWarnings(\"unchecked\")");
		bcls = bcls.addImport(SuppressWarnings.class);
		bcls = bcls.addImport(JImport.forClass(Generated.class));
		for(JField fld : fields.filter(f -> f.isStatic() == false)){
			bcls = bcls.addField(
				fld.notFinal()
					.noGetter()
					.noWith()
					.javaDoc(null)
					.withAnnotations(PList.empty())
					.withAccessLevel(AccessLevel.Private)
			);

			String resultTypeGenerics = reqFields.zipWithIndex().map(t->
				 t._2.getName().equals(fld.getName()) ? "SET": "_T" + (t._1+1))
				.toString(", ");
			String resultType = reqFields.isEmpty()
					? "Builder"
					: "Builder<" + resultTypeGenerics + ">";

			JMethod m = new JMethod("set" + UString.firstUpperCase(fld.getName()),resultType)
					.addArg(fld.asArgument())
					.withCode(out -> {
						out.println("this." + fld.getName() + "\t=\t" + fld.getName() + ";");
						if(clsGenerics.equals(resultTypeGenerics)){
							out.println("return this;");
						} else {
							out.println("return (" + resultType + ")this;");
						}

					});
					//.addAnnotation("@SuppressWarnings(\"unchecked\")");
			bcls = bcls.addMethod(m);

		}
		return addInternalClass(bcls);
	}
	private JClass addBuilderMethods() {
		if(hasAnnotation("NoBuilder")){
			return this;
		}
		JClass res = this;
		PList<JField> reqFields = getBuilderRequiredFields();
		res = res.addImport(NOT.class);
		res = res.addImport(SET.class);
		JArgument setterArg = new JArgument(
			"Function<Builder<" + reqFields.map(f -> "NOT").toString(",")
				+ ">, Builder<" + reqFields.map(f -> "SET").toString(", ") + ">>","setter"
		).addImport(JImport.forClass(Function.class));

		JMethod updated = new JMethod("updated",className)
			.addArg("Function<Builder,Builder>","updater",false)
			.withCode(out -> {
				out.println("Builder b = new Builder();");
				for(JField f : fields.filter(f -> f.isStatic() == false)){
					out.println("b.set" + UString.firstUpperCase(f.getName()) + "(this." + f.getName() + ");");
				}
				out.println("b = updater.apply(b);");
				out.println("return new " + className + "(" + getConstructorFields().map(f -> "b." + f.getName()).toString(", ") + ");");
			});
		updated = updated.addAnnotation("@Generated");
		updated = updated.addImport(JImport.forClass(Generated.class));
		res = res.addMethod(updated);
		JMethod build = new JMethod("build",className).asStatic()
				.addArg(setterArg)
			.withCode(out -> {
				out.println("Builder b = setter.apply(new Builder());");
				out.println("return new " + className + "(" + getConstructorFields().map(f -> "b." + f.getName()).toString(", ") + ");");
			});
		build = build.addAnnotation("@Generated");
		build = build.addImport(JImport.forClass(Generated.class));
		res = res.addMethod(build);
		return res;
	}

	public JClass addEquals(){
		JMethod m = new JMethod("equals","boolean")
			.addArg("Object","o",true)
			.overrides();
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		m = m.withCode(out -> {
			out.println("if(this == o) return true;");
			out.println("if(o instanceof " + className + " == false) return false;");
			out.println(className + " obj = (" + className + ")o;");
			for(JField f : fields.filter(f -> f.isIncludeInHash())){
				String thisField = f.getName();
				String otherField = "obj." + f.getName();
				if(f.getPrimitiveType().isPresent()){
					if(f.getPrimitiveType().get() == float.class ){
						out.println("if(Float.compare(" + thisField + ", " + otherField + ") != 0) return false;");
					}
					if(f.getPrimitiveType().get() == double.class ){
						out.println("if(Double.compare(" + thisField + ", " + otherField + ") != 0) return false;");
					}
					out.println("if(" + thisField + "!= "+ otherField+ ") return false;");
					continue;
				}
				if(f.isArrayArray()){
					out.println("if(!Arrays.deepEquals(" + thisField + ", " + otherField + ")) return false;");
					continue;
				}
				if(f.isArray()){
					out.println("if(!Arrays.equals(" + thisField + ", " + otherField + ")) return false;");
					continue;
				}
				if(f.isNullable()){
					out.println("if(" + thisField + " != null ? !" + thisField + ".equals(" + otherField + ") : " + otherField + "!= null) return false;");
				} else {
					out.println("if(!" + thisField + ".equals(" + otherField + ")) return false;");
				}
			}
			out.println("return true;");
		});
		if(fields.find(f -> f.isIncludeInHash() && f.isArray()).isPresent()){
			m = m.addImport(JImport.forClass(Arrays.class));
		}
		return hasMethodWithSignature(m) == false ? addMethod(m) : this;
	}

	public JClass addHashCode(){
		JMethod m = new JMethod("hashCode","int")
			.overrides();
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		m = m.withCode(out -> {
			out.println("int result;");
			if(fields.find(f -> f.isIncludeInHash() && double.class == f.getPrimitiveType().orElse(null)).isPresent()){
				out.println("long temp;");
			}
			boolean first = true;
			for(JField f : fields.filter(f -> f.isIncludeInHash())){
				String prefix = first ? "result = " : "result = 31 * result + ";
				first = false;
				if(f.getPrimitiveType().isPresent()){
					Class cls = f.getPrimitiveType().get();
					if(cls == double.class){
						out.println("temp =  Double.doubleToLongBits(this." + f.getName() + ");");
						out.println(prefix + "(int)(temp ^ (temp >>> 32));");
					} else if(cls == float.class){
						out.println(prefix + "(this." + f.getName() + " != +0.0f ? Float.floatToIntBits(this." + f.getName() + ") : 0);");
					} else if(cls == long.class){
						out.println(prefix + "(int) (this." + f.getName() + " ^ (this." + f.getName() + ">>> 32));");
					} else if(cls == boolean.class){
						out.println(prefix + "(this." + f.getName() + " ? 1 : 0);");
					} else if(cls == int.class){
						out.println(prefix + "this." +f.getName() + ";");
					} else {
						out.println(prefix + "(int)this." + f.getName() + ";");
					}
				} else {
					if(f.isArrayArray()){
						out.println(prefix + "Arrays.deepHashCode(this." + f.getName() + ");");
					} else if(f.isArray()){
						out.println(prefix + "Arrays.hashCode(this." + f.getName() + ");");
					} else {
						out.println(prefix + "(this." + f.getName() + " != null ? this." + f.getName() + ".hashCode() : 0);");
					}
				}

			}
			out.println("return result;");
		});
		if(fields.find(f -> f.isIncludeInHash() && f.isArray()).isPresent()){
			m = m.addImport(JImport.forClass(Arrays.class));
		}

		return hasMethodWithSignature(m) == false? addMethod(m) : this;
	}

	public JClass addEqualsHashCode(){
		return addEquals().addHashCode();
	}

	public JClass addToString(){
		if(hasAnnotation("NoToString")){
			return this;
		}
		PList<JField> toStringFields = fields
					  .filter(f -> f.isStatic() == false)
					  .filter(f -> f.hasAnnotation(NoToString.class.getSimpleName()) == false);
		JMethod m = new JMethod("toString","String")
			.overrides();
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		m = m.withCode(out -> {
			out.println("return \"" + className + "[\" + ");
			boolean first = true;

			for(JField f : toStringFields){
				if(first){
					out.print("\t\"" + f.getName() + "=\" + ");
				} else {
					out.print("\t\", " + f.getName() + "=\" + ");
				}
				first = false;
				if(f.getDefinition().equals("String")){
					out.println("(" + f.getName() + " == null ? \"null\" : '\\\"' + UString.present(UString.escapeToJavaString(" + f.getName() + "),32,\"...\") + '\\\"') +") ;
				} else if(f.isArray()){

					out.println("Arrays.toString(" + f.getName() + ") +");
				} else {
					out.println(f.getName() + " + ");
				}

			}

			out.println("\t']';");
		});
		if(toStringFields.find(f -> f.getDefinition().equals("String")).isPresent()){
			m = m.addImport(JImport.forClass(UString.class));
		}
		if(toStringFields.find(f -> f.isArray()).isPresent()){
			m = m.addImport(JImport.forClass(Arrays.class));
		}
		return hasMethodWithSignature(m) == false ? addMethod(m) : this;
	}

	public JClass withMethods(PList<JMethod> methods){
		return copyWith("methods",methods);
	}

	public JClass withClasses(PList<JClass> internalClasses){
		return copyWith("internalClasses",internalClasses);
	}

	public JClass makeFieldsFinal() {
		return withFields(fields.map(field ->
			field.asFinal()
		));
	}

	public PList<JClass> getInternalClasses() {
		return internalClasses;
	}

	public JClass makeCaseClass(){
		return addGettersAndSetters()
			.makeFieldsFinal()
			.removeGenerated()
			.addMainConstructor(AccessLevel.Public)
			.addGettersAndSetters()
			.addEqualsHashCode()
			.addToString()
			.addBuilder();
	}


	public JClass addBuilder() {
		return addBuilderClass().addBuilderMethods();
	}


	public JClass removeGenerated() {
		return
			   withMethods(methods.filter(m ->
			   		m.getAnnotations().find(ann -> ann.startsWith("@Generated")).isPresent() == false
			   ))
			   .withClasses(this.internalClasses.filter(cls ->
			   		cls.annotations.find(ann -> ann.startsWith("@Generated")).isPresent() == false
			   ));

	}

	public boolean hasMethodWithSignature(JMethod sign){
		return methods.find(m -> m.isSameSignature(sign)).isPresent();
	}

	static public void main(String...args){
		JJavaFile jfile = new JJavaFile("com.persistentbit.javacodegen.test");
		JClass cls = new JClass("PersoonData")
			   	.addField(new JField("id",int.class))
				.addField(new JField("floatValue",Float.class))
				.addField(new JField("doubleValueNullable",Double.class).asNullable())
				.addField(new JField("doubleValuePrimitive",double.class))
				.addField(new JField("longValue",long.class))
				.addField(new JField("charArr","char[]"))
				.addField(new JField("intArrArr","Integer[][]"))
				.addField(new JField("rrn",String.class))
				.addField(new JField("enabled",boolean.class).defaultValue("true"))
				.addField(new JField("inschrijving",Integer.class).asNullable())
				.makeCaseClass()
				//.removeGenerated()
				//.makeCaseClass()
			;
		jfile = jfile.addClass(cls);

		System.out.println(jfile.print().printToString());
	}
}
