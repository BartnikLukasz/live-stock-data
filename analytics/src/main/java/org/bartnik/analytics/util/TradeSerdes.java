package org.bartnik.analytics.util;

import model.StockTrade;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import model.TradeAggregate;
import util.Jackson3Deserializer;
import util.Jackson3Serializer;

public class TradeSerdes {

    public static Serde<TradeAggregate> tradeAggregateSerde() {

        Jackson3Serializer<TradeAggregate> serializer = new Jackson3Serializer<>();
        Jackson3Deserializer<TradeAggregate> deserializer = new Jackson3Deserializer<>(TradeAggregate.class);

        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<StockTrade> stockTradeSerde() {

        Jackson3Serializer<StockTrade> serializer = new Jackson3Serializer<>();
        Jackson3Deserializer<StockTrade> deserializer = new Jackson3Deserializer<>(StockTrade.class);

        return Serdes.serdeFrom(serializer, deserializer);
    }
}
