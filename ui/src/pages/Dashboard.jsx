import { useStargazerWallet } from "@stardust-collective/web3-react-stargazer-connector";
import { useWeb3React } from "@web3-react/core";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { sendTx } from "../helpers/transaction";
import { toast } from "react-toastify";
import { Rings } from "react-loader-spinner";
import { Accordion, AccordionDetails, AccordionSummary, Dialog, DialogTitle } from "@mui/material";
import {
  fetchDataRequests,
  fetchUserInfo,
  setDataRquestLoader,
} from "../store/metagraphSlice";
import Card from "../components/Card";
import { generateKeyPair, retrievePublicKey } from "../helpers/crypto";
import RefreshIcon from "@mui/icons-material/Refresh";
import Radio from "@mui/material/Radio";
import RadioGroup from "@mui/material/RadioGroup";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

export default function Dashboard() {
  const userInfo = useSelector((state) => state.metagraph.userInfo);
  const walletAddress = useSelector((state) => state.metagraph.walletAddress);

  const dataRequests = useSelector((state) => state.metagraph.dataRequests);
  const dataRequetsLoader = useSelector(
    (state) => state.metagraph.dataRequetsLoader
  );
  const wallet = useStargazerWallet();
  const navigate = useNavigate();
  const [showOnboarding, setOnBoarding] = useState(false);
  const [isLoading, setLoading] = useState(false);

  const [orgname, setOrgname] = useState();
  const [description, setDescription] = useState("");
  const [link, setLink] = useState("");
  const [jschema, setJSchema] = useState("");
  const [dataDesc, setDataDesc] = useState("");
  const [tab, setTab] = useState(0);
  const [createDialog, setCreateDialog] = useState(false);
  const [demoTest, setDemoTest] = useState("a");
  const [isSecret, setSecret] = useState(true);

  const [consetDialog, setConsetDialog] = useState(false);
  const [schemaConset, setSchemaConset] = useState("");
  const [orgNameConset, setOrgNameConset] = useState("");
  const [loadingConset, setLodingConset] = useState(false);
  const [proposalIdConset, setProposalIdConset] = useState("");
  const [buyerConset, setBuyerConset] = useState("");
  const [consetUrl, setConsetUrl] = useState("");

  const dispatch = useDispatch();
  useEffect(() => {
    if (Object.keys(userInfo).length == 0) {
      navigate("/");
    } else {
      console.log(userInfo);
      wallet.activate().then(() => {
        //setOnBoarding(true);
        if (!userInfo?.orgName) {
          setOnBoarding(true);
          let y = setInterval(() => {
            dispatch(fetchUserInfo(walletAddress));
            if (userInfo.orgName) {
              clearInterval(y);
            }
          }, 1000);
        }
        dispatch(setDataRquestLoader(true));
        dispatch(fetchDataRequests(walletAddress));
        let x = setInterval(() => {
          dispatch(fetchDataRequests(walletAddress));
        }, 1500);
        return () => clearInterval(x);
      });
    }
  }, []);

  return (
    <div className="flex flex-col min-h-screen">
      {showOnboarding ? (
        <div className="flex flex-col w-full items-center mt-5">
          <div className="w-1/2 flex flex-col  border border-white rounded-lg p-4">
            <span className="text-2xl text-orange-600 text-center">
              Please fill organisation details!
            </span>
            <div className="flex flex-col w-full mt-4">
              <div className="flex flex-col w-full text-black ">
                <span className="text-white font-bold text-lg">Name</span>
                <input
                  value={orgname}
                  onChange={(e) => setOrgname(e.target.value)}
                  className="w-full p-1 px-2 rounded-lg mt-1"
                  type="text"
                />
              </div>
              <div className="flex flex-col w-full text-black mt-2">
                <span className="text-white font-bold text-lg">
                  Description
                </span>
                <input
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full p-1 px-2 rounded-lg mt-1"
                  type="text"
                />
              </div>
              <div className="flex flex-col w-full text-black mt-2">
                <span className="text-white font-bold text-lg">Link</span>
                <input
                  value={link}
                  onChange={(e) => setLink(e.target.value)}
                  className="w-full p-1 px-2 rounded-lg mt-1"
                  type="text"
                />
              </div>
            </div>
            <div className="flex justify-center">
              {isLoading ? (
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
                <button
                  className="bg-white text-orange-600 font-bold px-4 py-2 text-lg rounded-full mt-4"
                  onClick={async () => {
                    if (orgname == "" || description == "" || link == "") {
                      toast.error("Please fill all the details!");
                      return;
                    }
                    setLoading(true);
                    let pubKey = null;
                    try {
                      pubKey = retrievePublicKey();
                    } catch (e) {}
                    if (!pubKey) {
                      const keypair = generateKeyPair();
                      pubKey = keypair.publicKey;
                    }
                    await sendTx(
                      {
                        RegisterUser: {
                          orgName: orgname,
                          orgDescription: description,
                          link,
                          logo: "",
                          pubkey: pubKey,
                        },
                      },
                      wallet
                    );
                    dispatch(setDataRquestLoader(true));
                    dispatch(fetchDataRequests(walletAddress));
                    setOnBoarding(false);
                    setLoading(false);
                  }}
                >
                  Update
                </button>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="flex flex-col px-4">
          <>
            <div className="w-full flex items-center justify-between  py-6  border-white">
              <span className="text-4xl font-bold">DataMart 📊</span>
              <div className="text-xl font-bold flex flex-col items-end">
                <span className="mr-2">
                  <span className="text-orange-600">Hi,</span>{" "}
                  {userInfo?.orgName}
                </span>
                <span className="text-sm bg-white/[0.35] px-2 rounded-lg">
                  {wallet.account}
                </span>
              </div>
            </div>
            <div className="flex ">
              <div className="h-[1px] bg-white w-full rounded-lg"></div>
            </div>
          </>
          <div className="flex mt-4 items-center">
            <div className="flex items-center">
              <div className="flex p-2 px-4 rounded-full bg-white  text-black">
                <button
                  className={`${
                    tab == 0
                      ? "text-orange-600 font-bold bg-slate-200 p-2 rounded-full shadow-inner"
                      : ""
                  }`}
                  onClick={() => setTab(0)}
                >
                  Data Requests
                </button>
                <button
                  className={`${
                    tab == 1
                      ? "text-orange-600 font-bold bg-slate-200 p-2 rounded-full shadow-inner"
                      : ""
                  } mx-6 active:scale-90`}
                  onClick={() => setTab(1)}
                >
                  Bought
                </button>
                <button
                  className={`${
                    tab == 2
                      ? "text-orange-600 font-bold bg-slate-200 p-2 rounded-full shadow-inner"
                      : ""
                  } active:scale-90 mr-6`}
                  onClick={() => setTab(2)}
                >
                  Provided
                </button>
                <button
                  className={`${
                    tab == 3
                      ? "text-orange-600 font-bold bg-slate-200 p-2 rounded-full shadow-inner"
                      : ""
                  } active:scale-90`}
                  onClick={() => setTab(3)}
                >
                  Demo
                </button>
              </div>
              <div>
                <div
                  onClick={() => {
                    dispatch(setDataRquestLoader(true));
                    dispatch(fetchDataRequests(walletAddress));
                  }}
                  className="bg-white rounded-full text-black  ml-2 cursor-pointer active:scale-95"
                >
                  <RefreshIcon
                    sx={{
                      width: 32,
                      height: 32,
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
          {tab == 0 && (
            <div className="flex flex-col mt-6">
              <Dialog
                onClose={() => setCreateDialog(false)}
                open={createDialog}
                sx={{
                  ".MuiPaper-root": {
                    background: "#1f2937",
                    width: "50%",
                  },
                }}
              >
                <DialogTitle
                  sx={{
                    fontWeight: "bold",
                  }}
                  className="text-orange-600 gray font-bold"
                >
                  New Data Request
                </DialogTitle>
                <div className=" flex flex-col p-4">
                  <div className="flex flex-col w-full">
                    <div className="flex flex-col w-full text-black ">
                      <span className="text-white font-bold text-lg">
                        Data Description
                      </span>
                      <textarea
                        value={dataDesc}
                        rows={4}
                        onChange={(e) => setDataDesc(e.target.value)}
                        className="w-full p-1 px-2 rounded-lg mt-1"
                        type="text"
                      />
                    </div>
                    <div className="flex flex-col w-full text-black mt-2">
                      <span className="text-white font-bold text-lg">
                        JSON Schema
                      </span>
                      <textarea
                        value={jschema}
                        rows={4}
                        onChange={(e) => setJSchema(e.target.value)}
                        className="w-full p-1 px-2 rounded-lg mt-1"
                        type="text"
                      />
                    </div>
                  </div>
                  <div className="flex justify-center">
                    {isLoading ? (
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
                      <button
                        className="bg-white text-orange-600 font-bold px-4 py-2 text-lg rounded-full mt-4"
                        onClick={async () => {
                          if (jschema == "" || dataDesc == "") {
                            toast.error("Please fill all the details!");
                            return;
                          }
                          setLoading(true);
                          await sendTx(
                            {
                              CreateDataRequest: {
                                schema: jschema,
                                description: dataDesc,
                                callBackWebhook: "",
                              },
                            },
                            wallet
                          );
                          setDataDesc("");
                          setJSchema("");
                          setCreateDialog(false);
                          setLoading(false);
                          dispatch(setDataRquestLoader(true));
                          dispatch(fetchDataRequests(walletAddress));
                        }}
                      >
                        Create
                      </button>
                    )}
                  </div>
                </div>
              </Dialog>
              <div>
                <button
                  className="bg-white text-orange-600 font-bold px-4 py-2 text-md rounded-full active:scale-90"
                  onClick={() => setCreateDialog(true)}
                >
                  ➕ Create Data Request
                </button>
              </div>
              <div className="flex justify-center">
                <Rings
                  visible={dataRequetsLoader}
                  height="80"
                  width="80"
                  color="#ea580c"
                  ariaLabel="rings-loading"
                  wrapperStyle={{}}
                  wrapperClass=""
                />
              </div>
              {!dataRequetsLoader && (
                <div className="flex mt-4 flex-wrap">
                  {dataRequests.map((v, i) => {
                    return <Card key={v.buyer} data={v} wallet={wallet} />;
                  })}
                </div>
              )}
            </div>
          )}
          {tab == 3 && (
            <div className="w-full flex items-center justify-center mt-8">
              <Dialog
                open={consetDialog}
                onClose={() => setConsetDialog(false)}
              >
                <DialogTitle
                  sx={{
                    fontWeight: "800",
                  }}
                  className="font-bold text-xl"
                >
                  Data Sharing Authorisation
                </DialogTitle>
                <div className="px-4 pb-4">
                  <span className="mb-2 text-lg">
                    You will be sharing following data with{" "}
                    <span className="text-orange-600 font-bold text-xl">
                      {orgNameConset}
                    </span>
                  </span>
                  <div className="bg-slate-700 rounded-lg p-2 text-white">
                    {schemaConset != ""
                      ? JSON.stringify(JSON.parse(schemaConset))
                      : ""}
                  </div>
                  <div className="w-full mt-2 flex justify-center items-center">
                    {!loadingConset ? (
                      <>
                        <button
                          key="submit_proposal_btn_auth"
                          className="bg-gray-300 text-green-600 font-bold px-4 py-2 text-lg rounded-full mt-4 active:scale-90 shadow-inner w-36"
                          onClick={async () => {
                            setLodingConset(true);

                            let pubKey = null;
                            try {
                              pubKey = retrievePublicKey();
                            } catch (e) {}
                            if (!pubKey) {
                              const keypair = generateKeyPair();
                              pubKey = keypair.publicKey;
                            }

                            await sendTx(
                              {
                                SubmitData: {
                                  id: proposalIdConset,
                                  buyer: buyerConset,
                                  dataSource: consetUrl,
                                  token: "test",
                                  pubkey: pubKey,
                                },
                              },
                              wallet
                            );
                            setSecret(false);
                            setLodingConset(false);
                            setConsetDialog(false);
                          }}
                        >
                          Authorise
                        </button>
                        <button
                          key="submit_proposal_btn_rej"
                          className="bg-gray-300 text-red-500 font-bold px-4 py-2 text-lg rounded-full mt-4 active:scale-90 shadow-inner ml-4 w-36"
                          onClick={async () => {
                            setConsetDialog(false);
                          }}
                        >
                          Reject
                        </button>
                      </>
                    ) : (
                      <Rings
                        visible={true}
                        height="80"
                        width="80"
                        color="#ea580c"
                        ariaLabel="rings-loading"
                        wrapperStyle={{}}
                        wrapperClass=""
                      />
                    )}
                  </div>
                </div>
              </Dialog>
              <div className="w-1/2 bg-slate-700 rounded-lg p-4">
                <div className="flex w-full text-white text-lg">
                  <div className="flex items-center justify-center">
                    <Radio
                      checked={demoTest === "a"}
                      onChange={(e) => setDemoTest(e.target.value)}
                      value="a"
                      name="radio-buttons"
                      inputProps={{ "aria-label": "A" }}
                      sx={{
                        color: "#ea580c",
                        span: {
                          color: "#ea580c",
                        },
                      }}
                    />
                    <span>Reveal It</span>
                  </div>
                  <div className="flex items-center justify-center ml-4">
                    <Radio
                      checked={demoTest === "b"}
                      onChange={(e) => setDemoTest(e.target.value)}
                      value="b"
                      name="radio-buttons"
                      inputProps={{ "aria-label": "B" }}
                      sx={{
                        color: "#ea580c",
                        span: {
                          color: "#ea580c",
                        },
                      }}
                    />
                    <span>Custom Test</span>
                  </div>
                </div>
                {demoTest == "a" && (
                  <>
                    <div className="text-white flex flex-col items-center justify-center mt-2">
                      <span className="font-bold text-lg">Secret Message!</span>
                      {!isSecret ? (
                        <span className="text-white text-kg">
                          Metagraphs are amazing!!
                        </span>
                      ) : (
                        <>
                          <div className="h-40 w-40 bg-orange-300 rounded-lg"></div>
                          <span className="text-center">
                            Allow following auth which share non PII data
                            generated by us for your account to thrid party
                          </span>
                          <div>
                            <button
                              key="submit_proposal_btn"
                              className="bg-white text-orange-600 font-bold px-4 py-2 text-lg rounded-full mt-4 active:scale-90"
                              onClick={async () => {
                                setConsetDialog(true);
                                let schema = "";
                                let orgNamex = "";
                                let drid =
                                  "888b8a35807cfaf57dcb22602ffd3598edd9245d147b5347b81c44cc0ece6f07";
                                for (let x of dataRequests) {
                                  if (x.dataRequests.hasOwnProperty(drid)) {
                                    schema = x.dataRequests[drid].schema;
                                    orgNamex = x.orgName;
                                  }
                                }
                                setSchemaConset(schema);
                                setOrgNameConset(orgNamex);
                                setProposalIdConset(
                                  "c30e4b44fd5d3174f5df8e280e920c6fd4b52f4f5189946a3e36adace14df0f1"
                                );
                                setBuyerConset(
                                  "DAG15DfFoV2SskZiDcgQEWnUnGuGYYAh2uA1yNo9"
                                );
                                setConsetUrl(
                                  //
                                  "https://img.xylicdata.com/test.json"
                                );
                              }}
                            >
                              Allow
                            </button>
                          </div>{" "}
                        </>
                      )}
                    </div>
                  </>
                )}
                {demoTest == "b" && <></>}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
