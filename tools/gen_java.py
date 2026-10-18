#!/usr/bin/env python3
"""Generates src/java/HelloEnterprise.java: a deliberately over-engineered
single-file Java program (Java 21 source launcher compatible) that turns a
message into a list of character codes through factories, builders,
strategies, an event bus, a DI container and a 30-stage pipeline."""
import os

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "java", "HelloEnterprise.java")

LABELS = {32: "SPACE", 34: "QUOTE", 39: "APOSTROPHE", 42: "ASTERISK", 47: "SLASH", 92: "BACKSLASH"}


def label(code):
    return LABELS.get(code, chr(code))


HEADER = """// GENERATED FILE. Do not edit by hand. Run tools/gen_java.py instead.
// Run with: java HelloEnterprise.java "Hello, World!"
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Entry point of the Enterprise Hello World Character Resolution Platform.
 */
public class HelloEnterprise {

    public static void main(String[] args) {
        String message = args.length > 0 ? args[0] : "Hello, World!";
        boolean verbose = "1".equals(System.getenv("HELLO_VERBOSE"));
        ApplicationContext context = ApplicationContextBuilder.newBuilder()
                .withMessage(message)
                .withStrategyName("default")
                .withVerbose(verbose)
                .build();
        ApplicationRunner runner = context.getContainer().resolve(ApplicationRunner.class);
        int exitCode = runner.run();
        System.exit(exitCode);
    }
}

/** Minimal logger. Writes to stderr so stdout stays machine readable. */
final class Logger {
    private final boolean verbose;

    Logger(boolean verbose) {
        this.verbose = verbose;
    }

    void debug(String message) {
        if (verbose) {
            System.err.println("[java][DEBUG] " + message);
        }
    }

    void info(String message) {
        if (verbose) {
            System.err.println("[java][INFO ] " + message);
        }
    }

    void error(String message) {
        System.err.println("[java][ERROR] " + message);
    }
}

/** Immutable configuration holder. */
final class AppConfig {
    private final String message;
    private final String strategyName;
    private final boolean verbose;

    AppConfig(String message, String strategyName, boolean verbose) {
        this.message = message;
        this.strategyName = strategyName;
        this.verbose = verbose;
    }

    String getMessage() {
        return message;
    }

    String getStrategyName() {
        return strategyName;
    }

    boolean isVerbose() {
        return verbose;
    }
}

/** Event passed around the event bus. */
final class Event {
    private final String topic;
    private final Object payload;

    Event(String topic, Object payload) {
        this.topic = topic;
        this.payload = payload;
    }

    String topic() {
        return topic;
    }

    Object payload() {
        return payload;
    }
}

/** Very small publish/subscribe bus. */
final class EventBus {
    private final Map<String, List<Consumer<Event>>> subscribers = new HashMap<>();
    private int publishedCount = 0;

    void subscribe(String topic, Consumer<Event> handler) {
        subscribers.computeIfAbsent(topic, key -> new ArrayList<>()).add(handler);
    }

    void publish(Event event) {
        publishedCount++;
        List<Consumer<Event>> handlers = subscribers.getOrDefault(event.topic(), Collections.emptyList());
        for (Consumer<Event> handler : handlers) {
            handler.accept(event);
        }
    }

    int getPublishedCount() {
        return publishedCount;
    }
}

/** Hand written dependency injection container. */
final class SimpleContainer {
    private final Map<Class<?>, Supplier<?>> providers = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();

    <T> void register(Class<T> type, Supplier<? extends T> supplier) {
        providers.put(type, supplier);
    }

    <T> T resolve(Class<T> type) {
        Object existing = singletons.get(type);
        if (existing != null) {
            return type.cast(existing);
        }
        Supplier<?> supplier = providers.get(type);
        if (supplier == null) {
            throw new IllegalStateException("No provider registered for " + type.getName());
        }
        Object created = supplier.get();
        singletons.put(type, created);
        return type.cast(created);
    }
}

/** Contract for objects that know exactly one character code. */
interface CharacterProvider {
    void initialize();

    int provide();

    char symbol();

    String name();

    boolean isInitialized();

    void dispose();
}

/** Shared lifecycle and validation behaviour for all providers. */
abstract class AbstractCharacterProvider implements CharacterProvider {
    protected final int code;

    protected AbstractCharacterProvider(int code) {
        this.code = code;
    }

    protected void ensureInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException("Provider not initialized: " + name());
        }
    }

    protected void validateRange(int candidate) {
        if (candidate < 32 || candidate > 126) {
            throw new IllegalArgumentException("Code out of printable ASCII range: " + candidate);
        }
    }
}

/** Registry of supplier functions, one per supported character. */
final class CharacterProviderRegistry {
    private final Map<Integer, Supplier<CharacterProvider>> suppliers = new LinkedHashMap<>();

    void register(int code, Supplier<CharacterProvider> supplier) {
        if (suppliers.containsKey(code)) {
            throw new IllegalStateException("Duplicate registration for code " + code);
        }
        suppliers.put(code, supplier);
    }

    boolean supports(int code) {
        return suppliers.containsKey(code);
    }

    CharacterProvider create(int code) {
        Supplier<CharacterProvider> supplier = suppliers.get(code);
        if (supplier == null) {
            throw new IllegalArgumentException("Unsupported character code: " + code);
        }
        return supplier.get();
    }

    int size() {
        return suppliers.size();
    }

    List<Integer> codes() {
        return new ArrayList<>(suppliers.keySet());
    }
}

/** Creates and initializes providers. */
final class CharacterProviderFactory {
    private final CharacterProviderRegistry registry;
    private final EventBus eventBus;

    CharacterProviderFactory(CharacterProviderRegistry registry, EventBus eventBus) {
        this.registry = registry;
        this.eventBus = eventBus;
    }

    CharacterProvider createFor(char symbol) {
        CharacterProvider provider = registry.create(symbol);
        provider.initialize();
        eventBus.publish(new Event("provider.created", provider.name()));
        return provider;
    }
}

/** Abstract builder, because a concrete one alone would be too simple. */
abstract class AbstractCharacterProviderFactoryBuilder {
    protected CharacterProviderRegistry registry;
    protected EventBus eventBus;

    AbstractCharacterProviderFactoryBuilder withRegistry(CharacterProviderRegistry registry) {
        this.registry = registry;
        return this;
    }

    AbstractCharacterProviderFactoryBuilder withEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    abstract CharacterProviderFactory build();
}

/** The one and only concrete builder. */
final class ConcreteCharacterProviderFactoryBuilder extends AbstractCharacterProviderFactoryBuilder {
    @Override
    CharacterProviderFactory build() {
        if (registry == null || eventBus == null) {
            throw new IllegalStateException("Builder is missing dependencies");
        }
        return new CharacterProviderFactory(registry, eventBus);
    }
}

/** Provides the factory. Yes, a provider of a factory. */
final class CharacterProviderFactoryProvider {
    private final CharacterProviderFactory factory;

    CharacterProviderFactoryProvider(CharacterProviderFactory factory) {
        this.factory = factory;
    }

    CharacterProviderFactory get() {
        return factory;
    }
}

/** Turns a message into character codes. */
interface MessageStrategy {
    String id();

    List<Integer> resolve(String message);
}

/** Default strategy: one provider per character. */
final class DefaultMessageStrategy implements MessageStrategy {
    private final CharacterProviderFactoryProvider factoryProvider;
    private final Logger logger;

    DefaultMessageStrategy(CharacterProviderFactoryProvider factoryProvider, Logger logger) {
        this.factoryProvider = factoryProvider;
        this.logger = logger;
    }

    @Override
    public String id() {
        return "default";
    }

    @Override
    public List<Integer> resolve(String message) {
        List<Integer> codes = new ArrayList<>(message.length());
        CharacterProviderFactory factory = factoryProvider.get();
        for (int index = 0; index < message.length(); index++) {
            CharacterProvider provider = factory.createFor(message.charAt(index));
            codes.add(provider.provide());
            provider.dispose();
        }
        logger.info("resolved " + codes.size() + " characters");
        return codes;
    }
}

/** Registry of strategies. */
final class MessageStrategyRegistry {
    private final Map<String, MessageStrategy> strategies = new HashMap<>();

    void register(MessageStrategy strategy) {
        strategies.put(strategy.id(), strategy);
    }

    MessageStrategy find(String id) {
        return strategies.get(id);
    }
}

/** Resolves a strategy by name. */
final class MessageStrategyResolver {
    private final MessageStrategyRegistry registry;

    MessageStrategyResolver(MessageStrategyRegistry registry) {
        this.registry = registry;
    }

    MessageStrategy resolve(String id) {
        MessageStrategy strategy = registry.find(id);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + id);
        }
        return strategy;
    }
}

/** One step of the pipeline. */
interface PipelineStage {
    String id();

    List<Integer> process(List<Integer> codes);
}

/** Runs all stages in order. */
final class Pipeline {
    private final List<PipelineStage> stages;

    Pipeline(List<PipelineStage> stages) {
        this.stages = stages;
    }

    List<Integer> run(List<Integer> input) {
        List<Integer> current = input;
        for (PipelineStage stage : stages) {
            current = stage.process(current);
        }
        return current;
    }

    int size() {
        return stages.size();
    }
}

/** Where output goes. */
interface OutputChannel {
    void write(String line);
}

/** Writes to stdout. */
final class ConsoleOutputChannel implements OutputChannel {
    @Override
    public void write(String line) {
        System.out.println(line);
    }
}

/** Decorator that joins codes with spaces before writing. */
final class ConsoleOutputDecorator {
    private final OutputChannel delegate;

    ConsoleOutputDecorator(OutputChannel delegate) {
        this.delegate = delegate;
    }

    void writeCodes(List<Integer> codes) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < codes.size(); index++) {
            if (index > 0) {
                builder.append(' ');
            }
            builder.append(codes.get(index));
        }
        delegate.write(builder.toString());
    }
}

/** Makes sure the output matches the input. */
final class OutputValidator {
    void validate(String message, List<Integer> codes) {
        if (codes.size() != message.length()) {
            throw new IllegalStateException("Length mismatch: " + codes.size() + " vs " + message.length());
        }
        for (int index = 0; index < codes.size(); index++) {
            if (codes.get(index) != message.charAt(index)) {
                throw new IllegalStateException("Mismatch at index " + index);
            }
        }
    }
}

/** Optional self test over every supported character. */
final class SelfTest {
    private final CharacterProviderRegistry registry;
    private final CharacterProviderFactory factory;
    private final Logger logger;

    SelfTest(CharacterProviderRegistry registry, CharacterProviderFactory factory, Logger logger) {
        this.registry = registry;
        this.factory = factory;
        this.logger = logger;
    }

    void run() {
        for (int code : registry.codes()) {
            CharacterProvider provider = factory.createFor((char) code);
            if (provider.provide() != code) {
                throw new IllegalStateException("Self test failed for " + code);
            }
            if (provider.symbol() != (char) code) {
                throw new IllegalStateException("Symbol mismatch for " + code);
            }
            provider.dispose();
        }
        logger.info("self test passed for " + registry.size() + " providers");
    }
}

/** Ties everything together. */
final class ApplicationRunner {
    private final AppConfig config;
    private final MessageStrategyResolver resolver;
    private final Pipeline pipeline;
    private final ConsoleOutputDecorator output;
    private final OutputValidator validator;
    private final SelfTest selfTest;
    private final EventBus eventBus;
    private final Logger logger;

    ApplicationRunner(AppConfig config, MessageStrategyResolver resolver, Pipeline pipeline,
            ConsoleOutputDecorator output, OutputValidator validator, SelfTest selfTest,
            EventBus eventBus, Logger logger) {
        this.config = config;
        this.resolver = resolver;
        this.pipeline = pipeline;
        this.output = output;
        this.validator = validator;
        this.selfTest = selfTest;
        this.eventBus = eventBus;
        this.logger = logger;
    }

    int run() {
        try {
            if ("1".equals(System.getenv("HELLO_SELFTEST"))) {
                selfTest.run();
            }
            MessageStrategy strategy = resolver.resolve(config.getStrategyName());
            List<Integer> codes = strategy.resolve(config.getMessage());
            List<Integer> processed = pipeline.run(codes);
            validator.validate(config.getMessage(), processed);
            output.writeCodes(processed);
            logger.info("events published: " + eventBus.getPublishedCount());
            return 0;
        } catch (RuntimeException exception) {
            logger.error(exception.getMessage());
            return 1;
        }
    }
}

/** Everything the application needs, wired by hand. */
final class ApplicationContext {
    private final SimpleContainer container;

    ApplicationContext(SimpleContainer container) {
        this.container = container;
    }

    SimpleContainer getContainer() {
        return container;
    }
}

/** Builds the application context. */
final class ApplicationContextBuilder {
    private String message = "Hello, World!";
    private String strategyName = "default";
    private boolean verbose = false;

    static ApplicationContextBuilder newBuilder() {
        return new ApplicationContextBuilder();
    }

    ApplicationContextBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    ApplicationContextBuilder withStrategyName(String strategyName) {
        this.strategyName = strategyName;
        return this;
    }

    ApplicationContextBuilder withVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    ApplicationContext build() {
        SimpleContainer container = new SimpleContainer();
        container.register(AppConfig.class, () -> new AppConfig(message, strategyName, verbose));
        container.register(Logger.class, () -> new Logger(verbose));
        container.register(EventBus.class, EventBus::new);
        container.register(CharacterProviderRegistry.class, () -> {
            CharacterProviderRegistry registry = new CharacterProviderRegistry();
            GeneratedRegistryPopulator.populate(registry, container.resolve(Logger.class));
            return registry;
        });
        container.register(CharacterProviderFactory.class, () -> new ConcreteCharacterProviderFactoryBuilder()
                .withRegistry(container.resolve(CharacterProviderRegistry.class))
                .withEventBus(container.resolve(EventBus.class))
                .build());
        container.register(CharacterProviderFactoryProvider.class,
                () -> new CharacterProviderFactoryProvider(container.resolve(CharacterProviderFactory.class)));
        container.register(MessageStrategyRegistry.class, () -> {
            MessageStrategyRegistry registry = new MessageStrategyRegistry();
            registry.register(new DefaultMessageStrategy(
                    container.resolve(CharacterProviderFactoryProvider.class),
                    container.resolve(Logger.class)));
            return registry;
        });
        container.register(MessageStrategyResolver.class,
                () -> new MessageStrategyResolver(container.resolve(MessageStrategyRegistry.class)));
        container.register(Pipeline.class,
                () -> GeneratedPipelineAssembler.assemble(container.resolve(EventBus.class)));
        container.register(ConsoleOutputDecorator.class,
                () -> new ConsoleOutputDecorator(new ConsoleOutputChannel()));
        container.register(OutputValidator.class, OutputValidator::new);
        container.register(SelfTest.class, () -> new SelfTest(
                container.resolve(CharacterProviderRegistry.class),
                container.resolve(CharacterProviderFactory.class),
                container.resolve(Logger.class)));
        container.register(ApplicationRunner.class, () -> new ApplicationRunner(
                container.resolve(AppConfig.class),
                container.resolve(MessageStrategyResolver.class),
                container.resolve(Pipeline.class),
                container.resolve(ConsoleOutputDecorator.class),
                container.resolve(OutputValidator.class),
                container.resolve(SelfTest.class),
                container.resolve(EventBus.class),
                container.resolve(Logger.class)));
        return new ApplicationContext(container);
    }
}

"""

CHAR_TEMPLATE = """/**
 * Provider for character code %CODE% (%LABEL%).
 * Knows exactly one thing and is very serious about it.
 */
final class Char%PAD%Provider extends AbstractCharacterProvider {
    private static final int CODE = %CODE%;
    private static final String DISPLAY_NAME = "CHAR_%PAD%_%LABEL%";
    private final Logger logger;
    private boolean initialized = false;

    Char%PAD%Provider(Logger logger) {
        super(CODE);
        this.logger = logger;
    }

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("Already initialized: " + DISPLAY_NAME);
        }
        initialized = true;
        logger.debug("initialized " + DISPLAY_NAME);
    }

    @Override
    public int provide() {
        ensureInitialized();
        validateRange(CODE);
        return CODE;
    }

    @Override
    public char symbol() {
        return (char) CODE;
    }

    @Override
    public String name() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void dispose() {
        initialized = false;
        logger.debug("disposed " + DISPLAY_NAME);
    }
}

"""

STAGE_TEMPLATE = """/** Pipeline stage %NN%. Carefully copies the list and changes nothing. */
final class PipelineStage%NN% implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage%NN%(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-%NN%";
    }

    @Override
    public List<Integer> process(List<Integer> codes) {
        eventBus.publish(new Event("pipeline.stage", id()));
        List<Integer> copy = new ArrayList<>(codes.size());
        for (Integer code : codes) {
            if (code == null) {
                throw new IllegalStateException(id() + ": null code");
            }
            copy.add(code);
        }
        return Collections.unmodifiableList(copy);
    }
}

"""


def main():
    parts = [HEADER]
    for code in range(32, 127):
        parts.append(
            CHAR_TEMPLATE.replace("%CODE%", str(code))
            .replace("%PAD%", "%03d" % code)
            .replace("%LABEL%", label(code))
        )
    for n in range(1, 31):
        parts.append(STAGE_TEMPLATE.replace("%NN%", "%02d" % n))

    populator = ["/** Registers every provider. Generated. */", "final class GeneratedRegistryPopulator {",
                 "    private GeneratedRegistryPopulator() {", "    }", "",
                 "    static void populate(CharacterProviderRegistry registry, Logger logger) {"]
    for code in range(32, 127):
        populator.append("        registry.register(%d, () -> new Char%03dProvider(logger));" % (code, code))
    populator += ["    }", "}", ""]
    parts.append("\n".join(populator) + "\n")

    assembler = ["/** Assembles the 30 stage pipeline. Generated. */", "final class GeneratedPipelineAssembler {",
                 "    private GeneratedPipelineAssembler() {", "    }", "",
                 "    static Pipeline assemble(EventBus eventBus) {",
                 "        List<PipelineStage> stages = new ArrayList<>();"]
    for n in range(1, 31):
        assembler.append("        stages.add(new PipelineStage%02d(eventBus));" % n)
    assembler += ["        return new Pipeline(stages);", "    }", "}", ""]
    parts.append("\n".join(assembler))

    path = os.path.normpath(OUT)
    with open(path, "w", encoding="utf-8") as handle:
        handle.write("".join(parts))
    print("wrote", path)


if __name__ == "__main__":
    main()
