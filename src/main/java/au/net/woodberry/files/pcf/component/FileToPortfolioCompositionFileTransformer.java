package au.net.woodberry.files.pcf.component;

import au.net.woodberry.files.pcf.domain.EmptyFieldTypeHandler;
import au.net.woodberry.files.pcf.domain.PortfolioComposition;
import au.net.woodberry.files.pcf.exception.CsvFileException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.beanio.BeanReaderException;
import org.beanio.InvalidRecordException;
import org.beanio.InvalidRecordGroupException;
import org.beanio.MalformedRecordException;
import org.beanio.StreamFactory;
import org.beanio.builder.CsvParserBuilder;
import org.beanio.builder.StreamBuilder;
import org.beanio.types.BigDecimalTypeHandler;
import org.beanio.types.BigIntegerTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FileToPortfolioCompositionFileTransformer implements GenericTransformer<File, List<PortfolioComposition>> {

    private StreamFactory streamFactory;

    @Autowired
    private MessageChannel outboundInvalidMessageChannel;

    @PostConstruct
    public void postConstruct() {
        this.streamFactory = StreamFactory.newInstance();
        val builder = new StreamBuilder("pcfFileStream")
                .format("csv")
                .parser(new CsvParserBuilder()
                        .quote('"')
                        .allowUnquotedQuotes())
                .addTypeHandler(EmptyFieldTypeHandler.class.getName(), String.class, new EmptyFieldTypeHandler())
                .addTypeHandler(BigDecimalTypeHandler.class.getName(), BigDecimal.class, new BigDecimalTypeHandler())
                .addTypeHandler(BigIntegerTypeHandler.class.getName(), BigInteger.class, new BigIntegerTypeHandler())
                .addRecord(PortfolioComposition.class)
                .ignoreUnidentifiedRecords();
        this.streamFactory.define(builder);
    }

    @Override
    public List<PortfolioComposition> transform(File source) {
        try {
            val beanReader = streamFactory.createReader("pcfFileStream", source);
            beanReader.setErrorHandler(this::handleError);

            val portfolioCompositions = new ArrayList<PortfolioComposition>();
            PortfolioComposition portfolioComposition;
            while ((portfolioComposition = (PortfolioComposition) beanReader.read()) != null) {
                portfolioCompositions.add(portfolioComposition);
            }
            beanReader.close();
            return portfolioCompositions;
        } catch (Exception ex) {
            outboundInvalidMessageChannel.send(MessageBuilder.withPayload(new CsvFileException(ex)).build());
            return null;
        }
    }

    private void handleError(BeanReaderException ex) {
        if (ex instanceof InvalidRecordException) {
            outboundInvalidMessageChannel.send(MessageBuilder.withPayload(new CsvFileException(ex)).build());
            return;
        }
        if (ex instanceof MalformedRecordException) {
            outboundInvalidMessageChannel.send(MessageBuilder.withPayload(new CsvFileException(ex)).build());
            return;
        }
        if (ex instanceof InvalidRecordGroupException) {
            outboundInvalidMessageChannel.send(MessageBuilder.withPayload(new CsvFileException(ex)).build());
            return;
        }
        throw new CsvFileException(ex);
    }
}
