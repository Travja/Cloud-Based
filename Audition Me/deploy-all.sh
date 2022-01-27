mvn install -f AuditionObjects

./Performances/deploy.sh && printf "\n\n\t\tPerformances deployed successfully\t\t\n\n\n" || \
 printf "\n\n\t\tPerformances FAILURE\t\t\n\n\n"

./Auditions/deploy.sh && printf "\n\n\t\tAuditions deployed successfully\t\t\n\n\n" || \
 printf "\n\n\t\tAudition FAILURE\t\t\n\n\n"

./Authorizer/deploy.sh && printf "\n\n\t\tAuthorizer deployed successfully\t\t\n\n\n" || \
  printf "\n\n\t\tAuthorizer FAILURE\t\t\n\n\n"

cd ../
