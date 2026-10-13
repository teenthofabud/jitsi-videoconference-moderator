package com.teenthofabud.videoconference.mdr.conference.outlook.service.impl;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.converter.OutlookConferenceEntityToOutlookConferenceVoConverter;
import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookAppointmentSentForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.repository.OutlookConferenceRepository;
import com.teenthofabud.videoconference.mdr.conference.outlook.service.OutlookConferenceService;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceSetupVo;
import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutlookConferenceServiceImpl implements OutlookConferenceService {

    private final OutlookConferenceRepository repository;
    private final OutlookConferenceEntityToOutlookConferenceVoConverter converter;
    private final UserService userService;
    private final ServerService serverService;

    @Override
    @Transactional(readOnly = true)
    public OutlookConferenceSetupVo getSetup(String email) throws ConferenceException {
        try {
            var user = userService.getUserByEmail(email);
            return new OutlookConferenceSetupVo(user.getId(), serverService.getServersByUserId(user.getId()));
        } catch (UserException e) {
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND, "Moderator user not found for Outlook account");
        }
    }

    @Override
    @Transactional
    public OutlookConferenceVo createConference(OutlookConferenceForm form) throws ConferenceException {
        requireUser(form.getUserId());
        requireServer(form.getServerId(), form.getUserId());
        var existing = repository.findByDraftTrackingId(form.getDraftTrackingId());
        if (existing.isPresent()) {
            if (!Objects.equals(existing.get().getUserId(), form.getUserId())) {
                throw new ConferenceException(MDRErrorCode.MDR_EXISTS,
                        "Outlook draft tracking ID is already associated with another user");
            }
            return toVo(existing.get());
        }

        OutlookConferenceEntity conference = OutlookConferenceEntity.builder()
                .draftTrackingId(form.getDraftTrackingId())
                .userId(form.getUserId())
                .serverId(form.getServerId())
                .roomId(UUID.randomUUID().toString())
                .status(ConferenceStatus.PENDING)
                .build();
        return toVo(repository.save(conference));
    }

    @Override
    @Transactional
    public OutlookConferenceVo markAppointmentSent(Long userId, String draftTrackingId,
                                                    OutlookAppointmentSentForm form) throws ConferenceException {
        requireUser(userId);
        OutlookConferenceEntity conference = findConference(userId, draftTrackingId);
        if (conference.getEventId() != null && !Objects.equals(conference.getEventId(), form.getEventId())) {
            throw new ConferenceException(MDRErrorCode.MDR_ATTRIBUTE_UNEXPECTED,
                    "Outlook appointment is already associated with another event ID");
        }
        conference.setEventId(form.getEventId());
        conference.setStatus(ConferenceStatus.ACTIVE);
        return toVo(repository.save(conference));
    }

    @Override
    @Transactional
    public void cancelConference(Long userId, String draftTrackingId) throws ConferenceException {
        requireUser(userId);
        OutlookConferenceEntity conference = findConference(userId, draftTrackingId);
        conference.setStatus(ConferenceStatus.CANCELLED);
        repository.save(conference);
    }

    private OutlookConferenceEntity findConference(Long userId, String draftTrackingId) throws ConferenceException {
        return repository.findByUserIdAndDraftTrackingId(userId, draftTrackingId)
                .orElseThrow(() -> new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                        "Outlook draft conference not found"));
    }

    private void requireUser(Long userId) throws ConferenceException {
        try {
            userService.getUserById(userId);
        } catch (UserException e) {
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("User with userId %s not found", userId));
        }
    }

    private void requireServer(Long serverId, Long userId) throws ConferenceException {
        try {
            var server = serverService.getServerById(serverId);
            if (server.getUserId() != null && !Objects.equals(server.getUserId(), userId)) {
                throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                        String.format("Server with ID %s not found", serverId));
            }
        } catch (ServerException e) {
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("Server with ID %s not found", serverId));
        }
    }

    private OutlookConferenceVo toVo(OutlookConferenceEntity conference) {
        OutlookConferenceVo vo = converter.convert(conference);
        try {
            var server = serverService.getServerById(conference.getServerId());
            String baseUrl = server.getServerUrl().trim();
            if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("http://")) {
                baseUrl = "https://" + baseUrl;
            }
            vo.setVideoUri(baseUrl.replaceAll("/+$", "") + "/" + conference.getRoomId());
            return vo;
        } catch (ServerException e) {
            throw new IllegalStateException("Conference server disappeared while building meeting URL", e);
        }
    }
}
