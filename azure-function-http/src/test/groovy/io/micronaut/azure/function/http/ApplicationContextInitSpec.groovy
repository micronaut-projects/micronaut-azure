package io.micronaut.azure.function.http

import spock.lang.Issue
import spock.lang.Specification

class ApplicationContextInitSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/688")
    void "application context should be initialized only once"() {
        given:
        AzureHttpFunction function1 = new AzureHttpFunction()
        Person person = new Person("John", 30)
        function1.getApplicationContext().registerSingleton(person)

        when:
        AzureHttpFunction function2 = new AzureHttpFunction()
        AzureHttpFunction function3 = new AzureHttpFunction()

        then:
        function1.getApplicationContext().getBeanDefinitions(Person.class).size() == 1
        function2.getApplicationContext().getBeanDefinitions(Person.class).size() == 1
        function3.getApplicationContext().getBeanDefinitions(Person.class).size() == 1

        cleanup:
        function1.close()
        function2.close()
        function3.close()
    }
}
