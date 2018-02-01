package dao;

import models.Login;


public interface LoginDAO {

    //create
    public void add(Login login);

    //read
    Integer getLoginId(String username, String password);
    Login findById(int id);
//    void Login getUserIdFromLogin(String username, String password);

    //update

    //delete
    void delete(int id);
}