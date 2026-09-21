# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut MQTT that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1` (needs a container runtime for the Mosquitto test container).
- Last full-suite result (micronaut-core 5.2.3, micronaut-build 8.1.2): build successful, 8 tests executed, 0 skipped, 0 failures.

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
  call is the Python-defined annotation type an `AnnotatedMqttBinder` returns to Java as a `java.lang.Class` (see
  "java.type usages" below).

## Active `@Disabled` Tests

None.

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.mqtt.docs.publisher.acknowledge.PublisherAcknowledgeSpec` | A class defined inside a method cannot extend an imported Java interface (`Subscriber`) with core 5.2.3: instantiating it fails with `TypeError: invalid instantiation of foreign object` (the runtime module keeps the host interface as the base; with the generated import modules of 5.2.2 the local class worked). The subscriber of the Java example's anonymous class is a module-level class taking the counters. `TODO(python)`. |
| `io.micronaut.mqtt.docs.MqttTestConfigurer` (Java, `src/test/java`) | The `@ContextConfigurer` providing the `mqtt.client.*` configuration of the shared Mosquitto test container to the `mqtt` environment is written in Java because Micronaut Test calls `TestPropertyProvider` before the application context, and with it the GraalPy runtime, exists. It uses the `configure(ApplicationContext)` callback (the builder callback runs before `@MicronautTest` selects the environments) and `environment.addPropertySource(...)`. |

## Intentionally Unsupported Snippet Targets

None.

## java.type usages

Every remaining `java.type(...)` call carries a `# TODO(python)` comment naming the reason.

| Location | Reason |
| --- | --- |
| `custom/annotation/CorrelationAnnotationBinder.py` (`CorrelationClass`) | `AnnotatedMqttBinder.getAnnotationType()` returns the annotation type to Java as a runtime `java.lang.Class`; returning the Python-defined annotation function still fails with core 5.2.3 (`Cannot convert '<function Correlation>' (language: Python, type: function) to Java type 'java.lang.Class': Unsupported target type.`, verified on the identical RabbitMQ binder). Python *classes* passed as `Class` arguments work. |

## Verified with micronaut-core 5.2.3 (workarounds removed)

- `AnnotatedMqttBinder[MqttV5BindingContext, Correlation]` with the Java signatures (`bindFrom(...) -> Optional[object]`):
  the `Optional` is converted with the right type variable (`CorrelationSpec` re-enabled).
- `TypedMqttBinder[MqttV5BindingContext, ProductInfo]` and `MqttPayloadSerDes[ProductInfo]` generic bases like in Java.
- `PythonRuntimeInitializer` (Java `TypeConverter`) removed: the GraalPy runtime is created on demand for the Python
  binder and ser-des beans the subscriber processor injects.
