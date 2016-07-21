package com.persistentbit.core.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations used to define the property names linked to the parameters of a constructor or method.<br>
 * This is not necessary if you compile with the -parameters flag or use the following in your maven pom.<br>
 * <pre>{@code
 * <plugin>
 *  <groupId>org.apache.maven.plugins</groupId>
 *  <artifactId>maven-compiler-plugin</artifactId>
 *  <version>3.1</version>
 *
 *  <configuration>
 *      <source>1.8</source>
 *      <target>1.8</target>
 *      <encoding>UTF-8</encoding>
 *      <forceJavacCompilerUse>true</forceJavacCompilerUse>
 *      <fork>true</fork>
 *      <compilerArgs>
 *          <arg>-parameters</arg>
 *      </compilerArgs>
 *  </configuration>
 * </plugin>
 * }</pre>
 *
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldNames {
    String[] names();
}
