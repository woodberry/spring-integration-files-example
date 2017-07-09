package au.net.woodberry.files.pcf.domain;

import org.beanio.types.ConfigurableTypeHandler;
import org.beanio.types.TypeConversionException;
import org.beanio.types.TypeHandler;

import java.util.Properties;

/**
 * Converts fields with a dash '-' to null.
 */
public class EmptyFieldTypeHandler implements ConfigurableTypeHandler {

    private String character;

    @Override
    public Object parse(String s) throws TypeConversionException {
        if (s != null) {
            return s.trim().equals(character) ? null : s;
        }
        return s;
    }

    @Override
    public String format(Object o) {
        return o != null ? o.toString() : null;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public TypeHandler newInstance(Properties properties) throws IllegalArgumentException {
        this.character = properties.getProperty(FORMAT_SETTING);
        return this;
    }
}
