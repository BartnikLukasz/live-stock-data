package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record StockTrade(
        @JsonProperty("s") String symbol,
        @JsonProperty("p") BigDecimal price,
        @JsonProperty("t") Long timestamp,
        @JsonProperty("v") Double volume,
        @JsonProperty("c") String[] conditions
) {
}
