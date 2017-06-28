<html>
<body>
    <div>
        <p id="error_message"><jsp:expression> servlet.Utility.getErrorMessage(request.getParameter("error")) </jsp:expression></p>
    </div>
    <form action="RecoveryServlet" method="POST">
        <div>
            <span>Email: </span> <input type="text" name="Email">
        </div>
        <input type="submit" value="Submit">
    </form>
</body>
</html>