import { Notify } from 'quasar'

  export function negativeNotify(message) {
    Notify.create({
      type: 'negative',
      color: 'negative',
      position: 'center',
      timeout: 3000,
      message: message
    })
  }