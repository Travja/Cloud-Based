FROM public.ecr.aws/lambda/java:11

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} ${LAMBDA_TASK_ROOT}/lib/

CMD ["me.travja.performances.AuditionHandler::handleRequest"]