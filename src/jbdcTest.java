import java.sql.*;

public class jbdcTest {
    public static void main(String[]args) {
        myDatabase db = new myDatabase("localhost", "1433", "chat_18127006", "sa", "sqlHH108");
    }

    void insert (String username, String password) {

    }
}

class myDatabase {
    Connection con = null;
    PreparedStatement getUserPw;
    PreparedStatement insertUser;

    PreparedStatement countGroups;
    PreparedStatement insertGroup;

    PreparedStatement insertFile;

    PreparedStatement insertUserGroup;

    public myDatabase(String db_host, String db_port, String db_name, String db_username, String db_password) {
        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.con = DriverManager.getConnection("jdbc:sqlserver://" + db_host + ":" + db_port
                    + ";databaseName=" + db_name
                    + ";user=" + db_username
                    + ";password=" + db_password);

            if (con != null) {
                System.out.println("Scess");

                this.getUserPw = con.prepareStatement("select * from users where usename = ?");
                this.insertUser = con.prepareStatement("insert into users (usename, password) values (?, ?)");

                this.countGroups = con.prepareStatement("select count(*) from groups where name = ?");
                this.insertGroup = con.prepareStatement("insert into groups (name) value (?)");

                this.insertFile = con.prepareStatement("insert into files (filename, groupname) values (?, ?)");
                this.insertUserGroup = con.prepareStatement("insert into user_groups");

                getUserPw.setString(1, "landmaster");

                ResultSet rs = getUserPw.executeQuery();

                String pw = null;
                while(rs.next()) {
                    pw = rs.getString("password");
                    System.out.println(pw);
                }

                if (pw == null) {
                    System.out.println("no");
                }

                con.close();
            }

        }catch (Exception e) {
            System.out.println(e);
        }
    }
}
