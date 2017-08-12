<%@ page import="java.sql.*" %>

<%
//Check if user is signed in
if(request.getSession().getAttribute("Email") == null){
    response.sendRedirect("index.jsp");
}else{
    try{
        Connection connection = servlet.Utility.getSQLConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT Users.first_name, Users.last_name, Users.grade, Score.score FROM Users INNER JOIN Score ON Users.account_name = Score.account_name ORDER BY Score.score DESC, Users.last_name");
        ResultSet rankingList = statement.executeQuery();
%>

        <html>
            <head>
                <script>
                    function filterList(){
                        var element = document.getElementById("grade");
                        var value = element.options[element.selectedIndex].value;

                        var targets = document.getElementsByClassName("option");
                        for(var i = 0; i < targets.length; i++){
                            targets[i].style.display = "none";
                        }

                        if(value != "All"){
                            targets = document.getElementsByClassName(value);
                        }

                        for(var i = 0; i < targets.length; i++){
                            targets[i].style.display = "table-row";
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
                        <option value="12">12th Grade</option>
                    </select>
                    <input type="button" onclick="filterList()" value="Update List"><br>
                </form>
                <table>
                        <tr>
                            <th>Rank</th>
                            <th>Name</th>
                            <th>Grade</th>
                            <th>Score</th>
                        </tr>

                    <%
                    int rank = 1;
                    while(rankingList.next()){
                        String firstName = rankingList.getString("first_name");
                        String lastName = rankingList.getString("last_name");
                        int grade = rankingList.getInt("grade");
                        double score = rankingList.getDouble("score");

                        //Capitalize first letter of first and last name
                        firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
                        lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1);
                    %>

                        <tr class="option <%=String.valueOf(grade)%>">
                            <td><%=String.valueOf(rank)%></td>
                            <td><%=(firstName + " " + lastName)%></td>
                            <td><%=String.valueOf(grade)%></td>
                            <td><%=String.valueOf(score)%></td>
                        </tr>

                    <%
                        rank++;
                    }
                    %>
                </table>
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