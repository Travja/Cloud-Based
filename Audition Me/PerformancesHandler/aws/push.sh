aws ecr create-repository --repository-name performances \
  --image-scanning-configuration scanOnPush=false \
  --region us-west-2
aws iam create-role --role-name performances-role --assume-role-policy-document file://aws/policy.json
aws iam attach-role-policy --role-name performances-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

pwd
docker build -t performances:1.0-SNAPSHOT . -f aws/Dockerfile && \
docker tag performances:1.0-SNAPSHOT 785245057076.dkr.ecr.us-west-2.amazonaws.com/performances:1.0-SNAPSHOT && \
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/performances:1.0-SNAPSHOT

aws --output text lambda create-function --function-name performances \
  --role arn:aws:iam::785245057076:role/performances-role \
  --package-type Image \
  --code ImageUri=785245057076.dkr.ecr.us-west-2.amazonaws.com/performances:1.0-SNAPSHOT \
&& aws lambda update-function-configuration --function-name performances \
    --environment "Variables={SMTP_EMAIL=the.only.t.craft@gmail.com}" \
|| aws --output text lambda update-function-code --function-name performances \
  --image-uri 785245057076.dkr.ecr.us-west-2.amazonaws.com/performances:1.0-SNAPSHOT