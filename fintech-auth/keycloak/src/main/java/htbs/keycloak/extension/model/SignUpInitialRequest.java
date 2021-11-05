package htbs.keycloak.extension.model;

import java.time.LocalDate;
import java.util.Date;

public class SignUpInitialRequest {
    private String mobile;
    private String password;
    private Date birthday;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "SignUpInitialRequest{" +
                "mobile='" + mobile + '\'' +
                ", birthday=" + birthday +
                '}';
    }
}
