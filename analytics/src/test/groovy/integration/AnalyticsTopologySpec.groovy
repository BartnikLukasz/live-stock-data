package integration

import model.StockTrade
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.bartnik.analytics.config.AnalyticsStreamsConfig
import org.bartnik.analytics.model.TradeAggregate
import org.bartnik.analytics.util.TradeSerdes
import spock.lang.Specification

class AnalyticsTopologySpec extends Specification {

    TopologyTestDriver driver
    TestInputTopic<String, StockTrade> inputTopic
    TestOutputTopic<String, TradeAggregate> outputTopic

    def setup() {
        def builder = new StreamsBuilder()
        def config = new AnalyticsStreamsConfig().vwapStream(builder)
        def topology = builder.build()

        def props = new Properties()
        props.put("application.id", "test-analytics")
        props.put("bootstrap.servers", "dummy:1234")

        driver = new TopologyTestDriver(topology, props)

        def tradeSerde = TradeSerdes.stockTradeSerde()
        def aggSerde = TradeSerdes.tradeAggregateSerde()

        inputTopic = driver.createInputTopic("ticker-raw", Serdes.String().serializer(), tradeSerde.serializer())
        outputTopic = driver.createOutputTopic("ticker-analytics", Serdes.String().deserializer(), aggSerde.deserializer())
    }

    def cleanup() {
        driver.close()
    }

    def "should process stream and output aggregated VWAP"() {
        given:
        def trade = new StockTrade("AAPL", new BigDecimal("150.00"), System.currentTimeMillis(), new BigDecimal("10"))

        when:
        inputTopic.pipeInput("AAPL", trade)

        then:
        def result = outputTopic.readRecord()
        result.key() == "AAPL"
        result.value().totalVolume().compareTo(new BigDecimal("10")) == 0
        result.value().vwap().compareTo(new BigDecimal("150.00")) == 0
    }
}