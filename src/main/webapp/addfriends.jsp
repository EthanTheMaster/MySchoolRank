<%@ page import="java.sql.*" %>
<%@ page import="java.util.HashSet" %>

<%
//Check If User is Signed In
if(request.getSession().getAttribute("Email") == null){
    response.sendRedirect("index.jsp");
}else{
    try{
        String userAccountName = ((String) request.getSession().getAttribute("Email")).split("@")[0];

        Connection connection = servlet.Utility.getSQLConnection();
        //Get list of every user
        PreparedStatement statement = connection.prepareStatement("SELECT account_name, first_name, last_name, grade FROM Users WHERE account_name != ? ORDER BY last_name");
        statement.setString(1, userAccountName);
        ResultSet rs = statement.executeQuery();

        //Get list of the user's friends so that checkboxes are prechecked for friends
        statement = connection.prepareStatement("SELECT user_friend FROM Friends WHERE user = ?");
        statement.setString(1, userAccountName);
        ResultSet friendsList = statement.executeQuery();

        HashSet<String> friends = new HashSet<String>();
        while(friendsList.next()){
            friends.add(friendsList.getString("user_friend"));
        }

%>

    <html>
        <head>
            <script>
                function filterList(){
                    var element = document.getElementById("grade");
                    var value = element.options[element.selectedIndex].value;

                    var targets = document.getElementsByClassName("option");
                    for(var i = 0; i < targets.length; i++){
                        targets[i].style.height = "0px";
                        targets[i].style.visibility = "hidden";
                    }

                    if(value != "All"){
                        targets = document.getElementsByClassName(value);
                    }

                    for(var i = 0; i < targets.length; i++){
                        targets[i].style.height = "auto";
                        targets[i].style.visibility = "visible";
                    }
                }
            </script>
        </head>
        <body>
            <form>
                <select id="grade">
                    <option value="All">All Grades</option>
                    <option value="7">7th Grade</option>
                    <option value="8">8th Grade</option>
                    <option value="9">9th Grade</option>
                    <option value="10">10th Grade</option>
                    <option value="11">11th Grade</option>
                </select>
                <input type="button" onclick="filterList()" value="Update List"><br>
            </form>
            <form action="SubmitFriendsServlet" method="POST">
                <%
                while(rs.next()){
                    String accountName = rs.getString("account_name");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    int grade = rs.getInt("grade");
                %>
                    <div class="option <%=String.valueOf(grade)%>"> <input type="checkbox" name="<%=accountName%>"  <%= (friends.contains(accountName) ? "checked" : "") %>  > <%=firstName + " " + lastName%> </div>
                <%
                }
                %>

                <input type="submit" value="Submit Friends List">
            </form>
        </body>
    </html>

<%
        statement.close();
        connection.close();
    }catch(Exception e){
        e.printStackTrace();
    }
}
%>