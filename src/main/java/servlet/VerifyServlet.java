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
 * Created by ethanlam on 6/22/17.
 */
public class VerifyServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getParameter("id") != null){
            try {
                Connection connection = Utility.getSQLConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Verification WHERE id = ?");
                statement.setInt(1, Integer.parseInt(request.getParameter("id")));

                ResultSet result = statement.executeQuery();
                //Check if there is a result
                if(result.next()){
                    String email = result.getString("email");
                    String password = result.getString("password");

                    //Transfer Verification Account Data to Accounts DB
                    statement = connection.prepareStatement("INSERT INTO Accounts VALUES (?, ?, ?)");

                    statement.setString(1, email.split("@")[0]);
                    statement.setString(2, email);
                    statement.setString(3, password);

                    statement.executeUpdate();

                    //Remove Row From Verification DB
                    statement = connection.prepareStatement("DELETE FROM Verification WHERE id = ?");
                    statement.setInt(1, Integer.parseInt(request.getParameter("id")));

                    statement.executeUpdate();

                    response.getWriter().println("Your Account Has Been Verified!");
                }else{
                    response.getWriter().println("Please Use The Correct Verification Link. If You Had Your Verification Link Resent, Use The Verification Link Most Recently Sent.");
                }

                statement.close();
                connection.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
