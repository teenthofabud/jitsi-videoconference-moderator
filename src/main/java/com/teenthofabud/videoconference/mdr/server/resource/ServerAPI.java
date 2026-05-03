package com.teenthofabud.videoconference.mdr.server.resource;

import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.server.form.ServerForm;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

//    @Hidden
//    @Operation(method = "POST", summary = "Default Server creation", description = "createNewDefaultServer")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Server created"),
//            @ApiResponse(
//                    responseCode = "409", description = "Server already created",
//                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
//    })
//    ResponseEntity<Void> createNewDefaultServer(@RequestBody(description = "Server form", required = true,
//            content = @Content(schema = @Schema(implementation = ServerForm.class))) ServerForm form) throws ServerException;

    @Operation(method = "GET", summary = "Get Server By ID", description = "getServerById")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server retrieved",content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerVo.class))),
            @ApiResponse(
                    responseCode = "404", description = "Server not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    @Parameter(description = "Server ID (PK)",name = "serverId",required = true,in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    ResponseEntity<ServerVo> getServerById(Long serverId) throws ServerException;

    @Operation(method = "POST", summary = "Add new Server preference for the user", description = "addServerPreference")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201",description = "Successfully added server preference"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))

    })
    ResponseEntity<Void> addServerPreference(ServerPreferenceForm form) throws UserException, ServerException;

}
