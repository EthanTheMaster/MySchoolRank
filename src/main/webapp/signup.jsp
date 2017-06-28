<html>
<body>
    <div>
        <p id="error_message"><jsp:expression> servlet.Utility.getErrorMessage(request.getParameter("error")) </jsp:expression></p>
    </div>
    <form action="SignupServlet" method="POST">
        <div>
            <span>School Email: </span> <input type="text" name="Email">
        </div>
        <div>
            <span>Password: </span> <input type="password" name="Password">
        </div>
        <div>
            <span>Confirm Password: </span> <input type="password" name="ConfirmPassword">
        </div>
        <input type="submit" value="Sign Up">
    </form>
</body>
</html>