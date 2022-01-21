aws ecr create-repository --repository-name auditions \
  --image-scanning-configuration scanOnPush=false \
  --region us-west-2
aws iam create-role --role-name auditions-role --assume-role-policy-document file://aws/policy.json
aws iam attach-role-policy --role-name auditions-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

pwd
docker build -t auditions:1.0-SNAPSHOT . -f aws/Dockerfile && \
docker tag auditions:1.0-SNAPSHOT 785245057076.dkr.ecr.us-west-2.amazonaws.com/auditions:1.0-SNAPSHOT && \
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/auditions:1.0-SNAPSHOT

aws --output text lambda create-function --function-name auditions \
  --role arn:aws:iam::785245057076:role/auditions-role \
  --package-type Image \
  --code ImageUri=785245057076.dkr.ecr.us-west-2.amazonaws.com/auditions:1.0-SNAPSHOT \
&& aws lambda update-function-configuration --function-name auditions \
    --environment "Variables={SMTP_EMAIL=the.only.t.craft@gmail.com}" \
|| aws --output text lambda update-function-code --function-name auditions \
  --image-uri 785245057076.dkr.ecr.us-west-2.amazonaws.com/auditions:1.0-SNAPSHOT