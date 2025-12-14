<%@ page import="java.util.Calendar" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Show Calendar Example</title>
</head>
<body>
    <h2>Current Date and Time</h2>
    
    <% 
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Months are 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
    %>
    
    <p>Date: <%= day %>/<%= month %>/<%= year %></p>
    <p>Time: <%= hour %>:<%= minute %>:<%= second %></p>
    
    <!-- Using calendar directly -->
    <p>Full Date: <%= calendar.getTime() %></p>
</body>
</html>