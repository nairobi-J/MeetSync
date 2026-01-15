// Global variables
const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

let currentMonth = new Date();
let slotsByDate = {};
let availableDates = [];
let selectedDate = null;
let selectedTime = null;
let emailPrefix = '';
let localeTz = '';

// Initialize booking schedule
function initializeBookingSchedule(prefix, availableSlotsData) {
    emailPrefix = prefix;
    localeTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    
    currentMonth.setDate(1);

    document.getElementById('timezoneDisplay').textContent = `Times shown in ${localeTz}`;
    document.getElementById('prevMonth').onclick = goToPrevMonthWithSlots;
    document.getElementById('nextMonth').onclick = goToNextMonthWithSlots;
    
    // Set timezone value
    document.getElementById('timezoneInput').value = localeTz;
    
    // Form submission handler
    document.getElementById('bookingForm').onsubmit = function(e) {
        if (!selectedDate || !selectedTime) {
            e.preventDefault();
            setStatus('Please pick a date and time.', true);
            return false;
        }
        document.getElementById('bookingDateInput').value = selectedDate;
        document.getElementById('bookingTimeInput').value = selectedTime;
        
        // Show loading spinner
        showLoadingSpinner(true);
        
        return true;
    };

    // Load from server-side data
    hydrateSlots(availableSlotsData);
    setToFirstAvailableMonth();
    renderCalendar();
    autoSelectFirstAvailable();
    renderSlotList(availableSlotsData);
}

// Hydrate slots from data
function hydrateSlots(data) {
    slotsByDate = {};
    data.forEach(slot => {
        if (!slot.startTime) return;
        const [datePart, timePart] = slot.startTime.split('T');
        if (!datePart || !timePart) return;
        const time = timePart.substring(0, 5);
        if (!slotsByDate[datePart]) slotsByDate[datePart] = [];
        slotsByDate[datePart].push({ time, label: formatTime(time) });
    });
    availableDates = Object.keys(slotsByDate).sort();
    availableDates.forEach(d => slotsByDate[d].sort((a, b) => a.time.localeCompare(b.time)));
}

// Set to first available month
function setToFirstAvailableMonth() {
    if (availableDates.length === 0) return;
    const firstDate = availableDates[0];
    const [y, m, d] = firstDate.split('-').map(Number);
    currentMonth = new Date(y, m - 1, 1);
}

// Check if month has slots
function monthHasSlots(year, month) {
    const monthStr = `${year}-${String(month + 1).padStart(2, '0')}`;
    return availableDates.some(date => date.startsWith(monthStr));
}

// Check if previous month has slots
function hasPrevMonthWithSlots() {
    let testMonth = new Date(currentMonth);
    testMonth.setMonth(testMonth.getMonth() - 1);
    let attempts = 0;
    while (attempts < 24) {
        if (monthHasSlots(testMonth.getFullYear(), testMonth.getMonth())) {
            return true;
        }
        testMonth.setMonth(testMonth.getMonth() - 1);
        attempts++;
    }
    return false;
}

// Check if next month has slots
function hasNextMonthWithSlots() {
    let testMonth = new Date(currentMonth);
    testMonth.setMonth(testMonth.getMonth() + 1);
    let attempts = 0;
    while (attempts < 24) {
        if (monthHasSlots(testMonth.getFullYear(), testMonth.getMonth())) {
            return true;
        }
        testMonth.setMonth(testMonth.getMonth() + 1);
        attempts++;
    }
    return false;
}

// Go to previous month with slots
function goToPrevMonthWithSlots() {
    let testMonth = new Date(currentMonth);
    testMonth.setMonth(testMonth.getMonth() - 1);
    let attempts = 0;
    while (attempts < 24) { // search up to 2 years back
        if (monthHasSlots(testMonth.getFullYear(), testMonth.getMonth())) {
            currentMonth = new Date(testMonth);
            renderCalendar();
            return;
        }
        testMonth.setMonth(testMonth.getMonth() - 1);
        attempts++;
    }
}

// Go to next month with slots
function goToNextMonthWithSlots() {
    let testMonth = new Date(currentMonth);
    testMonth.setMonth(testMonth.getMonth() + 1);
    let attempts = 0;
    while (attempts < 24) { // search up to 2 years forward
        if (monthHasSlots(testMonth.getFullYear(), testMonth.getMonth())) {
            currentMonth = new Date(testMonth);
            renderCalendar();
            return;
        }
        testMonth.setMonth(testMonth.getMonth() + 1);
        attempts++;
    }
}

// Update navigation buttons
function updateNavigationButtons() {
    const prevBtn = document.getElementById('prevMonth');
    const nextBtn = document.getElementById('nextMonth');

    const hasPrev = hasPrevMonthWithSlots();
    const hasNext = hasNextMonthWithSlots();

    if (hasPrev) {
        prevBtn.className = 'px-3 py-2 rounded-lg text-sm font-medium transition text-blue-600 hover:bg-blue-50';
        prevBtn.disabled = false;
    } else {
        prevBtn.className = 'px-3 py-2 rounded-lg text-sm font-medium transition text-gray-300 cursor-not-allowed';
        prevBtn.disabled = true;
    }

    if (hasNext) {
        nextBtn.className = 'px-3 py-2 rounded-lg text-sm font-medium transition text-blue-600 hover:bg-blue-50';
        nextBtn.disabled = false;
    } else {
        nextBtn.className = 'px-3 py-2 rounded-lg text-sm font-medium transition text-gray-300 cursor-not-allowed';
        nextBtn.disabled = true;
    }
}

// Render slot list
function renderSlotList(data) {
    const container = document.getElementById('slotsList');
    if (!container) return;
    container.innerHTML = '';
    if (!data || !data.length) {
        container.textContent = 'No available slots found.';
        return;
    }
    const formatter = new Intl.DateTimeFormat([], {
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit'
    });
    data.forEach((slot, idx) => {
        if (idx >= 100) return; // prevent overly long lists
        const row = document.createElement('div');
        const start = slot.startTime ? new Date(slot.startTime) : null;
        const end = slot.endTime ? new Date(slot.endTime) : null;
        const startLabel = start ? formatter.format(start) : 'Unknown start';
        const endLabel = end ? formatter.format(end) : 'Unknown end';
        const tz = slot.timezone || 'unspecified tz';
        row.textContent = `${startLabel} → ${endLabel} (${tz})`;
        container.appendChild(row);
    });
    if (data.length > 100) {
        const note = document.createElement('div');
        note.className = 'text-xs text-gray-500';
        note.textContent = `Showing first 100 of ${data.length} slots.`;
        container.appendChild(note);
    }
}

// Render calendar
function renderCalendar() {
    document.getElementById('monthLabel').textContent = `${monthNames[currentMonth.getMonth()]} ${currentMonth.getFullYear()}`;
    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = '';




     if (availableDates.length === 0) {
        monthLabel.textContent = '';
        grid.innerHTML = '<div class="  border border-blue-600 font-semibold flex justify-center items-center text-gray-500 py-8 w-full">No dates are available</div>';
        updateNavigationButtons(); 
        return;
    }
     
    // add grid cols and rows
    grid.className = 'grid grid-cols-7 gap-2';

    const firstDay = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), 1).getDay();
    const daysInMonth = new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 0).getDate();

    for (let i = 0; i < firstDay; i++) {
        const pad = document.createElement('div');
        grid.appendChild(pad);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dateStr = formatDate(currentMonth.getFullYear(), currentMonth.getMonth(), day);
        const btn = document.createElement('button');
        btn.textContent = day;
        btn.className = 'calendar-day text-sm font-medium';

        if (availableDates.includes(dateStr)) {
            btn.classList.add('available', 'text-gray-700', 'hover:bg-gray-100');
            btn.onclick = () => selectDate(dateStr);
            if (dateStr === selectedDate) {
                btn.classList.add('selected');
            }
        } else {
            btn.classList.add('disabled', 'text-gray-300');
            btn.disabled = true;
        }

        grid.appendChild(btn);
    }

    updateNavigationButtons();
}

// Select date
function selectDate(dateStr) {
    selectedDate = dateStr;
    selectedTime = null;
    renderCalendar();
    renderTimes();
    document.getElementById('selectedDateLabel').textContent = formatDateHuman(dateStr);
}

// Render times
function renderTimes() {
    const container = document.getElementById('timesContainer');
    const noTimes = document.getElementById('noTimes');
    container.innerHTML = '';
    const times = slotsByDate[selectedDate] || [];
    if (!times.length) {
        noTimes.classList.remove('hidden');
        return;
    }
    noTimes.classList.add('hidden');
    times.forEach(t => {
        const btn = document.createElement('button');
        btn.textContent = t.label;
        btn.className = 'time-slot w-full text-center text-sm px-4 py-2.5 rounded-lg border border-gray-300 bg-white hover:border-blue-500 hover:bg-blue-50 text-gray-700 font-medium transition';
        if (selectedTime === t.time) {
            btn.className = 'time-slot w-full text-center text-sm px-4 py-2.5 rounded-lg border-2 border-blue-600 bg-blue-600 text-white font-semibold';
        }
        btn.onclick = () => { selectedTime = t.time; renderTimes(); };
        container.appendChild(btn);
    });
}

// Auto select first available
function autoSelectFirstAvailable() {
    if (availableDates.length === 0) return;
    selectDate(availableDates[0]);
}

// Format date
function formatDate(y, mIndex, d) {
    return `${y}-${String(mIndex + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
}

// Format date human readable
function formatDateHuman(dateStr) {
    const [y, m, d] = dateStr.split('-').map(Number);
    const dt = new Date(y, m - 1, d);
    return dt.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });
}

// Format time
function formatTime(timeStr) {
    const [h, m] = timeStr.split(':').map(Number);
    const dt = new Date();
    dt.setHours(h, m, 0, 0);
    return dt.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

// Set status message
function setStatus(msg, isError = false, isSuccess = false) {
    const el = document.getElementById('status');
    el.textContent = msg || '';
    el.className = 'text-xs text-center';
    if (isError) el.classList.add('text-red-600');
    else if (isSuccess) el.classList.add('text-green-600');
    else el.classList.add('text-gray-500');
}

// Show/hide loading spinner
function showLoadingSpinner(show) {
    const spinner = document.getElementById('loadingSpinner');
    const btnText = document.getElementById('btnText');
    const bookBtn = document.getElementById('bookBtn');
    
    if (show) {
        spinner.classList.remove('hidden');
        btnText.textContent = 'Processing...';
        bookBtn.disabled = true;
    } else {
        spinner.classList.add('hidden');
        btnText.textContent = 'Confirm Booking';
        bookBtn.disabled = false;
    }
}
