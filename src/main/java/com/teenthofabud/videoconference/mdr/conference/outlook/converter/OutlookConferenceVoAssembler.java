package com.teenthofabud.videoconference.mdr.conference.outlook.converter;

import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Builds Outlook conference response values, including the meeting URL for the selected server. */
@Component
@RequiredArgsConstructor
public class OutlookConferenceVoAssembler {
    private final OutlookConferenceEntityToOutlookConferenceVoConverter converter;
    private final ServerService serverService;

    /**
     * Converts a stored conference to its API value and constructs its video meeting URL.
     *
     * @param conference conference entity to expose
     * @return API value including the normalized server URL and room ID
     * @throws IllegalStateException if the conference references a server that no longer exists
     */
    public OutlookConferenceVo assemble(OutlookConferenceEntity conference) {
        OutlookConferenceVo vo = converter.convert(conference);
        try {
            var server = serverService.getServerById(conference.getServerId());
            String baseUrl = server.getServerUrl().trim();
            if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("http://")) {
                baseUrl = "https://" + baseUrl;
            }
            vo.setVideoUri(baseUrl.replaceAll("/+$", "") + "/" + conference.getRoomId());
            return vo;
        } catch (ServerException ex) {
            throw new IllegalStateException("Conference server disappeared while building meeting URL", ex);
        }
    }
}
