aws ecr create-repository --repository-name ${project.name} \
  --image-scanning-configuration scanOnPush=false \
  --region us-west-2
aws iam create-role --role-name ${project.name}-role --assume-role-policy-document file://aws/policy.json
aws iam attach-role-policy --role-name ${project.name}-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

pwd
docker build -t ${project.name}:${project.version} . -f aws/Dockerfile && \
docker tag ${project.name}:${project.version} 785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version} && \
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version}

aws lambda create-function --function-name ${project.name} \
  --role arn:aws:iam::785245057076:role/${project.name}-role \
  --package-type Image \
  --code ImageUri=785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version} \
|| aws lambda update-function-code --function-name ${project.name} \
  --image-uri 785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version}