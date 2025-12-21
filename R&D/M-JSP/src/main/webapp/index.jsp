<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
<title>Year Calender</title>
<style>
body {
    font-family: Arial, sans-serif;
    background: #f4f6f8;
}

.year-header {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    margin: 20px;
}

.year-header button {
    padding: 6px 12px;
    font-size: 16px;
    cursor: pointer;
}

.calendar-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    padding: 20px;
}

.month-card {
    background: white;
    border-radius: 10px;
    padding: 10px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.month-title {
    text-align: center;
    font-weight: bold;
    margin-bottom: 8px;
}

table {
    width: 100%;
    border-collapse: collapse;
    font-size: 12px;
}

th {
    color: #555;
    padding-bottom: 4px;
}

td {
    text-align: center;
    padding: 4px;
}

.day {
    border-radius: 0;
    padding: 5px;
    cursor: pointer;
}

.day:hover {
    background: #d0e8ff;
}

.today {
    background: #4CAF50;
    color: white;
}
.holiday {
    background: rgb(192, 28, 40);
    color: white;
    font-weight: bold;
    border-radius: 0;
    
}

.holiday:hover {
    background: rgb(255, 77, 77);
}
.holiday-label{
display: block;
    font-size: 1px;
    margin-top: 2px;
}




</style>
</head>
<body>
<script>
function openHeatmap(year, month) {
    window.location.href =
        "heatmap.jsp?year=" + year + "&month=" + month;
}
</script>

<%
Map<String, String> bdHolidays = new HashMap<>();

// Fixed-date national holidays (Bangladesh)
bdHolidays.put("02-21", "Shaheed Day & International Mother Language Day");
bdHolidays.put("03-17", "Sheikh Mujibur Rahman's Birthday");
bdHolidays.put("03-26", "Independence Day");
bdHolidays.put("05-01", "May Day");
bdHolidays.put("08-15", "National Mourning Day");
bdHolidays.put("12-16", "Victory Day");
bdHolidays.put("12-25", "Christmas Day");

// Note: Islamic holidays change every year
%>


<%
     Calendar today = Calendar.getInstance();
     //int day = 1;
     int year;
  
    
   
     String[] months = {
    	        "January", "February", "March", "April", "May", "June",
    	        "July", "August", "September", "October", "November", "December"
    	    };
     String[] days = {
 	        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
 	        "Sat"
 	    };
     
     
     String yearValue = request.getParameter("yearValue");
     if(yearValue != null) year = Integer.parseInt(yearValue);
     else year = today.get(Calendar.YEAR);
     
     String hourParam = request.getParameter("hour");
     String minParam = request.getParameter("minute");
     String monthParam = request.getParameter("month");
     
   
    
     String yearAction = request.getParameter("yearAction");
     if ("add".equals(yearAction)) {
         year++;
     } else if ("sub".equals(yearAction)) {
         year--;
     }

     
     
%>



<form method="post" class="year-header">
    <button type="submit" name="yearAction" value="sub">-</button>

    <strong><%= year %></strong>

    <button type="submit" name="yearAction" value="add">+</button>

    <!-- persist state -->
    <input type="hidden" name="yearValue" value="<%= year %>">
    <%-- <input type="hidden" name="month" value="<%= month %>">
    <input type="hidden" name="hour" value="<%= hour %>">
<input type="hidden" name="minute" value="<%= minute %>"> --%>
    
</form>




<div class="calendar-grid">

<%
for (int month = 0; month < 12; month++) {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, 1);

    int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
    int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
%>

<div class="month-card" onclick="openHeatmap(<%= year %>, <%= month %>)"> 
    <div class="month-title"><%= months[month] %></div>

    <table>
        <tr>
            <% for (String d : days) { %>
                <th><%= d %></th>
            <% } %>
        </tr>

        <tr>
            <% for (int i = 0; i < firstDay; i++) { %>
                <td></td>
            <% } %>

            <%
            int day = 1;
            for (; day <= 7 - firstDay; day++) {
                boolean isToday =
                    year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH);
            %>
            <%
String monthKey = String.format("%02d", month + 1);
String dayKey = String.format("%02d", day);
String dateKey = monthKey + "-" + dayKey;

String holidayName = bdHolidays.get(dateKey);
boolean isHoliday = holidayName != null;
%>

<td>
    <div class="day
        <%= isToday ? "today" : "" %>
        <%= isHoliday ? "holiday" : "" %>"
        title="<%= isHoliday ? holidayName  : "" %>">

        <%= day %>

        <% if (isHoliday) { %>
            <span class="holiday-label">*</span>
        <% } %>
    </div>
</td>

            <% } %>
        </tr>

        <% while (day <= daysInMonth) { %>
        <tr>
            <% for (int i = 0; i < 7 && day <= daysInMonth; i++, day++) {
                boolean isToday =
                    year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH);
            %>
           <%
String monthKey = String.format("%02d", month + 1);
String dayKey = String.format("%02d", day);
String dateKey = monthKey + "-" + dayKey;

String holidayName = bdHolidays.get(dateKey);
boolean isHoliday = holidayName != null;
%>

<td>
    <div class="day
        <%= isToday ? "today" : "" %>
        <%= isHoliday ? "holiday" : "" %>"
        title="<%= isHoliday ? holidayName : "" %>">

        <%= day %>

        <% if (isHoliday) { %>
            <span class="holiday-label">*</span>
        <% } %>
    </div>
</td>

            <% } %>
        </tr>
        <% } %>

    </table>
</div>

<% } %>

</div>




</body>
</html>

