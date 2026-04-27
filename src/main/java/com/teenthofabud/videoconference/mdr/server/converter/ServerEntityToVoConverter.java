package com.teenthofabud.videoconference.mdr.server.converter;

import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.entity.ServerEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class ServerEntityToVoConverter implements Converter<ServerEntity, ServerVo> {


    @Override
    public ServerVo convert(ServerEntity source) {

        return ServerVo.builder()
                .name(source.getName())
                .id(source.getId())
                .serverUrl(source.getServerUrl())
                .serverType(source.getServerType())
                .owner(source.getOwner() != null ? source.getOwner().getName() : null)
                .build();
    }
}
