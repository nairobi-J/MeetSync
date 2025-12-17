<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>HeatMap</title>

<style>
body {
    font-family: Arial, sans-serif;
}

.calendar {
    display: grid;
    grid-template-columns: 70px repeat(29, 1fr);
    width: 100%;
    max-width: 2000px;
    border: 1px solid #ccc;
}

.header {
    background: #34a853;
    color: white;
    text-align: center;
    font-size: 14px;
    border-bottom: 2px solid #ccc;
    height: 40px;
}

.time {
    background: #f5f5f5;
    font-size: 12px;
    text-align: right;
    padding-right: 5px;
    border-right: 1px solid #ccc;
    height: 40px;
    line-height: 40px;
}

.cell {
    border: 1px solid #eee;
    height: 40px;
}
</style>
</head>

<body>

<div class="calendar">

    
    <div></div>
     <%ArrayList selected [] = new ArrayList(); %>

    <% for (int d = 1; d <= 29; d++) { %>
        <div class="header"><%= d %></div>
    <% } %>

  
    <% for (int h = 0; h < 24; h++) { %>

       
        <div class="time">
            <%= String.format("%02d:00", h) %>
        </div>

       
        <% for (int d = 1; d <= 29; d++) { %>
            <div class="cell"></div>
        <% } %>

    <% } %>

</div>

</body>
</html>
