package com.teenthofabud.videoconference.mdr.conference.google.resource.impl;

import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.google.constants.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.google.entity.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.google.service.GoogleConferenceService;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
import com.teenthofabud.videoconference.mdr.support.MockMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.operation;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.PatchOperation.replace;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.json;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.jsonObject;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.jsonWithNulls;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.patchBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP level edge cases for {@code /api/google/conferences}, including how failures are shaped for
 * the Google Calendar add-on.
 */
@ExtendWith(MockitoExtension.class)
class GoogleConferenceControllerTest {

    private static final String BASE = "/api/google/conferences";
    private static final String EMAIL = "organiser@example.com";
    private static final Long USER_ID = 1L;
    private static final String EVENT_ID = "evt-1";

    @Mock
    private GoogleConferenceService googleConferenceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GoogleConferenceController controller = new GoogleConferenceController();
        ReflectionTestUtils.setField(controller, "googleConferenceService", googleConferenceService);
        mockMvc = MockMvcTestSupport.standaloneMockMvcWithJsonPatch(controller);
    }

    /** What the Calendar add-on sends. */
    private GoogleConferenceForm addOnForm() {
        GoogleConferenceForm form = new GoogleConferenceForm();
        form.setEventData(new GoogleEventData(EVENT_ID, EMAIL));
        form.setCalendarEvent(new GoogleCalendarEvent(EMAIL, EVENT_ID));
        form.setServerId(5L);
        form.setUserId(USER_ID);
        return form;
    }

    private String createPayload() {
        return json(addOnForm());
    }

    @Nested
    @DisplayName("POST /api/google/conferences")
    class Create {

        @Test
        @DisplayName("a created conference comes back as 201 with the video entry point")
        void createsConference() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenReturn(GoogleConferenceDataVo.builder()
                            .id("room-abc").eventId(EVENT_ID).userId(USER_ID)
                            .videoUri("https://meet.jit.si/room-abc").build());

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createPayload()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("room-abc"))
                    .andExpect(jsonPath("$.eventId").value(EVENT_ID))
                    .andExpect(jsonPath("$.videoUri").value("https://meet.jit.si/room-abc"));
        }

        @Test
        @DisplayName("the add-on payload is bound onto the form, unknown properties and all")
        void bindsTheAddOnPayload() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenReturn(GoogleConferenceDataVo.builder().id("room-abc").build());

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createPayload()))
                    .andExpect(status().isCreated());

            ArgumentCaptor<GoogleConferenceForm> captor = ArgumentCaptor.forClass(GoogleConferenceForm.class);
            verify(googleConferenceService).createNewGoogleConference(captor.capture());
            GoogleConferenceForm form = captor.getValue();
            assertThat(form.getEventData().getEventId()).isEqualTo(EVENT_ID);
            assertThat(form.getEventData().getCalendarId()).isEqualTo(EMAIL);
            assertThat(form.getServerId()).isEqualTo(5L);
            assertThat(form.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("a payload that omits userId and still binds")
        void omittingUserIdIsTolerated() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenReturn(GoogleConferenceDataVo.builder().id("room-abc").build());

            GoogleConferenceForm request = addOnForm();
            request.setUserId(null);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(request)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<GoogleConferenceForm> captor = ArgumentCaptor.forClass(GoogleConferenceForm.class);
            verify(googleConferenceService).createNewGoogleConference(captor.capture());
            assertThat(captor.getValue().getUserId()).isNull();
        }

        @Test
        @DisplayName("an explicit null userId binds too")
        void nullUserIdIsTolerated() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenReturn(GoogleConferenceDataVo.builder().id("room-abc").build());

            GoogleConferenceForm request = addOnForm();
            request.setUserId(null);
            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(jsonWithNulls(request)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<GoogleConferenceForm> captor = ArgumentCaptor.forClass(GoogleConferenceForm.class);
            verify(googleConferenceService).createNewGoogleConference(captor.capture());
            assertThat(captor.getValue().getUserId()).isNull();
        }

        @Test
        @DisplayName("a body without eventData is rejected with 400 MDR-002")
        void missingEventDataIsRejected() throws Exception {
            // @Valid sits on GoogleConferenceAPI#createNewGoogleConference, so the @NotNull
            // constraints on the form are enforced even though the controller override omits it
            GoogleConferenceForm form = new GoogleConferenceForm();

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json(form)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-002"));

            verifyNoInteractions(googleConferenceService);
        }

        @Test
        @DisplayName("a conference error is reported to Calendar as HTTP 200 carrying the error type")
        void conferenceErrorsAreReportedWithHttp200() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenThrow(new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,
                            "User with userId 1 not found"));

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createPayload()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error").value("TEMPORARY"))
                    .andExpect(jsonPath("$.message").value("User with userId 1 not found"))
                    .andExpect(jsonPath("$.authenticationUrl").doesNotExist());
        }

        @Test
        @DisplayName("an authentication error additionally carries an authenticationUrl")
        void authenticationErrorsCarryAnAuthUrl() throws Exception {
            when(googleConferenceService.createNewGoogleConference(any(GoogleConferenceForm.class)))
                    .thenThrow(new GoogleConferenceException(GoogleConferenceErrorType.AUTHENTICATION, "please sign in"));

            mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createPayload()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error").value("AUTHENTICATION"))
                    .andExpect(jsonPath("$.authenticationUrl").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/google/conferences")
    class Read {

        @Test
        @DisplayName("a single conference is fetched by event id with the user id as a query parameter")
        void fetchesOneConference() throws Exception {
            when(googleConferenceService.getGoogleConferenceByUserIdAndEventId(USER_ID, EVENT_ID))
                    .thenReturn(GoogleConferenceVo.builder()
                            .eventId(EVENT_ID).roomId("room-abc").status(ConferenceStatus.ACTIVE).build());

            mockMvc.perform(get(BASE + "/events/" + EVENT_ID).param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.eventId").value(EVENT_ID))
                    .andExpect(jsonPath("$.roomId").value("room-abc"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("a missing userId query parameter answers 400 MDR-004 naming the parameter")
        void missingUserIdParameterIsRejected() throws Exception {
            mockMvc.perform(get(BASE + "/events/" + EVENT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-004"))
                    .andExpect(jsonPath("$.message").value("Required request parameter userId is missing"));

            verifyNoInteractions(googleConferenceService);
        }

        @Test
        @DisplayName("the list endpoint returns every conference for the user")
        void listsConferences() throws Exception {
            when(googleConferenceService.getAllGoogleConferencesByUserId(USER_ID)).thenReturn(List.of(
                    GoogleConferenceVo.builder().eventId("evt-1").roomId("room-a").status(ConferenceStatus.ACTIVE).build(),
                    GoogleConferenceVo.builder().eventId("evt-2").roomId("room-b").status(ConferenceStatus.PENDING).build()));

            mockMvc.perform(get(BASE).param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].roomId").value("room-a"))
                    .andExpect(jsonPath("$[1].status").value("PENDING"));
        }

        @Test
        @DisplayName("the list endpoint returns an empty array rather than 404 when there is nothing")
        void emptyListIsNotANotFound() throws Exception {
            when(googleConferenceService.getAllGoogleConferencesByUserId(USER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE).param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("an unknown conference surfaces as HTTP 200 with a TEMPORARY error body")
        void unknownConferenceIsAnHttp200Error() throws Exception {
            when(googleConferenceService.getGoogleConferenceByUserIdAndEventId(USER_ID, "missing"))
                    .thenThrow(new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,
                            "Calendar event with ID missing not found"));

            mockMvc.perform(get(BASE + "/events/missing").param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error").value("TEMPORARY"));
        }

        @Test
        @DisplayName("a malformed userId is rejected with 400 MDR-002 by the API's Long userId parameter")
        void malformedUserIdIsRejected() throws Exception {
            mockMvc.perform(get(BASE).param("userId", "not-a-number"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-002"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));

            verifyNoInteractions(googleConferenceService);
        }
    }

    @Nested
    @DisplayName("PATCH /api/google/conferences/events/{eventId}")
    class Patch {

        @Test
        @DisplayName("a json patch body is bound and applied, answering 204")
        void appliesPatch() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody(replace("/status", "CANCELLED"))))
                    .andExpect(status().isNoContent());

            ArgumentCaptor<JsonPatch> captor = ArgumentCaptor.forClass(JsonPatch.class);
            verify(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("the RFC 6902 media type application/json-patch+json is accepted too")
        void acceptsTheJsonPatchMediaType() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.valueOf(HttpMediaType.APPLICATION_JSON_PATCH))
                            .content(patchBody(replace("/status", "ACTIVE"))))
                    .andExpect(status().isNoContent());

            verify(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), any());
        }

        @Test
        @DisplayName("the bound patch carries the operations the client sent")
        void boundPatchCarriesTheOperations() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody(replace("/status", "CANCELLED"))))
                    .andExpect(status().isNoContent());

            ArgumentCaptor<JsonPatch> captor = ArgumentCaptor.forClass(JsonPatch.class);
            verify(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), captor.capture());

            // applying the bound patch reproduces the requested change
            JsonObject before = jsonObject(GoogleConferenceEntity.builder().status(ConferenceStatus.PENDING).version(0L)
                    .lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z")).build());
            assertThat(captor.getValue().apply(before).getString("status")).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("an empty patch document binds to a patch with no operations")
        void emptyPatchBinds() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody()))
                    .andExpect(status().isNoContent());

            verify(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), any());
        }

        @Test
        @DisplayName("a patch against an unknown conference is HTTP 200 with a TEMPORARY error body")
        void unknownConferenceIsAnHttp200Error() throws Exception {
            doThrow(new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,
                    "Calendar event with ID missing not found"))
                    .when(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq("missing"), any());

            mockMvc.perform(patch(BASE + "/events/missing")
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody(replace("/status", "CANCELLED"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error").value("TEMPORARY"));
        }

        @Test
        @DisplayName("a patch that parses but cannot be applied is answered with 400 MDR-002")
        void unapplicablePatchIsABadRequest() throws Exception {
            // JSON-P only reports a bad path when the patch is applied, so the failure surfaces
            // from the service as an unchecked JsonException rather than at binding time
            doThrow(new jakarta.json.JsonException("Non-existing property status"))
                    .when(googleConferenceService)
                    .patchGoogleConferenceByUserIdAndEventId(eq(USER_ID), eq(EVENT_ID), any());

            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody(replace("/doesNotExist", "x"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-002"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("JSON patch could not be applied")));
        }

        @Test
        @DisplayName("a patch document that is not a JSON array is rejected before the service is reached")
        void nonArrayPatchIsRejected() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(operation("replace", null))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-002"))
                    .andExpect(jsonPath("$.message").value("Request body could not be read"));

            verify(googleConferenceService, never())
                    .patchGoogleConferenceByUserIdAndEventId(any(), any(), any());
        }

        @Test
        @DisplayName("an operation with an unknown op is rejected before the service is reached")
        void unknownOperationIsRejected() throws Exception {
            mockMvc.perform(patch(BASE + "/events/" + EVENT_ID)
                            .param("userId", USER_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody(operation("frobnicate", "/status"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-002"));

            verify(googleConferenceService, never())
                    .patchGoogleConferenceByUserIdAndEventId(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/google/conferences/events/{eventId}")
    class Delete {

        @Test
        @DisplayName("a conference is deleted by event id in the path and userId in the query, answering 204")
        void deletesConference() throws Exception {
            mockMvc.perform(delete(BASE + "/events/" + EVENT_ID).param("userId", USER_ID.toString()))
                    .andExpect(status().isNoContent());

            verify(googleConferenceService).deleteGoogleConferenceByUserIdAndEventId(USER_ID, EVENT_ID);
        }

        @Test
        @DisplayName("the two values are not transposed on the way to the service")
        void userIdAndEventIdAreNotTransposed() throws Exception {
            mockMvc.perform(delete(BASE + "/events/evt-99").param("userId", "2"))
                    .andExpect(status().isNoContent());

            verify(googleConferenceService)
                    .deleteGoogleConferenceByUserIdAndEventId(2L, "evt-99");
        }

        @Test
        @DisplayName("a missing userId query parameter is rejected with 400, not silently treated as a delete")
        void missingUserIdIsNotADelete() throws Exception {
            mockMvc.perform(delete(BASE + "/events/" + EVENT_ID))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("MDR-004"));

            verifyNoInteractions(googleConferenceService);
        }

        @Test
        @DisplayName("an unknown conference is reported to Calendar as HTTP 200 with a TEMPORARY error")
        void unknownConferenceIsAnHttp200Error() throws Exception {
            doThrow(new GoogleConferenceException(GoogleConferenceErrorType.TEMPORARY,
                    "Calendar event with ID missing not found"))
                    .when(googleConferenceService)
                    .deleteGoogleConferenceByUserIdAndEventId(USER_ID, "missing");

            mockMvc.perform(delete(BASE + "/events/missing").param("userId", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error").value("TEMPORARY"));
        }
    }
}
