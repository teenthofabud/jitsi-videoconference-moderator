package com.teenthofabud.videoconference.mdr.conference.resource.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.error.google.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceFormV2;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.form.google.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
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

@Tag(name = "GoogleConferenceAPI", description = "Google Conference Management")
public interface GoogleConferenceAPI {

    @Operation(method = "POST", summary = "Google Conference creation", description = "createNewGoogleConference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conference created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoogleConferenceDataVo.class))
            ),
    })
    ResponseEntity<GoogleConferenceDataVo> createNewGoogleConference(@RequestBody(description = "Google conference form", required = true,
            content = @Content(schema = @Schema(implementation = GoogleConferenceFormV2.class))) GoogleConferenceFormV2 form) throws GoogleConferenceException, ConferenceException;


    @Operation(method = "GET", summary = "Get conference by ID", description = "getGoogleConferenceByUserEmailAndEventId")
    @Parameter(name = "eventId",description = "Calendar Event Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "email",description = "User Email",schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conference for given id",content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoogleConferenceVo.class))),
    })
    ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserEmailAndEventId(String eventId, String email) throws ConferenceException, GoogleConferenceException, UserException;


    @Operation(method = "POST", summary = "Get all conferences", description = "getAllConferencesByUserEmail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of conferences for the user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = GoogleConferenceVo.class))))
    })
    @Parameter(name = "email",description = "User Email",schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, required = true)
    ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUserEmail(String userId) throws GoogleConferenceException, UserException;


    @Operation(method = "PATCH", summary = "Update Google conference by ID", description = "patchGoogleConferenceByUserEmailAndEventIdpatchGoogleConferenceByUserEmailAndEventId")
    @Parameter(name = "eventId",description = "Calendar Event Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "email",description = "User Email",schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference edited")
    })
    ResponseEntity<Void> patchGoogleConferenceByUserEmailAndEventId(String eventId, String userId, @RequestBody(description = "JsonPatch", required = true,
            content = @Content(mediaType = HttpMediaType.APPLICATION_JSON_PATCH,schema = @Schema(implementation = JsonPatch.class))) JsonPatch patches) throws GoogleConferenceException, JsonPatchException, JsonProcessingException, UserException;

    @Operation(method = "DELETE", summary = "Delete Google conference by ID", description = "deleteGoogleConferenceByUserEmailAndEventId")
    @Parameter(name = "eventId",description = "Conference Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "email",description = "User Email",schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference deleted")
    })
    ResponseEntity<Void> deleteGoogleConferenceByUserEmailAndEventId(String email, String eventId) throws GoogleConferenceException, UserException;


}

