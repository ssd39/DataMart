import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import reportWebVitals from "./reportWebVitals";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import Home from "./pages/Home";
import store from "./store";
import { Provider } from "react-redux";
import { ToastContainer } from "react-toastify";
import { Web3ReactProvider } from "@web3-react/core";
import "react-toastify/dist/ReactToastify.css";
import { Web3Provider } from "@ethersproject/providers";
import { initializeConnector } from "@web3-react/core";
import { StargazerWeb3ReactConnector } from "@stardust-collective/web3-react-stargazer-connector";
import Dashboard from "./pages/Dashboard";

function getLibrary(provider) {
  const library = new Web3Provider(provider);
  return library;
}

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />,
  },
  {
    path: "/dashboard",
    element: <Dashboard />,
  },
]);

const [stargazer, hooks] = initializeConnector(
  (actions) => new StargazerWeb3ReactConnector({ actions })
);

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <ToastContainer />
    <Provider store={store}>
      <Web3ReactProvider
        getLibrary={getLibrary}
        connectors={[[stargazer, hooks]]}
      >
        <RouterProvider router={router} />
      </Web3ReactProvider>
    </Provider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
