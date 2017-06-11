package com.persistentbit.core.javacodegen;

import com.persistentbit.core.Nullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.javacodegen.annotations.Generated;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.UString;

import java.util.Objects;
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
	private final           boolean genGetter;
	private final           boolean genWith;
	@Nullable
	private final           String  doc;
	private final           boolean includeInHash;
	private final           boolean isNullable;
	@Nullable private final String  defaultValue;
	@Nullable private final String initValue;
	@Nullable private final Class primitiveType;
	private final PSet<JImport> imports;
	private final PList<String> annotations;

	public JField(String name, String definition, boolean isStatic, boolean isFinal, boolean genGetter,
				 boolean genWith,
				 String doc,
				 boolean includeInHash,
				 boolean isNullable,
				 String defaultValue,
				 String initValue,
				 Class primitiveType,
				 PSet<JImport> imports,
				  AccessLevel accessLevel,
				  PList<String> annotations
	) {
		this.name = name;
		this.definition = definition;
		this.isStatic = isStatic;
		this.isFinal = isFinal;
		this.genGetter = genGetter;
		this.genWith = genWith;
		this.doc = doc;
		this.includeInHash = includeInHash;
		this.isNullable = isNullable;
		this.defaultValue = null;
		this.initValue = initValue;
		this.primitiveType = primitiveType;
		this.imports = imports;
		this.accessLevel =  accessLevel;
		this.annotations = annotations;
	}

	public JField(String name, String definition,Class primitiveType){
		this(name,definition,
			 false,
			 true,
			 true,
			 true,
			 null,
			 true,
			 false,
			 null,
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
		return addImport(Nullable.class).copyWith("isNullable",true);
	}
	public JField defaultValue(String defaultValue){
		return copyWith("defaultValue",defaultValue);
	}

	public JField initValue(String initValue){
		return copyWith("initValue",initValue);
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
		return copyWith("genGetter", false);
	}
	public JField noWith() {
		return copyWith("genWith",false);
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
		return genGetter;
	}

	public boolean isGenWith() {
		return genWith;
	}

	public String getDoc() {
		return doc;
	}

	public boolean isIncludeInHash() {
		return includeInHash;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public Optional<String> getDefaultValue() {
		return Optional.ofNullable(defaultValue);
	}

	public Optional<String> getInitValue() {
		return Optional.ofNullable(initValue);
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
			res = res + "\t" + (isNullable && defaultValue == null ? getNullableDefinition() : definition);
			res = res + "\t" + name;
			res = initValue != null ? "\t=\t" + initValue : res;
			res += ";";
			if(this.doc != null){
				out.print(this.doc);
			}
			annotations.forEach(ann -> out.println(ann));
			out.println(res);
		};
	}

	public PrintableText printConstructAssign(){
		return out -> {
			String res = "this." + name + " = ";
			if(primitiveType != null){
				res += name;
			} else {
				if(isNullable){
					if(defaultValue != null){
						res += name + " == null ? " + defaultValue + " : " + defaultValue;
					} else {
						res += name;
					}
				} else {
					res += "Objects.requireNonNull(" + name + ", \"" + name + " can not be null\")";

				}
			}

			out.println(res + ";");
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
		boolean isOptional = isNullable && defaultValue == null;
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
		if(primitiveType == null && isNullable == false){
			res = res.plus(JImport.forClass(Objects.class));
		}
		return res;
	}

	public JArgument asArgument(){
		return new JArgument(definition,name,isNullable, PList.empty(),getAllImports());
	}
}