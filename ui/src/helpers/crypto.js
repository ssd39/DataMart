import JSEncrypt from 'jsencrypt';

// Generate RSA key pair using JSEncrypt
function generateKeyPair() {
  const encrypt = new JSEncrypt({ default_key_size: 2048 });
  const publicKey = encrypt.getPublicKey();
  const privateKey = encrypt.getPrivateKey();

  // Save keys in localStorage or return them
  storePublicKey(publicKey);
  storePrivateKey(privateKey);
  return { publicKey, privateKey };
}

// Encrypt with public key using PKCS1 padding
function encrypt(publicKey, plaintext) {
  const encrypt = new JSEncrypt();
  encrypt.setPublicKey(publicKey);
  const encrypted = encrypt.encrypt(plaintext);

  if (!encrypted) {
    throw new Error('Encryption failed');
  }

  return encrypted;
}

// Decrypt with private key using PKCS1 padding
function decrypt(privateKey, ciphertext) {
  const decrypt = new JSEncrypt();
  decrypt.setPrivateKey(privateKey);
  const decrypted = decrypt.decrypt(ciphertext);

  if (!decrypted) {
    throw new Error('Decryption failed');
  }

  return decrypted;
}

// Storing and retrieving keys in localStorage as before
function storePublicKey(publicKey) {
  localStorage.setItem('rsa-public-key', publicKey);
}

function storePrivateKey(privateKey) {
  localStorage.setItem('rsa-private-key', privateKey);
}

function retrievePublicKey() {
  const publicKey = localStorage.getItem('rsa-public-key');
  if (!publicKey) {
    throw new Error('No public key found in localStorage.');
  }
  return publicKey;
}

function retrievePrivateKey() {
  const privateKey = localStorage.getItem('rsa-private-key');
  if (!privateKey) {
    throw new Error('No private key found in localStorage.');
  }
  return privateKey;
}

export {
  generateKeyPair,
  encrypt,
  decrypt,
  storePublicKey,
  storePrivateKey,
  retrievePublicKey,
  retrievePrivateKey,
};
