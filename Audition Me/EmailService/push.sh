docker build -t email:1.0-SNAPSHOT . && \
docker tag email:1.0-SNAPSHOT 785245057076.dkr.ecr.us-west-2.amazonaws.com/email:1.0-SNAPSHOT && \
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/email:1.0-SNAPSHOT