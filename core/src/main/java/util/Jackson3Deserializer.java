package util;

import tools.jackson.databind.json.JsonMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class Jackson3Deserializer<T> implements Deserializer<T> {
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Class<T> targetType;

    public Jackson3Deserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        return mapper.readValue(data, targetType);
    }
}
