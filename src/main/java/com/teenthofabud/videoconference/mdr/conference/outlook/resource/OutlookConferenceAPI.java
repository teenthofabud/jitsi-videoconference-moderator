package com.teenthofabud.videoconference.mdr.conference.outlook.resource;

import com.teenthofabud.videoconference.mdr.conference.error.ConferenceException;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookAppointmentSentForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.form.OutlookConferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceVo;
import com.teenthofabud.videoconference.mdr.conference.outlook.vo.OutlookConferenceSetupVo;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OutlookConferenceAPI {

    ResponseEntity<OutlookConferenceSetupVo> getSetup(String email) throws ConferenceException;

    @Operation(method = "GET", summary = "Get all Outlook conferences for a user")
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "User conferences",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = OutlookConferenceVo.class)))))
    ResponseEntity<List<OutlookConferenceVo>> getAllConferencesByUserId(Long userId) throws ConferenceException;

    @Operation(method = "GET", summary = "Get an Outlook conference by draft tracking ID")
    @Parameter(name = "draftTrackingId", description = "Outlook appointment draft tracking ID", in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Outlook conference",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OutlookConferenceVo.class))))
    ResponseEntity<OutlookConferenceVo> getConference(String draftTrackingId, Long userId) throws ConferenceException;

    @Operation(method = "PATCH", summary = "Update the server for an Outlook conference")
    @Parameter(name = "draftTrackingId", description = "Outlook appointment draft tracking ID", in = ParameterIn.PATH, required = true)
    @Parameter(name = "userId", description = "Conference owner user ID", in = ParameterIn.QUERY, required = true)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Conference updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OutlookConferenceVo.class))))
    ResponseEntity<OutlookConferenceVo> updateConference(String draftTrackingId, Long userId,
            @Valid OutlookConferenceUpdateForm form) throws ConferenceException;

    ResponseEntity<OutlookConferenceVo> createConference(@Valid OutlookConferenceForm form) throws ConferenceException;

    ResponseEntity<OutlookConferenceVo> markAppointmentSent(String draftTrackingId, Long userId,
            @Valid OutlookAppointmentSentForm form) throws ConferenceException;

    ResponseEntity<Void> cancelConference(String draftTrackingId, Long userId) throws ConferenceException;
}
