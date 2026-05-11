package util;

import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;

public class Jackson3Serializer<T> implements Serializer<T> {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) return null;
        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Jackson 3 object", e);
        }
    }
}