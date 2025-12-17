<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
<title>Event Calendar</title>

<style>
body {
    font-family: Arial, sans-serif;
    padding: 20px;
}

.controls {
    margin-bottom: 15px;
}

.calendar {
    display: grid;
    grid-template-columns: 70px 1fr;
    width: 520px;
    border: 1px solid #ccc;
    user-select: none;
}

.hour {
    height: 60px;
    border-bottom: 1px solid #ddd;
    font-size: 12px;
    text-align: right;
    padding-right: 6px;
    line-height: 60px;
    background: #f5f5f5;
}

.timeline {
    position: relative;
    height: 1440px;
    background: white;
    cursor: crosshair;
}

.selection {
    position: absolute;
    left: 10px;
    right: 10px;
    background: rgba(66,133,244,0.5);
    border-radius: 4px;
    pointer-events: none;
}

.event {
    position: absolute;
    left: 10px;
    right: 10px;
    background: #34a853;
    color: white;
    border-radius: 4px;
    padding: 4px;
    font-size: 12px;
}

/* MODAL */
.modal {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.4);
    align-items: center;
    justify-content: center;
}

.modal-content {
    background: white;
    padding: 15px;
    border-radius: 6px;
    width: 300px;
}
</style>
</head>
<body>

<h2>Event Calendar</h2>

<div class="controls">
    Start: <input type="time" id="startTime">
    End: <input type="time" id="endTime">
    <button onclick="openModal()">Create Event</button>
</div>

<div class="calendar">

    <div>
        <% for (int h = 0; h < 24; h++) { %>
            <div class="hour"><%= String.format("%02d:00", h) %></div>
        <% } %>
    </div>

    <div class="timeline" id="timeline"></div>
</div>

<!-- MODAL -->
<div class="modal" id="eventModal">
    <div class="modal-content">
        <h4>Event Name</h4>
        <input type="text" id="eventName" style="width:100%">
        <br><br>
        <button onclick="saveEvent()">Save</button>
        <button onclick="closeModal()">Cancel</button>
    </div>
</div>

<script>
const timeline = document.getElementById("timeline");

let selection = null;
let isDragging = false;
let startMin = 0;
let selectedStart = null;
let selectedEnd = null;

let events = []; // {start, end, name}

function timeToMinutes(t) {
    const [h, m] = t.split(":").map(Number);
    return h * 60 + m;
}

function minutesToTime(m) {
    return String(Math.floor(m / 60)).padStart(2,'0') + ":" +
           String(m % 60).padStart(2,'0');
}

function updateInputs(start, end) {
    startTime.value = minutesToTime(start);
    endTime.value = minutesToTime(end);
   
}

function drawSelection(start, end) {
    if (!selection) {
        selection = document.createElement("div");
        selection.className = "selection";
        timeline.appendChild(selection);
    }
    selection.style.top = start + "px";
    selection.style.height = (end - start) + "px";
}

timeline.addEventListener("mousedown", e => {
    isDragging = true;
    startMin = Math.floor(e.offsetY);
});

timeline.addEventListener("mousemove", e => {
    if (!isDragging) return;

    const current = Math.floor(e.offsetY);
    selectedStart = Math.min(startMin, current);
    selectedEnd = Math.max(startMin, current);

    drawSelection(selectedStart, selectedEnd);
    updateInputs(selectedStart, selectedEnd);
});

document.addEventListener("mouseup", () => {
    isDragging = false;
    
});



function openModal() {
    if (selectedStart == null || selectedEnd == null || selectedEnd <= selectedStart) {
        alert("Select a valid time range first");
        return;
    }

    if (hasConflict(selectedStart, selectedEnd)) {
    	 
    	if (selection) selection.remove();
	    selection = null;
	    selectedStart = selectedEnd = null;
        alert("Time conflicts with existing event");
        return;
    }

    document.getElementById("eventModal").style.display = "flex";
}

function closeModal() {
	
    document.getElementById("eventModal").style.display = "none";
}

function saveEvent() {
	
    const name = document.getElementById("eventName").value.trim();
    
    if (!name) return;

    events.push({ start: selectedStart, end: selectedEnd, name });

    const ev = document.createElement("div");
    ev.className = "event";
    ev.style.top = selectedStart + "px";
    ev.style.height = (selectedEnd - selectedStart) + "px";
    ev.innerText = name;
    timeline.appendChild(ev);
    
    
    if (selection) selection.remove();
    selection = null;
    
    selectedStart = selectedEnd = null;
    eventName.value = "";
    closeModal();
    updateInputs(start, end);
}

function hasConflict(start, end) {
	
    return events.some(ev => !(end <= ev.start || start >= ev.end));
}
</script>

</body>
</html>
