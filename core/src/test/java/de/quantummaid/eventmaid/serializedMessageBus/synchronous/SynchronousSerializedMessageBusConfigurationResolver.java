package de.quantummaid.eventmaid.serializedMessageBus.synchronous;

import de.quantummaid.eventmaid.serializedMessageBus.givenWhenThen.SerializedMessageBusTestConfig;
import de.quantummaid.eventmaid.shared.config.AbstractTestConfigProvider;

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
