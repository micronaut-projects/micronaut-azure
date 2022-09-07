package io.micronaut.azure.cosmos.client

import com.azure.cosmos.CosmosAsyncClient
import com.azure.cosmos.CosmosClient
import com.azure.cosmos.CosmosContainer
import com.azure.cosmos.CosmosDatabase
import com.azure.cosmos.models.CosmosContainerProperties
import com.azure.cosmos.models.CosmosContainerResponse
import com.azure.cosmos.models.CosmosDatabaseResponse
import com.azure.cosmos.models.CosmosItemRequestOptions
import com.azure.cosmos.models.CosmosItemResponse
import com.azure.cosmos.models.PartitionKey
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class CosmosClientSpec extends Specification implements AzureCosmosTestProperties {

    @AutoCleanup
    @Shared
    ApplicationContext context = ApplicationContext.run(properties)

    def "should get cosmos client and perform write/read operations"() {
        when:
            CosmosClient client = context.getBean(CosmosClient)
            CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("testDb")
            CosmosDatabase database = client.getDatabase(databaseResponse.getProperties().getId())

            CosmosContainerProperties containerProperties =
                    new CosmosContainerProperties("person", "/name");

            CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
            CosmosContainer container = database.getContainer(containerResponse.getProperties().getId());

            Person person = new Person()
            person.id = UUID.randomUUID().toString()
            person.name = "Some Name"

            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            def partitionKey = new PartitionKey(person.getName())
            container.createItem(person, partitionKey, cosmosItemRequestOptions);

            CosmosItemResponse<Person> loadedItem = container.readItem(person.getId(), partitionKey, Person.class);

            def asyncClient = context.getBean(CosmosAsyncClient.class)
            def asyncContainer = asyncClient.getDatabase(database.getId()).getContainer(containerResponse.getProperties().getId())
            def asyncLoadedItem = asyncContainer.readItem(person.getId(), partitionKey, Person.class)

        then:
            loadedItem.getItem().id == person.id
            loadedItem.getItem().name == person.name

            asyncLoadedItem.block().getItem().name == person.name
    }
}
