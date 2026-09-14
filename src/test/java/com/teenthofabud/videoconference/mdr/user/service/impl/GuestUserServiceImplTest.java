package com.teenthofabud.videoconference.mdr.user.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.user.converter.UserEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.user.converter.UserFormToUserEntityConverter;
import com.teenthofabud.videoconference.mdr.user.entity.UserEntity;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.user.repository.UserRepository;
import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Edge cases around guest user creation, i.e. {@code createNewUser(form, false)} which backs
 * {@code POST /api/users/preview}. Guests differ from signed up users only by the is_managed flag,
 * so most cases here are contrasted against the managed path.
 */
@ExtendWith(MockitoExtension.class)
class GuestUserServiceImplTest {

    private static final String EMAIL = "guest@example.com";

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        // the converters are pure functions; using the real ones keeps the assertions meaningful
        ReflectionTestUtils.setField(userService, "userFormToUserEntityConverter", new UserFormToUserEntityConverter());
        ReflectionTestUtils.setField(userService, "userEntityToVoConverter", new UserEntityToVoConverter());
    }

    private UserForm form(String email, String name) {
        UserForm form = new UserForm();
        form.setEmail(email);
        form.setName(name);
        return form;
    }

    /** Stubs save() to mimic the DB assigning an IDENTITY primary key in place. */
    private void savePersistsWithId(Long id) {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("guest is persisted with is_managed = false")
        void guestIsPersistedUnmanaged() throws UserException {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            savePersistsWithId(42L);

            UserVo vo = userService.createNewUser(form(EMAIL, "Guest User"), false);

            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getManaged()).isFalse();
            assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
            assertThat(captor.getValue().getName()).isEqualTo("Guest User");

            assertThat(vo.getIsManaged()).isFalse();
            assertThat(vo.getId()).isEqualTo(42L);
            assertThat(vo.getEmail()).isEqualTo(EMAIL);
            assertThat(vo.getName()).isEqualTo("Guest User");
        }

        @Test
        @DisplayName("same form through the managed path yields is_managed = true")
        void managedFlagIsDrivenOnlyByTheCallerNotTheForm() throws UserException {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            savePersistsWithId(7L);

            UserVo vo = userService.createNewUser(form(EMAIL, "Guest User"), true);

            assertThat(vo.getIsManaged()).isTrue();
        }

        @Test
        @DisplayName("name is optional for a guest")
        void nameMayBeNull() throws UserException {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            savePersistsWithId(1L);

            UserVo vo = userService.createNewUser(form(EMAIL, null), false);

            assertThat(vo.getName()).isNull();
            assertThat(vo.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("blank name is stored verbatim - it is not normalised to null")
        void blankNameIsNotNormalised() throws UserException {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            savePersistsWithId(1L);

            UserVo vo = userService.createNewUser(form(EMAIL, "   "), false);

            assertThat(vo.getName()).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("duplicate email")
    class DuplicateEmail {

        @Test
        @DisplayName("a second guest signup on the same email is rejected as MDR_EXISTS")
        void duplicateGuestIsRejected() {
            UserEntity existing = UserEntity.builder().id(1L).email(EMAIL).managed(false).build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.createNewUser(form(EMAIL, "Guest User"), false))
                    .isInstanceOf(UserException.class)
                    .hasMessage("You are already on the waitlist!")
                    .extracting(e -> ((UserException) e).getError())
                    .isEqualTo(MDRErrorCode.MDR_EXISTS);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("a guest cannot claim an email already held by a managed user")
        void guestCannotReuseManagedUserEmail() {
            UserEntity existing = UserEntity.builder().id(1L).email(EMAIL).managed(true).build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.createNewUser(form(EMAIL, null), false))
                    .isInstanceOf(UserException.class)
                    .hasMessage("You are already on the waitlist!");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("an existing guest is never promoted to managed by re-registering")
        void existingGuestIsNotPromoted() {
            UserEntity existing = UserEntity.builder().id(1L).email(EMAIL).managed(false).build();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.createNewUser(form(EMAIL, null), true))
                    .isInstanceOf(UserException.class);

            assertThat(existing.getManaged()).isFalse();
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("email handling")
    class EmailHandling {

        @Test
        @DisplayName("lookup and persistence use the email exactly as supplied - no trimming, no case folding")
        void emailIsNotNormalised() throws UserException {
            // "Guest@Example.com" does not collide with "guest@example.com" at the service layer,
            // so both can be inserted; the unique index on principal.email is case sensitive too.
            when(userRepository.findByEmail(" Guest@Example.com ")).thenReturn(Optional.empty());
            savePersistsWithId(3L);

            UserVo vo = userService.createNewUser(form(" Guest@Example.com ", null), false);

            verify(userRepository).findByEmail(" Guest@Example.com ");
            assertThat(vo.getEmail()).isEqualTo(" Guest@Example.com ");
        }

        @Test
        @DisplayName("a null email reaches the repository - the guard is bean validation at the controller")
        void nullEmailIsNotGuardedByTheService() throws UserException {
            when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
            savePersistsWithId(4L);

            UserVo vo = userService.createNewUser(form(null, "Guest User"), false);

            assertThat(vo.getEmail()).isNull();
            verify(userRepository).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("repository interaction")
    class RepositoryInteraction {

        @Test
        @DisplayName("the existence check runs before the insert")
        void existenceCheckPrecedesInsert() throws UserException {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            savePersistsWithId(9L);

            userService.createNewUser(form(EMAIL, null), false);

            org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(userRepository);
            inOrder.verify(userRepository).findByEmail(EMAIL);
            inOrder.verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("a unique index violation surfacing from save() is not translated to a UserException")
        void concurrentInsertRaceSurfacesRawException() {
            // two simultaneous /preview calls both pass the findByEmail check; the loser hits the
            // DB unique constraint and the raw DataIntegrityViolationException propagates unmapped
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.save(any(UserEntity.class)))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key value violates unique constraint"));

            assertThatThrownBy(() -> userService.createNewUser(form(EMAIL, null), false))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
    }
}
