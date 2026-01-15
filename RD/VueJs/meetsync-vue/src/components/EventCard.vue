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
    </div>
  </div>
</template>

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
