package com.teenthofabud.videoconference.mdr.conference.form.google;

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
