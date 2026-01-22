package io.micronaut.azure.function.http;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "age"})
@Introspected
public class Person {
    private String name;
    private int age = 18;

    public Person(String name) {
        this.name = name;
    }

    @JsonCreator
    @Creator
    public Person(@JsonProperty("name") String name, @JsonProperty("age") int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
