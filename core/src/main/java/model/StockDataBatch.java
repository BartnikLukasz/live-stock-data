package model;

import java.util.List;

public record StockDataBatch(
        String type,
        List<StockTrade> data
) {
}
