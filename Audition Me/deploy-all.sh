mvn install -f AuditionObjects

cd Performances && \
./deploy.sh && printf "\n\n\t\tPerformances deployed successfully\t\t\n\n\n" || \
 printf "\n\n\t\tPerformances FAILURE\t\t\n\n\n"

cd ../Auditions && \
./deploy.sh && printf "\n\n\t\tAuditions deployed successfully\t\t\n\n\n" || \
 printf "\n\n\t\tAudition FAILURE\t\t\n\n\n"

cd ../
