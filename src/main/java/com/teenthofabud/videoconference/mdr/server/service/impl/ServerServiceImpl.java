package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerPreferenceFormToEntityConverter serverPreferenceFormToEntityConverter;
    private final ServerEntityToVoConverter serverEntityToVoConverter;

    @Override
    public ServerVo getServerById(Long id) throws ServerException {
        return serverRepository.findById(id)
                .map(serverEntityToVoConverter::convert)
                .orElseThrow(() -> {
                    log.error("Server not found with id: {}", id);
                    return new ServerException(
                            MDRErrorCode.MDR_NOT_FOUND,
                            new Object[]{id}
                    );
                });
    }

    @Override
    public ServerVo addServerPreference(ServerPreferenceForm form) throws ServerException {
        form.setServerUrl(sanitizeUrl(form.getServerUrl()));
        if(serverRepository.existsByUserIdAndServerUrl(form.getUserId(), form.getServerUrl())){
            log.error("Preferred server for the user({}) already exists with server url : {}",form.getUserId(),form.getServerUrl());
            throw new ServerException(MDRErrorCode.MDR_EXISTS,new Object[]{String.format("%s:%s",form.getUserId(),form.getServerUrl())});
        }
        ServerEntity server = serverPreferenceFormToEntityConverter.convert(form);
        server = serverRepository.save(server);
        return serverEntityToVoConverter.convert(server);
    }

    @Override
    public ServerVo updateServerPreference(ServerPreferenceUpdateForm form) throws ServerException {
        form.setServerUrl(sanitizeUrl(form.getServerUrl()));
        ServerEntity existingServer = serverRepository.findByIdAndUserId(form.getServerId(),form.getUserId())
                .orElseThrow(() -> {
                    log.error("Server preference not found for userId: {} and serverId: {}", form.getUserId(), form.getServerId());
                    return new ServerException(MDRErrorCode.MDR_NOT_FOUND, new Object[]{String.format("%s:%s",form.getUserId(),form.getServerId())});
                });
        // ensure the update won't result in unique constraint violation
        // since server URLs are unique per user
        if (!existingServer.getServerUrl().equals(form.getServerUrl())) {
            if (serverRepository.existsByUserIdAndServerUrl(form.getUserId(), form.getServerUrl())) {
                log.error("User({}) already has a preferred server with the url : {}", form.getUserId(), form.getServerUrl());
                throw new ServerException(MDRErrorCode.MDR_EXISTS, new Object[]{String.format("%s:%s",form.getUserId(),form.getServerUrl())});
            }
        }
        existingServer.setName(form.getName());
        existingServer.setServerUrl(form.getServerUrl());
        existingServer = serverRepository.save(existingServer);
        return serverEntityToVoConverter.convert(existingServer);
    }

    private String sanitizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }
        String cleanUrl = rawUrl.trim().toLowerCase();
        while (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        }
        if (cleanUrl.startsWith("http://")) {
            cleanUrl = cleanUrl.replaceFirst("http://", "https://");
        } else if (!cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }
        return cleanUrl;
    }
}
