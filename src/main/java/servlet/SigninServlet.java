package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ethanlam on 6/22/17.
 */
public class SigninServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getParameter("Email") != null && request.getParameter("Password") != null){
            try {
                Connection connection = Utility.getSQLConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Accounts WHERE email = ? AND password = ?");
                statement.setString(1, request.getParameter("Email").toLowerCase());
                statement.setString(2, Utility.hashContent(request.getParameter("Password")));

                ResultSet result = statement.executeQuery();
                //Check If There Are Any Results
                if(result.next()){
                    //There is a match...log user in

                    //Start New Session For User
                    HttpSession session = request.getSession();
                    session.setAttribute("Email", request.getParameter("Email").toLowerCase());
                    //Session will invalidate in 30 minutes
                    session.setMaxInactiveInterval(30 * 60);

                    response.sendRedirect("dashboard.jsp");
                }else{
                    response.sendRedirect("index.jsp?error=" + "FailedLogin");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
