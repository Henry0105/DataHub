<template>
  <div class="database-icon" :style="{ width: size + 'px', height: size + 'px' }">
    <img 
      v-if="config?.icon" 
      :src="config.icon" 
      :alt="config.label"
      :style="{ 
        width: '100%', 
        height: '100%',
        objectFit: 'contain'
      }"
      @error="handleImageError"
    />
    <div 
      v-else 
      class="default-icon"
      :style="{ 
        backgroundColor: config?.color || '#666',
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: '4px',
        color: '#fff',
        fontSize: (size / 2) + 'px',
        fontWeight: 'bold'
      }"
    >
      {{ getInitials(type) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getDatabaseConfig } from '@/config/database'

interface Props {
  type: string
  size?: number
}

const props = withDefaults(defineProps<Props>(), {
  size: 24
})

const imageError = ref(false)

const config = computed(() => getDatabaseConfig(props.type))

const handleImageError = () => {
  imageError.value = true
}

const getInitials = (type: string) => {
  if (type.length <= 2) return type
  // 获取首字母
  const words = type.match(/[A-Z][a-z]*/g) || [type]
  if (words.length === 1) {
    return type.substring(0, 2).toUpperCase()
  }
  return words.map(w => w[0]).join('').substring(0, 2).toUpperCase()
}
</script>

<style scoped lang="scss">
.database-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  img {
    border-radius: 4px;
  }
  
  .default-icon {
    user-select: none;
  }
}
</style>

