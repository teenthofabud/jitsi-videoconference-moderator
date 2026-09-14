package com.teenthofabud.videoconference.mdr.user.resource;

import com.teenthofabud.videoconference.mdr.user.vo.UserVo;
import com.teenthofabud.videoconference.mdr.user.error.UserException;
import com.teenthofabud.videoconference.mdr.user.form.UserForm;
import com.teenthofabud.videoconference.mdr.vo.ErrorVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "UserAPI", description = "User Management")
public interface UserAPI {

    @Operation(method = "POST", summary = "User creation", description = "createNewUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(
                    responseCode = "409", description = "User already created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    ResponseEntity<UserVo> createNewUser(@RequestBody(description = "User form", required = true,
            content = @Content(schema = @Schema(implementation = UserForm.class))) @Valid UserForm form) throws UserException;

    @Operation(method = "POST", summary = "Guest user creation", description = "createNewGuestUser")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(
                    responseCode = "409", description = "User already created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    @SecurityRequirements()
    ResponseEntity<UserVo> createNewGuestUser (@Valid @org.springframework.web.bind.annotation.RequestBody UserForm form) throws UserException;

    @Operation(method = "GET", summary = "Get User By Id", description = "getUserById")
    @Parameter(description = "User ID (PK)",name = "userID",required = true,in = ParameterIn.PATH, schema = @Schema(implementation = Long.class))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserVo.class))),
            @ApiResponse(
                    responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    ResponseEntity<UserVo> getUserById(Long Id) throws UserException;

    @Operation(method = "GET", summary = "Get User By Email", description = "getUserByEmail")
    @Parameter(description = "User Email", name = "email", required = true, in = ParameterIn.QUERY, schema = @Schema(implementation = String.class))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserVo.class))),
            @ApiResponse(
                    responseCode = "404", description = "User not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
    })
    ResponseEntity<UserVo> getUserByEmail(String email) throws UserException;


}
