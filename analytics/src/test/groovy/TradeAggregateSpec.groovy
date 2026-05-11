import model.StockTrade
import model.TradeAggregate
import spock.lang.Specification
import spock.lang.Unroll

class TradeAggregateSpec extends Specification {

    @Unroll
    def "should calculate correct VWAP for #symbol with multiple trades"() {
        given:
        def aggregate = new TradeAggregate(symbol)

        when:
        trades.each { trade ->
            aggregate = aggregate.add(trade)
        }

        then:
        aggregate.vwap().compareTo(expectedVwap) == 0
        aggregate.count == trades.size()

        where:
        symbol = "BTC-USD"
        trades                  | expectedVwap
        [t(100, 2), t(200, 1)]  | 133.33333333
        [t(50.5, 10), t(60, 5)] | 53.66666667
        [t(0.01, 1)]            | 0.01000000
    }

    private static StockTrade t(double price, double volume) {
        return new StockTrade("BTC-USD", new BigDecimal(price), System.currentTimeMillis(), new BigDecimal(volume))
    }
}