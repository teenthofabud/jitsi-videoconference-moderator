package com.teenthofabud.videoconference.mdr.conference.google.resource;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.error.GoogleConferenceException;
import com.teenthofabud.videoconference.mdr.conference.google.form.GoogleConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.conference.google.vo.GoogleConferenceVo;
import com.teenthofabud.videoconference.mdr.constants.HttpMediaType;
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
import jakarta.json.JsonPatch;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
            content = @Content(schema = @Schema(implementation = GoogleConferenceForm.class))) @Valid GoogleConferenceForm form) throws GoogleConferenceException, ConferenceException;


    @Operation(method = "GET", summary = "Get conference by ID", description = "getGoogleConferenceByUserIdAndEventId")
    @Parameter(name = "eventId",description = "Calendar Event Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId",description = "User ID",schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conference for given id",content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoogleConferenceVo.class))),
    })
    ResponseEntity<GoogleConferenceVo> getGoogleConferenceByUserIdAndEventId(String eventId, Long userId) throws ConferenceException, GoogleConferenceException;


    @Operation(method = "GET", summary = "Get all conferences", description = "getAllConferencesByUserId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of conferences for the user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = GoogleConferenceVo.class))))
    })
    @Parameter(name = "userId",description = "User ID",schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, required = true)
    ResponseEntity<List<GoogleConferenceVo>> getAllGoogleConferencesByUserId(Long userId) throws GoogleConferenceException;


    @Operation(method = "PATCH", summary = "Update Google conference by ID", description = "patchGoogleConferenceByUserIdAndEventId")
    @Parameter(name = "eventId",description = "Calendar Event Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId",description = "User ID",schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference edited")
    })
    ResponseEntity<Void> patchGoogleConferenceByUserIdAndEventId(String eventId, Long userId, @RequestBody(description = "JsonPatch", required = true,
            content = @Content(mediaType = HttpMediaType.APPLICATION_JSON_PATCH,schema = @Schema(implementation = String.class))) JsonPatch patches) throws GoogleConferenceException;

    @Operation(method = "DELETE", summary = "Delete Google conference by ID", description = "deleteGoogleConferenceByUserIdAndEventId")
    @Parameter(name = "eventId",description = "Conference Identifier",schema = @Schema(implementation = String.class), in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId",description = "User ID",schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conference deleted")
    })
    ResponseEntity<Void> deleteGoogleConferenceByUserIdAndEventId(String eventId, Long userId) throws GoogleConferenceException;


}

