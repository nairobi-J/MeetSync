<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<title>Event creation</title>
=======
<title>Time Slot Picker</title>
>>>>>>> Stashed changes
=======
<title>Time Slot Picker</title>
>>>>>>> Stashed changes

<style>
body {
    font-family: Arial, sans-serif;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    padding: 20px;
}

.controls {
    margin-bottom: 15px;
=======
=======
>>>>>>> Stashed changes
}

.controls {
    margin-bottom: 10px;
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}

.calendar {
    display: grid;
    grid-template-columns: 70px 1fr;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    width: 520px;
    border: 1px solid #ccc;
    user-select: none;
=======
=======
>>>>>>> Stashed changes
    width: 500px;
    border: 1px solid #ccc;
}

.hours {
    background: #f5f5f5;
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}

.hour {
    height: 60px;
    border-bottom: 1px solid #ddd;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    font-size: 12px;
    text-align: right;
    padding-right: 6px;
    line-height: 60px;
    background: #f5f5f5;
=======
=======
>>>>>>> Stashed changes
    text-align: right;
    padding-right: 6px;
    font-size: 12px;
    line-height: 60px;
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}

.timeline {
    position: relative;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
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
=======
=======
>>>>>>> Stashed changes
    height: 1440px; /* 24h * 60min */
    background: white;
}

.slot {
    position: absolute;
    left: 10px;
    right: 10px;
    border: 1px solid #1a73e8;
    background: #e3f2fd;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    padding: 2px 4px;
}

.slot.selected {
    background: #1a73e8;
    color: white;
}
</style>

<script>
let selected = null;

function buildSlots() {
    const duration = parseInt(document.getElementById("duration").value);
    const timeline = document.getElementById("timeline");
    timeline.innerHTML = "";

    for (let min = 0; min < 1440; min += duration) {
        const h = Math.floor(min / 60);
        const m = min % 60;

        const slot = document.createElement("div");
        slot.className = "slot";
        slot.style.top = min + "px";
        slot.style.height = duration + "px";
        slot.dataset.time =
            String(h).padStart(2,'0') + ":" +
            String(m).padStart(2,'0');

        slot.innerHTML = slot.dataset.time;

        slot.onclick = function () {
            if (selected) selected.classList.remove("selected");
            this.classList.add("selected");
            selected = this;
            document.getElementById("selectedTime").innerText =
                this.dataset.time + " (" + duration + " min)";
        };

        timeline.appendChild(slot);
    }
}
</script>

</head>
<body>

<h2>Time Slot Picker</h2>

<div class="controls">
    Slot Duration:
    <select id="duration" onchange="buildSlots()">
        <option value="15">15 minutes</option>
        <option value="20">20 minutes</option>
        <option value="25">25 minutes</option>
        <option value="30" selected>30 minutes</option>
        <option value="60">1 hour</option>
    </select>
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
</div>

<div class="calendar">

<<<<<<< Updated upstream
<<<<<<< Updated upstream
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
	if (selection) selection.remove();
	
    selection = null;
    updateInputs(0.00, 0.00)
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
    
    //selectedStart = selectedEnd = null;
    eventName.value = "";
    closeModal();
    updateInputs(selectedStart, selectedEnd);
}

function hasConflict(start, end) {
	
    return events.some(ev => !(end <= ev.start || start >= ev.end));
}
const startInput = document.getElementById("startTime");
const endInput = document.getElementById("endTime");
startInput.addEventListener("input", drawFromInputs);
endInput.addEventListener("input", drawFromInputs);
function drawFromInputs(){
	if(!startInput.value && !endInput.value) return;
	const start = timeToMinutes(startInput.value);
	const end = timeToMinutes(endInput.value);
	if(end <= start) return;
	selectedStart = start;
    selectedEnd   = end;
	drawSelection(start, end);
}
=======
=======
>>>>>>> Stashed changes
    <!-- HOURS (JSP LOOP) -->
    <div class="hours">
        <% for (int h = 0; h < 24; h++) { %>
            <div class="hour">
                <%= String.format("%02d:00", h) %>
            </div>
        <% } %>
    </div>

    <!-- TIME SLOTS -->
    <div class="timeline" id="timeline"></div>

</div>

<p>
    Selected Slot:
    <strong id="selectedTime">None</strong>
</p>

<script>
buildSlots(); // initial render
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
</script>

</body>
</html>
