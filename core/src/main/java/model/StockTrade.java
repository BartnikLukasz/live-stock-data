package model;

import java.math.BigDecimal;

public record StockTrade(
        String symbol,
        BigDecimal price,
        Long timestamp,
        Double volume
) {
}
