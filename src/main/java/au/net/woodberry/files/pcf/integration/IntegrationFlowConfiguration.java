package au.net.woodberry.files.pcf.integration;

import au.net.woodberry.files.pcf.component.FileToPortfolioCompositionFileTransformer;
import au.net.woodberry.files.pcf.exception.CsvFileException;
import au.net.woodberry.files.pcf.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.ErrorHandler;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableIntegration
@Slf4j
public class IntegrationFlowConfiguration {

    @Autowired
    private ConfigProps configProps;

    @Bean
    public MessageChannel outboundFileMessageChannel(ErrorHandler exceptionLoggingErrorHandler) {
        return MessageChannels.publishSubscribe().errorHandler(exceptionLoggingErrorHandler).get();
    }

    @Bean
    public MessageChannel outboundInvalidMessageChannel(ErrorHandler exceptionLoggingErrorHandler) {
        return MessageChannels.publishSubscribe().errorHandler(exceptionLoggingErrorHandler).get();
    }

    @Bean
    public ErrorHandler exceptionLoggingErrorHandler() {
        return ex -> log.error(LoggingUtils.throwableToMessage(ex), ex);
    }

    @Bean
    public IntegrationFlow inboundIntegrationFlow(FileReadingMessageSource messageSource,
                                                  MessageChannel outboundFileMessageChannel,
                                                  FileToPortfolioCompositionFileTransformer pcfTransformer) {
        return IntegrationFlows.from(messageSource, poll -> poll.poller(fixedDelayPoller()))
                .transform(pcfTransformer)
                .split()
                .channel(outboundFileMessageChannel)
                .get();
    }

    @Bean
    public IntegrationFlow outboundIntegrationFlow(FileWritingMessageHandler fileWritingMessageHandler,
                                                   MessageChannel outboundFileMessageChannel) {
        return IntegrationFlows.from(outboundFileMessageChannel)
                .transform(new ObjectToStringTransformer())
                .handle(fileWritingMessageHandler)
                .get();
    }

    @Bean
    public IntegrationFlow outboundErrorHandlingFlow(MessageChannel outboundInvalidMessageChannel,
                                                     FileWritingMessageHandler fileWritingErrorMessageHandler,
                                                     ErrorHandler exceptionLoggingErrorHandler) {
        return IntegrationFlows.from(outboundInvalidMessageChannel)
                .transform((GenericTransformer<CsvFileException, String>) source -> source.getMessage())
                .handle(fileWritingErrorMessageHandler)
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
                .autoCreateDirectory(true)
                .get();
    }

    @Bean
    public FileWritingMessageHandler fileWritingErrorMessageHandler() {
        return Files.outboundAdapter(new File(configProps.getOutgoingDirectoryErrors()))
                .autoCreateDirectory(true)
                .appendNewLine(true)
                .fileNameGenerator(message -> message.getHeaders().getId().toString()
                        + "-"
                        + Instant.now().toEpochMilli()
                        + "-error.csv")
                .get();
    }

    @Bean
    public FileWritingMessageHandler fileWritingMessageHandler() {
        return Files.outboundAdapter(new File(configProps.getOutgoingDirectory()))
                .autoCreateDirectory(true)
                .appendNewLine(true)
                .fileNameGenerator(message -> message.getHeaders().getId().toString()
                        + "-"
                        + Instant.now().toEpochMilli()
                        + ".csv")
                .get();
    }
}
