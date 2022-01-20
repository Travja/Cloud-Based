mvn install -f AuditionObjects

cd PerformancesHandler
./deploy.sh

cd ../AuditionHandler
./deploy.sh

cd ../
