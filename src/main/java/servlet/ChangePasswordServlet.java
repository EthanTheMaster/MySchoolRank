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

/**
 * Created by ethanlam on 6/23/17.
 */
public class ChangePasswordServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getParameter("id") != null && request.getParameter("Password") != null && request.getParameter("ConfirmPassword") != null){
            int id = Integer.parseInt(request.getParameter("id"));
            String password = request.getParameter("Password");
            String confirmPassword = request.getParameter("ConfirmPassword");

            String error = null;
            //Execute Loop Once...Loop is meant for code to break out of
            for (int i = 0; i == 0; i++) {
                //Password Checks
                if(!password.equals(confirmPassword)){
                    error = "PasswordsDoNotMatch";
                    break;
                }
                if(password == null || password.equals("")){
                    error = "EmptyPassword";
                    break;
                }

                //Do Database Operations
                try {
                    Connection connection = Utility.getSQLConnection();
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM Recover WHERE id = ?");
                    statement.setInt(1, id);

                    ResultSet result = statement.executeQuery();
                    //Check if there are any matches
                    if(result.next()){
                        String email = result.getString("email");
                        //Update Password in Accounts DB
                        statement = connection.prepareStatement("UPDATE Accounts SET password = ? WHERE email = ?");
                        statement.setString(1, Utility.hashContent(password));
                        statement.setString(2, email);
                        statement.executeUpdate();
                        //Remove Recovery Data From Recover DB
                        statement = connection.prepareStatement("DELETE FROM Recover WHERE email = ?");
                        statement.setString(1, email);
                        statement.executeUpdate();
                    }else{
                        //Set error to empty for nonexistent id
                        error = "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(error != null){
                //Do nothing for nonexistent id
                if(!error.equals("")) {
                    response.sendRedirect(String.format("recover.jsp?id=%d&error=%s", id, error));
                }
            }else{
                response.getWriter().println("Password has been changed!");
            }
        }
    }
}
