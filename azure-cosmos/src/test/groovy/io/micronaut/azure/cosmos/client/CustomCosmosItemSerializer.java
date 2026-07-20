package io.micronaut.azure.cosmos.client;

import com.azure.cosmos.CosmosItemSerializer;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Singleton
final class CustomCosmosItemSerializer extends CosmosItemSerializer {

    private final List<Object> serializedItems = new ArrayList<>();
    private final List<Object> deserializedItems = new ArrayList<>();

    @Override
    public <T> Map<String, Object> serialize(T item) {
        serializedItems.add(item);
        return DEFAULT_SERIALIZER.serialize(item);
    }

    @Override
    public <T> T deserialize(Map<String, Object> jsonNodeMap, Class<T> classType) {
        T result = DEFAULT_SERIALIZER.deserialize(jsonNodeMap, classType);
        deserializedItems.add(result);
        return result;
    }

    public List<Object> getSerializedItems() {
        return Collections.unmodifiableList(serializedItems);
    }

    public List<Object> getDeserializedItems() {
        return Collections.unmodifiableList(deserializedItems);
    }
}
