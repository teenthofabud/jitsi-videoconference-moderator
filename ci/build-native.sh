export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-25.jdk/Contents/Home && ## macOS brew
mvn clean native:compile -Pnative -DskipTests -e