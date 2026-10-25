package com.teenthofabud.videoconference.mdr.conference.calcom.service.impl;

import com.teenthofabud.videoconference.mdr.conference.calcom.converter.CalComConferenceEntityToCalComConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.calcom.entity.CalComConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.calcom.reducer.CalComConferenceFormToCalComConferenceEntityReducer;
import com.teenthofabud.videoconference.mdr.conference.calcom.repository.CalComConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.calcom.service.CalComConferenceService;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
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
import java.util.List;
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
        log.info("Received Cal.com conference create request: userId={}, bookingUid={}, serverId={}",
                form.getUserId(), form.getBookingUid(), form.getServerId());
        requireUser(form.getUserId());
        requireServer(form.getServerId(), form.getUserId());
        var existing = repository.findByBookingUid(form.getBookingUid());
        if (existing.isPresent()) {
            if (!Objects.equals(existing.get().getUserId(), form.getUserId())) {
                log.warn("Rejected Cal.com conference create because booking UID belongs to another user (requested user {})",
                        form.getUserId());
                throw new ConferenceException(MDRErrorCode.MDR_EXISTS,
                        "Cal.com booking UID is already associated with another user");
            }
            log.info("Reused existing Cal.com conference for user {}", form.getUserId());
            return toVo(existing.get());
        }
        CalComConferenceEntity created = repository.save(reducer.reduce(form));
        log.info("Created Cal.com conference for user {} on server {}", form.getUserId(), form.getServerId());
        return toVo(created);
    }

    @Override
    @Transactional(readOnly = true)
    public CalComConferenceVo getConference(Long userId, String bookingUid) throws ConferenceException {
        log.debug("Received Cal.com conference lookup: userId={}, bookingUid={}", userId, bookingUid);
        requireUser(userId);
        CalComConferenceVo conference = repository.findByUserIdAndBookingUid(userId, bookingUid)
                .map(this::toVo)
                .orElseThrow(() -> notFound(bookingUid));
        log.debug("Returning Cal.com conference: userId={}, bookingUid={}, status={}, serverId={}",
                userId, bookingUid, conference.getStatus(), conference.getServerId());
        return conference;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalComConferenceVo> getAllConferencesByUserId(Long userId) throws ConferenceException {
        log.debug("Received Cal.com conference list request: userId={}", userId);
        requireUser(userId);
        List<CalComConferenceVo> conferences = repository.findAllByUserId(userId).stream()
                .map(this::toVo)
                .toList();
        log.debug("Loaded {} Cal.com conferences for user {}", conferences.size(), userId);
        return conferences;
    }

    @Override
    @Transactional
    public void patchConference(Long userId, String bookingUid, JsonPatch patches) throws ConferenceException {
        log.info("Received Cal.com conference patch request: userId={}, bookingUid={}", userId, bookingUid);
        requireUser(userId);
        CalComConferenceEntity original = repository.findByUserIdAndBookingUid(userId, bookingUid)
                .orElseThrow(() -> notFound(bookingUid));
        String before = objectMapper.writeValueAsString(original);
        JsonObject patchedJson = patches.apply(readObject(before));
        CalComConferenceEntity patched = objectMapper.readValue(patchedJson.toString(), CalComConferenceEntity.class);
        rejectIdentityChanges(original, patched);
        if (!Objects.equals(original.getServerId(), patched.getServerId())) {
            requireServer(patched.getServerId(), original.getUserId());
        }
        repository.save(patched);
        log.info("Updated Cal.com conference for user {}", userId);
    }

    @Override
    @Transactional
    public void deleteConference(Long userId, String bookingUid) throws ConferenceException {
        log.info("Received Cal.com conference cancellation request: userId={}, bookingUid={}", userId, bookingUid);
        requireUser(userId);
        CalComConferenceEntity conference = repository.findByUserIdAndBookingUid(userId, bookingUid)
                .orElseThrow(() -> notFound(bookingUid));
        conference.setStatus(ConferenceStatus.CANCELLED);
        repository.save(conference);
        log.info("Cancelled Cal.com conference for user {}", userId);
    }

    private void rejectIdentityChanges(CalComConferenceEntity original, CalComConferenceEntity patched) throws ConferenceException {
        if (Objects.equals(original.getId(), patched.getId())
                && Objects.equals(original.getBookingUid(), patched.getBookingUid())
                && Objects.equals(original.getUserId(), patched.getUserId())
                && Objects.equals(original.getVersion(), patched.getVersion())
                && Objects.equals(original.getLastModifiedDate(), patched.getLastModifiedDate())) {
            return;
        }
        log.warn("Rejected Cal.com conference patch that changed identity or audit fields for user {}", original.getUserId());
        throw new ConferenceException(MDRErrorCode.MDR_ATTRIBUTE_UNEXPECTED,
                "Cal.com booking identity and audit fields cannot be patched");
    }

    private void requireUser(Long userId) throws ConferenceException {
        try {
            userService.getUserById(userId);
        } catch (UserException e) {
            log.warn("Cal.com conference request references unknown Moderator user {}", userId);
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("User with userId %s not found", userId));
        }
    }

    private void requireServer(Long serverId, Long userId) throws ConferenceException {
        try {
            var server = serverService.getServerById(serverId);
            if (server.getUserId() != null && !Objects.equals(server.getUserId(), userId)) {
                log.warn("Rejected Cal.com conference request: server {} belongs to a different user than {}", serverId, userId);
                throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                        String.format("Server with ID %s not found", serverId));
            }
        } catch (ServerException e) {
            log.warn("Cal.com conference request references unknown server {} for user {}", serverId, userId);
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("Server with ID %s not found", serverId));
        }
    }

    private CalComConferenceVo toVo(CalComConferenceEntity conference) {
        CalComConferenceVo vo = converter.convert(conference);
        try {
            var server = serverService.getServerById(conference.getServerId());
            vo.setVideoUri(buildMeetingUrl(server.getServerUrl(), conference.getRoomId()));
            return vo;
        } catch (ServerException e) {
            log.error("Cal.com conference server {} is missing for user {}", conference.getServerId(), conference.getUserId());
            throw new IllegalStateException("Conference server disappeared while building meeting URL", e);
        }
    }

    private String buildMeetingUrl(String serverUrl, String roomId) {
        String normalizedServerUrl = serverUrl.trim();
        if (!normalizedServerUrl.startsWith("https://") && !normalizedServerUrl.startsWith("http://")) {
            normalizedServerUrl = "https://" + normalizedServerUrl;
        }
        normalizedServerUrl = normalizedServerUrl.replaceAll("/+$", "");
        return normalizedServerUrl + "/" + roomId;
    }

    private ConferenceException notFound(String bookingUid) {
        log.warn("Cal.com conference record not found for bookingUid={}", bookingUid);
        return new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                String.format("Cal.com booking with UID %s not found", bookingUid));
    }

    private JsonObject readObject(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }
}
