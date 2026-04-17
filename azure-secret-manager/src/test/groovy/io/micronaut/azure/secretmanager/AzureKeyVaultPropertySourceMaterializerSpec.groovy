package io.micronaut.azure.secretmanager

import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import spock.lang.Specification

class AzureKeyVaultPropertySourceMaterializerSpec extends Specification {

    void "it materializes raw dot and underscore property names"() {
        when:
        Map<String, Object> secrets = AzureKeyVaultPropertySourceMaterializer.materialize([
                new KeyVaultSecret('secret-name', 'secretValue')
        ])

        then:
        secrets == [
                'secret-name': 'secretValue',
                'secret.name': 'secretValue',
                'secret_name': 'secretValue'
        ]
    }

    void "it returns empty property-source materialization for empty vault"() {
        expect:
        AzureKeyVaultPropertySourceMaterializer.materialize([]).isEmpty()
    }
}
