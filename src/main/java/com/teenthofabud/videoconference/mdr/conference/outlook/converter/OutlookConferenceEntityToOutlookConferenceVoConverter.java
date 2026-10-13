package com.teenthofabud.videoconference.mdr.conference.outlook.converter;

import com.teenthofabud.videoconference.mdr.conference.outlook.entity.OutlookConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OutlookConferenceEntityToOutlookConferenceVoConverter
        implements Converter<OutlookConferenceEntity, OutlookConferenceVo> {

    @Override
    public OutlookConferenceVo convert(OutlookConferenceEntity source) {
        return OutlookConferenceVo.builder()
                .draftTrackingId(source.getDraftTrackingId())
                .eventId(source.getEventId())
                .userId(source.getUserId())
                .serverId(source.getServerId())
                .roomId(source.getRoomId())
                .status(source.getStatus())
                .version(source.getVersion())
                .lastModifiedDate(source.getLastModifiedDate())
                .build();
    }
}
