package com.persistentbit.core.javacodegen;

import com.persistentbit.core.io.IOFiles;
import com.persistentbit.core.javacodegen.annotations.CaseClass;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.UReflect;

import java.nio.file.Path;

/**
 * TODOC
 *
 * @author petermuys
 * @since 22/06/17
 */
@CaseClass
public class GeneratedJavaSource{
	private final String        fullClassName;
	private final PrintableText code;

	public GeneratedJavaSource(String fullClassName, PrintableText code) {
		this.fullClassName = fullClassName;
		this.code = code;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public PrintableText getCode() {
		return code;
	}

	public Result<Path>	writeSource(Path sourceRoot){
		return
			UReflect.convertClassNameToPath(sourceRoot,fullClassName,"java")
			.flatMap(path -> IOFiles.mkdirsIfNotExisting(path.toFile().getParentFile()).map(f -> path))
			.flatMap(path -> PrintTextWriter.writeAndClose(path,code));
	}
}
