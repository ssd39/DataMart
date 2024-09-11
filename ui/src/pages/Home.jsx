import React, { useState } from "react";
import { Rings } from "react-loader-spinner";
import {
  StargazerConnectorError,
  useStargazerWallet,
} from "@stardust-collective/web3-react-stargazer-connector";
import { toast } from "react-toastify";
import Lottie from "lottie-react";
import globeAnimation from "../animation/globe.json";
import { useDispatch, useSelector } from "react-redux";
import { fetchUserInfo, setWalletAddress } from "../store/metagraphSlice";
import { useWeb3React } from "@web3-react/core";
import { useNavigate } from "react-router-dom";

export default function Home() {
  const [loading, setLoading] = useState(false);
  const { connector } = useWeb3React()
  const wallet = useStargazerWallet()
  const dispatch = useDispatch();
  const navigate = useNavigate()
  const connectWallet = async () => {
    setLoading(true);
    try {
      await connector.activate()
      const accounts = await connector.request({
        method: "dag_accounts",
        params: [],
      });
      try{
        await dispatch(setWalletAddress(accounts[0]))
      await dispatch(fetchUserInfo(accounts[0])).unwrap()
      }catch(e){}
      navigate('/dashboard')
    } catch (e) {
      if (e instanceof StargazerConnectorError) {
        if (e.message == "StargazerConnector: Providers are not available") {
          toast.error("Please install Stargazer wallet!");
        }
      }
      console.error(e);
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex flex-col items-center pt-16">
      <div className=" w-96">
        <Lottie animationData={globeAnimation} loop={true} />
      </div>
      <div className="mb-5 flex flex-col text-center">
        <span className="text-4xl font-bold">Welcome to DataMart 📊</span>
        <span className="text-2xl flex">
          A metagraph which solves{" "}
          <div className="mx-2 bg-white/[0.1] px-2">
            <span className="text-orange-600">data liquidity</span>
          </div>{" "}
          problem!
        </span>
      </div>
      {loading ? (
        <Rings
          visible={true}
          height="80"
          width="80"
          color="#ea580c"
          ariaLabel="rings-loading"
          wrapperStyle={{}}
          wrapperClass=""
        />
      ) : (
        <div className="flex flex-col">
          <button
            className="bg-white text-orange-600 font-bold px-4 py-2 text-lg rounded-full"
            onClick={() => connectWallet()}
          >
            Connect Wallet
          </button>
        </div>
      )}
    </div>
  );
}
