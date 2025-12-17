<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  title: String,
  isEdit: Boolean
})

const emit = defineEmits(['save', 'delete', 'close'])

const localTitle = ref('')

watch(
  () => props.title,
  (val) => {
    localTitle.value = val || ''
  },
  { immediate: true }
)

const save = () => {
  emit('save', localTitle.value)
}

const remove = () => {
  emit('delete')
}
</script>

<template>
  <div class="event-card">
    <h4>{{ isEdit ? 'Edit event' : 'Create event' }}</h4>

    <input
      v-model="localTitle"
      placeholder="Event title"
      class="event-input"
    />

    <div class="actions">
      <button class="save" @click="save">Save</button>
      <button
        v-if="isEdit"
        class="delete"
        @click="remove"
      >
        Delete
      </button>
    </div>
  </div>
</template>


<style scoped>
.event-card {
  position: fixed;
  right: 20px;
  top: 100px;
  width: 260px;
  background: white;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}

input {
  width: 100%;
  padding: 0.5rem;
  margin-bottom: 0.8rem;
}

.actions {
  display: flex;
  justify-content: space-between;
}

.delete {
  background: #e74c3c;
  color: white;
  border: none;
}
</style>
