package com.persistbit.core.collections;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
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
        public final PList<String> comments;

        @FieldNames(names = {"id","name","email","comments"})
        public Person(int id, Name name, String email,PList<String> comments) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.comments = comments;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", name=" + name +
                    ", email='" + email + '\'' +
                    ", comments='" + comments + '\'' +
                    '}';
        }
    }

    @Test
    public void testMappers() {
        Person p = new Person(1234,new Name("Peter","Muys"),"peter@test.com",PList.<String>empty().plusAll("bla","blabla","blablabla","gendaan met bla"));
        ObjectMapperRegistry reg = new ObjectMapperRegistry();
        reg.registerDefault(Name.class)
                .addAllFields()
                .rename("first","first_name").rename("last","last_name");
        reg.registerDefault(Person.class)
                .addAllFieldsExcept("comments")
                .prefix("name","real_name_")
                .ignore("comments",PList.<String>empty().plus("Hello"));
        MappedProperties props = new MappedProperties();
        reg.apply(p.getClass()).getProperties("person",p,props);
        System.out.println(props);
        System.out.println(reg.apply(Person.class).create("person",props));
    }
}
