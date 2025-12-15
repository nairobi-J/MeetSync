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
     int year = cal.get(Calendar.YEAR);
     int month = cal.get(Calendar.MONTH);
     /* cal.set(year,month, 1);
     int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
     int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH); */
     String[] months = {
    	        "January", "February", "March", "April", "May", "June",
    	        "July", "August", "September", "October", "November", "December"
    	    };
     String[] days = {
 	        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri",
 	        "Sat"
 	    };
     String monthParam = request.getParameter("month");

     if (monthParam != null) {
         month = Integer.parseInt(monthParam);
         
     }
     cal.set(year,month, 1);
     int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
     int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

     //cal.set(year + 1, month - 1, 1);
     
%>
<form method="post" style="display:flex; gap:10px;">
<%
    for (int i = 0; i < 12; i++) {
%>
    <button type="submit" name="month" value="<%= i %>">
        <%= months[i] %>
        <%cal.set(year,i, 1); %>
    </button>
   
<%
    }
%>
</form>

<%= month %>
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
    	 <td><%= day %></td>
      <%} %>
    	  
      
     
      </tr>
      
      <%for(int i = day; i <= daysInMonth ;) {%>
    	  <tr>
    	  <% for(int j = 0; j < 7 && i <= daysInMonth; i++, j++){ %>
    	  <td><%= i %></td>
    		  
    	 <%}
      } %> 
      </tr>
      
      
</table>



</body>
</html>

