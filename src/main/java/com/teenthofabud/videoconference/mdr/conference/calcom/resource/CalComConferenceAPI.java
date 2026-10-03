package com.teenthofabud.videoconference.mdr.conference.calcom.resource;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.calcom.form.CalComConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.calcom.vo.CalComConferenceVo;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "CalComConferenceAPI", description = "Cal.com Conference Management")
public interface CalComConferenceAPI {

    @Operation(method = "POST", summary = "Create a conference for a Cal.com booking")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Conference created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalComConferenceVo.class))))
    ResponseEntity<CalComConferenceVo> createConference(
            @RequestBody(description = "Cal.com booking and conference details", required = true,
                    content = @Content(schema = @Schema(implementation = CalComConferenceForm.class)))
            @Valid CalComConferenceForm form) throws ConferenceException;

    @Operation(method = "GET", summary = "Get all Cal.com conferences for a user")
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "User conferences",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = CalComConferenceVo.class)))))
    ResponseEntity<List<CalComConferenceVo>> getAllConferencesByUserId(Long userId) throws ConferenceException;

    @Operation(method = "GET", summary = "Get a conference by Cal.com booking UID")
    @Parameter(name = "bookingUid", description = "Cal.com event.uid", in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Conference details",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalComConferenceVo.class))))
    ResponseEntity<CalComConferenceVo> getConference(String bookingUid, Long userId) throws ConferenceException;

    @Operation(method = "PATCH", summary = "Patch a conference by Cal.com booking UID")
    @Parameter(name = "bookingUid", description = "Cal.com event.uid", in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Conference updated"))
    ResponseEntity<Void> patchConference(String bookingUid, Long userId,
            @RequestBody(description = "JSON Patch document", required = true,
                    content = @Content(mediaType = "application/json-patch+json",
                            schema = @Schema(implementation = String.class))) JsonPatch patches) throws ConferenceException;

    @Operation(method = "DELETE", summary = "Cancel a conference by Cal.com booking UID")
    @Parameter(name = "bookingUid", description = "Cal.com event.uid", in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Conference cancelled"))
    ResponseEntity<Void> deleteConference(String bookingUid, Long userId) throws ConferenceException;
}
