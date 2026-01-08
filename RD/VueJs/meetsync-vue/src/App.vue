<script setup >
import HelloWorld from './components/HelloWorld.vue'
import Calendar from './components/Calendar.vue';
import EventCard from './components/EventCard.vue';
</script>

<template>
  <div class="app-container">
    <header>
      <h1>Scheduling Calendar</h1>
    </header>
    
   <Calendar/>

  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  padding: 2rem;
}

header {
  text-align: center;
  margin-bottom: 2rem;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

p {
  color: #7f8c8d;
  font-size: 1rem;
}
</style>