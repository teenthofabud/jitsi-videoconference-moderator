package entity;

import constants.ConferenceStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Conference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "room_id", nullable = false)
    String roomId;

    @Column(name = "user_ref", nullable = false)
    String userRef;

    @Column(name = "server_url", nullable = false)
    String serverUrl;

    @Column(name = "status", nullable = false)
    ConferenceStatus status;
    

}
