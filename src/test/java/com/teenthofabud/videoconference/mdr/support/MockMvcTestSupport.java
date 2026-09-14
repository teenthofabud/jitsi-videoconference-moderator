package com.teenthofabud.videoconference.mdr.support;

import com.teenthofabud.videoconference.mdr.configuration.JsonPatchHttpMessageConverter;
import com.teenthofabud.videoconference.mdr.error.converter.MDRExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.converter.MethodArgNotValidExceptionToErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.converter.google.GoogleExceptionToConferenceErrorVoConverter;
import com.teenthofabud.videoconference.mdr.error.handler.MDRExceptionHandler;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Builds a standalone MockMvc around a single controller, wired with the real
 * {@link MDRExceptionHandler} so that tests observe the same status codes and error payloads the
 * running application produces. Standalone (rather than a Spring Boot slice) keeps the tests free of
 * the datasource and Auth0 environment variables the application context requires.
 */
public final class MockMvcTestSupport {

    private MockMvcTestSupport() {
    }

    public static MDRExceptionHandler exceptionHandler() {
        MDRExceptionHandler handler = new MDRExceptionHandler();
        ReflectionTestUtils.setField(handler, "mdrExceptionToErrorVoConverter", new MDRExceptionToErrorVoConverter());
        ReflectionTestUtils.setField(handler, "googleExceptionToConferenceErrorVoConverter", new GoogleExceptionToConferenceErrorVoConverter());
        ReflectionTestUtils.setField(handler, "methodArgNotValidExceptionToErrorVoConverter", new MethodArgNotValidExceptionToErrorVoConverter());
        return handler;
    }

    public static MockMvc standaloneMockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler())
                .build();
    }

    /**
     * Same, but with the converter stack the application registers in
     * {@code JsonPatchConfiguration}: the Jackson 2 backed JsonPatch reader ahead of the Jackson 3
     * converter that handles everything else.
     */
    public static MockMvc standaloneMockMvcWithJsonPatch(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler())
                .setMessageConverters(
                        new JsonPatchHttpMessageConverter(),
                        new JacksonJsonHttpMessageConverter(),
                        new StringHttpMessageConverter())
                .build();
    }
}
