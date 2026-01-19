document.addEventListener('DOMContentLoaded', async function () {
    const calendarData = document.getElementById('calendarData');
    const urlParams = new URLSearchParams(window.location.search);

    let currentMonth = parseInt(urlParams.get('month')) -1 || 
                       parseInt(localStorage.getItem('calendarMonth')) || 
                       (parseInt(calendarData?.dataset.currentMonth) || new Date().getMonth());

    let currentYear = parseInt(urlParams.get('year')) || 
                      parseInt(localStorage.getItem('calendarYear')) || 
                      (parseInt(calendarData?.dataset.currentYear) || new Date().getFullYear());

    // Cache: year → array of all events in that year
    const eventCache = new Map();
    if (initialEvents.length > 0) {
        eventCache.set(currentYear, initialEvents);
    }

    let miniCalendarDate = new Date(currentYear, currentMonth);

    // Show loader until first render
    document.getElementById('calendarLoading').classList.remove('hidden');
    document.getElementById('calendarGrid').style.display = 'none';

    await loadYearIfNeeded(currentYear);
    renderMainCalendar();
    updateMiniCalendar();

    document.getElementById('calendarLoading').classList.add('hidden');
    document.getElementById('calendarGrid').style.display = 'grid';

    // Navigation
    document.getElementById('prevMainMonth')?.addEventListener('click', () => changeMonth(-1));
    document.getElementById('nextMainMonth')?.addEventListener('click', () => changeMonth(1));

    document.getElementById('todayBtn')?.addEventListener('click', goToToday);

    document.getElementById('prevMonth')?.addEventListener('click', () => {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() - 1);
        changeMonthFromMini();
    });

    document.getElementById('nextMonth')?.addEventListener('click', () => {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() + 1);
        changeMonthFromMini();
    });

    // card data on click

    async function changeMonth(delta) {
        currentMonth += delta;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        } else if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        await navigateToMonth();
    }

    function changeMonthFromMini() {
        currentMonth = miniCalendarDate.getMonth();
        currentYear = miniCalendarDate.getFullYear();
        navigateToMonth();
    }

    async function navigateToMonth() {
        localStorage.setItem('calendarMonth', currentMonth);
        localStorage.setItem('calendarYear', currentYear);

        // Update URL without reload
        const newUrl = `/dashboard?year=${currentYear}&month=${currentMonth + 1}`;
        history.pushState({ month: currentMonth, year: currentYear }, '', newUrl);

        miniCalendarDate = new Date(currentYear, currentMonth);

        document.getElementById('calendarLoading').classList.remove('hidden');
        await loadYearIfNeeded(currentYear);
        renderMainCalendar();
        updateMiniCalendar();
        document.getElementById('calendarLoading').classList.add('hidden');
    }

    async function loadYearIfNeeded(year) {
        if (!eventCache.has(year)) {
            try {
                const res = await fetch(`/api/events/year?year=${year}`);
                if (res.ok) {
                    const data = await res.json();
                    eventCache.set(year, data);
                }
            } catch (err) {
                console.error('Failed to load year events:', err);
            }
        }
    }


    function goToToday() {
        const today = new Date();
        currentMonth = today.getMonth();
        currentYear = today.getFullYear();
        navigateToMonth();
    }

    function renderMainCalendar() {
        const grid = document.getElementById('calendarGrid');
        const title = document.getElementById('currentMonthYear');

        const monthNames = ['January','February','March','April','May','June','July','August','September','October','November','December'];
        title.textContent = `${monthNames[currentMonth]} ${currentYear}`;

        grid.innerHTML = '';

        const first = new Date(currentYear, currentMonth, 1);
        const last = new Date(currentYear, currentMonth + 1, 0);
        const daysInMonth = last.getDate();

        let startDay = first.getDay();
        startDay = startDay === 0 ? 6 : startDay - 1;

        const prevLast = new Date(currentYear, currentMonth, 0).getDate();
        for (let i = startDay - 1; i >= 0; i--) {
            grid.appendChild(createDayCell(prevLast - i, true));
        }

        const yearEvents = eventCache.get(currentYear) || [];
        for (let day = 1; day <= daysInMonth; day++) {
            const cell = createDayCell(day, false);
            const dayEvents = yearEvents.filter(e => {
                const [, m, d] = e.date.split('-').map(Number);
                return d === day && m === currentMonth + 1;
            });

            if (dayEvents.length > 0) {
                const container = document.createElement('div');
                container.className = 'mt-1 space-y-1 max-h-20 overflow-y-auto';

                // append badges (each badge will have its own click handler)
                dayEvents.forEach(ev => container.appendChild(createEventBadge(ev)));

                cell.appendChild(container);
            }

            grid.appendChild(cell);
        }

        const filled = grid.children.length;
        for (let i = 1; i <= 42 - filled; i++) {
            grid.appendChild(createDayCell(i, true));
        }
    }

    function createDayCell(day, otherMonth) {
        const div = document.createElement('div');
        div.className = `min-h-[110px] p-2 border-r border-b border-gray-200 ${
            otherMonth ? 'bg-gray-50 text-gray-400' : 'bg-white hover:bg-gray-50'
        }`;

        const num = document.createElement('div');
        num.className = `text-sm font-medium ${otherMonth ? 'text-gray-400' : 'text-gray-800'}`;
        num.textContent = day;
        div.appendChild(num);

        return div;
    }

    function createEventBadge(event) {
        const colors = {
            blue:   'bg-blue-100 text-blue-800 border-blue-300',
            orange: 'bg-orange-100 text-orange-800 border-orange-300',
            red:    'bg-red-100 text-red-800 border-red-300'
        };

        const badge = document.createElement('div');
        badge.className = `text-xs px-2 py-0.5 rounded border ${colors[event.color] || colors.blue} truncate cursor-pointer`;

        // Show start time + title
        const startTime = event.startTime ? event.startTime.substring(0, 5) : '';
        badge.textContent = startTime ? `${startTime} ${event.title}` : `${event.title}`;

        // Open dynamic modal for this single event
        badge.addEventListener('click', (e) => {
            e.stopPropagation();
            showEventModalForEvent(event);
        });

        return badge;
    }

    function getEventsForDay(day) {
        return events.filter(e => {
            if (!e.date) return false;
            const [y, m, d] = e.date.split('-').map(Number);
            return d === day && m === currentMonth + 1 && y === currentYear;
        });
    }

    // Create and show a dynamic modal for a single event
function showEventModalForEvent(ev) {
    // Remove any previous modal content
    const modal = document.getElementById('eventDetailModal');
    if (!modal) return;
    
    const contentArea = document.getElementById('modalEventContent');
    contentArea.innerHTML = ''; // clear previous

    // ── Header section ────────────────────────────────────────
    const header = document.createElement('div');
    header.className = 'p-6 border-b border-gray-200';

    const titleRow = document.createElement('div');
    titleRow.className = 'flex items-start justify-between mb-4';

    const title = document.createElement('h3');
    title.className = 'text-lg font-bold text-gray-900 flex-1';
    title.textContent = ev.title || 'Untitled Event';

    const icon = document.createElement('i');
    icon.className = 'fas fa-video text-blue-500 text-xl';

    titleRow.appendChild(title);
    titleRow.appendChild(icon);
    header.appendChild(titleRow);

    // Date & Time info
    const info = document.createElement('div');
    info.className = 'space-y-2 text-sm text-gray-600';

    // Date line
    const dateDiv = document.createElement('div');
    dateDiv.className = 'flex items-center space-x-2';
    dateDiv.innerHTML = `
        <i class="far fa-calendar w-4"></i>
        <span>${formatDate(ev.date)}</span>
    `;
    info.appendChild(dateDiv);

    // Time line
    const timeDiv = document.createElement('div');
    timeDiv.className = 'flex items-center space-x-2';
    const timeText = ev.startTime && ev.endTime 
        ? `${ev.startTime.substring(0,5)} - ${ev.endTime.substring(0,5)}`
        : 'Time not set';
    timeDiv.innerHTML = `
        <i class="far fa-clock w-4"></i>
        <span>${timeText}</span>
    `;
    info.appendChild(timeDiv);

    header.appendChild(info);

    contentArea.appendChild(header);

    // Show modal
    modal.classList.remove('hidden');

    // Close handlers
    document.getElementById('closeEventModal').onclick = () => {
        modal.classList.add('hidden');
    };

    // Close on outside click
    modal.onclick = (e) => {
        if (e.target === modal) {
            modal.classList.add('hidden');
        }
    };
}

// Simple helper — adapt format to your needs
function formatDate(dateStr) {
    if (!dateStr) return 'Date TBD';
    try {
        const [y, m, d] = dateStr.split('-').map(Number);
        const date = new Date(y, m-1, d);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    } catch (e) {
        return dateStr;
    }
}

    // Mini calendar functions (keep your original ones, just ensure sync)
    function initMiniCalendar() {
        updateMiniCalendar();
    }

    function updateMiniCalendar() {
        const monthDisplay = document.getElementById('miniCalendarMonth');
        if (monthDisplay) {
            const names = ['January','February','March','April','May','June','July','August','September','October','November','December'];
            monthDisplay.textContent = `${names[miniCalendarDate.getMonth()]} ${miniCalendarDate.getFullYear()}`;
        }
        renderMiniCalendar();
    }

    function renderMiniCalendar() {
        const container = document.getElementById('miniCalendarDays');
        if (!container) return;
        container.innerHTML = '';

        const y = miniCalendarDate.getFullYear();
        const m = miniCalendarDate.getMonth();

        const first = new Date(y, m, 1);
        const last = new Date(y, m + 1, 0);
        const days = last.getDate();

        let start = first.getDay();
        start = start === 0 ? 6 : start - 1;

        const prevDays = new Date(y, m, 0).getDate();
        for (let i = start - 1; i >= 0; i--) {
            container.appendChild(createMiniCell(prevDays - i, true));
        }

        const today = new Date();
        for (let d = 1; d <= days; d++) {
            const isToday = d === today.getDate() && m === today.getMonth() && y === today.getFullYear();
            container.appendChild(createMiniCell(d, false, isToday));
        }

        const total = container.children.length;
        for (let i = 1; i <= 42 - total; i++) {
            container.appendChild(createMiniCell(i, true));
        }
    }

    function createMiniCell(day, other, isToday = false) {
        const cell = document.createElement('div');
        cell.className = `py-1.5 text-center text-sm rounded hover:bg-gray-100 cursor-pointer ${
            other ? 'text-gray-400' : 'text-gray-700'
        } ${isToday ? 'bg-blue-600 text-white hover:bg-blue-700' : ''}`;

        cell.textContent = day;

        cell.addEventListener('click', () => {
            if (!other) {
                currentMonth = miniCalendarDate.getMonth();
                currentYear = miniCalendarDate.getFullYear();
                updateCalendar();
            }
        });

        return cell;
    }
});