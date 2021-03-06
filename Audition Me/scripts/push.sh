aws ecr create-repository --repository-name ${project.name} \
  --image-scanning-configuration scanOnPush=false \
  --region ${aws.region}
aws iam create-role --role-name ${project.name}-role --assume-role-policy-document file://.aws/policy.json
aws iam attach-role-policy --role-name ${project.name}-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

pwd
docker build -t ${project.name}:${project.version} . -f .aws/Dockerfile && \
docker tag ${project.name}:${project.version} ${aws.id}.dkr.ecr.${aws.region}.amazonaws.com/${project.name}:${project.version} && \
docker push ${aws.id}.dkr.ecr.${aws.region}.amazonaws.com/${project.name}:${project.version}

aws --output text lambda create-function --function-name ${project.name} \
  --role arn:aws:iam::${aws.id}:role/${project.name}-role \
  --package-type Image \
  --code ImageUri=${aws.id}.dkr.ecr.${aws.region}.amazonaws.com/${project.name}:${project.version} \
&& sleep 30 && aws lambda update-function-configuration --function-name ${project.name} \
    --environment "Variables={SMTP_EMAIL=${smtp.email}}" \
|| aws --output text lambda update-function-code --function-name ${project.name} \
  --image-uri ${aws.id}.dkr.ecr.${aws.region}.amazonaws.com/${project.name}:${project.version}