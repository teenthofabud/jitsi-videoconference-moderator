package com.teenthofabud.videoconference.mdr.conference.form.google;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NonNull;

@Data
public class ConferenceSearchForm {
    @NonNull
    @Email
    String calendarId;
}
