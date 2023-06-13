package balbucio.banco.manager;

import balbucio.banco.model.User;
import balbucio.sqlapi.sqlite.SQLiteInstance;

import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static List<User> users = new ArrayList<>();
    private static UserManager instance;

    public static UserManager getInstance() {
        return instance;
    }

    private SQLiteInstance sqlite;

    public UserManager(SQLiteInstance sqlite){
        instance = this;
        this.sqlite = sqlite;
        List<Object[]> u = sqlite.getAllValuesFromColumns("users", "name", "password", "token", "saldo");
        for(Object[] o : u){
            users.add(new User((String) o[2], (String) o[0], (String) o[1], Long.parseLong(String.valueOf(o[3]))));
        }
    }

    public boolean existUser(String user, String password){
        return users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(user) && u.getPassword().equalsIgnoreCase(password));
    }

    public User getUser(String user, String password){
        return users.stream().filter(u -> u.getName().equalsIgnoreCase(user) && u.getPassword().equalsIgnoreCase(password)).findFirst().orElse(null);
    }

    public User createUser(String user, String password){
        User newUser = getUser(user, password);
        if(newUser == null){
            newUser = new User(user, password);
            sqlite.insert("name, password, token, saldo", "'"+user+"', '"+password+"', '"+newUser.getToken()+"', '0'", "users");
        }
        return newUser;
    }

}
