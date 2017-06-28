<%
//Check If User is Signed In
if(request.getSession().getAttribute("Email") == null){
    response.sendRedirect("index.jsp");
}else{
%>

<html>
    <head>
        <style>
            .link{
                margin-bottom : 5px;
                margin-top : 5px;
            }
        </style>
    </head>
    <body>
        <p id=message><%= (request.getParameter("message") != null) ? "Friends List Has Been Submitted!" : "" %></p>
        <h1>Dashboard</h1>
        <p>Hello, <jsp:expression>servlet.Utility.extractNameFromEmail((String)request.getSession().getAttribute("Email"))</jsp:expression></p>
        <p>Your Score: <jsp:expression>servlet.Utility.getUserScore((String)request.getSession().getAttribute("Email"))</jsp:expression></p>
        <div>
            <div class="link">
                <a href="addfriends.jsp">Add Friends</a>
            </div>
            <div class="link">
                <a href="leaderboard.jsp">See Leaderboard</a>
            </div>
            <div class="link">
                <a href="https://en.wikipedia.org/wiki/PageRank">The Algorithm That Powers This Website</a>
            </div>
            <div class="link">
                <a href="signout">Sign Out</a>
            </div>
        </div>
    </body>
</html>

<%
}
%>