<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
<title>Basic Month calendar</title>
</head>
<body>

<%
     Calendar cal = Calendar.getInstance();
     int day = 1;
     int year;
     int hour = cal.get(Calendar.HOUR_OF_DAY);
     int minute = cal.get(Calendar.MINUTE);
     int month = cal.get(Calendar.MONTH);
     
     
     String dayParam = request.getParameter("day");
     if (dayParam != null) {
         day = Integer.parseInt(dayParam);
     }

    
   
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
     else year = cal.get(Calendar.YEAR);
     
     String hourParam = request.getParameter("hour");
     String minParam = request.getParameter("minute");
     String monthParam = request.getParameter("month");
     
     if(hourParam != null){
    	 hour = Integer.parseInt(hourParam);
     }
     if(minParam != null){
    	 minute = Integer.parseInt(minParam);
     }

     if (monthParam != null) {
         month = Integer.parseInt(monthParam);
         
     }
    
     String yearAction = request.getParameter("yearAction");
     if ("add".equals(yearAction)) {
         year++;
     } else if ("sub".equals(yearAction)) {
         year--;
     }

      
     cal.set(year, month, day, hour, minute, 0);

     
     int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
     int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
     

     //cal.set(year + 1, month - 1, 1);
     
%>



<form method="post" style="display:flex; gap:10px;">
    <button type="submit" name="yearAction" value="sub">-</button>

    <strong><%= year %></strong>

    <button type="submit" name="yearAction" value="add">+</button>

    <!-- persist state -->
    <input type="hidden" name="yearValue" value="<%= year %>">
    <input type="hidden" name="month" value="<%= month %>">
    <input type="hidden" name="hour" value="<%= hour %>">
<input type="hidden" name="minute" value="<%= minute %>">
    
</form>


<form method="post" style="display:flex; gap:10px;">
<%
    for (int i = 0; i < 12; i++) {
%>
    <button type="submit" name="month" value="<%= i %>">
        <%= months[i] %>
      
    </button>
    <input type="hidden" name="yearValue" value="<%= year %>">
    <input type="hidden" name="hour" value="<%= hour %>">
<input type="hidden" name="minute" value="<%= minute %>">
    
   
<%
    }
%>
</form>


<table>
      <tr>
          <th>Sun</th>
          <th>Mon</th>
          <th>Tue</th>
          <th>Wed</th>
          <th>Thu</th>
          <th>Fri</th>
          <th>Sat</th>  
      </tr>
      <tr>
      <%for(int i = 0; i < firstDayOfWeek; i++) {%>
      <td></td>
      <% } 
      
      for(day = 1; day <= 7 - firstDayOfWeek; day++)
      {
    	 %>
    	 <td>
        <form action="timechart.jsp" method="post" style="margin:0;">
            <input type="hidden" name="year" value="<%= year %>">
            <input type="hidden" name="month" value="<%= month %>">
            <button type="submit" name="day" value="<%= day %>">
                <%= day %>
            </button>
        </form>
    </td>
      <%} %>
    	  
      
     
      </tr>
      
      <%for(int i = day; i <= daysInMonth ;) {%>
    	  <tr>
    	  <% for(int j = 0; j < 7 && i <= daysInMonth; i++, j++){ %>
    	  <td>
        <form action="timechart.jsp" method="post" style="margin:0;">
            <input type="hidden" name="year" value="<%= year %>">
            <input type="hidden" name="month" value="<%= month %>">
            <button type="submit" name="day" value="<%= i %>">
                <%= i %>
            </button>
        </form>
    </td>
    		  
    	 <%}
      } %> 
      </tr>
      
      
</table>



</body>
</html>

