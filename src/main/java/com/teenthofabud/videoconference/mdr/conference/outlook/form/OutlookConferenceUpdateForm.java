package com.teenthofabud.videoconference.mdr.conference.outlook.form;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutlookConferenceUpdateForm {

    @NotNull
    Long serverId;
}
