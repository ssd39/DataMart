import { configureStore } from '@reduxjs/toolkit'
import metagraphReducer from './metagraphSlice'

export default configureStore({
  reducer: {
    metagraph: metagraphReducer
  }
})