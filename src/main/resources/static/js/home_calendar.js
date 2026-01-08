document.addEventListener('DOMContentLoaded', function() {
    
    // Current date state
    let currentDate = new Date();
    let currentMonth = currentDate.getMonth();
    let currentYear = currentDate.getFullYear();
    
    // Mini calendar state (sidebar)
    let miniCalendarDate = new Date(2026, 1); // January 2026
    
    // Initialize calendars
    initMainCalendar();
    initMiniCalendar();
    
    // Event listeners for mini calendar navigation
    document.getElementById('prevMonth').addEventListener('click', function() {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() - 1);
        updateMiniCalendar();
    });
    
    document.getElementById('nextMonth').addEventListener('click', function() {
        miniCalendarDate.setMonth(miniCalendarDate.getMonth() + 1);
        updateMiniCalendar();
    });
    
    // Initialize main calendar
    function initMainCalendar() {
        currentMonth = 0; // January (0-indexed)
        currentYear = 2026;
        renderMainCalendar();
    }
    
    // Render main calendar
    function renderMainCalendar() {
        const calendarGrid = document.getElementById('calendarGrid');
        const monthYearDisplay = document.getElementById('currentMonthYear');
        
        // Update month/year display
        const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                          'July', 'August', 'September', 'October', 'November', 'December'];
        monthYearDisplay.textContent = `${monthNames[currentMonth]} ${currentYear}`;
        
        // Clear existing calendar
        calendarGrid.innerHTML = '';
        
        // Get first day of month and number of days
        const firstDay = new Date(currentYear, currentMonth, 1);
        const lastDay = new Date(currentYear, currentMonth + 1, 0);
        const daysInMonth = lastDay.getDate();
        
        // Get day of week (0 = Sunday, 1 = Monday, etc.)
        let startDay = firstDay.getDay();
        // Convert to Monday = 0, Sunday = 6
        startDay = startDay === 0 ? 6 : startDay - 1;
        
        // Previous month days
        const prevMonthLastDay = new Date(currentYear, currentMonth, 0).getDate();
        for (let i = startDay - 1; i >= 0; i--) {
            const dayCell = createDayCell(prevMonthLastDay - i, true);
            calendarGrid.appendChild(dayCell);
        }
        
        // Current month days
        for (let day = 1; day <= daysInMonth; day++) {
            const dayCell = createDayCell(day, false);
            const events = getEventsForDay(day);
            
            if (events.length > 0) {
                const eventsContainer = document.createElement('div');
                eventsContainer.className = 'mt-2 space-y-1';
                
                events.forEach(event => {
                    const eventBadge = createEventBadge(event);
                    eventsContainer.appendChild(eventBadge);
                });
                
                dayCell.appendChild(eventsContainer);
            }
            
            calendarGrid.appendChild(dayCell);
        }
        
        // Next month days to fill grid
        const totalCells = calendarGrid.children.length;
        const remainingCells = 42 - totalCells; // 6 rows × 7 days
        for (let day = 1; day <= remainingCells; day++) {
            const dayCell = createDayCell(day, true);
            calendarGrid.appendChild(dayCell);
        }
    }
    
    // Create day cell
    function createDayCell(day, isOtherMonth) {
        const dayCell = document.createElement('div');
        dayCell.className = `min-h-[120px] p-3 border-r border-b border-gray-200 ${
            isOtherMonth ? 'bg-gray-50' : 'bg-white hover:bg-gray-50'
        } cursor-pointer transition`;
        
        const dayNumber = document.createElement('div');
        dayNumber.className = `text-sm font-semibold ${
            isOtherMonth ? 'text-gray-400' : 'text-gray-700'
        }`;
        dayNumber.textContent = day;
        
        dayCell.appendChild(dayNumber);
        return dayCell;
    }
    
    // Create event badge
    function createEventBadge(event) {
        const badge = document.createElement('div');
        const colorClasses = {
            blue: 'bg-blue-100 text-blue-700',
            orange: 'bg-orange-100 text-orange-700',
            red: 'bg-red-100 text-red-700'
        };
        
        badge.className = `text-xs px-2 py-1 rounded ${colorClasses[event.color] || colorClasses.blue}`;
        badge.textContent = event.title;
        
        return badge;
    }
    
    // Get events for a specific day
    function getEventsForDay(day) {
        // Use demo events from the global variable
        return demoEvents.filter(event => event.day === day);

    }
    
    // Initialize mini calendar (sidebar)
    function initMiniCalendar() {
        updateMiniCalendar();
    }
    
    // Update mini calendar display
    function updateMiniCalendar() {
        const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                          'July', 'August', 'September', 'October', 'November', 'December'];
        const monthDisplay = document.getElementById('miniCalendarMonth');
        monthDisplay.textContent = `${monthNames[miniCalendarDate.getMonth()]} ${miniCalendarDate.getFullYear()}`;
        
        renderMiniCalendar();
    }
    
    // Render mini calendar
    function renderMiniCalendar() {
        const miniCalendarDays = document.getElementById('miniCalendarDays');
        miniCalendarDays.innerHTML = '';
        
        const year = miniCalendarDate.getFullYear();
        const month = miniCalendarDate.getMonth();
        
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        
        let startDay = firstDay.getDay();
        startDay = startDay === 0 ? 6 : startDay - 1;
        
        // Previous month days
        const prevMonthLastDay = new Date(year, month, 0).getDate();
        for (let i = startDay - 1; i >= 0; i--) {
            const dayCell = createMiniDayCell(prevMonthLastDay - i, true);
            miniCalendarDays.appendChild(dayCell);
        }
        
        // Current month days
        const today = new Date();
        for (let day = 1; day <= daysInMonth; day++) {
            const isToday = day === today.getDate() && 
                          month === today.getMonth() && 
                          year === today.getFullYear();
            const dayCell = createMiniDayCell(day, false, isToday);
            miniCalendarDays.appendChild(dayCell);
        }
        
        // Next month days
        const totalCells = miniCalendarDays.children.length;
        const remainingCells = 42 - totalCells;
        for (let day = 1; day <= remainingCells; day++) {
            const dayCell = createMiniDayCell(day, true);
            miniCalendarDays.appendChild(dayCell);
        }
    }
    
    // Create mini calendar day cell
    function createMiniDayCell(day, isOtherMonth, isToday = false) {
        const dayCell = document.createElement('div');
        dayCell.className = `py-2 text-center cursor-pointer rounded hover:bg-gray-100 ${
            isOtherMonth ? 'text-gray-400' : 'text-gray-700'
        } ${isToday ? 'bg-blue-600 text-white hover:bg-blue-700' : ''}`;
        dayCell.textContent = day;
        
        // Click handler to navigate main calendar
        dayCell.addEventListener('click', function() {
            if (!isOtherMonth) {
                currentMonth = miniCalendarDate.getMonth();
                currentYear = miniCalendarDate.getFullYear();
                renderMainCalendar();
            }
        });
        
        return dayCell;
    }
});