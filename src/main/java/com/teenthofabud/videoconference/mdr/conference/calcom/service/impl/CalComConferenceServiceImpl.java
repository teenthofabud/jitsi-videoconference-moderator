package com.teenthofabud.videoconference.mdr.conference.calcom.service.impl;

import com.teenthofabud.videoconference.mdr.conference.calcom.converter.CalComConferenceEntityToCalComConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.calcom.entity.CalComConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.calcom.reducer.CalComConferenceFormToCalComConferenceEntityReducer;
import com.teenthofabud.videoconference.mdr.conference.calcom.repository.CalComConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.calcom.service.CalComConferenceService;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.io.StringReader;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalComConferenceServiceImpl implements CalComConferenceService {

    private final CalComConferenceRepository repository;
    private final CalComConferenceEntityToCalComConferenceVoConverter converter;
    private final CalComConferenceFormToCalComConferenceEntityReducer reducer;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ServerService serverService;

    @Override
    @Transactional
    public CalComConferenceVo createConference(CalComConferenceForm form) throws ConferenceException {
        requireUser(form.getUserId());
        requireServer(form.getServerId());
        var existing = repository.findByBookingUid(form.getBookingUid());
        if (existing.isPresent()) {
            if (!Objects.equals(existing.get().getUserId(), form.getUserId())) {
                throw new ConferenceException(MDRErrorCode.MDR_EXISTS,
                        "Cal.com booking UID is already associated with another user");
            }
            return converter.convert(existing.get());
        }
        return converter.convert(repository.save(reducer.reduce(form)));
    }

    @Override
    @Transactional(readOnly = true)
    public CalComConferenceVo getConference(Long userId, String bookingUid) throws ConferenceException {
        requireUser(userId);
        return repository.findByUserIdAndBookingUid(userId, bookingUid)
                .map(converter::convert)
                .orElseThrow(() -> notFound(bookingUid));
    }

    @Override
    @Transactional
    public void patchConference(Long userId, String bookingUid, JsonPatch patches) throws ConferenceException {
        requireUser(userId);
        CalComConferenceEntity original = repository.findByUserIdAndBookingUid(userId, bookingUid)
                .orElseThrow(() -> notFound(bookingUid));
        String before = objectMapper.writeValueAsString(original);
        JsonObject patchedJson = patches.apply(readObject(before));
        CalComConferenceEntity patched = objectMapper.readValue(patchedJson.toString(), CalComConferenceEntity.class);
        rejectIdentityChanges(original, patched);
        if (!Objects.equals(original.getServerId(), patched.getServerId())) {
            requireServer(patched.getServerId());
        }
        repository.save(patched);
    }

    private void rejectIdentityChanges(CalComConferenceEntity original, CalComConferenceEntity patched) throws ConferenceException {
        if (Objects.equals(original.getId(), patched.getId())
                && Objects.equals(original.getBookingUid(), patched.getBookingUid())
                && Objects.equals(original.getUserId(), patched.getUserId())
                && Objects.equals(original.getVersion(), patched.getVersion())
                && Objects.equals(original.getLastModifiedDate(), patched.getLastModifiedDate())) {
            return;
        }
        throw new ConferenceException(MDRErrorCode.MDR_ATTRIBUTE_UNEXPECTED,
                "Cal.com booking identity and audit fields cannot be patched");
    }

    private void requireUser(Long userId) throws ConferenceException {
        try {
            userService.getUserById(userId);
        } catch (UserException e) {
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("User with userId %s not found", userId));
        }
    }

    private void requireServer(Long serverId) throws ConferenceException {
        try {
            serverService.getServerById(serverId);
        } catch (ServerException e) {
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("Server with ID %s not found", serverId));
        }
    }

    private ConferenceException notFound(String bookingUid) {
        return new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                String.format("Cal.com booking with UID %s not found", bookingUid));
    }

    private JsonObject readObject(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }
}
