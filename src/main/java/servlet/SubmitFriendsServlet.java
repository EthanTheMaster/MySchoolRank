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
 * Created by ethanlam on 6/25/17.
 */
public class SubmitFriendsServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection connection = Utility.getSQLConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT account_name FROM Users");
            ResultSet allNames = statement.executeQuery();

            String currentAccountEmail = (String)request.getSession().getAttribute("Email");

            //Prevent Person From Accessing Servlet Without Signing In
            if(currentAccountEmail != null){
                String currentAccountName = currentAccountEmail.split("@")[0];

                //Remove all current friends and update DB with new submitted friends
                statement = connection.prepareStatement("DELETE FROM Friends WHERE user = ?");
                statement.setString(1, currentAccountName);
                statement.executeUpdate();

                //Populate DB with new friends
                while(allNames.next()){
                    String friendAccountName = allNames.getString("account_name");
                    //Check whether person submitted friendAccountName as a friend
                    if(request.getParameter(friendAccountName) != null) {
                        statement = connection.prepareStatement("INSERT INTO Friends VALUES(?, ?)");
                        statement.setString(1, currentAccountName);
                        statement.setString(2, friendAccountName);
                        statement.executeUpdate();
                    }
                }

                Utility.performRanking(100, .85);
                response.sendRedirect("dashboard.jsp?message=" + "Submitted");
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
