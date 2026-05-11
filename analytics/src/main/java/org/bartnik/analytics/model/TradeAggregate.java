package org.bartnik.analytics.model;

import model.StockTrade;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TradeAggregate(
        String symbol,
        BigDecimal totalVolume,
        BigDecimal weightedPriceSum,
        BigDecimal vwap,
        long count
) {
    private static final int SCALE = 8;

    public TradeAggregate(String symbol) {
        this(symbol, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }

    public TradeAggregate add(StockTrade trade) {
        BigDecimal newVolume = this.totalVolume.add(trade.volume());
        BigDecimal newWeightedSum = this.weightedPriceSum.add(trade.price().multiply(trade.volume()));

        BigDecimal newVwap = newVolume.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)
                : newWeightedSum.divide(newVolume, SCALE, RoundingMode.HALF_UP);

        return new TradeAggregate(trade.symbol(), newVolume, newWeightedSum, newVwap, this.count + 1);
    }
}