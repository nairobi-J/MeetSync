<<<<<<< Updated upstream:R&D/Vue Js/meetsync-vue/src/components/EventCard.vue
<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  isOpen: Boolean,
  initialTitle: String,
  isEditMode: Boolean
})

const emit = defineEmits(['close', 'save', 'delete'])

const title = ref('')

// Update local title when props change
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    title.value = props.initialTitle || ''
  }
})

const handleSave = () => {
  if (title.value.trim()) {
    emit('save', title.value)
  } else {
    alert('Please enter a title')
  }
}
</script>

<template>
  <div v-if="isOpen" class="card-overlay">
    <div class="card-container">
      <div class="card-header">
        <h3>{{ isEditMode ? 'Edit Event' : 'New Event' }}</h3>
        <button class="close-icon" @click="$emit('close')">×</button>
      </div>

      <div class="card-body">
        <label>Event Title</label>
        <input 
          v-model="title" 
          type="text" 
          placeholder="Enter meeting title..." 
          @keyup.enter="handleSave"
          autofocus
        />
      </div>

      <div class="card-footer">
        <button v-if="isEditMode" class="btn-delete" @click="$emit('delete')">
          🗑️ Delete
        </button>
        <div class="right-actions">
          <button class="btn-cancel" @click="$emit('close')">Cancel</button>
          <button class="btn-save" @click="handleSave">Save</button>
        </div>
      </div>
=======
<template>
  <div class="event-card">
    <h4>Create Event</h4>

    <input
      v-model="title"
      placeholder="Event title"
    />

    <div class="actions">
      <button @click="save">Save</button>
      <button class="delete" @click="$emit('delete')">Delete</button>
>>>>>>> Stashed changes:RD/VueJs/meetsync-vue/src/components/EventCard.vue
    </div>
  </div>
</template>

<<<<<<< Updated upstream:R&D/Vue Js/meetsync-vue/src/components/EventCard.vue
<style scoped>
.card-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.card-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 350px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.close-icon {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

button {
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  border: none;
}

.btn-save { background: #3b82f6; color: white; }
.btn-cancel { background: #e5e7eb; color: #374151; }
.btn-delete { background: #ef4444; color: white; }
</style>
=======
<script setup>
import { ref } from 'vue'

const emit = defineEmits(['save', 'delete'])
const title = ref('')

const save = () => {
  if (!title.value.trim()) return
  emit('save', { title: title.value })
  title.value = ''
}
</script>


<!-- <style scoped>
.event-card {
  position:absolute;
  right: 20px;
  top: 100px;
  width: 250px;
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}
.event-card h4 { margin: 0 0 0.5rem; }
.time { font-size: 0.85rem; color: #666; margin-bottom: 0.5rem; }
input { width: 100%; padding: 0.4rem; margin-bottom: 0.8rem; }
.actions { display: flex; justify-content: space-between; }
button { padding: 0.4rem 0.6rem; cursor: pointer; }
.delete { background: #e74c3c; color: white; border: none; }
</style> -->
>>>>>>> Stashed changes:RD/VueJs/meetsync-vue/src/components/EventCard.vue
