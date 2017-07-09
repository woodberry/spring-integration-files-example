package au.net.woodberry.files.pcf.exception;

import org.beanio.BeanReaderException;

public class CsvFileException extends RuntimeException {

    public CsvFileException(BeanReaderException ex) {
        super(ex);
    }
}
