package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.Util;
import me.travja.performances.api.models.LambdaRequest;
import me.travja.performances.api.models.Person;
import me.travja.performances.processor.LambdaController;

import java.util.*;

@LambdaController
public class AuthorizationHandler extends AuditionRequestHandler {

    @Override
    public void postConstruct() {
        super.postConstruct();
        clearCacheOnNewRequest = false;
    }

    @Override
    public Map<String, Object> handleRequest(LambdaRequest request, Context context,
                                             String[] path) {
        String authHeader = getAuthHeader(request).replace("Basic ", "");
        Person authUser   = request.getAuthUser();
        if (authHeader == null) return constructResponse(401);
        boolean authorized = authUser != null ? checkAuth(authHeader, authUser) : false;
        System.out.println("Passwords match? " + authorized);
        return constructAuthResponse(authorized, request.getString("methodArn"));
    }

    public boolean checkAuth(String auth, Person authUser) {
        String[] b64  = new String(Base64.getDecoder().decode(auth)).split(":", 2);
        String   pass = b64[1];

        System.out.println("Auth user is " + authUser.getName());

        return Util.checkHash(pass, authUser.getPassword());
    }

    public Map<String, Object> constructAuthResponse(boolean authorized, String arn) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyDocument", new PolicyDocument(authorized, arn));

        return map;
    }

    /*
     * 'policyDocument': {
     *     'Version': '2012-10-17',
     *     'Statement': [{
     *         'Action': 'execute-api:Invoke',
     *         'Effect': 'Allow|Deny',
     *         'Resource': event['methodArn']
     *     }]
     * }
     */
    @Data
    public class PolicyDocument {

        @JsonProperty("Version")
        private String          version    = "2012-10-17";
        @JsonProperty("Statement")
        private List<Statement> statements = new ArrayList<>();

        public PolicyDocument(boolean authorized, String methodArn) {
            statements.add(new Statement(authorized ? "Allow" : "Deny", methodArn));
        }

        @Data
        public class Statement {

            @JsonProperty("Action")
            private final String action = "execute-api:Invoke";
            @JsonProperty("Effect")
            private final String effect;
            @JsonProperty("Resource")
            private final String resource;

        }

    }
}
