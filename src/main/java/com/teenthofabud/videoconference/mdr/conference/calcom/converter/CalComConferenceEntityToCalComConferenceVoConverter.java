package com.teenthofabud.videoconference.mdr.conference.calcom.converter;

import com.teenthofabud.videoconference.mdr.conference.calcom.entity.CalComConferenceEntity;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CalComConferenceEntityToCalComConferenceVoConverter
        implements Converter<CalComConferenceEntity, CalComConferenceVo> {

    @Override
    public CalComConferenceVo convert(CalComConferenceEntity source) {
        return CalComConferenceVo.builder()
                .bookingUid(source.getBookingUid())
                .bookingId(source.getBookingId())
                .userId(source.getUserId())
                .serverId(source.getServerId())
                .roomId(source.getRoomId())
                .status(source.getStatus())
                .version(source.getVersion())
                .lastModifiedDate(source.getLastModifiedDate())
                .build();
    }
}
