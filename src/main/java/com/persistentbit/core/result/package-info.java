/**
 * A Result container is used to return a value from a function where we can have 3 types:<br>
 *     Success, Empty, Failure.<br>
 * Every Result can optionally have some Logging data.<br>
 * Result types can also be lazy or async.<br>
 * @author petermuys
 * @since 3/04/17
 */
@UmlPackage(note="Function call results on steroids!")
package com.persistentbit.core.result;

import com.persistentbit.core.doc.uml.UmlPackage;