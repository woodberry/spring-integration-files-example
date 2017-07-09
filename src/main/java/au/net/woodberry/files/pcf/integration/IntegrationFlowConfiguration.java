package au.net.woodberry.files.pcf.integration;

import au.net.woodberry.files.pcf.component.FileToPortfolioCompositionFileTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableIntegration
public class IntegrationFlowConfiguration {

    @Autowired
    private ConfigProps configProps;

    @Bean
    public MessageChannel fileOutboundChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    @Bean
    public IntegrationFlow inboundIntegrationFlow(FileReadingMessageSource messageSource,
                                                  MessageChannel fileOutboundChannel,
                                                  FileToPortfolioCompositionFileTransformer pcfTransformer) {
        return IntegrationFlows.from(messageSource, poll -> poll.poller(fixedDelayPoller()))
                .transform(pcfTransformer)
                .split()
                .channel(fileOutboundChannel)
                .get();
    }

    @Bean
    public IntegrationFlow outboundIntegrationFlow(FileWritingMessageHandler fileWritingMessageHandler,

                                                   MessageChannel fileOutboundChannel) {
        return IntegrationFlows.from(fileOutboundChannel)
                .transform(new ObjectToStringTransformer())
                .handle(fileWritingMessageHandler)
                .get();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata fixedDelayPoller() {
        return Pollers.fixedDelay(1, TimeUnit.SECONDS).get();
    }

    @Bean
    public FileReadingMessageSource fileReadingMessageSource() {
        return Files.inboundAdapter(new File(configProps.getIncomingDirectory()))
                .useWatchService(true)
                .watchEvents(FileReadingMessageSource.WatchEventType.CREATE)
                .autoCreateDirectory(configProps.isAutoCreateDirectory())
                .get();
    }

    @Bean
    public FileWritingMessageHandler fileWritingMessageHandler() {
        return Files.outboundAdapter(new File(configProps.getOutgoingDirectory()))
                .autoCreateDirectory(true)
                .appendNewLine(true)
                .get();
    }
}
