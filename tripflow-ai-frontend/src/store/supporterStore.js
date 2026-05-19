import { defineStore } from 'pinia'

export const useSupporterStore = defineStore('supporter', {
  state: () => ({
    userAddress : ''
  }),
  getters: {
    getUserAddress() {
        return this.userAddress
    }
  },
  actions: {
    setUserAddress(address) {
      this.userAddress = address
    }
  }
})
