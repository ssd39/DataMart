import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'


const ML0 = process.env.REACT_APP_ML0


export const fetchUserInfo = createAsyncThunk(
  'fetchUserInfo',
  async (userAddress, thunkAPI) => {
    const response = await fetch(`${ML0}/data-application/user-record/${userAddress}`)
    if (response.status == 404) {
      return {
        "orgName": ""
      }
    }
    return await response.json()
  },
)

export const fetchDataRequests = createAsyncThunk(
  'fetchDataRequests',
  async (userAddress, thunkAPI) => {
    const response = await fetch(`${ML0}/data-application/data-requests`)
    let dataRequests = []
    if (response.status == 200) {
      dataRequests = await response.json()
    }
    let myProposals = []
    try{
      const response1 = await fetch(`${ML0}/data-application/user-proposals/${userAddress}`)
      if (response1.status == 200) {
        myProposals = await response1.json()
      }
    }catch(e) {
      console.error(e)
    }

    return { myProposals, dataRequests}
  },
)


export const metagraphSlice = createSlice({
  name: 'metagraph',
  initialState: {
    userInfo: {},
    dataRequests: [],
    myProposals: [],
    dataRequetsLoader: false,
    walletAddress: ""
    
  },
  reducers: {
    setDataRquestLoader: (state, action) => {
      state.dataRequetsLoader = action.payload
    },
    setWalletAddress: (state, action) => {
      state.walletAddress = action.payload
    },
  },
  extraReducers: (builder) => {
    builder.addCase(fetchUserInfo.fulfilled, (state, action) => {
      state.userInfo = action.payload
    })
    builder.addCase(fetchDataRequests.fulfilled, (state, action) => {
      state.dataRequests = action.payload.dataRequests
      state.myProposals = action.payload.myProposals
      state.dataRequetsLoader = false
    })
    builder.addCase(fetchDataRequests.rejected, (state, action) => {
      state.dataRequetsLoader = false
    })
  },
})

export const { setDataRquestLoader, setWalletAddress } = metagraphSlice.actions
export default metagraphSlice.reducer