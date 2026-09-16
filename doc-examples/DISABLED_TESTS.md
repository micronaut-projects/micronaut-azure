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
- Last full-suite result: build successful, 5 tests executed, 0 skipped, 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets.
  The Micronaut and Azure annotations are imported from their Java packages (`micronaut.context.annotation`,
  `jakarta.inject`, `com.microsoft.azure.functions.annotation`).
- Methods are snake_case (`blob_service_client`, `sign_with_algorithm`); Java classes are imported
  (`from com.azure.storage.blob import BlobServiceClient, BlobServiceClientBuilder`, `from java.lang import System`).
- The example sources live in `src/main/python` (the guide uses `source="main"` snippets) and the tests in
  `src/test/python`; both roots are merged into one compilation by the `mergePythonSources` task of the build
  file, see below.
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
| `doc-examples/example-python/build.gradle.kts` (`mergePythonSources`) | The documentation classes live in `src/main/python` and the tests in `src/test/python`; compiling them separately yields two GraalPy VFS roots whose generated shim modules shadow each other at test time, and the Python compiler resolves the imports of a source file only within its own source root, so both roots are merged into one directory compiled by `compileTestPython`. |
| `example.MyHttpFunction` | A Python class cannot subclass the Java class `AzureHttpFunction`; the Python example delegates to an `AzureHttpFunction` instance created in `__init__` (composition) and the `invoke` method calls its `route` method. The test therefore builds the request with the delegate's `request(...)` builder and calls the Python `invoke` method directly. |
| `example.BlobFunction` | A Python class cannot subclass the abstract Java class `AzureFunction`, which starts the application context and injects the function in its constructor; the Python example is a plain class with an injected `ApplicationEventPublisher` that the test obtains from the running application context, and `BlobEvent` is a dataclass (Micronaut events need not extend `ApplicationEvent`). |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `example.MyHttpFunction`, `example.BlobFunction` as Azure Functions entry points | The Azure Functions runtime instantiates the function class reflectively and looks for `@FunctionName` methods. The generated Java class of a Python function class carries neither the `invoke`/`copy` method nor the `@FunctionName`, `@HttpTrigger`, `@BlobTrigger`, `@BlobOutput` and `@StorageAccount` annotations (the Python compiler bridges only bean, `@Executable` and test methods and does not emit these annotations), and its no-argument constructor needs the GraalPy runtime installed by a running Micronaut application context, which the Azure entry point is itself responsible for starting. The guide carries `[.lang-python]` warnings; the deployed entry point must be a Java, Kotlin or Groovy class. |

## java.type usages

None.
