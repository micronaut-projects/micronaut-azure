package io.micronaut.azure.cosmos.client

import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.CosmosDBEmulatorContainer
import org.testcontainers.utility.DockerImageName

import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.time.Duration

trait AzureCosmosTestProperties implements TestPropertyProvider {

    private static final String PARTITION_COUNT_PROP = "AZURE_COSMOS_EMULATOR_PARTITION_COUNT"
    // This will create 2 partitions, by default it creates 11 and we don't need that much
    // and it prolongs startup time which can cause test to fail intermittently
    private static final String PARTITION_COUNT_VAL = "1"
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3)

    @Override
    Map<String, String> getProperties() {
        CosmosDBEmulatorContainer emulator = new CosmosDBEmulatorContainer(
                DockerImageName.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator")
        ).withEnv(PARTITION_COUNT_PROP, PARTITION_COUNT_VAL).withStartupTimeout(STARTUP_TIMEOUT)
        emulator.start()
        Path keyStoreFile = Files.createTempFile("azure-cosmos-emulator", ".keystore")
        KeyStore keyStore = emulator.buildNewKeyStore()
        keyStore.store(new FileOutputStream(keyStoreFile.toFile()), emulator.getEmulatorKey().toCharArray())

        System.setProperty("javax.net.ssl.trustStore", keyStoreFile.toString())
        System.setProperty("javax.net.ssl.trustStorePassword", emulator.getEmulatorKey())
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12")

        return [
                'azure.cosmos.default-gateway-mode'      : 'true',
                'azure.cosmos.endpoint-discovery-enabled': 'false',
                'azure.cosmos.endpoint'                  : emulator.getEmulatorEndpoint(),
                'azure.cosmos.key'                       : emulator.getEmulatorKey(),
                'azure.cosmos.consistency-level'         : 'SESSION'
        ]
    }
}
