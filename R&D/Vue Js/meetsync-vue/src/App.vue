<script setup>
import {ref} from 'vue'
const name = ref('right')
const year = ref('2025');
</script>

<template>
  <header>
   <span>{{emoji}}</span>
   </header>
   <main>
    <h1> Hello, {{name}} </h1>
    <p> I am about to learn <a href="https://vuejs.org/" target="_blank">Vue.js</a>!</p>
    </main>

    <footer>
    <p>&copy;{{year}} </p>
    </footer>
</template>

<style scoped>
header span{
  font-size:3rem;
}
main h1{
  display:block;
}
main a{
  color: rgba(13, 3, 56, 1)
}

footer{
  position: fixed;
  bottom: 0;
  left: 48%;
  padding-bottom:20px;
}

</style>
