// Global variables
const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

let currentMonth = new Date();
let slotsByDate = {};
let availableDates = [];
let selectedDate = null;
let selectedTime = null;
let emailPrefix = '';
let localeTz = '';
let step = 1;

// Initialize booking schedule
function initializeBookingSchedule(prefix, availableSlotsData) {
        // Attach navigation handlers after DOM is ready
        const backBtn = document.getElementById('backbtn');
        const nextBtn = document.getElementById('nextbtn');
        if (backBtn) {
            backBtn.onclick = function () {
                if (step > 1) step--;
                updateStepUI();
            };
        }
        if (nextBtn) {
            nextBtn.onclick = function () {
                // If moving from step 1 -> 2, ensure a date and time are selected
                if (step === 1) {
                    if (!selectedDate || !selectedTime) {
                        setStatus('Please pick a date and time.', true);
                        return;
                    }
                    step = 2;
                    updateStepUI();
                    return;
                }
                if (step < 2) step++;
                updateStepUI();
            };
        }
        updateStepUI();
    emailPrefix = prefix;
    localeTz = Intl.DateTimeFormat().resolvedOptions().timeZone;

    currentMonth.setDate(1);

    document.getElementById('timezoneDisplay').textContent = `${localeTz}`;
    document.getElementById('prevMonth').onclick = goToPrevMonthWithSlots;
    document.getElementById('nextMonth').onclick = goToNextMonthWithSlots;



    // Form submission handler
    document.getElementById('bookingForm').onsubmit = function (e) {
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


function updateStepUI() {
    // Show/hide sections based on step
    const calendarDiv = document.getElementById('calendarDiv');
    const timesDiv = document.getElementById('timesDiv');
    const bookingDiv = document.getElementById('bookingDiv');
    const backBtn = document.getElementById('backbtn');
    const nextBtn = document.getElementById('nextbtn');
    if (calendarDiv && timesDiv && bookingDiv) {
        if (step === 1) {
            calendarDiv.classList.remove('hidden');
            timesDiv.classList.remove('hidden');
            bookingDiv.classList.add('hidden');
            if (backBtn) backBtn.style.display = 'none';
            if (nextBtn) nextBtn.style.display = '';
        } else if (step === 2) {
            calendarDiv.classList.add('hidden');
            timesDiv.classList.add('hidden');
            bookingDiv.classList.remove('hidden');
            if (backBtn) backBtn.style.display = '';
            if (nextBtn) nextBtn.style.display = 'none';
        }
    }
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
        grid.innerHTML = `
  <div class="flex flex-col items-center justify-center w-full py-12">
    <div class="flex items-center justify-center mb-4">
      <svg class="w-10 h-10 text-red-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
        <path stroke-linecap="round" stroke-linejoin="round" d="M9.5 9.5l5 5m0-5l-5 5" />
      </svg>
    </div>
    <div class="text-lg font-semibold text-red-600 mb-2 text-center">
      Host is Unavailable
    </div>
    <div class="text-sm text-gray-500 text-center max-w-xs">
      There are no available dates right now.<br>
      Please check back later or contact the host for more information.
    </div>
  </div>
`;
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
    // Reset base classes and apply warning/success styles
    el.className = 'text-xs text-center';
    if (isError) {
        // Show as warning (amber)
        el.classList.add('border', 'border-red-200', 'bg-red-50', 'text-red-800', 'px-3', 'py-2', 'rounded');
    } else if (isSuccess) {
        el.classList.add('border', 'border-green-200', 'bg-green-50', 'text-green-700', 'px-3', 'py-2', 'rounded');
    } else {
        el.classList.add('text-gray-500');
    }
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
