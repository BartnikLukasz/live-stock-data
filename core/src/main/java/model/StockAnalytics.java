package model;

import java.math.BigDecimal;
import java.time.Instant;

public record StockAnalytics(
        String symbol,
        BigDecimal averagePrice,
        BigDecimal movingAverage,
        Instant windowEnd,
        long tradeCount
) {
}
