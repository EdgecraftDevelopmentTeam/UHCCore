package smp.edgecraft.uhc.core.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Represents the details of a command e.g. the description and aliases
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface CommandInfo {

    String description();

    String[] aliases();

    String permission();

}
