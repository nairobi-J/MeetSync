<script setup>
import { ref, computed } from 'vue'
import EventCard from './EventCard.vue'
import { useCalendarLogic } from './CalendarLogic.js'


const showActionModal = ref(false)
const showDetailsModal = ref(false)
const selectedActionDate = ref(null) // Stores the date user clicked

const {
  viewMode, currentDate, selectedDates, calendarTitle, dayNames,
  calendarDays, weekDays, timeSlots, yearMonths,
  isDateSelected, getSlotTitle, handleMouseDown, handleMouseEnter,
  previousPeriod, nextPeriod, goToToday, jumpToDate,
  isCardOpen, currentCardTitle, editingEventId, saveEvent, deleteEvent,events
} = useCalendarLogic()

const formatDateToLocalYMD = (date) => {
  if (!date) return ''
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const result = `${y}-${m}-${d}`
  
   console.log('Generating Date:', result) 
  
  return result
}

//for monthly
const isDayHasEvent = (dateString) => {
      if(!dateString) return false

      return events.value.some(event =>
        event.slots.some(slot => slot.startsWith(dateString))
      )
  }

const isToday = (dateString) => {
  if (!dateString) return false
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return dateString === `${y}-${m}-${d}`
}



const handleMonthDateClick = (dateString) => {
  if (!dateString) return

  if (isDayHasEvent(dateString)) {
    selectedActionDate.value = dateString
    showActionModal.value = true
  } 
  else {
    jumpToDate(dateString)
  }
}

const handleActionAdd = () => {
  jumpToDate(selectedActionDate.value)
  showActionModal.value = false
}

const handleActionSee = () => {
  showActionModal.value = false
  showDetailsModal.value = true
}

//Get list of events for the Details 
const currentDayEvents = computed(() => {
  if (!selectedActionDate.value) return []

  const list = []
  
  events.value.forEach(event => {
    event.slots.forEach(slot => {
      if (slot.startsWith(selectedActionDate.value)) {
        const time = slot.split('T')[1]
        list.push({ time, title: event.title })
      }
    })
  })

  // Sort by time 
  return list.sort((a, b) => a.time.localeCompare(b.time))
})
</script>


<template>
  <div class="calendar-container">
    <div class="calendar-controls">
      <div class="view-switcher">
        <button :class="{ active: viewMode === 'daily' }" @click="viewMode = 'daily'">Daily</button>
        <button :class="{ active: viewMode === 'weekly' }" @click="viewMode = 'weekly'">Weekly</button>
        <button :class="{ active: viewMode === 'monthly' }" @click="viewMode = 'monthly'">Monthly</button>
        <button :class="{ active: viewMode === 'yearly' }" @click="viewMode = 'yearly'">Yearly</button>
      </div>
      
      <div class="navigation">
        <button @click="previousPeriod" class="nav-btn">←</button>
        <h2>{{ calendarTitle }}</h2>
        <button @click="nextPeriod" class="nav-btn">→</button>
      </div>

      <div class="action-buttons">
        <button @click="goToToday" class="today-btn">Today</button>
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
            'selected': day.isCurrentMonth && isDayHasEvent(day.dateString),
            'is-today': isToday(day.dateString)
          }
        ]"
        @click="day.date && handleMonthDateClick(day.dateString)"
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
          >
          <span class="slot-event-title">
            {{ getSlotTitle(`${day.dateString}T${time}`) }}
          </span>
          </div>
        </div>
      </div>
    </div>

  
    <div v-else-if="viewMode === 'daily'" class="calendar-grid daily">
      <div class="daily-header">
        <h3>{{ currentDate.toLocaleDateString('en-US', { weekday: 'long' }) }}</h3>
      </div>
      <div class="daily-body">
        <div v-for="time in timeSlots" :key="time"
          :class="[
            'time-slot-daily',
            { 'selected': isDateSelected(`${formatDateToLocalYMD(currentDate)}T${time}`) }
          ]"
          @mousedown="handleMouseDown(`${formatDateToLocalYMD(currentDate)}T${time}`)"
          @mouseenter="handleMouseEnter(`${formatDateToLocalYMD(currentDate)}T${time}`)"
        >
          <span class="time-label">{{ time }}</span>
          <span 
            v-if="getSlotTitle(`${formatDateToLocalYMD(currentDate)}T${time}`)"
            class="slot-event-title"
          >
             {{ getSlotTitle(`${formatDateToLocalYMD(currentDate)}T${time}`) }}
          </span>
        </div>
      </div>
    </div>

   <div v-else-if="viewMode === 'yearly'" class="calendar-grid yearly-dashboard">
      <div v-for="month in yearMonths" :key="month.index" class="mini-calendar">
        
        <div class="mini-month-title">{{ month.name }}</div>

        <div class="mini-week-header">
          <span v-for="dayName in dayNames" :key="dayName">{{ dayName.charAt(0) }}</span>
        </div>

        <div class="mini-days-grid">
          <div
            v-for="(day, dIndex) in month.days"
            :key="dIndex"
            :class="[
              'mini-day',
              { 
                'empty': !day.date,
                'selected': day.date && isDateSelected(day.dateString)
              }
            ]"
            @click="day.date && jumpToDate(day.dateString)"
          >
            {{ day.date }}
          </div>
        </div>

      </div>
    </div>

    <!-- for monthly viewmode card -->

    <div v-if="showActionModal" class="modal-overlay" @click.self="showActionModal = false">
      <div class="modal-content action-modal">
        <h3>Actions for {{ selectedActionDate }}</h3>
        <div class="modal-buttons">
          <button @click="handleActionAdd" class="btn-add">Add Event</button>
          <button @click="handleActionSee" class="btn-see">See Events</button>
        </div>
        <button class="btn-close-text" @click="showActionModal = false">Cancel</button>
      </div>
    </div>

    
    <div v-if="showDetailsModal" class="modal-overlay" @click.self="showDetailsModal = false">
      <div class="modal-content details-modal">
        <h3>Events on {{ selectedActionDate }}</h3>
        
        <div class="events-list">
          <div v-if="currentDayEvents.length === 0">No events found.</div>
          
          <div v-else v-for="(item, idx) in currentDayEvents" :key="idx" class="event-list-item">
            <span class="event-time">{{ item.time }}</span>
            <span class="event-name">{{ item.title }}</span>
          </div>
        </div>

        <button class="btn-close" @click="showDetailsModal = false">Close</button>
      </div>
    </div>

    <div class="selection-info">
      <p><strong>Selected:</strong> {{ selectedDates.size }} date(s)</p>
      
    </div>

 
    <EventCard 
      :is-open="isCardOpen"
      :initial-title="currentCardTitle"
      :is-edit-mode="!!editingEventId"
      @close="isCardOpen = false"
      @save="saveEvent"
      @delete="deleteEvent"
    />
  </div>
</template>

<style scoped src="./CalendarStyle.css"></style>