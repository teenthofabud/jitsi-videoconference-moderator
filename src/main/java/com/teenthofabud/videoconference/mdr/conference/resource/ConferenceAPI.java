package com.teenthofabud.videoconference.mdr.conference.resource;

import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "ConferenceAPI", description = "Conference Management")
public interface ConferenceAPI {

    @Operation(method = "POST", summary = "Google Conference creation", description = "createNewGoogleConference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conference created"),
    })
    ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(GoogleConferenceForm form) throws GoogleConferenceException;
}

