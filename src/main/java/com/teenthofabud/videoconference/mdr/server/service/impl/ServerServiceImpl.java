package com.teenthofabud.videoconference.mdr.server.service.impl;

import com.teenthofabud.videoconference.mdr.error.core.MDRErrorCode;
import com.teenthofabud.videoconference.mdr.server.constants.ServerType;
import com.teenthofabud.videoconference.mdr.server.converter.ServerEntityToVoConverter;
import com.teenthofabud.videoconference.mdr.server.converter.ServerPreferenceFormToEntityConverter;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerForm;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerUpdateForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.server.repository.ServerRepository;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerPreferenceFormToEntityConverter serverPreferenceFormToEntityConverter;
    private final ServerEntityToVoConverter serverEntityToVoConverter;
    private final UserService userService;

    @Override
    public ServerVo getServerById(Long id) throws ServerException {
        return serverRepository.findById(id)
                .map(serverEntityToVoConverter::convert)
                .orElseThrow(() -> {
                    log.error("Server not found with id: {}", id);
                    return new ServerException(
                            MDRErrorCode.MDR_NOT_FOUND,
                           String.format("Server not found with id: %s", id)
                    );
                });
    }

    @Override
    public ServerVo addServerPreference(ServerPreferenceForm form) throws ServerException {
        form.setServerUrl(sanitizeUrl(form.getServerUrl()));
        if(serverRepository.existsByUserIdAndServerUrl(form.getUserId(), form.getServerUrl())){
            log.error("A server already exists with url {} for the user with id {}",form.getUserId(),form.getServerUrl());
            throw new ServerException(MDRErrorCode.MDR_EXISTS,String.format("A server already exists with url %s for the user with id %s",form.getUserId(),form.getServerUrl()));
        }
        // temporary check which has to be removed once proper End user OAuth is setup
        try{
            userService.getUserById(form.getUserId());
        } catch (UserException e) {
            log.error("User doesn't exist with ID : {}", form.getUserId());
            throw new ServerException(MDRErrorCode.MDR_ATTRIBUTE_INVALID,String.format("User doesn't exist with id %s",form.getUserId()));
        }
        ServerEntity server = serverPreferenceFormToEntityConverter.convert(form);
        server.setServerType(ServerType.CUSTOM);
        server = serverRepository.save(server);
        return serverEntityToVoConverter.convert(server);
    }

    @Override
    public ServerVo updateServerPreference(ServerPreferenceUpdateForm form) throws ServerException {
        form.setServerUrl(sanitizeUrl(form.getServerUrl()));
        ServerEntity existingServer = serverRepository.findById(form.getServerId())
                .orElseThrow(() -> {
                    log.error("Server preference not found with serverId: {}", form.getUserId(), form.getServerId());
                    return new ServerException(MDRErrorCode.MDR_NOT_FOUND, String.format("Server not found with id %s",form.getServerId()));
                });
        // temporary check which has to be removed once proper End user OAuth is setup
        try{
            userService.getUserById(form.getUserId());
        } catch (UserException e) {
            log.error("User doesn't exist with ID : {}", form.getUserId());
            throw new ServerException(MDRErrorCode.MDR_ATTRIBUTE_INVALID,String.format("User doesn't exist with id %s",form.getUserId()));
        }
        // ensure the update won't result in unique constraint violation
        // since server URLs are unique per user
        if (!existingServer.getServerUrl().equals(form.getServerUrl())) {
            if (serverRepository.existsByUserIdAndServerUrl(form.getUserId(), form.getServerUrl())) {
                log.error("User with id {} already has a preferred server with the url {}", form.getUserId(), form.getServerUrl());
                throw new ServerException(MDRErrorCode.MDR_EXISTS, String.format("User with id %s already has a preferred server with the url %s",form.getUserId(),form.getServerUrl()));
            }
        }
        existingServer.setName(form.getName());
        existingServer.setServerUrl(form.getServerUrl());
        existingServer = serverRepository.save(existingServer);
        return serverEntityToVoConverter.convert(existingServer);
    }

    @Override
    public ServerVo createDefaultServer(DefaultServerForm form) throws ServerException {
        String cleanUrl = sanitizeUrl(form.getServerUrl());
        if (serverRepository.existsByServerUrlAndServerType(cleanUrl, ServerType.DEFAULT)) {
            log.error("Default server already exists with the url {}", cleanUrl);
            throw new ServerException(MDRErrorCode.MDR_EXISTS, String.format("Default server already exists with the url %s",cleanUrl));
        }
        ServerEntity server = ServerEntity.builder()
                .name(form.getName())
                .serverUrl(cleanUrl)
                .serverType(ServerType.DEFAULT)
                .build();
        server = serverRepository.save(server);
        return serverEntityToVoConverter.convert(server);
    }

    @Override
    public ServerVo updateDefaultServer(DefaultServerUpdateForm form) throws ServerException {
        String cleanUrl = sanitizeUrl(form.getServerUrl());
        ServerEntity existingServer = serverRepository.findById(form.getServerId())
                .orElseThrow(() -> {
                    log.error("Default server doesn't exist with the id {}", form.getServerId());
                    return new ServerException(MDRErrorCode.MDR_NOT_FOUND, String.format("Default server doesn't exist with the id  %s",form.getServerId()));
                });
        if (existingServer.getServerType() != ServerType.DEFAULT) {
            // reported as not found rather than as a type mismatch, so this endpoint cannot be used
            // to discover, or to rewrite, another user's server preference
            log.error("Server with id : {} is not a DEFAULT server", form.getServerId());
            throw new ServerException(MDRErrorCode.MDR_NOT_FOUND, String.format("Default server doesn't exist with the id  %s",form.getServerId()));
        }
        // Ensure the update won't result in a unique constraint violation
        if (!existingServer.getServerUrl().equals(cleanUrl)) {
            if (serverRepository.existsByServerUrlAndServerType(cleanUrl, ServerType.DEFAULT)) {
                log.error("Default server already exists with the url {}", cleanUrl);
                throw new ServerException(MDRErrorCode.MDR_EXISTS, String.format("Default server already exists with the url %s",cleanUrl));
            }
        }
        existingServer.setName(form.getName());
        existingServer.setServerUrl(cleanUrl);
        existingServer = serverRepository.save(existingServer);
        return serverEntityToVoConverter.convert(existingServer);
    }

    @Override
    // temporarily returns default servers merged to user's preferred servers;
    // change later if required
    // if plugin dropdown lists by name, guard against a common naming convention upon server creation
    public List<ServerVo> getServersByUserId(Long userId) {
        List<ServerEntity> userServers = serverRepository.findAllByUserId(userId);
        List<ServerEntity> defaultServers = serverRepository.findAllByServerType(ServerType.DEFAULT);
        // a DEFAULT row that also carries this user's id matches both queries, so keep the first
        // sighting of each server and preserve the preferences-before-defaults ordering
        return Stream.concat(userServers.stream(), defaultServers.stream())
                .collect(Collectors.toMap(ServerEntity::getId, Function.identity(),
                        (firstSeen, duplicate) -> firstSeen, LinkedHashMap::new))
                .values()
                .stream()
                .map(serverEntityToVoConverter::convert)
                .toList();
    }

    @Override
    public List<ServerVo> getAllDefaultServers() {
        return serverRepository.findAllByServerType(ServerType.DEFAULT)
                .stream()
                .map(serverEntityToVoConverter::convert)
                .toList();
    }

    private String sanitizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }
        // Trim whitespace
        String cleanUrl = rawUrl.trim();
        // Aggressively strip ALL leading occurrences of http:// or https:// (case-insensitive)
        // This fixes the http://https:// and https://https:// bugs
        cleanUrl = cleanUrl.replaceFirst("(?i)^(https?://)+", "");
        // Strip all trailing slashes without a while loop
        cleanUrl = cleanUrl.replaceFirst("/*$", "");
        // Prepend a single scheme 
        return "https://" + cleanUrl;
    }
}
