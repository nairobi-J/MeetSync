<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
<title>Time Slot Picker</title>

<style>
body {
    font-family: Arial, sans-serif;
}

.controls {
    margin-bottom: 10px;
}

.calendar {
    display: grid;
    grid-template-columns: 70px 1fr;
    width: 500px;
    border: 1px solid #ccc;
}

.hours {
    background: #f5f5f5;
}

.hour {
    height: 60px;
    border-bottom: 1px solid #ddd;
    text-align: right;
    padding-right: 6px;
    font-size: 12px;
    line-height: 60px;
}

.timeline {
    position: relative;
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
</div>

<div class="calendar">

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
</script>

</body>
</html>
