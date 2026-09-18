#!/bin/sh

USE_SAM="false"
ENV="dev"

print_usage() {
    echo "Usage: $0 [-s] [-e <environment>] [-h]"
    echo
    echo "  -s                Use SAM instead of Spring Boot"
    echo "  -e <environment>  Environment (dev/test/prod)"
    echo "  -h                Show help"
}

if [ $# -eq 0 ]; then
    echo "No args found"
    print_usage
    exit 1
fi

# Parse CLI flags
while getopts "se:h" opt; do
    case "$opt" in
        s)
            USE_SAM="true"
            ;;
        e)
            ENV="$OPTARG"
            ;;
        h)
            print_usage
            exit 0
            ;;
        :)
            echo "Error: Option -$OPTARG requires an argument."
            print_usage
            exit 1
            ;;
        \?)
            echo "Unknown option: -$OPTARG"
            print_usage
            exit 1
            ;;
    esac
done

#############################################
#  SAM MODE
#############################################
if [ "$USE_SAM" = "true" ]; then
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
    "AUTH0_API_IDENTIFIER":"$AUTH0_API_IDENTIFIER",
    "AWS_REGION":"$AWS_REGION"
  }
}
EOF

    mvn clean native:compile -Pnative -DskipTests
    sam build
    sam local start-api --debug --warm-containers EAGER --env-vars sam-env.json
    exit 0
fi

#############################################
#  SPRING BOOT MODE
#############################################
ENV_FILE=".env.$ENV"

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Error: environment file '$ENV_FILE' not found."
    exit 1
fi

echo "Loading env from $ENV_FILE"

while IFS= read -r line; do
    case "$line" in
        ''|\#*) continue;;
        *)
            varname=$(echo "$line" | cut -d '=' -f 1)
            varvalue=$(echo "$line" | cut -d '=' -f 2-)
            export "$varname=$varvalue"
            ;;
    esac
done < "$ENV_FILE"
echo "Running Spring Boot (env=$ENV)"
exec mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
