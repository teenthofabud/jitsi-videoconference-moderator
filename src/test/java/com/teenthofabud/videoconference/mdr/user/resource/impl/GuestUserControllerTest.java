package com.teenthofabud.videoconference.mdr.user.resource.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.support.MockMvcTestSupport;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.json;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.jsonWithNulls;
import static com.teenthofabud.videoconference.mdr.support.JsonTestSupport.truncated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Edge cases for {@code POST /api/users/preview}, the unauthenticated guest / waitlist endpoint.
 */
@ExtendWith(MockitoExtension.class)
class GuestUserControllerTest {

    private static final String PREVIEW = "/api/users/preview";

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        mockMvc = MockMvcTestSupport.standaloneMockMvc(controller);
    }

    private UserVo guestVo(Long id, String email, String name) {
        return UserVo.builder().id(id).email(email).name(name).isManaged(false).version(0L).lastModifiedDate(java.time.Instant.parse("2026-01-02T03:04:05Z")).build();
    }

    private UserForm signup(String email, String name) {
        UserForm form = new UserForm();
        form.setEmail(email);
        form.setName(name);
        return form;
    }

    /** A signup body carrying managed flags that {@link UserForm} does not declare. */
    private record SpoofedSignup(String email, Boolean isManaged, Boolean managed) {
    }

    @Test
    @DisplayName("guest creation returns 201 with a Location header pointing at the new user")
    void createsGuestAndReturnsLocation() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenReturn(guestVo(11L, "guest@example.com", "Guest User"));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("guest@example.com", "Guest User"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/preview/11"))
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.email").value("guest@example.com"))
                .andExpect(jsonPath("$.isManaged").value(false))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.lastModifiedDate").value("2026-01-02T03:04:05Z"));
    }

    @Test
    @DisplayName("the controller, not the request body, decides that a /preview signup is a guest")
    void managedFlagCannotBeSpoofedThroughTheBody() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenReturn(guestVo(12L, "guest@example.com", null));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SpoofedSignup("guest@example.com", true, true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isManaged").value(false));

        // UserForm carries no managed flag at all, so the extra JSON properties are simply ignored
        verify(userService).createNewUser(any(UserForm.class), eq(false));
        verify(userService, never()).createNewUser(any(UserForm.class), eq(true));
    }

    @Test
    @DisplayName("name is optional")
    void nameIsOptional() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenReturn(guestVo(13L, "guest@example.com", null));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("guest@example.com", null))))
                .andExpect(status().isCreated());

        ArgumentCaptor<UserForm> captor = ArgumentCaptor.forClass(UserForm.class);
        verify(userService).createNewUser(captor.capture(), eq(false));
        assertThat(captor.getValue().getName()).isNull();
    }

    @Test
    @DisplayName("a missing email is rejected with 400 MDR-002 before the service is reached")
    void missingEmailIsRejected() throws Exception {
        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup(null, "Guest User"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()))
                .andExpect(jsonPath("$.message").value("email can't be null"));

        verify(userService, never()).createNewUser(any(), any());
    }

    @Test
    @DisplayName("an explicit null email is rejected the same way")
    void explicitNullEmailIsRejected() throws Exception {
        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNulls(signup(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email can't be null"));

        verify(userService, never()).createNewUser(any(), any());
    }

    @Test
    @DisplayName("an empty email passes validation - @NotNull does not imply @NotBlank and there is no @Email")
    void blankEmailIsAccepted() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenReturn(guestVo(14L, "", null));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("", null))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("a syntactically invalid email is accepted - UserForm.email carries no @Email constraint")
    void malformedEmailIsAccepted() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenReturn(guestVo(15L, "not-an-email", null));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("not-an-email", null))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("a duplicate signup is surfaced as 409 MDR-005 with the waitlist message")
    void duplicateSignupIsConflict() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(false)))
                .thenThrow(new UserException(MDRErrorCode.MDR_EXISTS, "You are already on the waitlist!"));

        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("guest@example.com", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_EXISTS.getErrorCode()))
                .andExpect(jsonPath("$.message").value("You are already on the waitlist!"));
    }

    @Test
    @DisplayName("a malformed JSON body is rejected with 400 MDR-002")
    void malformedJsonIsRejected() throws Exception {
        mockMvc.perform(post(PREVIEW)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(truncated(json(signup("guest@example.com", null)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(MDRErrorCode.MDR_ATTRIBUTE_INVALID.getErrorCode()))
                .andExpect(jsonPath("$.message").value("Request body could not be read"));

        verify(userService, never()).createNewUser(any(), any());
    }

    @Test
    @DisplayName("the managed endpoint POST /api/users asks for a managed user instead")
    void managedEndpointCreatesManagedUser() throws Exception {
        when(userService.createNewUser(any(UserForm.class), eq(true)))
                .thenReturn(UserVo.builder().id(16L).email("user@example.com").isManaged(true).build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(signup("user@example.com", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isManaged").value(true));

        verify(userService).createNewUser(any(UserForm.class), eq(true));
    }
}
