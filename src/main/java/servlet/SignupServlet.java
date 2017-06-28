package servlet;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.spec.ECField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by ethanlam on 6/22/17.
 */
public class SignupServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("Email").toLowerCase();
        String password = request.getParameter("Password");
        String confirmPassword = request.getParameter("ConfirmPassword");

        String error = null;

        //Have loop for errors to break out of...loop has 1 iteration
        for (int i = 0; i == 0; i++) {
            //Check if valid school email
            if(email == null || !email.endsWith("@musowls.org")){
                error = "NotValidEmail";
                break;
            }
            //Check if passwords match
            if(!password.equals(confirmPassword)){
                error = "PasswordsDoNotMatch";
                break;
            }
            //Check if passwords are empty
            if(password.equals("") || password == null){
                error = "EmptyPassword";
                break;
            }
            //Check if user's school email is in database / is already used / is being verified
            try {
                Connection connection = Utility.getSQLConnection();

                //Check if email is in Users DB
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE account_name = ?");
                statement.setString(1, email.split("@")[0]);

                ResultSet result = statement.executeQuery();
                if(!result.next()){
                    statement.close();
                    connection.close();
                    error = "NotInDatabase";
                    break;
                }

                //Check if Accounts DB already has email
                statement = connection.prepareStatement("SELECT * FROM Accounts WHERE account_name = ?");
                statement.setString(1, email.split("@")[0]);

                result = statement.executeQuery();
                if(result.next()){
                    statement.close();
                    connection.close();
                    error = "EmailAlreadyUsed";
                    break;
                }

                //Check if Verification DB already has email
                statement = connection.prepareStatement("SELECT * FROM Verification WHERE email = ?");
                statement.setString(1, email);

                result = statement.executeQuery();
                if(result.next()){
                    statement.close();
                    connection.close();
                    error = "EmailIsBeingVerified";
                    break;
                }

                //Clean Up Connections
                statement.close();
                connection.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(error != null){
            //Error occured during signup
            response.sendRedirect("signup.jsp?error=" + error);
        }else{
            //Signup had valid content
            //Add Data to Verification DB
            int id = new String(email + password).hashCode() + new Random().nextInt(100);
            try {
                Connection connection = Utility.getSQLConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO Verification VALUES(?, ?, ?)");

                statement.setInt(1, id);
                statement.setString(2, email);
                statement.setString(3, Utility.hashContent(password));

                statement.executeUpdate();

                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Send Verification Email
            //TODO: CHANGE VERIFICATOIN LINK
            try {
                HashMap<String, String> settings = new Utility().getSettings();
                String verificationLink = settings.get("WEBSITE_URL") + "verify?id="+id;
                String emailContent = String.format("Go To <a href=\"%s\">%s</a> to verify your account.", verificationLink, verificationLink);
                Utility.sendEmail(email, "Verify Your MySchoolRank Account", emailContent);
                response.getWriter().println("Check Your Inbox For The Verification Email.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
