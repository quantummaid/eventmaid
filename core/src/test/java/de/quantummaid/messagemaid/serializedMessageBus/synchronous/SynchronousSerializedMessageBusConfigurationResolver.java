package de.quantummaid.messagemaid.serializedMessageBus.synchronous;

import de.quantummaid.messagemaid.serializedMessageBus.givenWhenThen.SerializedMessageBusTestConfig;
import de.quantummaid.messagemaid.shared.config.AbstractTestConfigProvider;

public class SynchronousSerializedMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return SerializedMessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return SerializedMessageBusTestConfig.synchronousMessageBusTestConfig();
    }
}
