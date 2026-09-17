package com.teenthofabud.videoconference.mdr.configuration;

import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

/**
 * Reads RFC 6902 patch documents into a {@link JsonPatch}.
 * <p>
 * {@link JsonPatch} is a Jakarta JSON Processing type rather than a Jackson one, so the default
 * Jackson converter cannot construct it. This converter parses the body with the JSON-P reader
 * instead, and is contributed to the converter stack by {@link JsonPatchConfiguration}.
 */
public class JsonPatchHttpMessageConverter extends AbstractHttpMessageConverter<JsonPatch> {

    public JsonPatchHttpMessageConverter() {
        super(MediaType.valueOf(HttpMediaType.APPLICATION_JSON_PATCH), MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JsonPatch.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        // patch documents are only ever read from requests, never written to responses
        return false;
    }

    @Override
    protected JsonPatch readInternal(Class<? extends JsonPatch> clazz, HttpInputMessage inputMessage) throws IOException {
        try (JsonReader reader = Json.createReader(inputMessage.getBody())) {
            JsonArray operations = reader.readArray();
            rejectUnknownOperations(operations);
            return Json.createPatch(operations);
        } catch (JsonException | ClassCastException | IllegalArgumentException e) {
            throw new HttpMessageNotReadableException(
                    String.format("Not a valid JSON patch document : %s", e.getMessage()), e, inputMessage);
        }
    }

    /**
     * {@code Json.createPatch} accepts any array and only complains when the patch is applied, so
     * an unusable document would reach the service and fail there. Checking the operation names up
     * front keeps the rejection at the request boundary.
     */
    private void rejectUnknownOperations(JsonArray operations) {
        operations.stream()
                .map(JsonValue::asJsonObject)
                .map(operation -> operation.getString("op", null))
                .forEach(JsonPatch.Operation::fromOperationName);
    }

    @Override
    protected void writeInternal(JsonPatch jsonPatch, HttpOutputMessage outputMessage) {
        throw new HttpMessageNotWritableException("JsonPatch is only supported as a request body");
    }
}
