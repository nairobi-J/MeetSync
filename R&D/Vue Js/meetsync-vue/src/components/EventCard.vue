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
    </div>
  </div>
</template>

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