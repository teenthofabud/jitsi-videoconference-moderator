package com.teenthofabud.videoconference.mdr.conference.google.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GoogleCommonEventObject {

    @JsonProperty("platform")
    String clientPlatform;

    @JsonProperty("hostApp")
    String hostApp;
}
