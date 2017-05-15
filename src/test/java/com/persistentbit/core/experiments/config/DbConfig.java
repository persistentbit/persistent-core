package com.persistentbit.core.experiments.config;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.validation.NumberValidator;
import com.persistentbit.core.validation.StringValidator;

import static com.persistentbit.core.experiments.config.Config.intVar;
import static com.persistentbit.core.experiments.config.Config.stringVar;
/**
 * TODOC
 *
 * @author petermuys
 * @since 15/05/17
 */
public class DbConfig extends BaseValueClass{
	public final ConfigVar<String> userName = stringVar("db.userName")
		.withInfo("Database user name")
		.withValidator(StringValidator.notEmpty().toValidator());
	public final ConfigVar<String> passWord = stringVar("db.password")
		.withInfo("Database user password")
		.withValidator(StringValidator.notEmpty().toValidator());
	public final ConfigVar<Integer> maxPoolConnections = intVar("db.poolconnections.max")
		.withInfo("Maximum number of database connections")
		.setValue(10)
		.withValidator(NumberValidator.range(1,500).toValidator())
	;
	public ConfigGroup asConfigGroup() {
		return new ConfigGroup().addFields(this);
	}

	public static void main(String[] args) {
		ModuleCore.consoleLogPrint.registerAsGlobalHandler();
		DbConfig config = new DbConfig();
		ConfigPropertyFile.load(config.asConfigGroup(),DbConfig.class.getResourceAsStream("/config/db.properties")).orElseThrow();
		System.out.println(config);
		System.out.println(config.maxPoolConnections.get());
	}
}
