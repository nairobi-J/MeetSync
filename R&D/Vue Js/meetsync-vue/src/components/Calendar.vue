
<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const viewMode = ref('monthly') // 'daily' | 'weekly' | 'monthly' | 'yearly'
const currentDate = ref(new Date())
const selectedDates = ref(new Set())
const isDragging = ref(false)
const dragStartDate = ref(null)

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
]

const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const currentMonth = computed(() => currentDate.value.getMonth())
const currentYear = computed(() => currentDate.value.getFullYear())

const calendarTitle = computed(() => {
  if (viewMode.value === 'daily') {
    return `${monthNames[currentMonth.value]} ${currentDate.value.getDate()}, ${currentYear.value}`
  } else if (viewMode.value === 'weekly') {
    return `Week of ${monthNames[currentMonth.value]} ${currentYear.value}`
  } else if (viewMode.value === 'monthly') {
    return `${monthNames[currentMonth.value]} ${currentYear.value}`
  } else {
    return `${currentYear.value}`
  }
})

const getDaysInMonth = (month, year) => {
  return new Date(year, month + 1, 0).getDate()
}

const getFirstDayOfMonth = (month, year) => {
  return new Date(year, month, 1).getDay()
}

const calendarDays = computed(() => {
  const daysInMonth = getDaysInMonth(currentMonth.value, currentYear.value)
  const firstDay = getFirstDayOfMonth(currentMonth.value, currentYear.value)
  const days = []

  const prevMonthDays = getDaysInMonth(
    currentMonth.value === 0 ? 11 : currentMonth.value - 1,
    currentMonth.value === 0 ? currentYear.value - 1 : currentYear.value
  )

  for (let i = firstDay - 1; i >= 0; i--) {
    const prevMonth = currentMonth.value === 0 ? 11 : currentMonth.value - 1
    const prevYear = currentMonth.value === 0 ? currentYear.value - 1 : currentYear.value

    days.push({
      date: prevMonthDays - i,
      dateString: `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-${String(prevMonthDays - i).padStart(2, '0')}`,
      isCurrentMonth: false
    })
  }

  for (let i = 1; i <= daysInMonth; i++) {
    days.push({
      date: i,
      dateString: `${currentYear.value}-${String(currentMonth.value + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`,
      isCurrentMonth: true
    })
  }

  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i++) {
    const nextMonth = currentMonth.value === 11 ? 0 : currentMonth.value + 1
    const nextYear = currentMonth.value === 11 ? currentYear.value + 1 : currentYear.value

    days.push({
      date: i,
      dateString: `${nextYear}-${String(nextMonth + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`,
      isCurrentMonth: false
    })
  }

  return days
})

const weekDays = computed(() => {
  const days = []
  const startOfWeek = new Date(currentDate.value)
  startOfWeek.setDate(currentDate.value.getDate() - currentDate.value.getDay())

  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)

    days.push({
      date,
      dateString: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    })
  }

  return days
})

const timeSlots = computed(() => {
  const slots = []
  for (let hour = 0; hour < 24; hour++) {
    for (let min = 0; min < 60; min += 30) {
      slots.push(`${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`)
    }
  }
  return slots
})

const yearMonths = computed(() => {
  return monthNames.map((name, index) => ({
    name,
    index,
    dateString: `${currentYear.value}-${String(index + 1).padStart(2, '0')}`
  }))
})

const toggleDateSelection = (dateString) => {
  if (selectedDates.value.has(dateString)) {
    selectedDates.value.delete(dateString)
  } else {
    selectedDates.value.add(dateString)
  }
}

const handleMouseDown = (dateString) => {
  isDragging.value = true
  dragStartDate.value = dateString
  toggleDateSelection(dateString)
}

const handleMouseEnter = (dateString) => {
  if (isDragging.value) {
    toggleDateSelection(dateString)
  }
}

const handleMouseUp = () => {
  if (!isDragging.value) return
  isDragging.value = false
  dragStartDate.value = null
  console.log('Selected Dates:', [...selectedDates.value].sort())
}

const isDateSelected = (dateString) => {
  return selectedDates.value.has(dateString)
}

const previousPeriod = () => {
  if (viewMode.value === 'daily') {
    currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() - 1))
  } else if (viewMode.value === 'weekly') {
    currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() - 7))
  } else if (viewMode.value === 'monthly') {
    currentDate.value = new Date(currentDate.value.setMonth(currentDate.value.getMonth() - 1))
  } else {
    currentDate.value = new Date(currentDate.value.setFullYear(currentDate.value.getFullYear() - 1))
  }
}

const nextPeriod = () => {
  if (viewMode.value === 'daily') {
    currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() + 1))
  } else if (viewMode.value === 'weekly') {
    currentDate.value = new Date(currentDate.value.setDate(currentDate.value.getDate() + 7))
  } else if (viewMode.value === 'monthly') {
    currentDate.value = new Date(currentDate.value.setMonth(currentDate.value.getMonth() + 1))
  } else {
    currentDate.value = new Date(currentDate.value.setFullYear(currentDate.value.getFullYear() + 1))
  }
}

const goToToday = () => {
  currentDate.value = new Date()
}

const clearSelection = () => {
  selectedDates.value.clear()
}

onMounted(() => {
  document.addEventListener('mouseup', handleMouseUp)
})

onUnmounted(() => {
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<template>
  <div class="calendar-container">
    <div class="calendar-controls">
      <div class="view-switcher">
        <button
          :class="{ active: viewMode === 'daily' }"
          @click="viewMode = 'daily'"
        >
          Daily
        </button>
        <button
          :class="{ active: viewMode === 'weekly' }"
          @click="viewMode = 'weekly'"
        >
          Weekly
        </button>
        <button
          :class="{ active: viewMode === 'monthly' }"
          @click="viewMode = 'monthly'"
        >
          Monthly
        </button>
        <button
          :class="{ active: viewMode === 'yearly' }"
          @click="viewMode = 'yearly'"
        >
          Yearly
        </button>
      </div>

      <div class="navigation">
        <button @click="previousPeriod" class="nav-btn">←</button>
        <h2>{{ calendarTitle }}</h2>
        <button @click="nextPeriod" class="nav-btn">→</button>
      </div>

      <div class="action-buttons">
        <button @click="goToToday" class="today-btn">Today</button>
        <button @click="clearSelection" class="clear-btn">Clear Selection</button>
      </div>
    </div>

    <div v-if="viewMode === 'monthly'" class="calendar-grid monthly">
      <div class="day-header" v-for="day in dayNames" :key="day">
        {{ day }}
      </div>
      <div
        v-for="(day, index) in calendarDays"
        :key="index"
        :class="[
          'calendar-day',
          {
            'other-month': !day.isCurrentMonth,
            'selected': isDateSelected(day.dateString)
          }
        ]"
        @mousedown="handleMouseDown(day.dateString)"
        @mouseenter="handleMouseEnter(day.dateString)"
      >
        {{ day.date }}
      </div>
    </div>

    <div v-else-if="viewMode === 'weekly'" class="calendar-grid weekly">
      <div class="week-header">
        <div class="time-column">Time</div>
        <div v-for="day in weekDays" :key="day.dateString" class="day-column">
          <div class="day-name">{{ dayNames[day.date.getDay()] }}</div>
          <div class="day-date">{{ day.date.getDate() }}</div>
        </div>
      </div>
      <div class="week-body">
        <div v-for="time in timeSlots" :key="time" class="time-row">
          <div class="time-label">{{ time }}</div>
          <div
            v-for="day in weekDays"
            :key="`${day.dateString}-${time}`"
            :class="[
              'time-slot',
              { 'selected': isDateSelected(`${day.dateString}T${time}`) }
            ]"
            @mousedown="handleMouseDown(`${day.dateString}T${time}`)"
            @mouseenter="handleMouseEnter(`${day.dateString}T${time}`)"
          />
        </div>
      </div>
    </div>

    <div v-else-if="viewMode === 'daily'" class="calendar-grid daily">
      <div class="daily-header">
        <h3>{{ currentDate.toLocaleDateString('en-US', { weekday: 'long' }) }}</h3>
      </div>
      <div class="daily-body">
        <div
          v-for="time in timeSlots"
          :key="time"
          :class="[
            'time-slot-daily',
            { 'selected': isDateSelected(`${currentDate.toISOString().split('T')[0]}T${time}`) }
          ]"
          @mousedown="handleMouseDown(`${currentDate.toISOString().split('T')[0]}T${time}`)"
          @mouseenter="handleMouseEnter(`${currentDate.toISOString().split('T')[0]}T${time}`)"
        >
          <span class="time-label">{{ time }}</span>
        </div>
      </div>
    </div>

    <div v-else-if="viewMode === 'yearly'" class="calendar-grid yearly">
      <div
        v-for="month in yearMonths"
        :key="month.index"
        :class="[
          'year-month',
          { 'selected': isDateSelected(month.dateString) }
        ]"
        @mousedown="handleMouseDown(month.dateString)"
        @mouseenter="handleMouseEnter(month.dateString)"
      >
        {{ month.name }}
      </div>
    </div>

    <div class="selection-info">
      <p><strong>Selected:</strong> {{ selectedDates.size }} date(s)</p>
      <small>Check console for detailed selection</small>
    </div>
  </div>
</template>

<style scoped>
.calendar-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.calendar-controls {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.view-switcher {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.view-switcher button {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.view-switcher button:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.view-switcher button.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.navigation {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
}

.navigation h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
  min-width: 250px;
  text-align: center;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #3498db;
  background: white;
  color: #3498db;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: #3498db;
  color: white;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.today-btn,
.clear-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #2ecc71;
  background: white;
  color: #2ecc71;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.clear-btn {
  border-color: #e74c3c;
  color: #e74c3c;
}

.today-btn:hover {
  background: #2ecc71;
  color: white;
}

.clear-btn:hover {
  background: #e74c3c;
  color: white;
}

.calendar-grid {
  user-select: none;
}

.calendar-grid.monthly {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.day-header {
  padding: 1rem;
  text-align: center;
  font-weight: 600;
  color: #7f8c8d;
  background: #f8f9fa;
  border-radius: 6px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.calendar-day:hover {
  border-color: #3498db;
  background: #ebf5fb;
}

.calendar-day.selected {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.calendar-day.other-month {
  color: #bdc3c7;
  background: #f8f9fa;
}

.calendar-grid.weekly {
  display: flex;
  flex-direction: column;
  max-height: 600px;
  overflow-y: auto;
}

.week-header {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 4px;
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #e0e0e0;
}

.time-column {
  font-weight: 600;
  color: #7f8c8d;
}

.day-column {
  text-align: center;
  padding: 0.5rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.day-name {
  font-weight: 600;
  color: #7f8c8d;
}

.day-date {
  font-size: 1.2rem;
  color: #2c3e50;
  margin-top: 0.25rem;
}

.week-body {
  display: flex;
  flex-direction: column;
}

.time-row {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 4px;
}

.time-label {
  font-size: 0.85rem;
  color: #7f8c8d;
  padding: 0.5rem;
  text-align: right;
}

.time-slot {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 40px;
}

.time-slot:hover {
  background: #ebf5fb;
  border-color: #3498db;
}

.time-slot.selected {
  background: #3498db;
  border-color: #3498db;
}

.calendar-grid.daily {
  max-height: 600px;
  overflow-y: auto;
}

.daily-header {
  text-align: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.daily-header h3 {
  margin: 0;
  color: #2c3e50;
}

.daily-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-slot-daily {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.time-slot-daily:hover {
  background: #ebf5fb;
  border-color: #3498db;
}

.time-slot-daily.selected {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.calendar-grid.yearly {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.year-month {
  padding: 2rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
  font-size: 1.1rem;
}

.year-month:hover {
  border-color: #3498db;
  background: #ebf5fb;
}

.year-month.selected {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.selection-info {
  margin-top: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.selection-info p {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
}

.selection-info small {
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .calendar-container {
    padding: 1rem;
  }

  .navigation h2 {
    font-size: 1.2rem;
    min-width: 180px;
  }

  .calendar-grid.yearly {
    grid-template-columns: repeat(2, 1fr);
  }

  .week-header,
  .time-row {
    grid-template-columns: 60px repeat(7, 1fr);
  }
}
</style>