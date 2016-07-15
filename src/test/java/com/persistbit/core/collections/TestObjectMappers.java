package com.persistbit.core.collections;

import com.persistentbit.core.mappers.ObjectMapperDefault;
import com.persistentbit.core.mappers.ObjectMapperRegistry;
import com.persistentbit.core.mappers.MappedProperties;
import com.persistentbit.core.properties.FieldNames;
import org.testng.annotations.Test;

/**
 * User: petermuys
 * Date: 15/07/16
 * Time: 14:19
 */
public class TestObjectMappers {

    static public class Name{
        public final String first;
        public final String last;

        @FieldNames(names = {"first","last"})
        public Name(String first, String last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Name{");
            sb.append("first='").append(first).append('\'');
            sb.append(", last='").append(last).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    static public class Person{
        public final int id;
        public final Name name;
        public final String email;

        @FieldNames(names = {"id","name","email"})
        public Person(int id, Name name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", name=" + name +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    @Test
    public void testMappers() {
        Person p = new Person(1234,new Name("Peter","Muys"),"peter@test.com");
        ObjectMapperRegistry reg = new ObjectMapperRegistry();
        reg.registerDefault(Name.class).rename("first","first_name").rename("last","last_name");
        reg.registerDefault(Person.class).prefix("name","real_name_");
        MappedProperties props = new MappedProperties();
        reg.apply(p.getClass()).getProperties("person",p,props);
        System.out.println(props);
        System.out.println(reg.apply(Person.class).create("person",props));
    }
}
