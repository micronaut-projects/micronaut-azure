package io.micronaut.azure.condition

import io.micronaut.context.condition.ConditionContext
import spock.lang.Specification

import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class EnvironmentCredentialsConditionSpec extends Specification{

    void "condition doesn't match without variables"() {
        given:
        EnvironmentCredentialsCondition condition = new EnvironmentCredentialsCondition()
        ConditionContext context = Mock()

        when:
        boolean matches = condition.matches(context)

        then:
        !matches
    }

    void "condition matches for client secret"() {
        given:
        EnvironmentCredentialsCondition condition = new EnvironmentCredentialsCondition()
        ConditionContext context = Mock()

        when:
        boolean matches = withEnvironmentVariable(PROPERTY_AZURE_CLIENT_ID, "x")
                .and(PROPERTY_AZURE_CLIENT_SECRET, "x")
                .and(PROPERTY_AZURE_TENANT_ID, "x")
                .execute {
            return condition.matches(context)
        }

        then:
        matches
    }

    void "condition matches for client certificate"() {
        given:
        EnvironmentCredentialsCondition condition = new EnvironmentCredentialsCondition()
        ConditionContext context = Mock()

        when:
        boolean matches = withEnvironmentVariable(PROPERTY_AZURE_CLIENT_ID, "x")
                .and(PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "x")
                .and(PROPERTY_AZURE_TENANT_ID, "x")
                .execute {
                    return condition.matches(context)
                }

        then:
        matches
    }

    void "condition matches for username and password"() {
        given:
        EnvironmentCredentialsCondition condition = new EnvironmentCredentialsCondition()
        ConditionContext context = Mock()

        when:
        boolean matches = withEnvironmentVariable(PROPERTY_AZURE_CLIENT_ID, "x")
                .and(PROPERTY_AZURE_USERNAME, "x")
                .and(PROPERTY_AZURE_PASSWORD, "x")
                .execute {
                    return condition.matches(context)
                }

        then:
        matches
    }
}
