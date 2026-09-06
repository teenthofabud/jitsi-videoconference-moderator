package com.teenthofabud.videoconference.mdr.error.converter;

import com.teenthofabud.videoconference.mdr.error.core.MDRBaseException;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;

@Component
public class MDRExceptionToErrorVoConverter implements Converter<MDRBaseException, ErrorVo> {


    @Override
    public ErrorVo convert(MDRBaseException source) {

        return ErrorVo.builder()
                .errorCode(source.getError().getErrorCode())
                .message(source.getMessage())
                .build();
    }


}