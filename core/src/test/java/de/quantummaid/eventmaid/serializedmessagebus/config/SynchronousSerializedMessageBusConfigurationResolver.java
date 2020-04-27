package de.quantummaid.eventmaid.serializedmessagebus.config;

import de.quantummaid.eventmaid.serializedmessagebus.givenwhenthen.SerializedMessageBusTestConfig;
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
