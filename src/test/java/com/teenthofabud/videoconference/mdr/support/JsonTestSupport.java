package com.teenthofabud.videoconference.mdr.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.StringReader;
import java.util.List;

/**
 * Produces request and document bodies from the real POJOs rather than from hand written JSON, so
 * a renamed or re-annotated property breaks the test at compile time (or through the mapper) instead
 * of silently sending a body the application no longer understands.
 */
public final class JsonTestSupport {

    /** Leaves unset properties out, the way a client that never populated them would. */
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();

    /** Writes unset properties out as explicit nulls. */
    private static final JsonMapper MAPPER_WITH_NULLS = JsonMapper.builder().build();

    private JsonTestSupport() {
    }

    public static String json(Object body) {
        return MAPPER.writeValueAsString(body);
    }

    public static String jsonWithNulls(Object body) {
        return MAPPER_WITH_NULLS.writeValueAsString(body);
    }

    /**
     * A strict prefix of a JSON object or array can never itself be a complete document (its
     * closing bracket is always the last character), so cutting a real body in half yields a
     * malformed one without having to write malformed JSON by hand.
     */
    public static String truncated(String json) {
        return json.substring(0, json.length() / 2);
    }

    /** JSON-P view of a POJO, built the same way the service builds the document it patches. */
    public static JsonObject jsonObject(Object pojo) {
        try (JsonReader reader = Json.createReader(new StringReader(json(pojo)))) {
            return reader.readObject();
        }
    }

    /** One RFC 6902 operation as sent over the wire; {@code value} is omitted when null. */
    public record PatchOperation(String op, String path, Object value) {

        public static PatchOperation replace(String path, Object value) {
            return new PatchOperation("replace", path, value);
        }

        public static PatchOperation test(String path, Object value) {
            return new PatchOperation("test", path, value);
        }

        public static PatchOperation operation(String op, String path) {
            return new PatchOperation(op, path, null);
        }
    }

    /** A patch document: the JSON array of the given operations. */
    public static String patchBody(PatchOperation... operations) {
        return json(List.of(operations));
    }
}
