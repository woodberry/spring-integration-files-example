package au.net.woodberry.files.pcf.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

import java.math.BigDecimal;
import java.math.BigInteger;

@Record(minOccurs = 1)
@NoArgsConstructor
@Data
public class PortfolioComposition {

    @Field(at = 0, required = true)
    private String ticker;

    @Field(at = 1, required = true)
    private String name;

    @Field(at = 2, required = true)
    private String assetClass;

    @Field(at = 3, required = true, format = "#,#.#")
    private BigDecimal weight;

    @Field(at = 4, required = true, format = "#,#.#")
    private BigDecimal price;

    @Field(at = 5, required = true, format = "#,#.#")
    private BigInteger shares;

    @Field(at = 6, required = true, format = "#,#.#")
    private BigDecimal marketValue;

    @Field(at = 7, required = true, format = "#,#.#")
    private BigDecimal notionalValue;

    @Field(at = 8, required = true)
    private String sector;

    @Field(at = 9, handlerClass = EmptyFieldTypeHandler.class, format = "-")
    private String ISIN;

    @Field(at = 10, handlerClass = EmptyFieldTypeHandler.class, format = "-")
    private String CUSIP;

    @Field(at = 11, handlerClass = EmptyFieldTypeHandler.class, format = "-")
    private String exchange;

    @Field(at = 12, handlerClass = EmptyFieldTypeHandler.class, format = "-")
    private String country;

    @Field(at = 13, required = true)
    private String currency;

    @Field(at = 14, required = true)
    private String marketCurrency;
}
