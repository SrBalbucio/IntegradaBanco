package balbucio.banco.manager;

import balbucio.banco.Main;
import balbucio.banco.model.User;
import balbucio.sqlapi.sqlite.SQLiteInstance;
import com.google.gson.Gson;

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
        if(Main.connected()){
            return (boolean) Main.request("EXISTUSER", user+":"+password);
        }
        return users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(user) && u.getPassword().equalsIgnoreCase(password));
    }

    public User getUser(String user, String password){
        if(Main.connected()){
            return new Gson().fromJson((String) Main.request("GETUSER", user+":"+password), User.class);
        }
        return users.stream().filter(u -> u.getName().equalsIgnoreCase(user) && u.getPassword().equalsIgnoreCase(password)).findFirst().orElse(null);
    }

    public User createUser(String user, String password){
        User newUser = getUser(user, password);
        if(newUser == null){
            newUser = new User(user, password);
            if(Main.connected()){
                return new Gson().fromJson((String) Main.request("CREATEUSER", user+":"+password), User.class);
            }
            sqlite.insert("name, password, token, saldo", "'"+user+"', '"+password+"', '"+newUser.getToken()+"', '0'", "users");
        }
        users.add(newUser);
        return newUser;
    }

    public String getUserName(String token){
        if(Main.connected()){
            return (String) Main.request("GETUSERNAME", token);
        }
        return users.stream().filter(u -> u.getToken().equalsIgnoreCase(token)).findFirst().orElse(new User(token, "20")).getName();
    }

    public User getUserByName(String userName){
        if(Main.connected()){
            return new Gson().fromJson((String) Main.request("GETUSERBYNAME", userName), User.class);
        }
        return users.stream().filter(u -> u.getName().equalsIgnoreCase(userName)).findFirst().orElse(null);
    }

}
