FROM eclipse-temurin:25.0.4_7-jre-alpine
RUN apk --no-cache add curl
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib app/lib
COPY ${DEPENDENCY}/META-INF app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes app
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENV TZ=Europe/Berlin
#https://stackoverflow.com/questions/62943487/intellij-debug-docker-container-keeps-giving-me-io-exception-handshake-failed
ENTRYPOINT ["/entrypoint.sh"]
