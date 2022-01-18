mvn package
./login.sh
docker build -t java_calc_lambda:1 .
docker tag java_calc_lambda:1 785245057076.dkr.ecr.us-west-2.amazonaws.com/java_calc_lambda:1
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/java_calc_lambda:1