./login.sh
docker build -t calc_lambda:1 .
docker tag calc_lambda:1 785245057076.dkr.ecr.us-west-2.amazonaws.com/calc_lambda:1
docker push 785245057076.dkr.ecr.us-west-2.amazonaws.com/calc_lambda:1