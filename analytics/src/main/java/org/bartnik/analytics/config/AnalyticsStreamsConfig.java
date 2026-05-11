package org.bartnik.analytics.config;

import model.StockTrade;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import model.TradeAggregate;
import org.bartnik.analytics.util.TradeSerdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class AnalyticsStreamsConfig {

    @Bean
    public KStream<String, TradeAggregate> vwapStream(StreamsBuilder builder) {
        Serde<StockTrade> tradeSerde = TradeSerdes.stockTradeSerde();
        Serde<TradeAggregate> aggSerde = TradeSerdes.tradeAggregateSerde();

        KStream<String, TradeAggregate> analyticsStream = builder.stream("ticker-raw", Consumed.with(Serdes.String(), tradeSerde))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
                .aggregate(
                        () -> new TradeAggregate("PENDING"),
                        (key, trade, agg) -> agg.add(trade),
                        Materialized.<String, TradeAggregate, WindowStore<Bytes, byte[]>>as("vwap-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(TradeSerdes.tradeAggregateSerde())
                )
                .toStream()
                .map((windowedKey, agg) -> KeyValue.pair(windowedKey.key(), agg));

        analyticsStream.to("ticker-analytics", Produced.with(Serdes.String(), aggSerde));

        return analyticsStream;
    }
}
