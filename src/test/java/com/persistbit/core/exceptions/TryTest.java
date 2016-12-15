package com.persistbit.core.exceptions;


import com.persistentbit.core.exceptions.Try;

class Address{

}

class Person{
    public String getName() {
        return "A Person";
    }
    public Try<Address> getAddress() {
        return Try.success(new Address());
    }
}

class PersonDao{
    Try<Person> getById(int id){
        return Try.flatRun(() -> {
            if(id == 1){
                return Try.success(new Person());
            }
            return Try.runtimeFailure("Person with id " + id + " does not exist");
        });
    }
    Try<Person> save(Person p){
        return Try.failure(new RuntimeException("Not Yet Implemented"));
    }
}

/**
 * @author Peter Muys
 * @since 7/09/2016
 */
public class TryTest {
    private PersonDao dao = new PersonDao();
    Try<String> getPersonName(int id){
        return dao.getById(id).map(p -> p.getName());
    }

    Try<PersonDao> getDao() {
        return Try.success(dao);
    }

    Try<Address> getPersonAdres(int id){
        return dao.getById(id).flatMap(p -> p.getAddress());
    }



    public static void main(String... args) throws Exception {
        TryTest tt = new TryTest();
        System.out.println(tt.getPersonName(1));
        System.out.println(tt.getPersonName(10));
        String adrString =tt.dao.getById(0)
                .flatMap(p -> p.getAddress())
                .map(a -> a.toString()).orElse("?");
        System.out.println(adrString);
        Try<PersonDao> dao = tt.getDao();
        Person person = dao.flatMap(d -> d.getById(1))
            .flatMap(p -> dao.getUnchecked().save(p)).orElse(null);
        System.out.println(person);

        System.out.println(Try.flatRun(() -> {
           PersonDao pd = tt.getDao().get();
           Person p = pd.getById(1).get();
           return pd.save(p);
        }));


    }
}
