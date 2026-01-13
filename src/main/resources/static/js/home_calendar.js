document.addEventListener('DOMContentLoaded', function () {

    const calendarData = document.getElementById('calendarData');

    let currentMonth = localStorage.getItem('calendarMonth')
        ? parseInt(localStorage.getItem('calendarMonth'))
        : (parseInt(calendarData?.dataset.currentMonth) || new Date().getMonth());

    let currentYear = localStorage.getItem('calendarYear')
        ? parseInt(localStorage.getItem('calendarYear'))
        : (parseInt(calendarData?.dataset.currentYear) || new Date().getFullYear());

    let miniCalendarDate = new Date(currentYear, currentMonth);

    // Initialize
    initMainCalendar();
    initMiniCalendar();

    // Main nav buttons
    document.getElementById('prevMainMonth')?.addEventListener('click', prevMainMonth);
    document.getElementById('nextMainMonth')?.addEventListener('click', nextMainMonth);

    // Mini nav buttons (sync with main)
    document.getElementById('prevMonth')?.addEventListener('click', () => {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() - 1);
        currentMonth = miniCalendarDate.getMonth();
        currentYear = miniCalendarDate.getFullYear();
        updateCalendar();
    });

    document.getElementById('nextMonth')?.addEventListener('click', () => {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() + 1);
        currentMonth = miniCalendarDate.getMonth();
        currentYear = miniCalendarDate.getFullYear();
        updateCalendar();
    });

    document.getElementById('closeModal')?.addEventListener('click', () => {
        document.getElementById('eventModal').classList.add('hidden');
    });

    function prevMainMonth() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        updateCalendar();
    }

    function nextMainMonth() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        updateCalendar();
    }

    async function updateCalendar() {
        document.getElementById('calendarLoading').classList.remove('hidden');

        localStorage.setItem('calendarMonth', currentMonth);
        localStorage.setItem('calendarYear', currentYear);

        miniCalendarDate = new Date(currentYear, currentMonth);

        await fetchEventsForCurrentMonth();

        renderMainCalendar();
        updateMiniCalendar();

        document.getElementById('calendarLoading').classList.add('hidden');
    }

    async function fetchEventsForCurrentMonth() {
        try {
            const res = await fetch(`/api/events?month=${currentMonth + 1}&year=${currentYear}`);
            if (res.ok) {
                events = await res.json();
            } else {
                events = [];
                console.error('Failed to fetch events:', res.status);
            }
        } catch (err) {
            console.error("Failed to load events:", err);
            events = [];
        }
    }

    function initMainCalendar() {
        renderMainCalendar();
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

        for (let day = 1; day <= daysInMonth; day++) {
            const cell = createDayCell(day, false);
            const dayEvents = getEventsForDay(day);

            if (dayEvents.length > 0) {
                const container = document.createElement('div');
                container.className = 'mt-1 space-y-1 max-h-20 overflow-y-auto';

                dayEvents.forEach(ev => {
                    const badge = createEventBadge(ev);
                    container.appendChild(badge);
                });

                cell.appendChild(container);

                cell.addEventListener('click', () => showEventModal(dayEvents, day));
                cell.classList.add('cursor-pointer');
            }

            grid.appendChild(cell);
        }

        const filled = grid.children.length;
        const remaining = 42 - filled;
        for (let i = 1; i <= remaining; i++) {
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
        badge.className = `text-xs px-2 py-0.5 rounded border ${colors[event.color] || colors.blue} truncate`;
        badge.textContent = event.title;
        return badge;
    }

    function getEventsForDay(day) {
        return events.filter(e => {
            if (!e.date) return false;
            const [y, m, d] = e.date.split('-').map(Number);
            return d === day && m === currentMonth + 1 && y === currentYear;
        });
    }

    function showEventModal(dayEvents, day) {
        if (dayEvents.length === 0) return;

        // For now show first event – you can extend to list all
        const ev = dayEvents[0];

        document.getElementById('modalTitle').textContent = ev.title;
        document.getElementById('modalDate').textContent = `${ev.date}`;
        document.getElementById('modalDesc').textContent = ev.description || 'No additional details';

        document.getElementById('eventModal').classList.remove('hidden');
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