package com.teenthofabud.videoconference.mdr.server.resource;

import com.teenthofabud.videoconference.mdr.server.form.DefaultServerForm;
import com.teenthofabud.videoconference.mdr.server.form.DefaultServerUpdateForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceForm;
import com.teenthofabud.videoconference.mdr.server.form.ServerPreferenceUpdateForm;
import com.teenthofabud.videoconference.mdr.server.vo.ServerVo;
import com.teenthofabud.videoconference.mdr.server.error.ServerException;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "ServerAPI", description = "Server Management")
public interface ServerAPI {

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
            @ApiResponse(responseCode = "201",description = "Successfully added server preference",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerVo.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))

    })
    ResponseEntity<ServerVo> addServerPreference(ServerPreferenceForm form) throws ServerException;

    @Operation(method = "POST", summary = "Create a default server", description = "createDefaultServer")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201",description = "Successfully created default server",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerVo.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))

    })
    ResponseEntity<ServerVo> createDefaultServer(DefaultServerForm form) throws ServerException;

    @Operation(method = "PUT", summary = "Create a default server", description = "updateDefaultServer")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201",description = "Successfully updated default server",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerVo.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))

    })
    ResponseEntity<ServerVo> updateDefaultServer(DefaultServerUpdateForm form) throws ServerException;

    @Operation(method = "PUT", summary = "Update Server preference for the user", description = "updateServerPreference")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201",description = "Successfully updated server preference",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ServerVo.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))

    })
    ResponseEntity<ServerVo> updateServerPreference(ServerPreferenceUpdateForm form) throws ServerException;

    @Operation(method = "GET", summary = "Get All servers for a user", description = "getServerByUserID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servers retrieved",content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ServerVo.class)))),
            @ApiResponse(
                    responseCode = "404", description = "No servers found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    @Parameter(description = "User ID (PK)",name = "userId",required = true,in = ParameterIn.QUERY, schema = @Schema(implementation = Long.class))
    ResponseEntity<List<ServerVo>> getServersByUserID(Long userId) throws ServerException;


}
