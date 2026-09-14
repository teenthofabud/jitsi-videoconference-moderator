package com.teenthofabud.videoconference.mdr.conference.service.impl;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonPatchBuilder;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.constants.google.GoogleConferenceErrorType;
import com.teenthofabud.videoconference.mdr.conference.converter.ConferenceEntityToConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.converter.google.GoogleConferenceEntityToGoogleConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.entity.google.GoogleConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleEventData;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceEntityToGoogleConferenceDataVoReducer;
import com.teenthofabud.videoconference.mdr.conference.reducer.GoogleConferenceFormToGoogleConferenceEntityReducer;
import com.teenthofabud.videoconference.mdr.conference.repository.google.GoogleConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Edge cases for Google conference management: creation from the Calendar add-on plus the
 * read, patch and delete operations keyed on (organizer email, calendar event id).
 */
@ExtendWith(MockitoExtension.class)
class GoogleConferenceServiceImplTest {

    private static final String EMAIL = "organiser@example.com";
    private static final String EVENT_ID = "evt-1";
    private static final Long SERVER_ID = 5L;

    @Mock
    private GoogleConferenceRepository googleConferenceRepository;
    @Mock
    private UserService userService;
    @Mock
    private ServerService serverService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private GoogleConferenceServiceImpl conferenceService;

    @BeforeEach
    void setUp() {
        conferenceService = new GoogleConferenceServiceImpl(
                googleConferenceRepository,
                new ConferenceEntityToConferenceVoConverter(),
                new GoogleConferenceEntityToGoogleConferenceVoConverter(),
                objectMapper,
                new GoogleConferenceFormToGoogleConferenceEntityReducer(),
                new GoogleConferenceEntityToGoogleConferenceDataVoReducer(),
                userService,
                serverService);
    }

    private GoogleConferenceForm form(String calendarId, String eventId, Long serverId) {
        GoogleConferenceForm form = new GoogleConferenceForm();
        form.setEventData(new GoogleEventData(eventId, calendarId));
        form.setCalendarEvent(new GoogleCalendarEvent(calendarId, eventId));
        form.setServerId(serverId);
        return form;
    }

    private UserVo user() {
        return UserVo.builder().id(1L).email(EMAIL).name("Organiser").isManaged(true).build();
    }

    private ServerVo server(String url) {
        return ServerVo.builder().id(SERVER_ID).name("Jitsi Meet").serverUrl(url)
                .serverType(ServerType.DEFAULT).build();
    }

    private GoogleConferenceEntity conference(Long id, String roomId, ConferenceStatus status) {
        return GoogleConferenceEntity.builder()
                .id(id).eventId(EVENT_ID).organizerEmail(EMAIL).roomId(roomId).status(status).build();
    }

    private JsonPatchBuilder patch() {
        return Json.createPatchBuilder();
    }

    @Nested
    @DisplayName("createNewGoogleConference")
    class Create {

        @Test
        @DisplayName("a first request creates a PENDING conference with a generated room id")
        void createsPendingConference() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID)).thenReturn(Optional.empty());
            when(googleConferenceRepository.save(any(GoogleConferenceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            ArgumentCaptor<GoogleConferenceEntity> captor = ArgumentCaptor.forClass(GoogleConferenceEntity.class);
            verify(googleConferenceRepository).save(captor.capture());
            GoogleConferenceEntity saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(ConferenceStatus.PENDING);
            assertThat(saved.getEventId()).isEqualTo(EVENT_ID);
            assertThat(saved.getOrganizerEmail()).isEqualTo(EMAIL);
            assertThat(UUID.fromString(saved.getRoomId())).hasToString(saved.getRoomId());

            assertThat(vo.getEventId()).isEqualTo(EVENT_ID);
            assertThat(vo.getAdminEmail()).isEqualTo(EMAIL);
            assertThat(vo.getId()).isEqualTo(saved.getRoomId());
        }

        @Test
        @DisplayName("two requests for the same calendar event reuse the first conference")
        void isIdempotentPerCalendarEvent() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            GoogleConferenceEntity existing = conference(3L, "room-abc", ConferenceStatus.ACTIVE);
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(existing));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            verify(googleConferenceRepository, never()).save(any());
            assertThat(vo.getId()).isEqualTo("room-abc");
        }

        @Test
        @DisplayName("a re-request after the server preference changed points at the new server")
        void existingConferenceFollowsTheCurrentServer() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("other.example.org"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.ACTIVE)));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            assertThat(vo.getVideoUri()).isEqualTo("https://other.example.org/room-abc");
        }

        @Test
        @DisplayName("an unknown organizer email is reported as a TEMPORARY google error")
        void unknownUserIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "User doesn't exist with email organiser@example.com"));

            assertThatThrownBy(() -> conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID)))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("User with calendarId/email organiser@example.com not found")
                    .extracting(e -> ((GoogleConferenceException) e).getErrorType())
                    .isEqualTo(GoogleConferenceErrorType.TEMPORARY);

            verifyNoInteractions(serverService);
            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("an unknown server id is reported as a TEMPORARY google error")
        void unknownServerIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_NOT_FOUND, "Server not found with id: 5"));

            assertThatThrownBy(() -> conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID)))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Server with ID 5 not found")
                    .extracting(e -> ((GoogleConferenceException) e).getErrorType())
                    .isEqualTo(GoogleConferenceErrorType.TEMPORARY);

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("a null server id is rejected the same way as an unknown one")
        void nullServerIdIsRejected() throws Exception {
            // the add-on omits serverId when the user never picked a server from the dropdown
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(null))
                    .thenThrow(new ServerException(MDRErrorCode.MDR_NOT_FOUND, "Server not found with id: null"));

            assertThatThrownBy(() -> conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, null)))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Server with ID null not found");
        }

        @Test
        @DisplayName("the user is resolved before the server, so a bad email masks a bad server id")
        void userIsResolvedBeforeServer() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            InOrder inOrder = org.mockito.Mockito.inOrder(userService, serverService, googleConferenceRepository);
            inOrder.verify(userService).getUserByEmail(EMAIL);
            inOrder.verify(serverService).getServerById(SERVER_ID);
            inOrder.verify(googleConferenceRepository).findByOrganizerEmailAndEventId(EMAIL, EVENT_ID);
        }

        @Test
        @DisplayName("eventData wins over the calendar block when the two disagree")
        void eventDataIsTheSourceOfTruth() throws Exception {
            // the payload carries the calendar id twice; only form.eventData is read
            GoogleConferenceForm form = form(EMAIL, EVENT_ID, SERVER_ID);
            form.setCalendarEvent(new GoogleCalendarEvent("someone.else@example.com", "other-event"));

            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID)).thenReturn(Optional.empty());
            when(googleConferenceRepository.save(any(GoogleConferenceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form);

            assertThat(vo.getEventId()).isEqualTo(EVENT_ID);
            assertThat(vo.getAdminEmail()).isEqualTo(EMAIL);
            verify(userService).getUserByEmail(EMAIL);
        }

        @Test
        @DisplayName("the organizer email is taken from the resolved user, not from the payload")
        void organizerEmailComesFromTheResolvedUser() throws Exception {
            when(userService.getUserByEmail(EMAIL))
                    .thenReturn(UserVo.builder().id(1L).email("canonical@example.com").isManaged(true).build());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID)).thenReturn(Optional.empty());
            when(googleConferenceRepository.save(any(GoogleConferenceEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            ArgumentCaptor<GoogleConferenceEntity> captor = ArgumentCaptor.forClass(GoogleConferenceEntity.class);
            verify(googleConferenceRepository).save(captor.capture());
            assertThat(captor.getValue().getOrganizerEmail()).isEqualTo("canonical@example.com");
            // the response still echoes the calendar id the add-on sent
            assertThat(vo.getAdminEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("the videoUri reuses the scheme the stored server URL already carries")
        void videoUriKeepsASingleScheme() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("https://meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            assertThat(vo.getVideoUri()).isEqualTo("https://meet.jit.si/room-abc");
        }

        @Test
        @DisplayName("a server URL stored without a scheme still yields an https entry point")
        void videoUriAddsASchemeWhenTheStoredUrlHasNone() throws Exception {
            // rows written before server URLs were sanitised on the way in may be bare hosts
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("meet.jit.si"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            assertThat(vo.getVideoUri()).isEqualTo("https://meet.jit.si/room-abc");
        }

        @Test
        @DisplayName("a path on the server URL is preserved in the entry point")
        void videoUriPreservesAHostedPath() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(serverService.getServerById(SERVER_ID)).thenReturn(server("https://example.org/jitsi"));
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            GoogleConferenceDataVo vo = conferenceService.createNewGoogleConference(form(EMAIL, EVENT_ID, SERVER_ID));

            assertThat(vo.getVideoUri()).isEqualTo("https://example.org/jitsi/room-abc");
        }

        @Test
        @DisplayName("a payload without eventData fails with a NullPointerException rather than a google error")
        void missingEventDataIsUnguarded() {
            // over HTTP the @Valid on GoogleConferenceAPI stops this with a 400; direct service
            // callers get an unguarded dereference of form.getEventData() instead
            GoogleConferenceForm form = new GoogleConferenceForm();

            assertThatThrownBy(() -> conferenceService.createNewGoogleConference(form))
                    .isInstanceOf(NullPointerException.class);

            verifyNoInteractions(userService, serverService, googleConferenceRepository);
        }
    }

    @Nested
    @DisplayName("getGoogleConferenceByUserEmailAndEventId")
    class GetOne {

        @Test
        @DisplayName("an existing conference is returned with its room id and status")
        void returnsConference() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.ACTIVE)));

            GoogleConferenceVo vo = conferenceService.getGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID);

            assertThat(vo.getEventId()).isEqualTo(EVENT_ID);
            assertThat(vo.getRoomId()).isEqualTo("room-abc");
            assertThat(vo.getStatus()).isEqualTo(ConferenceStatus.ACTIVE);
        }

        @Test
        @DisplayName("an unknown user is a TEMPORARY google error")
        void unknownUserIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "no such user"));

            assertThatThrownBy(() -> conferenceService.getGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("User with calendarId/email organiser@example.com not found");

            verifyNoInteractions(googleConferenceRepository);
        }

        @Test
        @DisplayName("a known user with no such event is a TEMPORARY google error naming the event")
        void unknownEventIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, "missing"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.getGoogleConferenceByUserEmailAndEventId(EMAIL, "missing"))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Calendar event with ID missing not found")
                    .extracting(e -> ((GoogleConferenceException) e).getErrorType())
                    .isEqualTo(GoogleConferenceErrorType.TEMPORARY);
        }

        @Test
        @DisplayName("another user's conference is invisible - the lookup is keyed on both email and event")
        void anotherUsersConferenceIsNotVisible() throws Exception {
            when(userService.getUserByEmail("intruder@example.com"))
                    .thenReturn(UserVo.builder().id(2L).email("intruder@example.com").isManaged(true).build());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId("intruder@example.com", EVENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.getGoogleConferenceByUserEmailAndEventId("intruder@example.com", EVENT_ID))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Calendar event with ID evt-1 not found");
        }

        @Test
        @DisplayName("a null event id falls through to the not found branch")
        void nullEventIdIsNotFound() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.getGoogleConferenceByUserEmailAndEventId(EMAIL, null))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Calendar event with ID null not found");
        }
    }

    @Nested
    @DisplayName("getAllGoogleConferencesByUserEmail")
    class GetAll {

        @Test
        @DisplayName("every conference organised by the user is returned")
        void returnsAllConferences() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findAllByOrganizerEmail(EMAIL)).thenReturn(List.of(
                    conference(1L, "room-a", ConferenceStatus.ACTIVE),
                    conference(2L, "room-b", ConferenceStatus.CANCELLED)));

            List<GoogleConferenceVo> conferences = conferenceService.getAllGoogleConferencesByUserEmail(EMAIL);

            assertThat(conferences).extracting(GoogleConferenceVo::getRoomId).containsExactly("room-a", "room-b");
            assertThat(conferences).extracting(GoogleConferenceVo::getStatus)
                    .containsExactly(ConferenceStatus.ACTIVE, ConferenceStatus.CANCELLED);
        }

        @Test
        @DisplayName("a user with no conferences gets an empty list, not an error")
        void emptyListForUserWithoutConferences() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findAllByOrganizerEmail(EMAIL)).thenReturn(List.of());

            assertThat(conferenceService.getAllGoogleConferencesByUserEmail(EMAIL)).isEmpty();
        }

        @Test
        @DisplayName("an unknown user is a TEMPORARY google error rather than an empty list")
        void unknownUserIsTemporaryError() throws Exception {
            when(userService.getUserByEmail("nobody@example.com"))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "no such user"));

            assertThatThrownBy(() -> conferenceService.getAllGoogleConferencesByUserEmail("nobody@example.com"))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("User with calendarId/email nobody@example.com not found");

            verifyNoInteractions(googleConferenceRepository);
        }

        @Test
        @DisplayName("cancelled conferences are listed alongside active ones - nothing is filtered out")
        void cancelledConferencesAreNotFiltered() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findAllByOrganizerEmail(EMAIL)).thenReturn(List.of(
                    conference(1L, "room-a", ConferenceStatus.CANCELLED)));

            assertThat(conferenceService.getAllGoogleConferencesByUserEmail(EMAIL))
                    .extracting(GoogleConferenceVo::getStatus)
                    .containsExactly(ConferenceStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("patchGoogleConferenceByUserEmailAndEventId")
    class Patch {

        @Test
        @DisplayName("a status change is applied and persisted")
        void appliesStatusChange() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/status", "CANCELLED").build());

            ArgumentCaptor<GoogleConferenceEntity> captor = ArgumentCaptor.forClass(GoogleConferenceEntity.class);
            verify(googleConferenceRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ConferenceStatus.CANCELLED);
        }

        @Test
        @DisplayName("the primary key survives the round trip through JSON, so the patch updates rather than inserts")
        void primaryKeyIsPreserved() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/status", "ACTIVE").build());

            ArgumentCaptor<GoogleConferenceEntity> captor = ArgumentCaptor.forClass(GoogleConferenceEntity.class);
            verify(googleConferenceRepository).save(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(3L);
            assertThat(captor.getValue().getEventId()).isEqualTo(EVENT_ID);
            assertThat(captor.getValue().getOrganizerEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("an empty patch is accepted and rewrites the row unchanged")
        void emptyPatchIsAccepted() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID, patch().build());

            ArgumentCaptor<GoogleConferenceEntity> captor = ArgumentCaptor.forClass(GoogleConferenceEntity.class);
            verify(googleConferenceRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ConferenceStatus.PENDING);
        }

        @Test
        @DisplayName("rewriting the room id is refused - it would orphan anyone already in the meeting")
        void roomIdCannotBePatched() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/roomId", "hijacked").build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Only the status of a conference can be patched")
                    .extracting(e -> ((GoogleConferenceException) e).getErrorType())
                    .isEqualTo(GoogleConferenceErrorType.PERMANENT);

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("reassigning the organizer is refused - it would hand the conference to another user")
        void organizerEmailCannotBePatched() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/organizerEmail", "someone.else@example.com").build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Only the status of a conference can be patched");

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("moving the conference to another calendar event is refused")
        void eventIdCannotBePatched() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/eventId", "evt-other").build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Only the status of a conference can be patched");

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("repointing the primary key is refused, so a patch can never overwrite another row")
        void primaryKeyCannotBePatched() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/id", 99).build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Only the status of a conference can be patched");

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("a status change bundled with an identity change is refused wholesale")
        void aMixedPatchIsRefusedEntirely() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/status", "ACTIVE").replace("/roomId", "hijacked").build()))
                    .isInstanceOf(GoogleConferenceException.class);

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("a patch that rewrites an identity field to its existing value is harmless and allowed")
        void rewritingAnIdentityFieldToTheSameValueIsAllowed() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/roomId", "room-abc").build());

            verify(googleConferenceRepository).save(any(GoogleConferenceEntity.class));
        }

        @Test
        @DisplayName("an unknown user is a TEMPORARY google error and nothing is written")
        void unknownUserIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "no such user"));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/status", "ACTIVE").build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("User with calendarId/email organiser@example.com not found");

            verifyNoInteractions(googleConferenceRepository);
        }

        @Test
        @DisplayName("an unknown event is a TEMPORARY google error and nothing is written")
        void unknownEventIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, "missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, "missing",
                    patch().replace("/status", "ACTIVE").build()))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Calendar event with ID missing not found");

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("a patch against a path that does not exist raises JsonPatchException, which is not mapped to a google error")
        void unknownPathRaisesJsonPatchException() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/doesNotExist", "x").build()))
                    .isInstanceOf(JsonException.class);

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("a failing test op aborts the patch before anything is written")
        void failingTestOpAbortsThePatch() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().test("/status", "ACTIVE").replace("/status", "CANCELLED").build()))
                    .isInstanceOf(JsonException.class);

            verify(googleConferenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("an unparseable status value is rejected before the row is written")
        void invalidStatusValueIsRejected() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.PENDING)));

            assertThatThrownBy(() -> conferenceService.patchGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID,
                    patch().replace("/status", "NOT_A_STATUS").build()))
                    .isInstanceOf(Exception.class);

            verify(googleConferenceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteGoogleConferenceByUserEmailAndEventId")
    class Delete {

        @Test
        @DisplayName("an existing conference is deleted by primary key")
        void deletesByPrimaryKey() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.ACTIVE)));

            conferenceService.deleteGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID);

            verify(googleConferenceRepository).deleteById(3L);
        }

        @Test
        @DisplayName("deleting is a hard delete, not a transition to CANCELLED")
        void deleteIsPermanent() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID))
                    .thenReturn(Optional.of(conference(3L, "room-abc", ConferenceStatus.ACTIVE)));

            conferenceService.deleteGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID);

            verify(googleConferenceRepository, never()).save(any());
            verify(googleConferenceRepository).deleteById(anyLong());
        }

        @Test
        @DisplayName("an unknown user is a TEMPORARY google error and nothing is deleted")
        void unknownUserIsTemporaryError() throws Exception {
            when(userService.getUserByEmail(EMAIL))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "no such user"));

            assertThatThrownBy(() -> conferenceService.deleteGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("User with calendarId/email organiser@example.com not found");

            verifyNoInteractions(googleConferenceRepository);
        }

        @Test
        @DisplayName("deleting an already deleted conference is a TEMPORARY google error, not a no-op")
        void secondDeleteIsAnError() throws Exception {
            when(userService.getUserByEmail(EMAIL)).thenReturn(user());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId(EMAIL, EVENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.deleteGoogleConferenceByUserEmailAndEventId(EMAIL, EVENT_ID))
                    .isInstanceOf(GoogleConferenceException.class)
                    .hasMessage("Calendar event with ID evt-1 not found")
                    .extracting(e -> ((GoogleConferenceException) e).getErrorType())
                    .isEqualTo(GoogleConferenceErrorType.TEMPORARY);

            verify(googleConferenceRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("one user cannot delete another user's conference")
        void cannotDeleteAnotherUsersConference() throws Exception {
            when(userService.getUserByEmail("intruder@example.com"))
                    .thenReturn(UserVo.builder().id(2L).email("intruder@example.com").isManaged(true).build());
            when(googleConferenceRepository.findByOrganizerEmailAndEventId("intruder@example.com", EVENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> conferenceService.deleteGoogleConferenceByUserEmailAndEventId("intruder@example.com", EVENT_ID))
                    .isInstanceOf(GoogleConferenceException.class);

            verify(googleConferenceRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("google error contract")
    class ErrorContract {

        @Test
        @DisplayName("every google conference error is reported to Calendar with HTTP 200")
        void googleErrorsUseHttp200() throws Exception {
            // Google Calendar treats any non-200 as a transport failure and hides the add-on's
            // message, so GoogleConferenceErrorType pins the status code to 200
            when(userService.getUserByEmail(EMAIL))
                    .thenThrow(new UserException(MDRErrorCode.MDR_NOT_FOUND, "no such user"));

            assertThatThrownBy(() -> conferenceService.getAllGoogleConferencesByUserEmail(EMAIL))
                    .isInstanceOf(GoogleConferenceException.class)
                    .extracting(e -> ((GoogleConferenceException) e).getError().getHttpStatusCode())
                    .isEqualTo(200);
        }
    }
}
