<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title>Product search</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="searchStyle.css">
    </head>

    <body>
        <div>
            <form method = "GET" action="displayLeadingTechnologists.jsp">
                <h3>Введите номер изделия:</h3>
                <label>
                    <input class="label" type="text" name="numberProduct" value="" placeholder="0000.00.00.000" autocomplete="off" required>
                </label>
                <br>
                <button>Ввод</button>
            </form>
        </div>
    </body>

</html>