import { ref, computed, onMounted, onUnmounted } from 'vue'
export function useCalendarLogic() {

  //  STATE 
  const viewMode = ref('monthly') 
  const currentDate = ref(new Date())
  const selectedDates = ref(new Set())
  const events = ref([])
  const isDragging = ref(false)
  const dragStartDate = ref(null)
  const isInteractingWithEvent = ref(false)

  // Card State
  const isCardOpen = ref(false)
  const editingEventId = ref(null)
  const currentCardTitle = ref('')

  
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

  const getDaysInMonth = (month, year) => new Date(year, month + 1, 0).getDate()
  const getFirstDayOfMonth = (month, year) => new Date(year, month, 1).getDay()

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
    return monthNames.map((name, monthIndex) => {
      const daysInMonth = new Date(currentYear.value, monthIndex + 1, 0).getDate()
      const firstDayIndex = new Date(currentYear.value, monthIndex, 1).getDay()
      const days = []
      
      for (let i = 0; i < firstDayIndex; i++) days.push({ date: null, dateString: null })
      
      for (let i = 1; i <= daysInMonth; i++) {
        const dateString = `${currentYear.value}-${String(monthIndex + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
        days.push({ date: i, dateString })
      }
      return { name, index: monthIndex, days }
    })
  })

  // --- METHODS ---
  const getEventBySlot = (slot) => events.value.find(event => event.slots.includes(slot))

  const isDateSelected = (dateString) => {
    if (selectedDates.value.has(dateString)) return true
    const savedEvent = getEventBySlot(dateString)
    return !!savedEvent
  }

  const getSlotTitle = (dateString) => {
    const event = getEventBySlot(dateString)
    return event ? event.title : ''
  }

  // Event Management
  const openCreateCard = () => {
    editingEventId.value = null
    currentCardTitle.value = ''
    isCardOpen.value = true
  }

  const openEditCard = (event) => {
    editingEventId.value = event.id
    currentCardTitle.value = event.title
    isCardOpen.value = true
  }

  const saveEvent = (title) => {
    if (editingEventId.value) {
      const event = events.value.find(e => e.id === editingEventId.value)
      if (event) event.title = title
    } else {
      events.value.push({ id: Date.now(), title, slots: Array.from(selectedDates.value) })
      selectedDates.value.clear()
    }
    isCardOpen.value = false
  }

  const deleteEvent = () => {
    if (editingEventId.value) events.value = events.value.filter(e => e.id !== editingEventId.value)
    isCardOpen.value = false
    selectedDates.value.clear()
  }

  // Navigation
  const toggleDateSelection = (dateString) => {
    if (selectedDates.value.has(dateString)) selectedDates.value.delete(dateString)
    else selectedDates.value.add(dateString)
  }

  const handleMouseDown = (dateString) => {
    const existingEvent = getEventBySlot(dateString)
    if (existingEvent) {
      if (viewMode.value === 'daily') {
        isInteractingWithEvent.value = true
        openEditCard(existingEvent)
      }
      return
    }

    isDragging.value = true
    isInteractingWithEvent.value = false
    dragStartDate.value = dateString
    
    toggleDateSelection(dateString)
  }

  const handleMouseEnter = (dateString) => {
    if (isDragging.value && !isInteractingWithEvent.value) toggleDateSelection(dateString)
  }

  const handleMouseUp = () => {
    if (isInteractingWithEvent.value) {
      isInteractingWithEvent.value = false
      return
    }
    if (isDragging.value) {
      isDragging.value = false
      dragStartDate.value = null

      console.log('Selected Slots:', [...selectedDates.value].sort())

      
      if (viewMode.value === 'daily' && selectedDates.value.size > 0) openCreateCard()
    }
  }

  const previousPeriod = () => {
    if (viewMode.value === 'daily') currentDate.value.setDate(currentDate.value.getDate() - 1)
    else if (viewMode.value === 'weekly') currentDate.value.setDate(currentDate.value.getDate() - 7)
    else if (viewMode.value === 'monthly') currentDate.value.setMonth(currentDate.value.getMonth() - 1)
    else currentDate.value.setFullYear(currentDate.value.getFullYear() - 1)
    currentDate.value = new Date(currentDate.value) // Trigger reactivity
  }

  const nextPeriod = () => {
    if (viewMode.value === 'daily') currentDate.value.setDate(currentDate.value.getDate() + 1)
    else if (viewMode.value === 'weekly') currentDate.value.setDate(currentDate.value.getDate() + 7)
    else if (viewMode.value === 'monthly') currentDate.value.setMonth(currentDate.value.getMonth() + 1)
    else currentDate.value.setFullYear(currentDate.value.getFullYear() + 1)
    currentDate.value = new Date(currentDate.value) // Trigger reactivity
  }

  const goToToday = () => currentDate.value = new Date()
  const clearSelection = () => selectedDates.value.clear()
  
  const jumpToDate = (dateString) => {
    const [y, m, d] = dateString.split('-').map(Number)
    currentDate.value = new Date(y, m - 1, d)
    viewMode.value = 'daily'
  }

  onMounted(() => document.addEventListener('mouseup', handleMouseUp))
  onUnmounted(() => document.removeEventListener('mouseup', handleMouseUp))

  
  return {
    viewMode, currentDate, selectedDates, events, calendarTitle, monthNames, dayNames,
    calendarDays, weekDays, timeSlots, yearMonths,
    isDateSelected, getSlotTitle, handleMouseDown, handleMouseEnter,
    previousPeriod, nextPeriod, goToToday, clearSelection, jumpToDate,
    isCardOpen, currentCardTitle, editingEventId, saveEvent, deleteEvent
  }

}