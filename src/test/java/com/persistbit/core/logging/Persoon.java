package com.persistbit.core.logging;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/12/2016
 */
public class Persoon extends BaseValueClass{
    private int id;
    private String firstName;
    private String lastName;

    public Persoon(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
