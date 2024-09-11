import { Dialog, DialogTitle } from "@mui/material";
import React, { useEffect, useState } from "react";
import { Rings } from "react-loader-spinner";
import { sendTx } from "../helpers/transaction";
import { useDispatch, useSelector } from "react-redux";
import { fetchDataRequests } from "../store/metagraphSlice";
import Accordion from "@mui/material/Accordion";
import AccordionActions from "@mui/material/AccordionActions";
import AccordionSummary from "@mui/material/AccordionSummary";
import AccordionDetails from "@mui/material/AccordionDetails";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import CloseIcon from "@mui/icons-material/Close";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import { toast } from "react-toastify";

export default function Card({ data, wallet }) {
  const [rcount, setRcount] = useState(0);
  const [pcount, setPcount] = useState(0);
  const [detailDialog, setDetailDialog] = useState(false);
  const [recordId, setRecordId] = useState("");
  const [loading, setLoding] = useState(false);
  const [submittedP, setSUbmittedP] = useState({});
  const dispatch = useDispatch();
  const myProposals = useSelector((state) => state.metagraph.myProposals);
  const walletAddress = useSelector((state) => state.metagraph.walletAddress);

  useEffect(() => {
    setRcount(Object.keys(data.dataRequests).length);
    setPcount(
      Object.keys(
        myProposals.find((v) => v.buyer == data.buyer)?.proposals || {}
      ).length
    );
    const x = {};
    const t = myProposals.find((v) => v.buyer == data.buyer);
    if (t) {
      for (let k in t.proposals) {
        x[t.proposals[k].dataRequestId] = true;
      }
    }

    setSUbmittedP(x);
    //wallet.activate()
  }, [data, myProposals]);
  return (
    <>
      <Dialog
        sx={{
          ".MuiPaper-root": {
            background: "#1f2937",
            width: "50%",
          },
        }}
        open={detailDialog}
        onClose={() => {
          setRecordId("");
          setDetailDialog(false);
        }}
      >
        <DialogTitle
          sx={{
            fontWeight: "bold",
          }}
          className="text-orange-600 gray font-bold"
        >
          {recordId != "" ? (
            <div className="flex">
              <div className="mr-3">
                <button
                  onClick={() => {
                    setRecordId("");
                  }}
                  className="text-sm bg-white rounded-full p-1 flex justify-center items-center active:scale-90"
                >
                  <ArrowBackIosIcon className="ml-1.5" />
                </button>
              </div>
              <div className="flex items-center justify-center">
                <div>
                  <span className="text-white">Data Request</span>{" "}
                  {recordId.slice(0, 12)}...
                  {recordId.slice(recordId.length - 12, recordId.length)}
                </div>
                <div
                  onClick={async () => {
                    await navigator.clipboard.writeText(recordId);
                    toast.info("Copied to clipboard!");
                  }}
                  className="ml-1.5 bg-slate-500 cursor-pointer rounded-full p-1 text-sm text-white active:scale-90"
                >
                  <ContentCopyIcon />
                </div>
              </div>
            </div>
          ) : (
            <div className="flex">
              <div className="mr-3">
                <button
                  onClick={() => {
                    setRecordId("");
                    setDetailDialog(false);
                  }}
                  className="text-sm bg-white rounded-full p-1  active:scale-90"
                >
                  <CloseIcon />
                </button>
              </div>
              <div className="flex">
                <div>
                  <span className="text-white">Data requests from</span>{" "}
                  {data.buyer.slice(0, 9)}...
                  {data.buyer.slice(data.buyer.length - 9, data.buyer.length)}
                </div>
                <div
                  onClick={async () => {
                    await navigator.clipboard.writeText(data.buyer);
                    toast.info("Copied to clipboard!");
                  }}
                  className="ml-1.5 bg-slate-500 cursor-pointer rounded-full p-1 text-sm text-white active:scale-90"
                >
                  <ContentCopyIcon />
                </div>
              </div>
            </div>
          )}
        </DialogTitle>
        {recordId == "" ? (
          <div className="flex flex-col px-4 mb-4">
            {Object.keys(data.dataRequests).map((v, i) => {
              return (
                <div
                  key={v}
                  className="bg-white p-1 px-2 rounded-lg mt-1 mb-1 cursor-pointer active:scale-90 flex justify-between"
                  onClick={() => {
                    setRecordId(v);
                  }}
                >
                  <div>
                    <span className="font-bold text-orange-600">ID:</span>{" "}
                    {v.slice(0, 12)}...{v.slice(v.length - 12, v.length)}
                  </div>
                  {submittedP[v] && (
                    <div className="bg-slate-500 rounded-lg px-2 text-white">
                      Submitted proposal
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="flex flex-col px-4 mb-4">
            {data.dataRequests[recordId].description != "" && (
              <div className="flex flex-col">
                <span className="text-xl text-orange-600 font-bold ml-2">
                  Description:
                </span>
                <span className="text-black bg-white font-bold text-xl p-2 rounded-lg">
                  {data.dataRequests[recordId].description}
                </span>
              </div>
            )}
            <div className="flex flex-col">
              <span className="text-xl text-orange-600 font-bold ml-2">
                Schema:
              </span>
              <span className="text-black bg-white font-bold text-xl p-2 rounded-lg">
                {JSON.stringify(JSON.parse(data.dataRequests[recordId].schema))}
              </span>
            </div>
            {!submittedP[recordId] && (
              /*data.buyer !=walletAddress &&*/ <div className="flex justify-center">
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
                  <button
                    key="submit_proposal_btn"
                    className="bg-white text-orange-600 font-bold px-4 py-2 text-lg rounded-full mt-4 active:scale-90"
                    onClick={async () => {
                      setLoding(true);
                      await sendTx(
                        {
                          CreateProviderProposal: {
                            id: recordId,
                            description: "",
                            amount: 1,
                            buyer: data.dataRequests[recordId].buyer,
                          },
                        },
                        wallet
                      );
                      const x = { ...submittedP };
                      x[recordId] = true;
                      setSUbmittedP(x);
                      setRecordId("");
                      setLoding(false);
                      dispatch(fetchDataRequests(walletAddress));
                    }}
                  >
                    Submit Proposal
                  </button>
                )}
              </div>
            )}

            {Object.keys(data?.proposals || {}).filter(
              (v) => data.proposals[v].dataRequestId == recordId
            ).length > 0 && (
              <div className="w-full flex flex-col mt-4">
                <span className="text-xl text-orange-600 font-bold ml-2">
                  Proposals:
                </span>
                {Object.keys(data?.proposals || {})
                  .filter((v) => data.proposals[v].dataRequestId == recordId)
                  .map((v, i) => {
                    return (
                      <Accordion
                        sx={{
                          width: "100% !important",
                          background: "white !important",
                        }}
                        className="mt-2"
                        key={v}
                      >
                        <AccordionSummary
                          expandIcon={<ExpandMoreIcon />}
                          aria-controls="panel1-content"
                          id="panel1-header"
                          className="font-bold"
                        >
                          <div className="flex items-center justify-between">
                            <div>
                              {v.slice(0, 12)}...
                              {v.slice(v.length - 12, v.length)}
                            </div>
                            <div
                              onClick={async (e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                await navigator.clipboard.writeText(v);
                                toast.info("Copied to clipboard!");
                              }}
                              className="ml-2 bg-slate-500 cursor-pointer rounded-full p-1 text-sm text-white active:scale-90"
                            >
                              <ContentCopyIcon />
                            </div>
                            {data.proposals[v].provider == walletAddress && (
                              <div className="bg-gray-400/[0.54] text-orange-600 rounded-full px-4 ml-4 shadow-inner">
                                Your Proposal
                              </div>
                            )}
                          </div>
                        </AccordionSummary>
                        <AccordionDetails>
                          <div className="flex flex-col">
                            <div>
                              <span>
                                <span className="font-bold text-orange-600">
                                  Provider:
                                </span>{" "}
                                {data.proposals[v].provider}
                              </span>
                            </div>

                            <div className="flex">
                              <span>
                                <span className="font-bold text-orange-600">
                                  Org Name:
                                </span>{" "}
                                {data.proposals[v].orgName}
                              </span>
                              <span className="ml-4">
                                <span className="font-bold text-orange-600">
                                  Org Link:
                                </span>{" "}
                                {data.proposals[v].orgLink}
                              </span>
                            </div>
                            <div>
                              <span>
                                <span className="font-bold text-orange-600">
                                  Org Description:
                                </span>{" "}
                                {data.proposals[v].orgDescription}
                              </span>
                            </div>
                            <div>
                              <span>
                                <span className="font-bold text-orange-600">
                                  Message:
                                </span>{" "}
                                {data.proposals[v].description}
                              </span>
                            </div>
                            <div>
                              <span>
                                <span className="font-bold text-orange-600">
                                  Is Approved:
                                </span>{" "}
                                {data.proposals[v].isApproved ? "Yes" : "No"}
                              </span>
                            </div>
                            {data.buyer == walletAddress &&
                              !data.proposals[v].isApproved && (
                                <div>
                                  <button
                                    onClick={() => {
                                      sendTx(
                                        {
                                          ApproveProposal: {
                                            id: v,
                                          },
                                        },
                                        wallet
                                      );
                                    }}
                                    className="bg-black text-orange-600 font-bold px-4 py-2 text-lg rounded-full mt-4 active:scale-90"
                                  >
                                    Approve
                                  </button>
                                </div>
                              )}
                          </div>
                        </AccordionDetails>
                      </Accordion>
                    );
                  })}
              </div>
            )}
          </div>
        )}
      </Dialog>
      <div
        onClick={() => {
          console.log("dj:lol");
          setDetailDialog(true);
        }}
        className="flex flex-col bg-slate-700 rounded-lg p-4 cursor-pointer active:scale-90"
      >
        <div>
          <span className="text-white font-bold text-xl">Buyer:</span>
          {data.buyer == walletAddress ? (
            <span className="bg-gray-400/[0.54] text-orange-600 rounded-full px-4 ml-1 py-1 shadow-inner">
              You
            </span>
          ) : (
            <span className="text-xl text-orange-600 font-bold ml-2">
              {data.buyer.slice(0, 9)}...
              {data.buyer.slice(data.buyer.length - 9, data.buyer.length)}
            </span>
          )}
        </div>
        <div>
          <span className="text-white font-bold text-lg">Organisation:</span>
          <span className="text-lg text-orange-600 font-bold ml-2">
            {data.orgName}
          </span>
        </div>
        <div>
          <span className="text-white font-bold text-lg">Description:</span>
          <span className="text-lg text-orange-600 font-bold ml-2">
            {data.orgDescription.slice(0, 100)}
          </span>
        </div>
        <div>
          <span className="text-white font-bold text-lg">Link:</span>
          <a
            className="text-lg text-orange-600 font-bold ml-2"
            href={data.link}
          >
            {data.link}
          </a>
        </div>
        <div className="flex mt-2">
          <div
            className={
              "mr-1 text-center text-orange-600 font-bold bg-slate-200 p-2 py-1 rounded-full shadow-inner"
            }
          >
            <span className="text-black">Requests:</span>
            <span className="ml-1">{rcount}</span>
          </div>
          <div
            className={
              "flex-1 ml-1 text-center text-orange-600 font-bold bg-slate-200  p-2 py-1 rounded-full shadow-inner"
            }
          >
            <span className="text-black">Your Proposals:</span>
            <span className="ml-1">{pcount}</span>
          </div>
        </div>
      </div>
    </>
  );
}
