import { toast } from "react-toastify";

const generateProof = async (message, connector) => {
  // Recursively remove nulls
  const removeNulls = (obj) => {
    if (Array.isArray(obj)) {
      return obj
        .filter((v) => v !== null)
        .map((v) => (v && typeof v === "object" ? removeNulls(v) : v));
    } else if (typeof obj === "object" && obj !== null) {
      return Object.fromEntries(
        Object.entries(obj)
          .filter(([, v]) => v !== null)
          .map(([k, v]) => [k, v && typeof v === "object" ? removeNulls(v) : v])
      );
    } else {
      return obj;
    }
  };

  const getEncoded = (value) => {
    let nonNullValue = removeNulls(value);
    return JSON.stringify(nonNullValue);
  };

  const encoded = getEncoded(message);

  console.log(`\nMessage (JSON): ${encoded}`);

  const accounts = await connector.request({
    method: "dag_accounts",
    params: [],
  });

  console.log("ddl:account", accounts);

  const userAddress = accounts[0];

  const signature = await connector.request({
    method: "dag_signData",
    params: [userAddress, btoa(encoded)],
  });
  console.log("ddl:signature", signature);
 
  const publicKey = await connector.request({
    method: "dag_getPublicKey",
    params: [userAddress],
  });
  const uncompressedPublicKey =
    publicKey.length === 128 ? "04" + publicKey : publicKey;

  return {
    id: uncompressedPublicKey.substring(2),
    signature,
  };
};

const sendTx = async (basePayload, wallet) => {
  const proof = await generateProof(basePayload, wallet);
  const body = {
    value: {
      ...basePayload
    },
    proofs: [proof],
  };

  const res = await fetch(`${process.env.REACT_APP_DATAL1}/data`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  const jsonRes = await res.json()
  if(res.status == 200) {
    toast.success(`Transaction sent successfully!`)
    toast.info(`TX Hash: ${jsonRes.hash}`)
  } else {
    
    toast.error(jsonRes.details.reason)
  }
}

export { generateProof, sendTx }