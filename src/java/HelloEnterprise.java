// GENERATED FILE. Do not edit by hand. Run tools/gen_java.py instead.
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

/**
 * Provider for character code 32 (SPACE).
 * Knows exactly one thing and is very serious about it.
 */
final class Char032Provider extends AbstractCharacterProvider {
    private static final int CODE = 32;
    private static final String DISPLAY_NAME = "CHAR_032_SPACE";
    private final Logger logger;
    private boolean initialized = false;

    Char032Provider(Logger logger) {
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

/**
 * Provider for character code 33 (!).
 * Knows exactly one thing and is very serious about it.
 */
final class Char033Provider extends AbstractCharacterProvider {
    private static final int CODE = 33;
    private static final String DISPLAY_NAME = "CHAR_033_!";
    private final Logger logger;
    private boolean initialized = false;

    Char033Provider(Logger logger) {
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

/**
 * Provider for character code 34 (QUOTE).
 * Knows exactly one thing and is very serious about it.
 */
final class Char034Provider extends AbstractCharacterProvider {
    private static final int CODE = 34;
    private static final String DISPLAY_NAME = "CHAR_034_QUOTE";
    private final Logger logger;
    private boolean initialized = false;

    Char034Provider(Logger logger) {
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

/**
 * Provider for character code 35 (#).
 * Knows exactly one thing and is very serious about it.
 */
final class Char035Provider extends AbstractCharacterProvider {
    private static final int CODE = 35;
    private static final String DISPLAY_NAME = "CHAR_035_#";
    private final Logger logger;
    private boolean initialized = false;

    Char035Provider(Logger logger) {
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

/**
 * Provider for character code 36 ($).
 * Knows exactly one thing and is very serious about it.
 */
final class Char036Provider extends AbstractCharacterProvider {
    private static final int CODE = 36;
    private static final String DISPLAY_NAME = "CHAR_036_$";
    private final Logger logger;
    private boolean initialized = false;

    Char036Provider(Logger logger) {
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

/**
 * Provider for character code 37 (%).
 * Knows exactly one thing and is very serious about it.
 */
final class Char037Provider extends AbstractCharacterProvider {
    private static final int CODE = 37;
    private static final String DISPLAY_NAME = "CHAR_037_%";
    private final Logger logger;
    private boolean initialized = false;

    Char037Provider(Logger logger) {
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

/**
 * Provider for character code 38 (&).
 * Knows exactly one thing and is very serious about it.
 */
final class Char038Provider extends AbstractCharacterProvider {
    private static final int CODE = 38;
    private static final String DISPLAY_NAME = "CHAR_038_&";
    private final Logger logger;
    private boolean initialized = false;

    Char038Provider(Logger logger) {
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

/**
 * Provider for character code 39 (APOSTROPHE).
 * Knows exactly one thing and is very serious about it.
 */
final class Char039Provider extends AbstractCharacterProvider {
    private static final int CODE = 39;
    private static final String DISPLAY_NAME = "CHAR_039_APOSTROPHE";
    private final Logger logger;
    private boolean initialized = false;

    Char039Provider(Logger logger) {
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

/**
 * Provider for character code 40 (().
 * Knows exactly one thing and is very serious about it.
 */
final class Char040Provider extends AbstractCharacterProvider {
    private static final int CODE = 40;
    private static final String DISPLAY_NAME = "CHAR_040_(";
    private final Logger logger;
    private boolean initialized = false;

    Char040Provider(Logger logger) {
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

/**
 * Provider for character code 41 ()).
 * Knows exactly one thing and is very serious about it.
 */
final class Char041Provider extends AbstractCharacterProvider {
    private static final int CODE = 41;
    private static final String DISPLAY_NAME = "CHAR_041_)";
    private final Logger logger;
    private boolean initialized = false;

    Char041Provider(Logger logger) {
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

/**
 * Provider for character code 42 (ASTERISK).
 * Knows exactly one thing and is very serious about it.
 */
final class Char042Provider extends AbstractCharacterProvider {
    private static final int CODE = 42;
    private static final String DISPLAY_NAME = "CHAR_042_ASTERISK";
    private final Logger logger;
    private boolean initialized = false;

    Char042Provider(Logger logger) {
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

/**
 * Provider for character code 43 (+).
 * Knows exactly one thing and is very serious about it.
 */
final class Char043Provider extends AbstractCharacterProvider {
    private static final int CODE = 43;
    private static final String DISPLAY_NAME = "CHAR_043_+";
    private final Logger logger;
    private boolean initialized = false;

    Char043Provider(Logger logger) {
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

/**
 * Provider for character code 44 (,).
 * Knows exactly one thing and is very serious about it.
 */
final class Char044Provider extends AbstractCharacterProvider {
    private static final int CODE = 44;
    private static final String DISPLAY_NAME = "CHAR_044_,";
    private final Logger logger;
    private boolean initialized = false;

    Char044Provider(Logger logger) {
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

/**
 * Provider for character code 45 (-).
 * Knows exactly one thing and is very serious about it.
 */
final class Char045Provider extends AbstractCharacterProvider {
    private static final int CODE = 45;
    private static final String DISPLAY_NAME = "CHAR_045_-";
    private final Logger logger;
    private boolean initialized = false;

    Char045Provider(Logger logger) {
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

/**
 * Provider for character code 46 (.).
 * Knows exactly one thing and is very serious about it.
 */
final class Char046Provider extends AbstractCharacterProvider {
    private static final int CODE = 46;
    private static final String DISPLAY_NAME = "CHAR_046_.";
    private final Logger logger;
    private boolean initialized = false;

    Char046Provider(Logger logger) {
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

/**
 * Provider for character code 47 (SLASH).
 * Knows exactly one thing and is very serious about it.
 */
final class Char047Provider extends AbstractCharacterProvider {
    private static final int CODE = 47;
    private static final String DISPLAY_NAME = "CHAR_047_SLASH";
    private final Logger logger;
    private boolean initialized = false;

    Char047Provider(Logger logger) {
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

/**
 * Provider for character code 48 (0).
 * Knows exactly one thing and is very serious about it.
 */
final class Char048Provider extends AbstractCharacterProvider {
    private static final int CODE = 48;
    private static final String DISPLAY_NAME = "CHAR_048_0";
    private final Logger logger;
    private boolean initialized = false;

    Char048Provider(Logger logger) {
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

/**
 * Provider for character code 49 (1).
 * Knows exactly one thing and is very serious about it.
 */
final class Char049Provider extends AbstractCharacterProvider {
    private static final int CODE = 49;
    private static final String DISPLAY_NAME = "CHAR_049_1";
    private final Logger logger;
    private boolean initialized = false;

    Char049Provider(Logger logger) {
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

/**
 * Provider for character code 50 (2).
 * Knows exactly one thing and is very serious about it.
 */
final class Char050Provider extends AbstractCharacterProvider {
    private static final int CODE = 50;
    private static final String DISPLAY_NAME = "CHAR_050_2";
    private final Logger logger;
    private boolean initialized = false;

    Char050Provider(Logger logger) {
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

/**
 * Provider for character code 51 (3).
 * Knows exactly one thing and is very serious about it.
 */
final class Char051Provider extends AbstractCharacterProvider {
    private static final int CODE = 51;
    private static final String DISPLAY_NAME = "CHAR_051_3";
    private final Logger logger;
    private boolean initialized = false;

    Char051Provider(Logger logger) {
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

/**
 * Provider for character code 52 (4).
 * Knows exactly one thing and is very serious about it.
 */
final class Char052Provider extends AbstractCharacterProvider {
    private static final int CODE = 52;
    private static final String DISPLAY_NAME = "CHAR_052_4";
    private final Logger logger;
    private boolean initialized = false;

    Char052Provider(Logger logger) {
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

/**
 * Provider for character code 53 (5).
 * Knows exactly one thing and is very serious about it.
 */
final class Char053Provider extends AbstractCharacterProvider {
    private static final int CODE = 53;
    private static final String DISPLAY_NAME = "CHAR_053_5";
    private final Logger logger;
    private boolean initialized = false;

    Char053Provider(Logger logger) {
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

/**
 * Provider for character code 54 (6).
 * Knows exactly one thing and is very serious about it.
 */
final class Char054Provider extends AbstractCharacterProvider {
    private static final int CODE = 54;
    private static final String DISPLAY_NAME = "CHAR_054_6";
    private final Logger logger;
    private boolean initialized = false;

    Char054Provider(Logger logger) {
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

/**
 * Provider for character code 55 (7).
 * Knows exactly one thing and is very serious about it.
 */
final class Char055Provider extends AbstractCharacterProvider {
    private static final int CODE = 55;
    private static final String DISPLAY_NAME = "CHAR_055_7";
    private final Logger logger;
    private boolean initialized = false;

    Char055Provider(Logger logger) {
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

/**
 * Provider for character code 56 (8).
 * Knows exactly one thing and is very serious about it.
 */
final class Char056Provider extends AbstractCharacterProvider {
    private static final int CODE = 56;
    private static final String DISPLAY_NAME = "CHAR_056_8";
    private final Logger logger;
    private boolean initialized = false;

    Char056Provider(Logger logger) {
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

/**
 * Provider for character code 57 (9).
 * Knows exactly one thing and is very serious about it.
 */
final class Char057Provider extends AbstractCharacterProvider {
    private static final int CODE = 57;
    private static final String DISPLAY_NAME = "CHAR_057_9";
    private final Logger logger;
    private boolean initialized = false;

    Char057Provider(Logger logger) {
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

/**
 * Provider for character code 58 (:).
 * Knows exactly one thing and is very serious about it.
 */
final class Char058Provider extends AbstractCharacterProvider {
    private static final int CODE = 58;
    private static final String DISPLAY_NAME = "CHAR_058_:";
    private final Logger logger;
    private boolean initialized = false;

    Char058Provider(Logger logger) {
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

/**
 * Provider for character code 59 (;).
 * Knows exactly one thing and is very serious about it.
 */
final class Char059Provider extends AbstractCharacterProvider {
    private static final int CODE = 59;
    private static final String DISPLAY_NAME = "CHAR_059_;";
    private final Logger logger;
    private boolean initialized = false;

    Char059Provider(Logger logger) {
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

/**
 * Provider for character code 60 (<).
 * Knows exactly one thing and is very serious about it.
 */
final class Char060Provider extends AbstractCharacterProvider {
    private static final int CODE = 60;
    private static final String DISPLAY_NAME = "CHAR_060_<";
    private final Logger logger;
    private boolean initialized = false;

    Char060Provider(Logger logger) {
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

/**
 * Provider for character code 61 (=).
 * Knows exactly one thing and is very serious about it.
 */
final class Char061Provider extends AbstractCharacterProvider {
    private static final int CODE = 61;
    private static final String DISPLAY_NAME = "CHAR_061_=";
    private final Logger logger;
    private boolean initialized = false;

    Char061Provider(Logger logger) {
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

/**
 * Provider for character code 62 (>).
 * Knows exactly one thing and is very serious about it.
 */
final class Char062Provider extends AbstractCharacterProvider {
    private static final int CODE = 62;
    private static final String DISPLAY_NAME = "CHAR_062_>";
    private final Logger logger;
    private boolean initialized = false;

    Char062Provider(Logger logger) {
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

/**
 * Provider for character code 63 (?).
 * Knows exactly one thing and is very serious about it.
 */
final class Char063Provider extends AbstractCharacterProvider {
    private static final int CODE = 63;
    private static final String DISPLAY_NAME = "CHAR_063_?";
    private final Logger logger;
    private boolean initialized = false;

    Char063Provider(Logger logger) {
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

/**
 * Provider for character code 64 (@).
 * Knows exactly one thing and is very serious about it.
 */
final class Char064Provider extends AbstractCharacterProvider {
    private static final int CODE = 64;
    private static final String DISPLAY_NAME = "CHAR_064_@";
    private final Logger logger;
    private boolean initialized = false;

    Char064Provider(Logger logger) {
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

/**
 * Provider for character code 65 (A).
 * Knows exactly one thing and is very serious about it.
 */
final class Char065Provider extends AbstractCharacterProvider {
    private static final int CODE = 65;
    private static final String DISPLAY_NAME = "CHAR_065_A";
    private final Logger logger;
    private boolean initialized = false;

    Char065Provider(Logger logger) {
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

/**
 * Provider for character code 66 (B).
 * Knows exactly one thing and is very serious about it.
 */
final class Char066Provider extends AbstractCharacterProvider {
    private static final int CODE = 66;
    private static final String DISPLAY_NAME = "CHAR_066_B";
    private final Logger logger;
    private boolean initialized = false;

    Char066Provider(Logger logger) {
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

/**
 * Provider for character code 67 (C).
 * Knows exactly one thing and is very serious about it.
 */
final class Char067Provider extends AbstractCharacterProvider {
    private static final int CODE = 67;
    private static final String DISPLAY_NAME = "CHAR_067_C";
    private final Logger logger;
    private boolean initialized = false;

    Char067Provider(Logger logger) {
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

/**
 * Provider for character code 68 (D).
 * Knows exactly one thing and is very serious about it.
 */
final class Char068Provider extends AbstractCharacterProvider {
    private static final int CODE = 68;
    private static final String DISPLAY_NAME = "CHAR_068_D";
    private final Logger logger;
    private boolean initialized = false;

    Char068Provider(Logger logger) {
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

/**
 * Provider for character code 69 (E).
 * Knows exactly one thing and is very serious about it.
 */
final class Char069Provider extends AbstractCharacterProvider {
    private static final int CODE = 69;
    private static final String DISPLAY_NAME = "CHAR_069_E";
    private final Logger logger;
    private boolean initialized = false;

    Char069Provider(Logger logger) {
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

/**
 * Provider for character code 70 (F).
 * Knows exactly one thing and is very serious about it.
 */
final class Char070Provider extends AbstractCharacterProvider {
    private static final int CODE = 70;
    private static final String DISPLAY_NAME = "CHAR_070_F";
    private final Logger logger;
    private boolean initialized = false;

    Char070Provider(Logger logger) {
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

/**
 * Provider for character code 71 (G).
 * Knows exactly one thing and is very serious about it.
 */
final class Char071Provider extends AbstractCharacterProvider {
    private static final int CODE = 71;
    private static final String DISPLAY_NAME = "CHAR_071_G";
    private final Logger logger;
    private boolean initialized = false;

    Char071Provider(Logger logger) {
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

/**
 * Provider for character code 72 (H).
 * Knows exactly one thing and is very serious about it.
 */
final class Char072Provider extends AbstractCharacterProvider {
    private static final int CODE = 72;
    private static final String DISPLAY_NAME = "CHAR_072_H";
    private final Logger logger;
    private boolean initialized = false;

    Char072Provider(Logger logger) {
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

/**
 * Provider for character code 73 (I).
 * Knows exactly one thing and is very serious about it.
 */
final class Char073Provider extends AbstractCharacterProvider {
    private static final int CODE = 73;
    private static final String DISPLAY_NAME = "CHAR_073_I";
    private final Logger logger;
    private boolean initialized = false;

    Char073Provider(Logger logger) {
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

/**
 * Provider for character code 74 (J).
 * Knows exactly one thing and is very serious about it.
 */
final class Char074Provider extends AbstractCharacterProvider {
    private static final int CODE = 74;
    private static final String DISPLAY_NAME = "CHAR_074_J";
    private final Logger logger;
    private boolean initialized = false;

    Char074Provider(Logger logger) {
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

/**
 * Provider for character code 75 (K).
 * Knows exactly one thing and is very serious about it.
 */
final class Char075Provider extends AbstractCharacterProvider {
    private static final int CODE = 75;
    private static final String DISPLAY_NAME = "CHAR_075_K";
    private final Logger logger;
    private boolean initialized = false;

    Char075Provider(Logger logger) {
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

/**
 * Provider for character code 76 (L).
 * Knows exactly one thing and is very serious about it.
 */
final class Char076Provider extends AbstractCharacterProvider {
    private static final int CODE = 76;
    private static final String DISPLAY_NAME = "CHAR_076_L";
    private final Logger logger;
    private boolean initialized = false;

    Char076Provider(Logger logger) {
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

/**
 * Provider for character code 77 (M).
 * Knows exactly one thing and is very serious about it.
 */
final class Char077Provider extends AbstractCharacterProvider {
    private static final int CODE = 77;
    private static final String DISPLAY_NAME = "CHAR_077_M";
    private final Logger logger;
    private boolean initialized = false;

    Char077Provider(Logger logger) {
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

/**
 * Provider for character code 78 (N).
 * Knows exactly one thing and is very serious about it.
 */
final class Char078Provider extends AbstractCharacterProvider {
    private static final int CODE = 78;
    private static final String DISPLAY_NAME = "CHAR_078_N";
    private final Logger logger;
    private boolean initialized = false;

    Char078Provider(Logger logger) {
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

/**
 * Provider for character code 79 (O).
 * Knows exactly one thing and is very serious about it.
 */
final class Char079Provider extends AbstractCharacterProvider {
    private static final int CODE = 79;
    private static final String DISPLAY_NAME = "CHAR_079_O";
    private final Logger logger;
    private boolean initialized = false;

    Char079Provider(Logger logger) {
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

/**
 * Provider for character code 80 (P).
 * Knows exactly one thing and is very serious about it.
 */
final class Char080Provider extends AbstractCharacterProvider {
    private static final int CODE = 80;
    private static final String DISPLAY_NAME = "CHAR_080_P";
    private final Logger logger;
    private boolean initialized = false;

    Char080Provider(Logger logger) {
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

/**
 * Provider for character code 81 (Q).
 * Knows exactly one thing and is very serious about it.
 */
final class Char081Provider extends AbstractCharacterProvider {
    private static final int CODE = 81;
    private static final String DISPLAY_NAME = "CHAR_081_Q";
    private final Logger logger;
    private boolean initialized = false;

    Char081Provider(Logger logger) {
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

/**
 * Provider for character code 82 (R).
 * Knows exactly one thing and is very serious about it.
 */
final class Char082Provider extends AbstractCharacterProvider {
    private static final int CODE = 82;
    private static final String DISPLAY_NAME = "CHAR_082_R";
    private final Logger logger;
    private boolean initialized = false;

    Char082Provider(Logger logger) {
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

/**
 * Provider for character code 83 (S).
 * Knows exactly one thing and is very serious about it.
 */
final class Char083Provider extends AbstractCharacterProvider {
    private static final int CODE = 83;
    private static final String DISPLAY_NAME = "CHAR_083_S";
    private final Logger logger;
    private boolean initialized = false;

    Char083Provider(Logger logger) {
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

/**
 * Provider for character code 84 (T).
 * Knows exactly one thing and is very serious about it.
 */
final class Char084Provider extends AbstractCharacterProvider {
    private static final int CODE = 84;
    private static final String DISPLAY_NAME = "CHAR_084_T";
    private final Logger logger;
    private boolean initialized = false;

    Char084Provider(Logger logger) {
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

/**
 * Provider for character code 85 (U).
 * Knows exactly one thing and is very serious about it.
 */
final class Char085Provider extends AbstractCharacterProvider {
    private static final int CODE = 85;
    private static final String DISPLAY_NAME = "CHAR_085_U";
    private final Logger logger;
    private boolean initialized = false;

    Char085Provider(Logger logger) {
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

/**
 * Provider for character code 86 (V).
 * Knows exactly one thing and is very serious about it.
 */
final class Char086Provider extends AbstractCharacterProvider {
    private static final int CODE = 86;
    private static final String DISPLAY_NAME = "CHAR_086_V";
    private final Logger logger;
    private boolean initialized = false;

    Char086Provider(Logger logger) {
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

/**
 * Provider for character code 87 (W).
 * Knows exactly one thing and is very serious about it.
 */
final class Char087Provider extends AbstractCharacterProvider {
    private static final int CODE = 87;
    private static final String DISPLAY_NAME = "CHAR_087_W";
    private final Logger logger;
    private boolean initialized = false;

    Char087Provider(Logger logger) {
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

/**
 * Provider for character code 88 (X).
 * Knows exactly one thing and is very serious about it.
 */
final class Char088Provider extends AbstractCharacterProvider {
    private static final int CODE = 88;
    private static final String DISPLAY_NAME = "CHAR_088_X";
    private final Logger logger;
    private boolean initialized = false;

    Char088Provider(Logger logger) {
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

/**
 * Provider for character code 89 (Y).
 * Knows exactly one thing and is very serious about it.
 */
final class Char089Provider extends AbstractCharacterProvider {
    private static final int CODE = 89;
    private static final String DISPLAY_NAME = "CHAR_089_Y";
    private final Logger logger;
    private boolean initialized = false;

    Char089Provider(Logger logger) {
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

/**
 * Provider for character code 90 (Z).
 * Knows exactly one thing and is very serious about it.
 */
final class Char090Provider extends AbstractCharacterProvider {
    private static final int CODE = 90;
    private static final String DISPLAY_NAME = "CHAR_090_Z";
    private final Logger logger;
    private boolean initialized = false;

    Char090Provider(Logger logger) {
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

/**
 * Provider for character code 91 ([).
 * Knows exactly one thing and is very serious about it.
 */
final class Char091Provider extends AbstractCharacterProvider {
    private static final int CODE = 91;
    private static final String DISPLAY_NAME = "CHAR_091_[";
    private final Logger logger;
    private boolean initialized = false;

    Char091Provider(Logger logger) {
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

/**
 * Provider for character code 92 (BACKSLASH).
 * Knows exactly one thing and is very serious about it.
 */
final class Char092Provider extends AbstractCharacterProvider {
    private static final int CODE = 92;
    private static final String DISPLAY_NAME = "CHAR_092_BACKSLASH";
    private final Logger logger;
    private boolean initialized = false;

    Char092Provider(Logger logger) {
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

/**
 * Provider for character code 93 (]).
 * Knows exactly one thing and is very serious about it.
 */
final class Char093Provider extends AbstractCharacterProvider {
    private static final int CODE = 93;
    private static final String DISPLAY_NAME = "CHAR_093_]";
    private final Logger logger;
    private boolean initialized = false;

    Char093Provider(Logger logger) {
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

/**
 * Provider for character code 94 (^).
 * Knows exactly one thing and is very serious about it.
 */
final class Char094Provider extends AbstractCharacterProvider {
    private static final int CODE = 94;
    private static final String DISPLAY_NAME = "CHAR_094_^";
    private final Logger logger;
    private boolean initialized = false;

    Char094Provider(Logger logger) {
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

/**
 * Provider for character code 95 (_).
 * Knows exactly one thing and is very serious about it.
 */
final class Char095Provider extends AbstractCharacterProvider {
    private static final int CODE = 95;
    private static final String DISPLAY_NAME = "CHAR_095__";
    private final Logger logger;
    private boolean initialized = false;

    Char095Provider(Logger logger) {
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

/**
 * Provider for character code 96 (`).
 * Knows exactly one thing and is very serious about it.
 */
final class Char096Provider extends AbstractCharacterProvider {
    private static final int CODE = 96;
    private static final String DISPLAY_NAME = "CHAR_096_`";
    private final Logger logger;
    private boolean initialized = false;

    Char096Provider(Logger logger) {
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

/**
 * Provider for character code 97 (a).
 * Knows exactly one thing and is very serious about it.
 */
final class Char097Provider extends AbstractCharacterProvider {
    private static final int CODE = 97;
    private static final String DISPLAY_NAME = "CHAR_097_a";
    private final Logger logger;
    private boolean initialized = false;

    Char097Provider(Logger logger) {
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

/**
 * Provider for character code 98 (b).
 * Knows exactly one thing and is very serious about it.
 */
final class Char098Provider extends AbstractCharacterProvider {
    private static final int CODE = 98;
    private static final String DISPLAY_NAME = "CHAR_098_b";
    private final Logger logger;
    private boolean initialized = false;

    Char098Provider(Logger logger) {
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

/**
 * Provider for character code 99 (c).
 * Knows exactly one thing and is very serious about it.
 */
final class Char099Provider extends AbstractCharacterProvider {
    private static final int CODE = 99;
    private static final String DISPLAY_NAME = "CHAR_099_c";
    private final Logger logger;
    private boolean initialized = false;

    Char099Provider(Logger logger) {
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

/**
 * Provider for character code 100 (d).
 * Knows exactly one thing and is very serious about it.
 */
final class Char100Provider extends AbstractCharacterProvider {
    private static final int CODE = 100;
    private static final String DISPLAY_NAME = "CHAR_100_d";
    private final Logger logger;
    private boolean initialized = false;

    Char100Provider(Logger logger) {
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

/**
 * Provider for character code 101 (e).
 * Knows exactly one thing and is very serious about it.
 */
final class Char101Provider extends AbstractCharacterProvider {
    private static final int CODE = 101;
    private static final String DISPLAY_NAME = "CHAR_101_e";
    private final Logger logger;
    private boolean initialized = false;

    Char101Provider(Logger logger) {
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

/**
 * Provider for character code 102 (f).
 * Knows exactly one thing and is very serious about it.
 */
final class Char102Provider extends AbstractCharacterProvider {
    private static final int CODE = 102;
    private static final String DISPLAY_NAME = "CHAR_102_f";
    private final Logger logger;
    private boolean initialized = false;

    Char102Provider(Logger logger) {
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

/**
 * Provider for character code 103 (g).
 * Knows exactly one thing and is very serious about it.
 */
final class Char103Provider extends AbstractCharacterProvider {
    private static final int CODE = 103;
    private static final String DISPLAY_NAME = "CHAR_103_g";
    private final Logger logger;
    private boolean initialized = false;

    Char103Provider(Logger logger) {
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

/**
 * Provider for character code 104 (h).
 * Knows exactly one thing and is very serious about it.
 */
final class Char104Provider extends AbstractCharacterProvider {
    private static final int CODE = 104;
    private static final String DISPLAY_NAME = "CHAR_104_h";
    private final Logger logger;
    private boolean initialized = false;

    Char104Provider(Logger logger) {
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

/**
 * Provider for character code 105 (i).
 * Knows exactly one thing and is very serious about it.
 */
final class Char105Provider extends AbstractCharacterProvider {
    private static final int CODE = 105;
    private static final String DISPLAY_NAME = "CHAR_105_i";
    private final Logger logger;
    private boolean initialized = false;

    Char105Provider(Logger logger) {
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

/**
 * Provider for character code 106 (j).
 * Knows exactly one thing and is very serious about it.
 */
final class Char106Provider extends AbstractCharacterProvider {
    private static final int CODE = 106;
    private static final String DISPLAY_NAME = "CHAR_106_j";
    private final Logger logger;
    private boolean initialized = false;

    Char106Provider(Logger logger) {
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

/**
 * Provider for character code 107 (k).
 * Knows exactly one thing and is very serious about it.
 */
final class Char107Provider extends AbstractCharacterProvider {
    private static final int CODE = 107;
    private static final String DISPLAY_NAME = "CHAR_107_k";
    private final Logger logger;
    private boolean initialized = false;

    Char107Provider(Logger logger) {
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

/**
 * Provider for character code 108 (l).
 * Knows exactly one thing and is very serious about it.
 */
final class Char108Provider extends AbstractCharacterProvider {
    private static final int CODE = 108;
    private static final String DISPLAY_NAME = "CHAR_108_l";
    private final Logger logger;
    private boolean initialized = false;

    Char108Provider(Logger logger) {
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

/**
 * Provider for character code 109 (m).
 * Knows exactly one thing and is very serious about it.
 */
final class Char109Provider extends AbstractCharacterProvider {
    private static final int CODE = 109;
    private static final String DISPLAY_NAME = "CHAR_109_m";
    private final Logger logger;
    private boolean initialized = false;

    Char109Provider(Logger logger) {
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

/**
 * Provider for character code 110 (n).
 * Knows exactly one thing and is very serious about it.
 */
final class Char110Provider extends AbstractCharacterProvider {
    private static final int CODE = 110;
    private static final String DISPLAY_NAME = "CHAR_110_n";
    private final Logger logger;
    private boolean initialized = false;

    Char110Provider(Logger logger) {
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

/**
 * Provider for character code 111 (o).
 * Knows exactly one thing and is very serious about it.
 */
final class Char111Provider extends AbstractCharacterProvider {
    private static final int CODE = 111;
    private static final String DISPLAY_NAME = "CHAR_111_o";
    private final Logger logger;
    private boolean initialized = false;

    Char111Provider(Logger logger) {
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

/**
 * Provider for character code 112 (p).
 * Knows exactly one thing and is very serious about it.
 */
final class Char112Provider extends AbstractCharacterProvider {
    private static final int CODE = 112;
    private static final String DISPLAY_NAME = "CHAR_112_p";
    private final Logger logger;
    private boolean initialized = false;

    Char112Provider(Logger logger) {
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

/**
 * Provider for character code 113 (q).
 * Knows exactly one thing and is very serious about it.
 */
final class Char113Provider extends AbstractCharacterProvider {
    private static final int CODE = 113;
    private static final String DISPLAY_NAME = "CHAR_113_q";
    private final Logger logger;
    private boolean initialized = false;

    Char113Provider(Logger logger) {
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

/**
 * Provider for character code 114 (r).
 * Knows exactly one thing and is very serious about it.
 */
final class Char114Provider extends AbstractCharacterProvider {
    private static final int CODE = 114;
    private static final String DISPLAY_NAME = "CHAR_114_r";
    private final Logger logger;
    private boolean initialized = false;

    Char114Provider(Logger logger) {
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

/**
 * Provider for character code 115 (s).
 * Knows exactly one thing and is very serious about it.
 */
final class Char115Provider extends AbstractCharacterProvider {
    private static final int CODE = 115;
    private static final String DISPLAY_NAME = "CHAR_115_s";
    private final Logger logger;
    private boolean initialized = false;

    Char115Provider(Logger logger) {
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

/**
 * Provider for character code 116 (t).
 * Knows exactly one thing and is very serious about it.
 */
final class Char116Provider extends AbstractCharacterProvider {
    private static final int CODE = 116;
    private static final String DISPLAY_NAME = "CHAR_116_t";
    private final Logger logger;
    private boolean initialized = false;

    Char116Provider(Logger logger) {
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

/**
 * Provider for character code 117 (u).
 * Knows exactly one thing and is very serious about it.
 */
final class Char117Provider extends AbstractCharacterProvider {
    private static final int CODE = 117;
    private static final String DISPLAY_NAME = "CHAR_117_u";
    private final Logger logger;
    private boolean initialized = false;

    Char117Provider(Logger logger) {
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

/**
 * Provider for character code 118 (v).
 * Knows exactly one thing and is very serious about it.
 */
final class Char118Provider extends AbstractCharacterProvider {
    private static final int CODE = 118;
    private static final String DISPLAY_NAME = "CHAR_118_v";
    private final Logger logger;
    private boolean initialized = false;

    Char118Provider(Logger logger) {
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

/**
 * Provider for character code 119 (w).
 * Knows exactly one thing and is very serious about it.
 */
final class Char119Provider extends AbstractCharacterProvider {
    private static final int CODE = 119;
    private static final String DISPLAY_NAME = "CHAR_119_w";
    private final Logger logger;
    private boolean initialized = false;

    Char119Provider(Logger logger) {
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

/**
 * Provider for character code 120 (x).
 * Knows exactly one thing and is very serious about it.
 */
final class Char120Provider extends AbstractCharacterProvider {
    private static final int CODE = 120;
    private static final String DISPLAY_NAME = "CHAR_120_x";
    private final Logger logger;
    private boolean initialized = false;

    Char120Provider(Logger logger) {
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

/**
 * Provider for character code 121 (y).
 * Knows exactly one thing and is very serious about it.
 */
final class Char121Provider extends AbstractCharacterProvider {
    private static final int CODE = 121;
    private static final String DISPLAY_NAME = "CHAR_121_y";
    private final Logger logger;
    private boolean initialized = false;

    Char121Provider(Logger logger) {
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

/**
 * Provider for character code 122 (z).
 * Knows exactly one thing and is very serious about it.
 */
final class Char122Provider extends AbstractCharacterProvider {
    private static final int CODE = 122;
    private static final String DISPLAY_NAME = "CHAR_122_z";
    private final Logger logger;
    private boolean initialized = false;

    Char122Provider(Logger logger) {
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

/**
 * Provider for character code 123 ({).
 * Knows exactly one thing and is very serious about it.
 */
final class Char123Provider extends AbstractCharacterProvider {
    private static final int CODE = 123;
    private static final String DISPLAY_NAME = "CHAR_123_{";
    private final Logger logger;
    private boolean initialized = false;

    Char123Provider(Logger logger) {
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

/**
 * Provider for character code 124 (|).
 * Knows exactly one thing and is very serious about it.
 */
final class Char124Provider extends AbstractCharacterProvider {
    private static final int CODE = 124;
    private static final String DISPLAY_NAME = "CHAR_124_|";
    private final Logger logger;
    private boolean initialized = false;

    Char124Provider(Logger logger) {
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

/**
 * Provider for character code 125 (}).
 * Knows exactly one thing and is very serious about it.
 */
final class Char125Provider extends AbstractCharacterProvider {
    private static final int CODE = 125;
    private static final String DISPLAY_NAME = "CHAR_125_}";
    private final Logger logger;
    private boolean initialized = false;

    Char125Provider(Logger logger) {
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

/**
 * Provider for character code 126 (~).
 * Knows exactly one thing and is very serious about it.
 */
final class Char126Provider extends AbstractCharacterProvider {
    private static final int CODE = 126;
    private static final String DISPLAY_NAME = "CHAR_126_~";
    private final Logger logger;
    private boolean initialized = false;

    Char126Provider(Logger logger) {
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

/** Pipeline stage 01. Carefully copies the list and changes nothing. */
final class PipelineStage01 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage01(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-01";
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

/** Pipeline stage 02. Carefully copies the list and changes nothing. */
final class PipelineStage02 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage02(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-02";
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

/** Pipeline stage 03. Carefully copies the list and changes nothing. */
final class PipelineStage03 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage03(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-03";
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

/** Pipeline stage 04. Carefully copies the list and changes nothing. */
final class PipelineStage04 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage04(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-04";
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

/** Pipeline stage 05. Carefully copies the list and changes nothing. */
final class PipelineStage05 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage05(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-05";
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

/** Pipeline stage 06. Carefully copies the list and changes nothing. */
final class PipelineStage06 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage06(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-06";
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

/** Pipeline stage 07. Carefully copies the list and changes nothing. */
final class PipelineStage07 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage07(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-07";
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

/** Pipeline stage 08. Carefully copies the list and changes nothing. */
final class PipelineStage08 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage08(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-08";
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

/** Pipeline stage 09. Carefully copies the list and changes nothing. */
final class PipelineStage09 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage09(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-09";
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

/** Pipeline stage 10. Carefully copies the list and changes nothing. */
final class PipelineStage10 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage10(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-10";
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

/** Pipeline stage 11. Carefully copies the list and changes nothing. */
final class PipelineStage11 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage11(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-11";
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

/** Pipeline stage 12. Carefully copies the list and changes nothing. */
final class PipelineStage12 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage12(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-12";
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

/** Pipeline stage 13. Carefully copies the list and changes nothing. */
final class PipelineStage13 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage13(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-13";
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

/** Pipeline stage 14. Carefully copies the list and changes nothing. */
final class PipelineStage14 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage14(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-14";
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

/** Pipeline stage 15. Carefully copies the list and changes nothing. */
final class PipelineStage15 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage15(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-15";
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

/** Pipeline stage 16. Carefully copies the list and changes nothing. */
final class PipelineStage16 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage16(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-16";
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

/** Pipeline stage 17. Carefully copies the list and changes nothing. */
final class PipelineStage17 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage17(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-17";
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

/** Pipeline stage 18. Carefully copies the list and changes nothing. */
final class PipelineStage18 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage18(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-18";
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

/** Pipeline stage 19. Carefully copies the list and changes nothing. */
final class PipelineStage19 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage19(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-19";
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

/** Pipeline stage 20. Carefully copies the list and changes nothing. */
final class PipelineStage20 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage20(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-20";
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

/** Pipeline stage 21. Carefully copies the list and changes nothing. */
final class PipelineStage21 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage21(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-21";
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

/** Pipeline stage 22. Carefully copies the list and changes nothing. */
final class PipelineStage22 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage22(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-22";
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

/** Pipeline stage 23. Carefully copies the list and changes nothing. */
final class PipelineStage23 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage23(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-23";
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

/** Pipeline stage 24. Carefully copies the list and changes nothing. */
final class PipelineStage24 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage24(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-24";
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

/** Pipeline stage 25. Carefully copies the list and changes nothing. */
final class PipelineStage25 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage25(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-25";
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

/** Pipeline stage 26. Carefully copies the list and changes nothing. */
final class PipelineStage26 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage26(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-26";
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

/** Pipeline stage 27. Carefully copies the list and changes nothing. */
final class PipelineStage27 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage27(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-27";
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

/** Pipeline stage 28. Carefully copies the list and changes nothing. */
final class PipelineStage28 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage28(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-28";
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

/** Pipeline stage 29. Carefully copies the list and changes nothing. */
final class PipelineStage29 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage29(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-29";
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

/** Pipeline stage 30. Carefully copies the list and changes nothing. */
final class PipelineStage30 implements PipelineStage {
    private final EventBus eventBus;

    PipelineStage30(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public String id() {
        return "stage-30";
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

/** Registers every provider. Generated. */
final class GeneratedRegistryPopulator {
    private GeneratedRegistryPopulator() {
    }

    static void populate(CharacterProviderRegistry registry, Logger logger) {
        registry.register(32, () -> new Char032Provider(logger));
        registry.register(33, () -> new Char033Provider(logger));
        registry.register(34, () -> new Char034Provider(logger));
        registry.register(35, () -> new Char035Provider(logger));
        registry.register(36, () -> new Char036Provider(logger));
        registry.register(37, () -> new Char037Provider(logger));
        registry.register(38, () -> new Char038Provider(logger));
        registry.register(39, () -> new Char039Provider(logger));
        registry.register(40, () -> new Char040Provider(logger));
        registry.register(41, () -> new Char041Provider(logger));
        registry.register(42, () -> new Char042Provider(logger));
        registry.register(43, () -> new Char043Provider(logger));
        registry.register(44, () -> new Char044Provider(logger));
        registry.register(45, () -> new Char045Provider(logger));
        registry.register(46, () -> new Char046Provider(logger));
        registry.register(47, () -> new Char047Provider(logger));
        registry.register(48, () -> new Char048Provider(logger));
        registry.register(49, () -> new Char049Provider(logger));
        registry.register(50, () -> new Char050Provider(logger));
        registry.register(51, () -> new Char051Provider(logger));
        registry.register(52, () -> new Char052Provider(logger));
        registry.register(53, () -> new Char053Provider(logger));
        registry.register(54, () -> new Char054Provider(logger));
        registry.register(55, () -> new Char055Provider(logger));
        registry.register(56, () -> new Char056Provider(logger));
        registry.register(57, () -> new Char057Provider(logger));
        registry.register(58, () -> new Char058Provider(logger));
        registry.register(59, () -> new Char059Provider(logger));
        registry.register(60, () -> new Char060Provider(logger));
        registry.register(61, () -> new Char061Provider(logger));
        registry.register(62, () -> new Char062Provider(logger));
        registry.register(63, () -> new Char063Provider(logger));
        registry.register(64, () -> new Char064Provider(logger));
        registry.register(65, () -> new Char065Provider(logger));
        registry.register(66, () -> new Char066Provider(logger));
        registry.register(67, () -> new Char067Provider(logger));
        registry.register(68, () -> new Char068Provider(logger));
        registry.register(69, () -> new Char069Provider(logger));
        registry.register(70, () -> new Char070Provider(logger));
        registry.register(71, () -> new Char071Provider(logger));
        registry.register(72, () -> new Char072Provider(logger));
        registry.register(73, () -> new Char073Provider(logger));
        registry.register(74, () -> new Char074Provider(logger));
        registry.register(75, () -> new Char075Provider(logger));
        registry.register(76, () -> new Char076Provider(logger));
        registry.register(77, () -> new Char077Provider(logger));
        registry.register(78, () -> new Char078Provider(logger));
        registry.register(79, () -> new Char079Provider(logger));
        registry.register(80, () -> new Char080Provider(logger));
        registry.register(81, () -> new Char081Provider(logger));
        registry.register(82, () -> new Char082Provider(logger));
        registry.register(83, () -> new Char083Provider(logger));
        registry.register(84, () -> new Char084Provider(logger));
        registry.register(85, () -> new Char085Provider(logger));
        registry.register(86, () -> new Char086Provider(logger));
        registry.register(87, () -> new Char087Provider(logger));
        registry.register(88, () -> new Char088Provider(logger));
        registry.register(89, () -> new Char089Provider(logger));
        registry.register(90, () -> new Char090Provider(logger));
        registry.register(91, () -> new Char091Provider(logger));
        registry.register(92, () -> new Char092Provider(logger));
        registry.register(93, () -> new Char093Provider(logger));
        registry.register(94, () -> new Char094Provider(logger));
        registry.register(95, () -> new Char095Provider(logger));
        registry.register(96, () -> new Char096Provider(logger));
        registry.register(97, () -> new Char097Provider(logger));
        registry.register(98, () -> new Char098Provider(logger));
        registry.register(99, () -> new Char099Provider(logger));
        registry.register(100, () -> new Char100Provider(logger));
        registry.register(101, () -> new Char101Provider(logger));
        registry.register(102, () -> new Char102Provider(logger));
        registry.register(103, () -> new Char103Provider(logger));
        registry.register(104, () -> new Char104Provider(logger));
        registry.register(105, () -> new Char105Provider(logger));
        registry.register(106, () -> new Char106Provider(logger));
        registry.register(107, () -> new Char107Provider(logger));
        registry.register(108, () -> new Char108Provider(logger));
        registry.register(109, () -> new Char109Provider(logger));
        registry.register(110, () -> new Char110Provider(logger));
        registry.register(111, () -> new Char111Provider(logger));
        registry.register(112, () -> new Char112Provider(logger));
        registry.register(113, () -> new Char113Provider(logger));
        registry.register(114, () -> new Char114Provider(logger));
        registry.register(115, () -> new Char115Provider(logger));
        registry.register(116, () -> new Char116Provider(logger));
        registry.register(117, () -> new Char117Provider(logger));
        registry.register(118, () -> new Char118Provider(logger));
        registry.register(119, () -> new Char119Provider(logger));
        registry.register(120, () -> new Char120Provider(logger));
        registry.register(121, () -> new Char121Provider(logger));
        registry.register(122, () -> new Char122Provider(logger));
        registry.register(123, () -> new Char123Provider(logger));
        registry.register(124, () -> new Char124Provider(logger));
        registry.register(125, () -> new Char125Provider(logger));
        registry.register(126, () -> new Char126Provider(logger));
    }
}

/** Assembles the 30 stage pipeline. Generated. */
final class GeneratedPipelineAssembler {
    private GeneratedPipelineAssembler() {
    }

    static Pipeline assemble(EventBus eventBus) {
        List<PipelineStage> stages = new ArrayList<>();
        stages.add(new PipelineStage01(eventBus));
        stages.add(new PipelineStage02(eventBus));
        stages.add(new PipelineStage03(eventBus));
        stages.add(new PipelineStage04(eventBus));
        stages.add(new PipelineStage05(eventBus));
        stages.add(new PipelineStage06(eventBus));
        stages.add(new PipelineStage07(eventBus));
        stages.add(new PipelineStage08(eventBus));
        stages.add(new PipelineStage09(eventBus));
        stages.add(new PipelineStage10(eventBus));
        stages.add(new PipelineStage11(eventBus));
        stages.add(new PipelineStage12(eventBus));
        stages.add(new PipelineStage13(eventBus));
        stages.add(new PipelineStage14(eventBus));
        stages.add(new PipelineStage15(eventBus));
        stages.add(new PipelineStage16(eventBus));
        stages.add(new PipelineStage17(eventBus));
        stages.add(new PipelineStage18(eventBus));
        stages.add(new PipelineStage19(eventBus));
        stages.add(new PipelineStage20(eventBus));
        stages.add(new PipelineStage21(eventBus));
        stages.add(new PipelineStage22(eventBus));
        stages.add(new PipelineStage23(eventBus));
        stages.add(new PipelineStage24(eventBus));
        stages.add(new PipelineStage25(eventBus));
        stages.add(new PipelineStage26(eventBus));
        stages.add(new PipelineStage27(eventBus));
        stages.add(new PipelineStage28(eventBus));
        stages.add(new PipelineStage29(eventBus));
        stages.add(new PipelineStage30(eventBus));
        return new Pipeline(stages);
    }
}
