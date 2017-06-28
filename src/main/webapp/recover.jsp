<%@ page import="java.sql.*" %>

<html>
    <body>
    <div>
            <p id="error_message"><jsp:expression> servlet.Utility.getErrorMessage(request.getParameter("error")) </jsp:expression></p>
    </div>


    <%
        if(request.getParameter("id") != null){
            try{
                Connection connection = servlet.Utility.getSQLConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Recover WHERE id = ?");
                statement.setInt(1, Integer.parseInt(request.getParameter("id")));

                ResultSet result = statement.executeQuery();
                if(result.next()){
    %>
                     <form action="ChangePassword" method="POST">
                         <input type="hidden" name="id" value="<jsp:expression>request.getParameter("id")</jsp:expression>">
                         <div>
                             <span>Password: </span> <input type="password" name="Password">
                         </div>
                         <div>
                             <span>Confirm Password: </span> <input type="password" name="ConfirmPassword">
                         </div>
                         <input type="submit" value="Sign Up">
                     </form>
    <%
                }else{
    %>
                    <p>Not A Valid Recovery Link!</p>
    <%
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
    %>
            <p>Not A Valid Recovery Link!</p>
    <%
        }
    %>
    </body>
</html>