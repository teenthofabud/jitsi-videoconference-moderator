package com.teenthofabud.videoconference.mdr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class JitsiConferenceModeratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JitsiConferenceModeratorApplication.class, args);
	}

}
