package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by ethanlam on 6/23/17.
 */
public class RecoveryServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getParameter("Email") != null){
            try {
                boolean success = false;
                HashMap<String, String> settings = new Utility().getSettings();

                String email = request.getParameter("Email").toLowerCase();
                Connection connection = Utility.getSQLConnection();

                //Recover Already Made Account
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Accounts WHERE email = ?");
                statement.setString(1, email);

                ResultSet result = statement.executeQuery();
                if(result.next()){
                    //Email Input Is In Accounts DB
                    //Insert Data Into Recover DB
                    int id = new String(email + result.getString("password")).hashCode() + new Random().nextInt(100);
                    statement = connection.prepareStatement("INSERT INTO Recover VALUES(?, ?)");
                    statement.setInt(1, id);
                    statement.setString(2, email);

                    statement.executeUpdate();
                    //Send Recovery Email
                    //TODO: Change Recovery Link
                    String recoveryLink = settings.get("WEBSITE_URL") + "recover.jsp?id="+id;
                    String emailContent = String.format("Go To <a href=\"%s\">%s</a> To Change Your Password.", recoveryLink, recoveryLink);
                    Utility.sendEmail(email, "Recover Your MySchoolRank Account", emailContent);

                    response.getWriter().println("Recovery Link Has Been Sent To Your Email!");
                    success = true;
                }

                //Resend Verification Link
                statement = connection.prepareStatement("SELECT * FROM Verification WHERE email = ?");
                statement.setString(1, email);

                result = statement.executeQuery();
                if(result.next()){
                    //Email Input Is In Verification DB
                    //Update Verfication DB With New id
                    int id = new String(result.getInt("id") + result.getString("email")).hashCode() + new Random().nextInt(100);
                    statement = connection.prepareStatement("UPDATE Verification SET id = ? WHERE email = ?");
                    statement.setInt(1, id);
                    statement.setString(2, email);

                    statement.executeUpdate();
                    //Send New Verification Email
                    //TODO: Change Verification Link
                    String verificationLink = settings.get("WEBSITE_URL") + "verify?id="+id;
                    String emailContent = String.format("Go To <a href=\"%s\">%s</a> to verify your account.", verificationLink, verificationLink);
                    Utility.sendEmail(email, "Verify Your MySchoolRank Account", emailContent);

                    response.getWriter().println("New Verification Link Has Been Sent To Your Email!");
                    success = true;
                }

                if(!success){
                    //Send error if email is not in not in Accounts or Verification DB
                    response.sendRedirect("recovery.jsp?error=" + "NotRecoverableEmail");
                }

                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            response.sendRedirect("recovery.jsp?error=" + "NotRecoverableEmail");
        }
    }
}
