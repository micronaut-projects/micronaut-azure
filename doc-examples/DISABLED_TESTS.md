# Python Docs Disabled Test Inventory

This file tracks the Python documentation examples under `doc-examples/example-python`
that are present but disabled, or that deviate from the Java example because the direct port does
not compile or does not behave like the Java example yet. It is the bug-fixing task list for
the Python compiler (`micronaut-inject-python` / `micronaut-context-python`); every row references a
`TODO(python)` comment in the sources or a workaround described below.

The Python examples are compiled by every build and their tests run with
`./gradlew pythonCheck -Ppython-ci` (the "Python CI" GitHub workflow).

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\(" doc-examples/example-python/src/test/python`.
- Last full-suite command: `./gradlew :micronaut-doc-examples:micronaut-example-python:test -Ppython-ci`.
- Last full-suite result (micronaut-core 5.2.3 / micronaut-build 8.1.2): build successful, 5 tests executed, 0 skipped, 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets.
  The Micronaut and Azure annotations are imported from their Java packages (`micronaut.context.annotation`,
  `jakarta.inject`, `com.microsoft.azure.functions.annotation`).
- Methods are snake_case (`blob_service_client`, `sign_with_algorithm`); Java classes are imported
  (`from com.azure.storage.blob import BlobServiceClient, BlobServiceClientBuilder`, `from java.lang import System`).
- The example sources live in `src/main/python` (the guide uses `source="main"` snippets) and the tests in
  `src/test/python`, compiled as two source roots.
- The Azure Functions annotations (`@FunctionName`, `@StorageAccount`, `@BlobOutput`) are copied onto the generated
  methods because the build passes `-Amicronaut.introspection.allowReflection=example.*` to the Python compiler
  (`build.gradle.kts`); the annotations of the method *parameters* (`@HttpTrigger`, `@BlobTrigger`) are not emitted.
- The tests are `@MicronautTest` classes; the `AZURE_BLOB_ENDPOINT` environment variable read by
  `example.BlobServiceFactory` is set by the Gradle test task of every example project, and the Key Vault backed
  `KeyVaultKeySigner` is replaced by the Java `example.support.FakeKeyVaultKeySigner` test bean.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |

None.

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `example.BlobFunction` | The Java class extends the abstract `AzureFunction`, whose constructor starts the application context and injects the function (`applicationContext.inject(this)`). A Python subclass compiles (the generated class extends `AzureFunction`) but cannot be used: instantiated from Python, the base constructor injects the Java adapter instead of the Python object, so the `@Inject` attribute is missing (`AttributeError: 'BlobFunction' object has no attribute 'event_publisher'`); obtained as a bean of the test's application context, the base constructor starts a second application context whose Python runtime owns the object, so injecting it into the test fails (`IllegalArgumentException: Python wrapper example.BlobFunction cannot be reconstructed in the target context`). The Python example is a plain class with an injected `ApplicationEventPublisher` that the test obtains from the running application context, and `BlobEvent` is a dataclass (Micronaut events need not extend `ApplicationEvent`). |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `example.MyHttpFunction`, `example.BlobFunction` as Azure Functions entry points | The Azure Functions runtime instantiates the function class reflectively and looks for `@FunctionName` methods. The generated Java class of `MyHttpFunction` now extends `AzureHttpFunction`, bridges `invoke` and carries `@FunctionName` (and `BlobFunction` would carry `@FunctionName`/`@StorageAccount`/`@BlobOutput`), but the parameter annotations `@HttpTrigger`/`@BlobTrigger` are not emitted, and the no-argument constructor of the generated class needs the GraalPy runtime installed by a running Micronaut application context, which the Azure entry point is itself responsible for starting. The guide carries `[.lang-python]` warnings; the deployed entry point must be a Java, Kotlin or Groovy class. |

## java.type usages

None.
