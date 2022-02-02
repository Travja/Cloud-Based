package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.models.*;
import me.travja.performances.processor.LambdaController;

import java.util.Map;

// '/performance' endpoint
@LambdaController("user")
public class UserHandler extends AuditionRequestHandler {

    @Override
    public Map<String, Object> handlePost(LambdaRequest request, String[] path) {
        //Create a user

        String name     = request.getString("name");
        String email    = request.getString("email");
        String phone    = request.getString("phone");
        String password = request.getString("password");
        String type     = request.getString("type");

        if (state.getByEmail(email, Person.class).isPresent())
            return constructResponse(409, "message", "A user with that email already exists.");

        Person person;
        if (type.equalsIgnoreCase("CastingDirector"))
            person = new CastingDirector(name, email, phone, password);
        else if (type.equalsIgnoreCase("Director"))
            person = new Director(name, email, phone, password);
        else
            person = new Performer(name, email, phone, password);

        state.save(person);

        return constructResponse(204);

    }

}
