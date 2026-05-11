import org.bartnik.ingestor.client.FinnhubClient
import spock.lang.Specification
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper
import org.springframework.web.socket.TextMessage

class FinnhubClientSpec extends Specification {

    KafkaTemplate kafkaTemplate = Mock()
    JsonMapper jsonMapper = new JsonMapper()

    FinnhubClient client

    def setup() {
        client = new FinnhubClient(kafkaTemplate, jsonMapper)
    }

    def "should parse valid trade message and send to kafka"() {
        given:
        def payload = """
        {
          "data": [{"p": 72000.5, "s": "BINANCE:BTCUSDT", "t": 1775725630, "v": 0.01}],
          "type": "trade"
        }
        """
        def message = new TextMessage(payload)

        when:
        client.handleTextMessage(null, message)

        then:
        1 * kafkaTemplate.send("ticker-raw", "BINANCE:BTCUSDT", _)
    }

    def "should ignore messages that are not of type 'trade'"() {
        given:
        def payload = '{"type":"ping"}'
        def message = new TextMessage(payload)

        when:
        client.handleTextMessage(null, message)

        then:
        0 * kafkaTemplate.send(*_)
    }
}