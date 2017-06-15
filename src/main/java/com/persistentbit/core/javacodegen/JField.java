package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.javacodegen.annotations.*;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;
import com.persistentbit.core.utils.UString;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/05/17
 */
public class JField extends BaseValueClass{
	private final String name;
	private final String definition;
	private final boolean isStatic ;
	private final boolean isFinal;
	private final AccessLevel accessLevel;
	@Nullable
	private final           String  doc;

	@Nullable private final Class primitiveType;
	private final PSet<JImport> imports;
	private final PList<String> annotations;

	public JField(String name, String definition, boolean isStatic, boolean isFinal,
				 String doc,
				 Class primitiveType,
				 PSet<JImport> imports,
				  AccessLevel accessLevel,
				  PList<String> annotations
	) {
		this.name = name;
		this.definition = definition;
		this.isStatic = isStatic;
		this.isFinal = isFinal;

		this.doc = doc;
		this.primitiveType = primitiveType;
		this.imports = imports;
		this.accessLevel =  accessLevel;
		this.annotations = annotations;
	}
	public boolean hasAnnotation(String name){
		return getAnnotation(name).isPresent();
	}

	public Optional<String> getAnnotation(String name){
		return annotations.find(str -> str.startsWith("@" + name));
	}

	public JField(String name, String definition,Class primitiveType){
		this(name,definition,false,true,
			 null,
			 primitiveType,
			 PSet.empty(),
			 AccessLevel.Private,
			 PList.empty()
		);
	}
	public JField(String name, String definition){
		this(name,definition,null);
	}

	public JField(String name, Class type){
		this(name,type.getSimpleName(), type.isPrimitive() ? type : null);
	}

	public JField withAccessLevel(AccessLevel accessLevel){
		return copyWith("accessLevel",accessLevel);
	}

	public JField primitive(Class cls){
		return copyWith("primitiveType",cls);
	}
	public JField asStatic(){
		return copyWith("isStatic",true);
	}
	public JField asNullable() {
		if(isNullable()){
			return this;
		}
		return addImport(Nullable.class).addAnnotation("@" + Nullable.class.getSimpleName());
	}
	public JField defaultValue(String defaultValue){
		return addImport(DefaultValue.class).addAnnotation("@" + DefaultValue.class.getSimpleName() + "(\"" + defaultValue + "\")");
	}

	public JField initValue(String initValue){
		return addImport(InitValue.class).addAnnotation("@" + InitValue.class.getSimpleName() + "(\"" + initValue + "\")");
	}

	public JField withFinal(boolean isFinal){
		return copyWith("isFinal",isFinal);
	}

	public JField notFinal() {
		return withFinal(false);
	}
	public JField asFinal() {
		return withFinal(true);
	}

	public JField noGetter() {
		if(isGenGetter() == false){
			return this;
		}
		return addImport(NoGet.class).addAnnotation("@" + NoGet.class.getSimpleName());
	}
	public JField noWith() {
		if(isGenWith() == false){
			return this;
		}
		return addImport(NoWith.class).addAnnotation("@" + NoWith.class.getSimpleName());
	}
	public JField javaDoc(String doc){
		return copyWith("doc",doc);
	}
	public JField excludeHash(){
		return copyWith("includeInHash",false);
	}

	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public boolean isGenGetter() {
		return hasAnnotation(NoGet.class.getSimpleName()) == false;
	}

	public boolean isGenWith() {
		return hasAnnotation(NoWith.class.getSimpleName()) == false;
	}

	public String getDoc() {
		return doc;
	}

	public boolean isIncludeInHash() {
		return hasAnnotation(NoEqual.class.getSimpleName()) == false;
	}

	public boolean isNullable() {
		return hasAnnotation(Nullable.class.getSimpleName());
	}


	private Optional<String> annotationValue(String ann){
		int start = ann.indexOf("\"");
		int end = ann.lastIndexOf("\"");
		if(start < 0 || end < 0){
			return Optional.empty();
		}

		return Optional.of(UString.unEscapeJavaString(ann.substring(start+1,end)));
	}

	public Optional<String> getDefaultValue() {

		return getAnnotation(DefaultValue.class.getSimpleName()).flatMap(ann -> annotationValue(ann));
	}

	public Optional<String> getInitValue() {
		return getAnnotation(InitValue.class.getSimpleName()).flatMap(ann -> annotationValue(ann));
	}

	public JField withAnnotations(PList<String> annotations){
		return copyWith("annotations",annotations);
	}

	public JField addAnnotation(String annotation){
		return copyWith("annotations",annotations.plus(annotation));
	}

	public PrintableText printDef() {
		return out -> {
			String res = accessLevel.label();
			res = res.isEmpty()? res : res + " ";
			res = isStatic ? res + " static" : res;
			res = isFinal ? res + " final" : res;
			res = res.trim();
			res = res + "\t" + (isNullable() && getDefaultValue().isPresent()==false ? getNullableDefinition() : definition);
			res = res + "\t" + name;
			res = getInitValue().isPresent() ? "\t=\t" + getInitValue().get() : res;
			res += ";";
			if(this.doc != null){
				out.print(this.doc);
			}
			annotations.forEach(ann -> out.println(ann));
			out.println(res);
		};
	}

	public boolean isRequired(){
		return isNullable() == false && getDefaultValue().isPresent() == false;
	}

	public PrintableText printConstructAssign(String assignValue){
		return out -> {

			if(getDefaultValue().isPresent()){
				if(assignValue.equals("null")){
					out.println("this." + name + " = " + getDefaultValue().get() + ";");
				} else {
					if(isNullable()){
						out.println("this." + name + " = " + assignValue + " == null ? " + getDefaultValue().get() + " : " + assignValue + ";");
					} else {
						out.println("this." + name + " = " + assignValue + ";");
					}
				}
			} else {
				if(isNullable()){
					out.println("this." + name + " = " + assignValue + ";");
				} else {
					out.println("this." + name + " = Objects.requireNonNull(" + assignValue + ", \"" + name + " can not be null\");");
				}
			}
		};
	}


	public String getNullableDefinition(){
		if(primitiveType == null){
			return definition;
		}
		switch (primitiveType.getSimpleName()){
			case "int" : return "Integer";
			case "byte" : return "Byte";
			case "short" : return "Short";
			case "long" : return "Long";
			case "float" : return "Float";
			case "double" : return "Double";
			default: throw new RuntimeException("Unknown: " + primitiveType);
		}

	}

	public Optional<Class> getPrimitiveType(){
		return Optional.ofNullable(primitiveType);
	}

	public boolean isArray() {
		return getDefinition().endsWith("[]");
	}
	public boolean isArrayArray() {
		return getDefinition().endsWith("[][]");
	}

	public JMethod	createGetter() {
		boolean isOptional = isNullable() && getDefaultValue().isPresent() == false;
		String resType = isOptional
			   ? "Optional<" + getNullableDefinition() + ">"
			   : definition ;
		JMethod m = new JMethod("get" + UString.firstUpperCase(name),resType);
		if(isOptional) m = m.addImport(JImport.forClass(Optional.class));
		m = m.withCode(out -> {
			if(isOptional){
				out.println("return Optional.ofNullable(" + (isStatic? "" : "this.") + name + ");");
			} else {
				out.println("return " + (isStatic ? "" : "this.") + name + ";");
			}
		});
		m = m.addAnnotation("@Generated");
		m = m.addImport(JImport.forClass(Generated.class));
		return m;
	}



	public JField addImport(JImport imp){
		return copyWith("imports",imports.plus(imp));
	}

	public JField addImport(Class cls){
		return addImport(JImport.forClass(cls));
	}

	public PSet<JImport> getAllImports(){
		PSet<JImport> res = imports;
		return res;
	}

	public JArgument asArgument(){
		return new JArgument(definition,name,isNullable(), PList.empty(),getAllImports());
	}
}