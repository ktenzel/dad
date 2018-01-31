import dao.LoginSQL;
import models.Login;
import models.User;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dao.*;
import spark.ModelAndView;

import static spark.Spark.*;
import static spark.Spark.staticFileLocation;


public class App {
    public static void main(String[] args) {
        staticFileLocation("/public");
        String connectionString = "jdbc:h2:~/dad5.db;INIT=RUNSCRIPT from 'classpath:db/create.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        InterestSQL interestDao = new InterestSQL(sql2o);
        UserSQL userDao = new UserSQL(sql2o);
        LoginSQL loginDao = new LoginSQL(sql2o);


        //HOME PAGE/LOGIN
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            return new ModelAndView(model, "login.hbs");
        }, new HandlebarsTemplateEngine());


        //new account:

        get("/user/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "sign-up.hbs");
        }, new HandlebarsTemplateEngine());


        post("/user/new", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            String userName = request.queryParams("user-name");
            String password = request.queryParams("password");
            String birthday = request.queryParams("birthday");

            Login newLogin = new Login(userName, password, birthday);
            loginDao.add(newLogin);
            int loginId = newLogin.getId();

            String name = request.queryParams("name");
            String gender = request.queryParams("gender");
            String preferredGender = request.queryParams("preference");
            String userTagLine = request.queryParams("userTagLine");
            String age = request.queryParams("age");
            String location = request.queryParams("location");
            String sign = request.queryParams("sign");
            String job = request.queryParams("job");
            String kids = request.queryParams("kids");

            User newUser = new User(loginId, name, gender, preferredGender, userTagLine, age, location, sign, job, kids);
            userDao.add(newUser);

            model.put("user", userDao.findById(newUser.getId()));
            model.put("login", loginDao.findById(newLogin.getId()));

            return new ModelAndView(model, "welcome.hbs");
        }, new HandlebarsTemplateEngine());

        //

        //personal info page
        get("/user/:user_id/profile", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            User user = userDao.findById(Integer.parseInt(request.params("user_id")));
            model.put("user", user);

            int login_id = user.getLoginId();
            Login login = loginDao.findById(login_id);
            model.put("login", login);

            return new ModelAndView(model, "profile.hbs");
        }, new HandlebarsTemplateEngine());


        //log in:
        post("/user", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            String userName = request.queryParams("username");
            String password = request.queryParams("password");

            int login_id = loginDao.getLoginId(userName, password);
            User user = userDao.findUserByLoginId(login_id);

            model.put("user", user);

            return new ModelAndView(model, "welcome.hbs");
        }, new HandlebarsTemplateEngine());



        //SEARCH FOR A POTENTIAL MATCH
        get("/profile/:user_id/search", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            int user_id = Integer.parseInt(request.params("user_id"));
            User user = userDao.findById(user_id);

            List<User> users = userDao.matchingGender(user);

            model.put("users", users);
            model.put("size", users.size());
            model.put("our_id", user_id);
            return new ModelAndView(model, "search.hbs");
        }, new HandlebarsTemplateEngine());


        //view profile
        get("/profile/:user_id/search/:profile_id", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            int user_id = Integer.parseInt(request.params("user_id"));
            User user = userDao.findById(user_id);

            int profile_id = Integer.parseInt(request.params("profile_id"));
            User profile = userDao.findById(profile_id);

            model.put("our_user", user);
            model.put("profile", profile);

            return new ModelAndView(model, "view-profile.hbs");
        }, new HandlebarsTemplateEngine());

        post("/profile/:user_id/search/:profile_id", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            int user_id = Integer.parseInt(request.params("user_id"));
            int profile_id = Integer.parseInt(request.params("profile_id"));

            userDao.addLike(user_id, profile_id);

            User user = userDao.findById(user_id);
            User profile = userDao.findById(profile_id);
            model.put("our_user", user);
            model.put("profile", profile);

            return new ModelAndView(model, "view-profile.hbs");
        }, new HandlebarsTemplateEngine());


        //SEE A LIST OF USERS YOU'VE MATCHED WITH
        get("/profile/:user_id/matches", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            int user_id = Integer.parseInt(request.params("user_id"));
            User user = userDao.findById(user_id);
            List<User> matches = userDao.getMatchedPairs(user);

            model.put("matches", matches);
            model.put("size", matches.size());
            model.put("our_user", user);

            return new ModelAndView(model, "matches.hbs");
        }, new HandlebarsTemplateEngine());


        //update
        get("/profile/:user_id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "update.hbs");
        }, new HandlebarsTemplateEngine());


        post("/profile/:user_id/update", (request, response) -> {
            Map<String, Object> model = new HashMap<>();



            String name = request.queryParams("name");
            String gender = request.queryParams("gender");
            String preferredGender = request.queryParams("preference");
            String userTagLine = request.queryParams("userTagLine");
            String age = request.queryParams("age");
            String location = request.queryParams("location");
            String sign = request.queryParams("sign");
            String job = request.queryParams("job");
            String kids = request.queryParams("kids");

            int user_id = Integer.parseInt(request.params("user_id"));

            User user = userDao.findById(user_id);

            userDao.updateUser(user_id, name, gender, preferredGender, userTagLine, age, location, sign, job, kids);

            model.put("user", userDao.findById(user_id));
            model.put("login", loginDao.findById(user.getLoginId()));

            return new ModelAndView(model, "profile.hbs");
        }, new HandlebarsTemplateEngine());


        //DELETE PROFILE

        get("/profile/:user_id/delete", (request, response)-> {
            Map<String, Object> model = new HashMap<>();

            int idOfUser = Integer.parseInt(request.params("user_id"));
            User deleteUser = userDao.findById(idOfUser);
            userDao.deleteById(idOfUser);

            return new ModelAndView(model, "goodbye.hbs");
        }, new HandlebarsTemplateEngine());

    }
}