package model;

public abstract class User {

    private String userId;
    private String profileId;
    private String role;
    private String password;
    private String email;
    private String fullname;

    public User(String userId, String profileId, String role,
                String password, String email, String fullname) {



        this.userId    = userId;
        this.profileId = profileId;
        this.role      = role;
        this.password  = password;
        this.email     = email;
        this.fullname  = fullname;


    }

    public String getUserId()    {

        return userId;

    }
    public String getProfileId() {

        return profileId;

    }
    public String getRole()      {

        return role;

    }
    public String getPassword()  {

        return password;

    }
    public String getEmail()     {

        return email;

    }
    public String getFullName()  {

        return fullname;

    }

    public void setEmail(String email)     {

        this.email = email;

    }
    public void setFullname(String name)   {

        this.fullname = name;

    }

    public abstract void openDashboard();



}
