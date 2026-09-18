package com.teenthofabud.videoconference.mdr.configuration;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonValue;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.operation;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.replace;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.json;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.jsonObject;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.patchBody;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.truncated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The Jackson converter Spring Boot installs by default cannot construct a {@link JsonPatch},
 * because that is a Jakarta JSON Processing interface with no Jackson creator. These cover the
 * JSON-P backed replacement and its registration.
 */
class JsonPatchHttpMessageConverterTest {

    private static final MediaType JSON_PATCH = MediaType.valueOf(HttpMediaType.APPLICATION_JSON_PATCH);

    private final JsonPatchHttpMessageConverter converter = new JsonPatchHttpMessageConverter();

    /** The document a patch is applied to in these tests. */
    private record Document(String status, String roomId) {
    }

    private HttpInputMessage body(String json) {
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }

    @Nested
    @DisplayName("media type and target type")
    class Supports {

        @Test
        @DisplayName("reads JsonPatch from application/json")
        void readsJsonPatchFromApplicationJson() {
            assertThat(converter.canRead(JsonPatch.class, MediaType.APPLICATION_JSON)).isTrue();
        }

        @Test
        @DisplayName("reads JsonPatch from the RFC 6902 media type")
        void readsJsonPatchFromJsonPatchMediaType() {
            assertThat(converter.canRead(JsonPatch.class, JSON_PATCH)).isTrue();
        }

        @Test
        @DisplayName("claims nothing but JsonPatch, so ordinary request bodies keep using Jackson 3")
        void doesNotClaimOtherTypes() {
            assertThat(converter.canRead(String.class, MediaType.APPLICATION_JSON)).isFalse();
            assertThat(converter.canRead(JsonObject.class, MediaType.APPLICATION_JSON)).isFalse();
        }

        @Test
        @DisplayName("never writes - patch documents are request bodies only")
        void neverWrites() {
            assertThat(converter.canWrite(JsonPatch.class, MediaType.APPLICATION_JSON)).isFalse();
            assertThat(converter.canWrite(JsonPatch.class, JSON_PATCH)).isFalse();
        }
    }

    @Nested
    @DisplayName("reading a patch document")
    class Reading {

        @Test
        @DisplayName("a replace operation is parsed and applies to a document")
        void parsesAReplaceOperation() throws Exception {
            JsonPatch patch = converter.read(JsonPatch.class,
                    body(patchBody(replace("/status", "CANCELLED"))));

            JsonObject result = patch.apply(jsonObject(new Document("PENDING", null)));

            assertThat(result.getString("status")).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("several operations are applied in order")
        void parsesMultipleOperations() throws Exception {
            JsonPatch patch = converter.read(JsonPatch.class,
                    body(patchBody(replace("/status", "ACTIVE"), replace("/roomId", "room-z"))));

            JsonObject result = patch.apply(jsonObject(new Document("PENDING", "room-a")));

            assertThat(result.getString("status")).isEqualTo("ACTIVE");
            assertThat(result.getString("roomId")).isEqualTo("room-z");
        }

        @Test
        @DisplayName("an empty document parses to a patch that changes nothing")
        void parsesAnEmptyDocument() throws Exception {
            JsonPatch patch = converter.read(JsonPatch.class, body(patchBody()));

            JsonObject result = patch.apply(jsonObject(new Document("PENDING", null)));

            assertThat(result.getString("status")).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("a document that is not an array is rejected as unreadable")
        void rejectsANonArrayDocument() {
            // a single operation object rather than the array of them
            assertThatThrownBy(() -> converter.read(JsonPatch.class, body(json(operation("replace", null)))))
                    .isInstanceOf(HttpMessageNotReadableException.class);
        }

        @Test
        @DisplayName("an unknown operation is rejected as unreadable")
        void rejectsAnUnknownOperation() {
            assertThatThrownBy(() -> converter.read(JsonPatch.class,
                    body(patchBody(operation("frobnicate", "/status")))))
                    .isInstanceOf(HttpMessageNotReadableException.class);
        }

        @Test
        @DisplayName("malformed JSON is rejected as unreadable")
        void rejectsMalformedJson() {
            assertThatThrownBy(() -> converter.read(JsonPatch.class,
                    body(truncated(patchBody(replace("/status", "CANCELLED"))))))
                    .isInstanceOf(HttpMessageNotReadableException.class);
        }

        @Test
        @DisplayName("writing a patch out is refused rather than producing a broken body")
        void refusesToWrite() {
            assertThatThrownBy(() -> converter.write(Json.createPatch(JsonValue.EMPTY_JSON_ARRAY),
                    MediaType.APPLICATION_JSON, new org.springframework.mock.http.MockHttpOutputMessage()))
                    .isInstanceOf(HttpMessageNotWritableException.class);
        }
    }

    @Nested
    @DisplayName("registration")
    class Registration {

        @Test
        @DisplayName("the customizer contributes the patch converter to the server converter stack")
        void customizerContributesTheConverter() {
            HttpMessageConverters.ServerBuilder builder = HttpMessageConverters.forServer().registerDefaults();
            JsonPatchConfiguration configuration = new JsonPatchConfiguration();

            configuration.jsonPatchConvertersCustomizer(
                    configuration.jsonPatchHttpMessageConverter()).customize(builder);

            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            builder.build().forEach(converters::add);
            assertThat(converters).anySatisfy(converter ->
                    assertThat(converter).isInstanceOf(JsonPatchHttpMessageConverter.class));
            // where it lands relative to the defaults is asserted against the real context in
            // JitsiConferenceModeratorApplicationContextTest
        }

        @Test
        @DisplayName("the Jackson converter would otherwise claim the body and fail on it")
        void jacksonConverterCannotProduceAJsonPatch() {
            // this is why the converter has to be registered: JsonPatch is a JSON-P interface with
            // no Jackson creator, so the default converter accepts the read and then blows up
            JacksonJsonHttpMessageConverter jackson = new JacksonJsonHttpMessageConverter();

            assertThat(jackson.canRead(JsonPatch.class, MediaType.APPLICATION_JSON)).isTrue();
            assertThatThrownBy(() -> jackson.read(JsonPatch.class,
                    body(patchBody(replace("/status", "CANCELLED")))))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("the configuration builds a converter that claims patch bodies")
        void configurationBuildsAUsableConverter() {
            JsonPatchHttpMessageConverter built =
                    new JsonPatchConfiguration().jsonPatchHttpMessageConverter();

            assertThat(built.canRead(JsonPatch.class, MediaType.APPLICATION_JSON)).isTrue();
        }
    }
}
