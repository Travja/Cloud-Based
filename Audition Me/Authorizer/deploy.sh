cd "${0%/*}"
mvn clean package && \
rm target/original*.jar && \
./.aws/login.sh && \
./.aws/push.sh