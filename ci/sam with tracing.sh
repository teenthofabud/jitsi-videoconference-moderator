#!/bin/sh

#############################################
#  SAM MODE
#############################################
  echo "Running SAM..."

  ENV_FILE=".env.sam"
  if [ ! -f "$ENV_FILE" ]; then
      echo "❌ Error: environment file '$ENV_FILE' not found."
      exit 1
  fi

  echo "Loading env from $ENV_FILE"
  set -a;
  source "$ENV_FILE"
  set +a;

  echo "Creating sam-env.json"
  cat > sam-env.json <<EOF
{
"Parameters": {
  "JITSI_DB_URL": "$JITSI_DB_URL",
  "JITSI_DB_USERNAME": "$JITSI_DB_USERNAME",
  "JITSI_DB_PASSWORD": "$JITSI_DB_PASSWORD",
  "JITSI_DB_DRIVER_CLASS": "$JITSI_DB_DRIVER_CLASS",
  "JITSI_DB_DIALECT": "$JITSI_DB_DIALECT",
  "AUTH0_DOMAIN":"$AUTH0_DOMAIN",
  "AUTH0_API_IDENTIFIER":"$AUTH0_API_IDENTIFIER"
}
}
EOF
  mvn clean package -DskipTests
  # Start the app with the tracing agent in the background
  ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
  VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
  GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)

  JAR_PATH="target/${ARTIFACT_ID}-${VERSION}.jar"
  CONFIG_DIR="src/main/resources/META-INF/native-image/${GROUP_ID}/${ARTIFACT_ID}"

  echo "Running agent for: $JAR_PATH"
  echo "Output config dir: $CONFIG_DIR"

  # Run the app with the tracing agent in the background
  java -agentlib:native-image-agent=config-output-dir="$CONFIG_DIR" -jar "$JAR_PATH" &
  APP_PID=$!
  # Wait a few seconds for Spring Boot to fully start up
  sleep 20
  # hit an endpoint so hibernate lazy loaded classes are traced
  curl -X POST http://localhost:8091/api/user/preview
  # Gracefully stop the application to flush the JSON files out
  kill -SIGTERM $APP_PID
  wait $APP_PID || true
  mvn native:compile -Pnative -DskipTests -Dnative-image.image-build-arg.J=-Xmx10g
  sam build
  sam local start-api --debug --warm-containers EAGER --env-vars sam-env.json
  exit 0
