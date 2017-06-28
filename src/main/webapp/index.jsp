<%
//Check If User is Signed In
if(request.getSession().getAttribute("Email") != null){
    response.sendRedirect("dashboard.jsp");
}
%>

<html>
    <head>
        <style>
            #error_message{
                color : red;
                text-align : center;
                margin : 20px;
            }
        </style>
    </head>
    <body>
        <div>
            <p id="error_message"><jsp:expression> servlet.Utility.getErrorMessage(request.getParameter("error")) </jsp:expression></p>
        </div>
        <form action="SigninServlet" method="POST">
            <div>
                <span>Email: </span> <input type="text" name="Email">
            </div>
            <div>
                <span>Password: </span> <input type="password" name="Password">
            </div>
            <div>
                <input type="submit" value="Log In">
            </div>
        </form>
        <div>
            <a href="signup.jsp">Don't have an account? Sign up here!</a>
        </div>
        <div>
            <a href="recovery.jsp">Forgot Your Password? Recover Your Account Here.</a>
        </div>
    </body>
</html>
