package com.persistentbit.core.javacodegen;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 11/06/17
 */
public class JEnum extends BaseValueClass{

	private final JClass	instanceClass;
	private final PList<JEnumInstance> instances;

	public JEnum(JClass instanceClass,
				 PList<JEnumInstance> instances
	) {
		this.instanceClass = instanceClass;
		this.instances = instances;
	}

	public JClass getInstanceClass() {
		return instanceClass;
	}

	public PList<JEnumInstance> getInstances() {
		return instances;
	}

	public JEnum withInstances(PList<JEnumInstance> instances){
		return copyWith("instances", instances);
	}

	public JEnum addInstance(JEnumInstance instance){
		return withInstances(instances.plus(instance));
	}


	public String getClassName() {
		return instanceClass.getClassName();
	}

	public PSet<JImport> getAllImports() {
		return getInstanceClass().getAllImports();
	}


	public PrintableText printInstances() {
		return out -> {
			boolean first = true;

			for(JEnumInstance instance : instances){
				if(first == false){
					out.println(",");
				} else {
					first = false;
				}
				out.print(instance.print());

			}
			out.println(";");
		};
	}

	public PrintableText print() {
		return out -> {
			String res ="";

			res += instanceClass.getAccessLevel().label();
			if(res.isEmpty() == false){
				res += " ";
			}
			res += "enum " + getClassName();

			if(instanceClass.getImplementsDef().isEmpty() == false){
				res += " implements " + instanceClass.getImplementsDef().toString(", ");
			}
			res += " {";
			if(instanceClass.getDoc() != null){
				out.print(instanceClass.getDoc());
			}
			for(String ann :instanceClass.getAnnotations()){
				out.println(ann);
			}
			out.println(res);
			out.indent(printInstances());
			out.indent(instanceClass.printClassContent());
			out.println("}");
		};
	}

}
