package com.teenthofabud.videoconference.moderator.conference.resource;

import com.teenthofabud.videoconference.moderator.conference.dto.ConferenceDto;
import com.teenthofabud.videoconference.moderator.conference.form.google.GoogleConferenceForm;
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
    ResponseEntity<ConferenceDto> createNewGoogleConference(GoogleConferenceForm form);
}

