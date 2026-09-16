# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut MQTT that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 1.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1` (needs a container runtime for the Mosquitto test container).
- Last full-suite result: build successful, 8 tests executed, 1 skipped (`CorrelationSpec`, see below), 0 failures.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut and MQTT annotations are generated from imports (`from micronaut.mqtt.annotation import MqttSubscriber,
  Qos, Retained, Topic`, `from micronaut.mqtt.annotation.v5 import MqttProperty, MqttPublisher`).
- `@MqttPublisher` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies; `@MqttSubscriber`
  beans are plain classes with `@Topic` methods. Parameter annotations use `Annotated[str, Topic]`,
  `Annotated[int, Qos]`, `Annotated[bool, Retained]`, `Annotated[str, MqttProperty("userId")]`; repeatable annotations
  (`@Topic`, `@MqttProperty`) are stacked as decorators.
- Method overloading does not exist in Python: the overloads of the Java publisher interfaces are ported as
  separately named methods (`send_to_topic`, `send_with_qos`, `send_retained`, `send_with_properties`,
  `receive_from_topic`).
- Methods that implement or override a Java interface keep the Java (camelCase) name; other methods are snake_case.
- Message payloads are `bytes` (`byte[]`); a received payload is a foreign `byte[]`, decode it with
  `bytes(data).decode()`.
- Do not add Java-style getters or setters to Python docs models (`ProductInfo` uses plain attributes).
- Tests are `@MicronautTest(environments=["mqtt"])` classes with `@Property(name="spec.name", ...)`; the publisher
  proxy and the listener are field-injected (`product_client: Annotated[ProductClient, Inject]`) from the imported
  Python classes. The broker configuration (`mqtt.client.server-uri`, `mqtt.client.client-id`) of the shared Mosquitto
  test container is supplied to the `mqtt` environment by the Java `io.micronaut.mqtt.docs.MqttTestConfigurer`
  `@ContextConfigurer` of this project (see below).
- Prefer normal imports over `java.type(...)`: the imported Python classes work as runtime type arguments
  (`Argument.of(ProductInfo)`, `argument.getType().isAssignableFrom(ProductInfo)`). The only remaining `java.type`
  call is the annotation type an `AnnotatedMqttBinder` returns to Java as a `java.lang.Class` (see "java.type usages"
  below).

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `io.micronaut.mqtt.docs.custom.annotation.CorrelationSpec` | The generated bridge of `AnnotatedMqttBinder.bindFrom` converts the returned `Optional` with `PythonConversion.convertOptional(value, Annotation.class)`: the element type is resolved from the type variable `T extends Annotation` of `AnnotatedMqttBinder<M, T>` instead of the `T` of the inherited `MqttBinder<M, Object>` method (same type variable name), so the bound `byte[]` becomes an `Annotation` proxy and the subscriber fails with `Invalid type [jdk.proxy2.$Proxy] for argument [byte[] correlation]`. The binder itself (`bindTo`, `bindFrom`, `getAnnotationType`) is invoked correctly. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.mqtt.docs.PythonRuntimeInitializer` (Java, `src/test/java`) | The MQTT subscriber processor (`ExecutableMethodProcessor<Topic>`) is created by `DefaultBeanContext.processExecutableMethodsProcessAtStartup()` before the `@Context` beans (the GraalPy runtime) are initialized, and its constructor injects the binder and ser-des registries, so a Python `MqttBinder`/`MqttPayloadSerDes` bean would be instantiated before the GraalPy runtime exists (`GraalPy context has not been initialized`). A `BeanCreatedEventListener` (the Kafka workaround) is too late here because the Python beans are constructor dependencies of the processor; `TypeConverter` beans are created before the startup processors (`DefaultApplicationContext.initializeTypeConverters()`), so a no-op Java `TypeConverter<Object, Object>` injecting the `@Named("python") Context` forces the runtime first. |
| `io.micronaut.mqtt.docs.MqttTestConfigurer` (Java, `src/test/java`) | The `@ContextConfigurer` providing the `mqtt.client.*` configuration of the shared Mosquitto test container to the `mqtt` environment is written in Java because Micronaut Test calls `TestPropertyProvider` before the application context, and with it the GraalPy runtime, exists. It uses the `configure(ApplicationContext)` callback (the builder callback runs before `@MicronautTest` selects the environments) and `environment.addPropertySource(...)`. |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

Every remaining `java.type(...)` call carries a `# TODO(python)` comment naming the reason.

| Location | Reason |
| --- | --- |
| `custom/annotation/CorrelationAnnotationBinder.py` (`CorrelationClass`) | `AnnotatedMqttBinder.getAnnotationType()` returns the annotation type to Java as a runtime `java.lang.Class`; returning the imported Python annotation function fails with `Cannot convert '<function Correlation>' (language: Python, type: function) to Java type 'java.lang.Class'`. |
