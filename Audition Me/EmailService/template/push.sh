docker build -t ${project.name}:${project.version} . && \
docker tag ${project.name}:${project.version} 785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version} && \
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/${project.name}:${project.version}