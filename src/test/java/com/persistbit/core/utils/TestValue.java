package com.persistbit.core.utils;

import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.utils.NoEqual;
import com.persistentbit.core.utils.NoToString;

/**
 * @author Peter Muys
 * @since 1/09/2016
 */
@SuppressWarnings("FieldCanBeLocal")
public class TestValue extends BaseValueClass{

  private final Integer id;
  private final String  name;
  @NoEqual
  @NoToString
  private final int     extra;

  public TestValue(Integer id, String name) {
	this(id, name, 0);
  }

  public TestValue(Integer id, String name, int extra) {
	this.id = id;
	this.name = name;
	this.extra = extra;
  }

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getExtra() {
		return extra;
	}
}