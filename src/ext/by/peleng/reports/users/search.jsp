<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title>TestJSP</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="searchStyle.css">
    </head>

    <body>
        <div>
            <form method = "GET" action="users.jsp">
                <h3>Введите фамилию:</h3>
                <label>
                    <input type="text" name="lastName" value="" placeholder="Иванов*" autocomplete="off" required>
                </label>
                <br>
                <button><span>Ввод </span></button>
            </form>
        </div>
    </body>

</html>