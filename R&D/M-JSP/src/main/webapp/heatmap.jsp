<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Generic HeatMap</title>

<style>
.calendar {
    display: grid;
    grid-template-columns: 70px repeat(29, 1fr);
    border: 1px solid #ccc;
    user-select: none;
}

.header {
    background: #34a853;
    color: white;
    text-align: center;
    height: 40px;
    line-height: 40px;
}

.time {
    background: #f5f5f5;
    text-align: right;
    padding-right: 5px;
    height: 40px;
    line-height: 40px;
}
.cell {
    border: 1px solid #eee;
    height: 40px;
    cursor: pointer;

    /* heat value: 0 = white */
    --heat: 0;

    /* green heatmap */
    background-color: hsl(
        140,               /* green hue */
        90%,               /* saturation */
        calc(100% - var(--heat) * 10%)
    );

    transition: background-color 0.15s ease;
}

.cell.temp {
    outline: 2px solid rgba(66,133,244,0.8);
}


.cell.committed {
    background: rgba(66,133,244,0.3);
}


</style>
</head>

<body>

<button onclick="commitSelection()">Done</button>

<div class="calendar">

    <div></div>

    <% for (int d = 1; d <= 29; d++) { %>
        <div class="header"><%= d %></div>
    <% } %>

    <% for (int h = 0; h < 24; h++) { %>
        <div class="time"><%= String.format("%02d:00", h) %></div>
        <% for (int d = 1; d <= 29; d++) { %>
            <div class="cell" data-key="<%= d %>-<%= h %>"></div>
        <% } %>
    <% } %>

</div>

<script>
const committed = {};
let isMouseDown = false;

document.addEventListener("mousedown", () => isMouseDown = true);
document.addEventListener("mouseup", () => isMouseDown = false);

document.querySelectorAll(".cell").forEach((cell, index) => {

    //cell.dataset.key = index;

    // click select
    cell.addEventListener("click", () => {
        cell.classList.add("temp");
    });

    // drag select
    cell.addEventListener("mouseenter", () => {
        if (isMouseDown) {
            cell.classList.add("temp");
        }
    });
});

function commitSelection() {
    document.querySelectorAll(".cell.temp").forEach(cell => {

    	const key = cell.dataset.key;
    	committed[key] = (committed[key] || 0) + 1;

    	const count = committed[key];

    	cell.style.setProperty("--heat", committed[key]);

        cell.classList.remove("temp");

    });

    console.log(committed);
}
</script>


</body>
</html>
