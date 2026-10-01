package bushfireAlert.com.example.myapp.model;

public class User {
    public String uid;
    public String name;
    public String email;
    public String phone;

    public User() {
        // Required for Firebase
    }

    public User(String uid, String name, String email, String phone) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
