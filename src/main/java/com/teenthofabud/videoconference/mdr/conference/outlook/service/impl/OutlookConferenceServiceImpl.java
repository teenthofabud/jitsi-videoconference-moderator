package com.teenthofabud.videoconference.mdr.conference.outlook.service.impl;

import com.teenthofabud.videoconference.mdr.conference.constants.ConferenceStatus;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.converter.OutlookConferenceVoAssembler;
import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookEventLinkForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceUpdateForm;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutlookConferenceServiceImpl implements OutlookConferenceService {

    private final OutlookConferenceRepository repository;
    private final OutlookConferenceVoAssembler voAssembler;
    private final UserService userService;
    private final ServerService serverService;

    @Override
    @Transactional(readOnly = true)
    public OutlookConferenceSetupVo getSetup(String email) throws ConferenceException {
        log.debug("Received Outlook setup request");
        try {
            var user = userService.getUserByEmail(email);
            var userServers = serverService.getServersByUserId(user.getId());
            log.info("Loaded Outlook setup for Moderator user {} ({} servers)", user.getId(), userServers.size());
            return new OutlookConferenceSetupVo(user.getId(), userServers);
        } catch (UserException e) {
            log.warn("Outlook setup could not resolve a Moderator user for the signed-in account");
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND, "Moderator user not found for Outlook account");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutlookConferenceVo> getAllConferencesByUserId(Long userId) throws ConferenceException {
        log.debug("Received Outlook conference list request: userId={}", userId);
        requireUser(userId);
        List<OutlookConferenceVo> conferences = repository.findAllByUserId(userId).stream()
                .map(voAssembler::assemble)
                .toList();
        log.debug("Returning {} Outlook conferences for userId={}", conferences.size(), userId);
        return conferences;
    }

    @Override
    @Transactional(readOnly = true)
    public OutlookConferenceVo getConference(Long userId, String draftTrackingId) throws ConferenceException {
        log.debug("Received Outlook conference lookup: userId={}, draftTrackingId={}", userId, draftTrackingId);
        requireUser(userId);
        OutlookConferenceVo conference = voAssembler.assemble(findConference(userId, draftTrackingId));
        log.debug("Returning Outlook conference: userId={}, draftTrackingId={}, status={}, eventLinked={}",
                userId, draftTrackingId, conference.getStatus(), conference.getEventId() != null);
        return conference;
    }

    @Override
    @Transactional
    public OutlookConferenceVo updateConference(Long userId, String draftTrackingId,
            OutlookConferenceUpdateForm form) throws ConferenceException {
        log.info("Received Outlook conference server update: userId={}, draftTrackingId={}, serverId={}",
                userId, draftTrackingId, form.getServerId());
        requireUser(userId);
        OutlookConferenceEntity conference = findConference(userId, draftTrackingId);
        requireServer(form.getServerId(), userId);
        conference.setServerId(form.getServerId());
        conference.setStatus(ConferenceStatus.ACTIVE);
        OutlookConferenceEntity saved = repository.save(conference);
        log.info("Changed Outlook conference server for Moderator user {} to {}", userId, form.getServerId());
        return voAssembler.assemble(saved);
    }

    @Override
    @Transactional
    public OutlookConferenceVo createConference(OutlookConferenceForm form) throws ConferenceException {
        log.info("Received Outlook conference create request: userId={}, draftTrackingId={}, serverId={}",
                form.getUserId(), form.getDraftTrackingId(), form.getServerId());
        requireUser(form.getUserId());
        requireServer(form.getServerId(), form.getUserId());
        var existing = repository.findByDraftTrackingId(form.getDraftTrackingId());
        if (existing.isPresent()) {
            if (!Objects.equals(existing.get().getUserId(), form.getUserId())) {
                throw new ConferenceException(MDRErrorCode.MDR_EXISTS,
                        "Outlook draft tracking ID is already associated with another user");
            }
            log.info("Reused Outlook conference for Moderator user {}", form.getUserId());
            return voAssembler.assemble(existing.get());
        }

        OutlookConferenceEntity conference = OutlookConferenceEntity.builder()
                .draftTrackingId(form.getDraftTrackingId())
                .userId(form.getUserId())
                .serverId(form.getServerId())
                .roomId(UUID.randomUUID().toString())
                .status(ConferenceStatus.ACTIVE)
                .build();
        OutlookConferenceEntity saved = repository.save(conference);
        log.info("Created Outlook conference for Moderator user {} on server {}", form.getUserId(), form.getServerId());
        return voAssembler.assemble(saved);
    }

    @Override
    @Transactional
    public OutlookConferenceVo linkAppointmentEvent(Long userId, String draftTrackingId,
                                                    OutlookEventLinkForm form) throws ConferenceException {
        log.info("Received Outlook event-link request: userId={}, draftTrackingId={}, eventId={}",
                userId, draftTrackingId, form.getEventId());
        requireUser(userId);
        OutlookConferenceEntity conference = findConference(userId, draftTrackingId);
        if (conference.getEventId() != null && !Objects.equals(conference.getEventId(), form.getEventId())) {
            throw new ConferenceException(MDRErrorCode.MDR_ATTRIBUTE_UNEXPECTED,
                    "Outlook appointment is already linked to another event ID");
        }
        conference.setEventId(form.getEventId());
        conference.setStatus(ConferenceStatus.ACTIVE);
        OutlookConferenceEntity saved = repository.save(conference);
        log.info("Linked Outlook event to conference for Moderator user {}", userId);
        return voAssembler.assemble(saved);
    }

    @Override
    @Transactional
    public void cancelConference(Long userId, String draftTrackingId) throws ConferenceException {
        log.info("Received Outlook conference cancellation request: userId={}, draftTrackingId={}", userId, draftTrackingId);
        requireUser(userId);
        OutlookConferenceEntity conference = findConference(userId, draftTrackingId);
        conference.setStatus(ConferenceStatus.CANCELLED);
        repository.save(conference);
        log.info("Cancelled Outlook conference for Moderator user {}", userId);
    }

    private OutlookConferenceEntity findConference(Long userId, String draftTrackingId) throws ConferenceException {
        return repository.findByUserIdAndDraftTrackingId(userId, draftTrackingId)
                .orElseThrow(() -> {
                    log.warn("Outlook conference not found: userId={}, draftTrackingId={}", userId, draftTrackingId);
                    return new ConferenceException(MDRErrorCode.MDR_NOT_FOUND, "Outlook draft conference not found");
                });
    }

    private void requireUser(Long userId) throws ConferenceException {
        try {
            userService.getUserById(userId);
        } catch (UserException e) {
            log.warn("Outlook conference request references unknown Moderator user {}", userId);
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("User with userId %s not found", userId));
        }
    }

    private void requireServer(Long serverId, Long userId) throws ConferenceException {
        try {
            var server = serverService.getServerById(serverId);
            if (server.getUserId() != null && !Objects.equals(server.getUserId(), userId)) {
                log.warn("Rejected Outlook conference request: server {} belongs to a different user than {}", serverId, userId);
                throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                        String.format("Server with ID %s not found", serverId));
            }
        } catch (ServerException e) {
            log.warn("Outlook conference request references unknown server {} for user {}", serverId, userId);
            throw new ConferenceException(MDRErrorCode.MDR_NOT_FOUND,
                    String.format("Server with ID %s not found", serverId));
        }
    }

}
