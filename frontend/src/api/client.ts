import axios from 'axios'

export const api = axios.create({
  // Production is served by Spring Boot, so API calls stay on the current host.
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
})
