package com.teenthofabud.videoconference.mdr.serverpreference.resource;

import com.teenthofabud.videoconference.mdr.serverpreference.error.ServerPreferenceException;
import com.teenthofabud.videoconference.mdr.serverpreference.form.ServerPreferenceForm;
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

@Tag(name="ServerPreferenceAPI", description = "Server Preference Management")
public interface ServerPreferenceAPI {

    @Operation(method = "POST",summary = "Server Preference creation",description = "createServerPreference")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Server preference created"),
            @ApiResponse(
                    responseCode = "409", description = "Server already created",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorVo.class)))
        }
    )
    ResponseEntity<Void> createServerPreference(@RequestBody(description = "Server preference form", required = true, content = @Content(schema = @Schema(implementation = ServerPreferenceForm.class))) ServerPreferenceForm form) throws ServerPreferenceException;

}
