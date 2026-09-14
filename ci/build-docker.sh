mvn clean package -e && \
mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar) && \
docker buildx build --platform linux/amd64,linux/arm64 --progress=plain -t ghcr.io/teenthofabud/jitsi-videoconference-moderator:X .