// Global variables
let currentDate = new Date();
let selectedDates = new Set();
let dateOverrides = {};
let overrideCounter = 0;
let existingWeeklyAvailability = [];
let existingDateOverrides = [];

// Calendar rendering
function renderCalendar() {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    // Update month display
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('currentMonth').textContent = `${monthNames[month]} ${year}`;

    // Get first day of month and number of days
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const grid = document.getElementById('calendarGrid');
    grid.innerHTML = '';

    // Add empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
        const emptyCell = document.createElement('div');
        grid.appendChild(emptyCell);
    }

    // Add days of month
    for (let day = 1; day <= daysInMonth; day++) {
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const dayCell = document.createElement('div');
        dayCell.className = 'calendar-day';
        dayCell.textContent = day;
        dayCell.dataset.date = dateStr;

        if (selectedDates.has(dateStr)) {
            dayCell.classList.add('selected');
        }

        dayCell.onclick = () => toggleDate(dateStr, dayCell);
        grid.appendChild(dayCell);
    }
}

// Toggle advanced settings
function toggleAdvancedSettings() {
    const section = document.getElementById("advancedSettings");
    const arrow = document.getElementById("advancedArrow");

    section.classList.toggle("hidden");
    arrow.classList.toggle("rotate-180");
}

// Toggle date selection
function toggleDate(dateStr, element) {
    if (selectedDates.has(dateStr)) {
        selectedDates.delete(dateStr);
        element.classList.remove('selected');
        removeDateOverride(dateStr);
    } else {
        selectedDates.add(dateStr);
        element.classList.add('selected');
        addDateOverride(dateStr);
    }
    updateSelectedDatesDisplay();
}

// Add date override
function addDateOverride(dateStr) {
    const container = document.getElementById('overrideEntriesContainer');
    const index = overrideCounter++;

    const overrideDiv = document.createElement('div');
    overrideDiv.id = `override-${dateStr}`;
    overrideDiv.className = 'p-4 bg-blue-50 border border-blue-200 rounded-lg';
    overrideDiv.innerHTML = `
        <div class="flex items-center justify-between mb-3">
            <span class="font-semibold text-gray-800">
                <i class="fas fa-calendar-day text-blue-600 mr-2"></i>
                ${new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}
            </span>
            <button type="button" onclick="removeDate('${dateStr}')" 
                    class="text-red-600 hover:text-red-800">
                <i class="fas fa-times"></i>
            </button>
        </div>
        
        <div class="space-y-2">
            <label class="flex items-center">
                <input type="checkbox" 
                       id="unavailable-${dateStr}"
                       onchange="toggleUnavailable('${dateStr}')"
                       class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 mr-2">
                <span class="text-sm text-gray-700">Mark as unavailable</span>
            </label>
            
            <div id="timeFields-${dateStr}" class="grid grid-cols-2 gap-2">
                <div>
                    <label class="text-xs text-gray-600">Start Time</label>
                    <input type="time" 
                           id="start-${dateStr}"
                           value="09:00"
                           onchange="updateOverrideData('${dateStr}')"
                           class="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="text-xs text-gray-600">End Time</label>
                    <input type="time" 
                           id="end-${dateStr}"
                           value="17:00"
                           onchange="updateOverrideData('${dateStr}')"
                           class="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-blue-500">
                </div>
            </div>
        </div>
    `;

    container.appendChild(overrideDiv);

    // Initialize override data
    dateOverrides[dateStr] = {
        index: index,
        date: dateStr,
        startTime: '09:00',
        endTime: '17:00',
        unavailable: false
    };
    updateFormInputs();
}

// Remove date override
function removeDateOverride(dateStr) {
    const overrideDiv = document.getElementById(`override-${dateStr}`);
    if (overrideDiv) {
        overrideDiv.remove();
    }
    delete dateOverrides[dateStr];
    updateFormInputs();
}

// Remove date
function removeDate(dateStr) {
    selectedDates.delete(dateStr);
    removeDateOverride(dateStr);
    renderCalendar();
    updateSelectedDatesDisplay();
}

// Toggle unavailable
function toggleUnavailable(dateStr) {
    const checkbox = document.getElementById(`unavailable-${dateStr}`);
    const timeFields = document.getElementById(`timeFields-${dateStr}`);

    if (checkbox.checked) {
        timeFields.style.display = 'none';
        dateOverrides[dateStr].unavailable = true;
        dateOverrides[dateStr].startTime = null;
        dateOverrides[dateStr].endTime = null;
    } else {
        timeFields.style.display = 'grid';
        dateOverrides[dateStr].unavailable = false;
        dateOverrides[dateStr].startTime = document.getElementById(`start-${dateStr}`).value;
        dateOverrides[dateStr].endTime = document.getElementById(`end-${dateStr}`).value;
    }
    updateFormInputs();
}

// Update override data
function updateOverrideData(dateStr) {
    const startTime = document.getElementById(`start-${dateStr}`).value;
    const endTime = document.getElementById(`end-${dateStr}`).value;

    dateOverrides[dateStr].startTime = startTime;
    dateOverrides[dateStr].endTime = endTime;
    updateFormInputs();
}

// Update form inputs
function updateFormInputs() {
    const container = document.getElementById('dateOverridesInputsContainer');
    container.innerHTML = '';

    Object.values(dateOverrides).forEach((override, idx) => {
        // Create hidden inputs for each override
        const dateInput = document.createElement('input');
        dateInput.type = 'hidden';
        dateInput.name = `dateOverrides[${idx}].date`;
        dateInput.value = override.date;
        container.appendChild(dateInput);

        const unavailableInput = document.createElement('input');
        unavailableInput.type = 'hidden';
        unavailableInput.name = `dateOverrides[${idx}].unavailable`;
        unavailableInput.value = override.unavailable;
        container.appendChild(unavailableInput);

        if (!override.unavailable && override.startTime && override.endTime) {
            const startTimeInput = document.createElement('input');
            startTimeInput.type = 'hidden';
            startTimeInput.name = `dateOverrides[${idx}].startTime`;
            startTimeInput.value = override.startTime + ':00';
            container.appendChild(startTimeInput);

            const endTimeInput = document.createElement('input');
            endTimeInput.type = 'hidden';
            endTimeInput.name = `dateOverrides[${idx}].endTime`;
            endTimeInput.value = override.endTime + ':00';
            container.appendChild(endTimeInput);
        }
    });
}

// Update selected dates display
function updateSelectedDatesDisplay() {
    const container = document.getElementById('selectedDatesContainer');
    if (selectedDates.size > 0) {
        container.classList.remove('hidden');
    } else {
        container.classList.add('hidden');
    }
}

// Previous month
function previousMonth() {
    currentDate.setMonth(currentDate.getMonth() - 1);
    renderCalendar();
}

// Next month
function nextMonth() {
    currentDate.setMonth(currentDate.getMonth() + 1);
    renderCalendar();
}

// Toggle day fields
function toggleDayFields(checkbox) {
    const index = checkbox.getAttribute('data-index');
    const startTimeInput = document.getElementById(`startTime-${index}`);
    const endTimeInput = document.getElementById(`endTime-${index}`);

    if (checkbox.checked) {
        startTimeInput.disabled = false;
        endTimeInput.disabled = false;
    } else {
        startTimeInput.disabled = true;
        endTimeInput.disabled = true;
        // Reset to default values when disabled
        startTimeInput.value = '09:00';
        endTimeInput.value = '17:00';
    }
    updateWeeklyAvailability();
}

// Update weekly availability
function updateWeeklyAvailability() {
    const container = document.getElementById('weeklyAvailabilityInputsContainer');
    container.innerHTML = '';

    const checkboxes = document.querySelectorAll('.day-checkbox');
    let idx = 0;

    checkboxes.forEach(checkbox => {
        if (checkbox.checked) {
            const index = checkbox.getAttribute('data-index');
            const day = checkbox.getAttribute('data-day');
            const startTime = document.getElementById(`startTime-${index}`).value;
            const endTime = document.getElementById(`endTime-${index}`).value;

            // Create hidden inputs for this day
            const dayInput = document.createElement('input');
            dayInput.type = 'hidden';
            dayInput.name = `weeklyAvailability[${idx}].dayOfWeek`;
            dayInput.value = day;
            container.appendChild(dayInput);

            const startInput = document.createElement('input');
            startInput.type = 'hidden';
            startInput.name = `weeklyAvailability[${idx}].startTime`;
            startInput.value = startTime + ':00';
            container.appendChild(startInput);

            const endInput = document.createElement('input');
            endInput.type = 'hidden';
            endInput.name = `weeklyAvailability[${idx}].endTime`;
            endInput.value = endTime + ':00';
            container.appendChild(endInput);

            idx++;
        }
    });
}

// Set all weekly times
function setAllWeeklyTimes() {
    const startValue = document.getElementById('globalStartTime')?.value || '09:00';
    const endValue = document.getElementById('globalEndTime')?.value || '17:00';

    document.querySelectorAll('.day-checkbox').forEach(checkbox => {
        const index = checkbox.getAttribute('data-index');
        const startTimeInput = document.getElementById(`startTime-${index}`);
        const endTimeInput = document.getElementById(`endTime-${index}`);

        if (startTimeInput) {
            startTimeInput.value = startValue;
        }
        if (endTimeInput) {
            endTimeInput.value = endValue;
        }
    });

    updateWeeklyAvailability();
}

// Load existing availability
function loadExistingAvailability() {
    // Load existing weekly availability
    if (existingWeeklyAvailability && existingWeeklyAvailability.length > 0) {
        existingWeeklyAvailability.forEach(availability => {
            const dayOfWeek = availability.dayOfWeek;
            const startTime = availability.startTime ? availability.startTime.slice(0, 5) : '09:00';
            const endTime = availability.endTime ? availability.endTime.slice(0, 5) : '17:00';

            // Find the checkbox for this day
            const dayCheckbox = document.querySelector(`[data-day="${dayOfWeek}"]`);
            if (dayCheckbox) {
                const index = dayCheckbox.getAttribute('data-index');

                // Check the checkbox
                dayCheckbox.checked = true;

                // Enable and set time fields
                const startTimeInput = document.getElementById(`startTime-${index}`);
                const endTimeInput = document.getElementById(`endTime-${index}`);

                if (startTimeInput && endTimeInput) {
                    startTimeInput.disabled = false;
                    endTimeInput.disabled = false;
                    startTimeInput.value = startTime;
                    endTimeInput.value = endTime;
                }
            }
        });
        updateWeeklyAvailability();
    }

    // Load existing date overrides
    if (existingDateOverrides && existingDateOverrides.length > 0) {
        existingDateOverrides.forEach(override => {
            const dateStr = override.date;
            selectedDates.add(dateStr);

            const container = document.getElementById('overrideEntriesContainer');
            const index = overrideCounter++;

            const overrideDiv = document.createElement('div');
            overrideDiv.id = `override-${dateStr}`;
            overrideDiv.className = 'p-4 bg-blue-50 border border-blue-200 rounded-lg';

            const startTime = override.startTime ? override.startTime.slice(0, 5) : '09:00';
            const endTime = override.endTime ? override.endTime.slice(0, 5) : '17:00';
            const isUnavailable = override.unavailable || false;

            overrideDiv.innerHTML = `
                <div class="flex items-center justify-between mb-3">
                    <span class="font-semibold text-gray-800">
                        <i class="fas fa-calendar-day text-blue-600 mr-2"></i>
                        ${new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}
                    </span>
                    <button type="button" onclick="removeDate('${dateStr}')" 
                            class="text-red-600 hover:text-red-800">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                
                <div class="space-y-2">
                    <label class="flex items-center">
                        <input type="checkbox" 
                               id="unavailable-${dateStr}"
                               ${isUnavailable ? 'checked' : ''}
                               onchange="toggleUnavailable('${dateStr}')"
                               class="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 mr-2">
                        <span class="text-sm text-gray-700">Mark as unavailable</span>
                    </label>
                    
                    <div id="timeFields-${dateStr}" class="grid grid-cols-2 gap-2" style="${isUnavailable ? 'display:none' : ''}">
                        <div>
                            <label class="text-xs text-gray-600">Start Time</label>
                            <input type="time" 
                                   id="start-${dateStr}"
                                   value="${startTime}"
                                   onchange="updateOverrideData('${dateStr}')"
                                   class="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-blue-500">
                        </div>
                        <div>
                            <label class="text-xs text-gray-600">End Time</label>
                            <input type="time" 
                                   id="end-${dateStr}"
                                   value="${endTime}"
                                   onchange="updateOverrideData('${dateStr}')"
                                   class="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-blue-500">
                        </div>
                    </div>
                </div>
            `;

            container.appendChild(overrideDiv);

            // Initialize override data
            dateOverrides[dateStr] = {
                index: index,
                date: dateStr,
                startTime: isUnavailable ? null : startTime,
                endTime: isUnavailable ? null : endTime,
                unavailable: isUnavailable
            };
        });

        updateFormInputs();
        renderCalendar();
    }
}


// Set existing data (called from template inline script)
function setExistingData(weeklyAvailability, dateOverridesData) {
    existingWeeklyAvailability = weeklyAvailability || [];
    existingDateOverrides = dateOverridesData || [];
}

// Show/hide loading spinner for save button
function showSaveLoadingSpinner(show) {
    const spinner = document.getElementById('saveLoadingSpinner');
    const icon = document.getElementById('saveIcon');
    const btnText = document.getElementById('saveBtnText');
    const saveBtn = document.getElementById('saveAvailabilityBtn');
    
    if (show) {
        spinner.classList.remove('hidden');
        icon.classList.add('hidden');
        btnText.textContent = 'Saving...';
        saveBtn.disabled = true;
    } else {
        spinner.classList.add('hidden');
        icon.classList.remove('hidden');
        btnText.textContent = 'Save Availability';
        saveBtn.disabled = false;
    }
}

function showPersonalLinkBlock() {
    const block = document.getElementById('personalLinkBlock');
    if (block) {
        block.classList.remove('hidden');
        block.scrollIntoView({ behavior: 'smooth' });
    }
}


// Initialize on page load
document.addEventListener('DOMContentLoaded', function () {
    renderCalendar();
    setAllWeeklyTimes();
    loadExistingAvailability();
    
    // Add form submit handler for loading spinner
    const form = document.querySelector('form[action*="/availability/setup"]');
    if (form) {
        form.addEventListener('submit', function(e) {
            showSaveLoadingSpinner(true);
        });
    }
});
