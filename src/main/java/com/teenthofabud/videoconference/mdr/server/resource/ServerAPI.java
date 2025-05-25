package com.teenthofabud.videoconference.mdr.server.resource;

import com.teenthofabud.videoconference.mdr.conference.vo.google.GoogleConferenceDataVo;
import com.teenthofabud.videoconference.mdr.server.dto.ServerDto;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "ServerAPI", description = "Server Management")
public interface ServerAPI {

    @Operation(method = "POST", summary = "Server creation", description = "createNewServer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Server created"),
            @ApiResponse(
                    responseCode = "409", description = "Server already created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    ResponseEntity<Void> createNewServer(@RequestBody(description = "Server form", required = true,
            content = @Content(schema = @Schema(implementation = ServerForm.class))) ServerForm form) throws ServerException;

    @Operation(method = "GET", summary = "Get Server By Url", description = "getServerByUrl")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server retrieved",content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerDto.class))),
            @ApiResponse(
                    responseCode = "404", description = "Server not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    ResponseEntity<ServerDto> getServerByUrl(String serverUrl) throws ServerException;

}
