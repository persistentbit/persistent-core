package com.persistbit.core;

import com.persistentbit.core.resources.ClassPathResourceLoader;
import com.persistentbit.core.resources.FileResourceLoader;
import com.persistentbit.core.resources.ResourceLoader;
import com.persistentbit.core.resources.URLResourceLoader;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.utils.IO;

import java.io.File;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/02/17
 */
public class ResourceLoaderTest{

	static final TestCase loaderTest = TestCase.name("Resource Loader test").code(tr -> {
		File f       = IO.createTempDir("resourceLoaderTest").orElseThrow();
		File outFile = new File(f, "testje.txt");
		IO.getClassPathResourceStream("/resourceLoaderTest.txt")
		  .flatMap(is ->
			  IO.fileToOutputStream(outFile)
				.flatMap(out -> IO.copyAndClose(is, out))
		  );

		ResourceLoader loader =
			ClassPathResourceLoader.inst.forNames(name -> name.startsWith("http") == false)
										.orTry(URLResourceLoader.inst
											.forNames(name -> name.toLowerCase().startsWith("http")))
										.orTry(FileResourceLoader.forRoot(f).withNameMapper(name -> name
											.equals("VanFile") ? "testje.txt" : name)
																 .forNames(name -> name.equals("VanFile")));
		tr.isEquals(loader.apply("resourceLoaderTest.txt").map(pb -> pb.toText(IO.utf8))
						  .orElseThrow(), "This is the text.");
		tr.isTrue(loader.apply("https://www.google.be").map(pb -> pb.toText(IO.utf8)).orElseThrow().toLowerCase()
						.contains("<html"));
		tr.isEquals(loader.apply("VanFile").map(pb -> pb.toText(IO.utf8)).orElseThrow(), "This is the text.");

	});


	public void testAll() {
		CoreTest.runTests(ResourceLoaderTest.class);
	}

	public static void main(String... args) throws Exception {
		new ResourceLoaderTest().testAll();
	}
}
