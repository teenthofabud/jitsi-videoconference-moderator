package com.teenthofabud.videoconference.mdr.conference.resource;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.teenthofabud.videoconference.mdr.conference.vo.ConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleCalendarEvent;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "ConferenceAPI", description = "Conference Management")
public interface ConferenceAPI {

    @Operation(method = "POST", summary = "Google Conference creation", description = "createNewGoogleConference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conference created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoogleConferenceDataVo.class))
            ),
    })
    ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@RequestBody(description = "UOM form", required = true,
            content = @Content(schema = @Schema(implementation = GoogleConferenceForm.class))) GoogleConferenceForm form) throws GoogleConferenceException, ServerPreferenceException, UserException;


    @Operation(method = "GET", summary = "Get conference by ID", description = "getConferenceByID")
    @Parameter(name = "Id",description = "Conference Identifier",schema = @Schema(implementation = Long.class), in = ParameterIn.PATH, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conference for given id",content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ConferenceVo.class))),
    })
    ResponseEntity<ConferenceVo> getGoogleConferenceByID(Long conferenceId) throws ConferenceException;


    @Operation(method = "GET", summary = "Get all conferences", description = "getAllConferencesByUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of conferences for the user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ConferenceVo.class))))
    })
    ResponseEntity<List<ConferenceVo>> getAllConferencesByUser(String calendarId) throws ServerPreferenceException;


    @Operation(method = "PATCH", summary = "Update Google conference by ID", description = "patchGoogleConferenceByID")
    @Parameter(name = "Id",description = "Conference Identifier",schema = @Schema(implementation = Long.class), in = ParameterIn.PATH, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference edited")
    })
    ResponseEntity<Void> patchGoogleConferenceByID(Long conferenceId,@RequestBody(description = "GoogleCalendarEvent form", required = true,
            content = @Content(schema = @Schema(implementation = GoogleCalendarEvent.class))) GoogleCalendarEvent form);

    @Operation(method = "DELETE", summary = "Delete Google conference by ID", description = "deleteConferenceByID")
    @Parameter(name = "Id",description = "Conference Identifier",schema = @Schema(implementation = Long.class), in = ParameterIn.PATH, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference deleted")
    })
    ResponseEntity<Void> deleteGoogleConferenceByID(Long conferenceId) throws ConferenceException;


}

