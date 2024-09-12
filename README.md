### Project Description: DataMart - Solving the Data Liquidity Problem

## Setup Instructions

### To Start Metagraph

1. Clone the Euclid development environment:
   ```bash
   git clone https://github.com/Constellation-Labs/euclid-development-environment.git
   ```

2. Clone the current repository inside the `euclid-development-environment/source/project` folder.

3. Navigate to the root folder `euclid-sdk`:
   ```bash
   cd euclid-development-environment
   ```

4. Modify following keys in the `euclid.json` file with your configuration:
   ```json
   {
     "github_token": "YOUR_GITHUB_TOKEN",
     "tessellation_version": "2.8.1",
      ...
     "project_name": "datamart-metagraph"
   }
   ```

5. Review the following page before running the next commands:  
   [Hydra CLI Documentation](https://docs.constellationnetwork.io/sdk/elements/hydra-cli)

6. Run the build script:
   ```bash
   ./scripts/hydra build
   ```

7. Start the genesis network:
   ```bash
   ./scripts/hydra start-genesis
   ```

### To Start Frontend

1. Navigate to the `ui` folder:
   ```bash
   cd euclid-development-environment/source/project/ui
   ```

2. Install the required packages:
   ```bash
   npm install
   ```

3. Start the frontend:
   ```bash
   npm run start
   ```


## Details

**DataMart** is an innovative solution designed to address the complex issues surrounding data liquidity. It leverages **metagraph technology**, a type of blockchain that enables the creation of application-specific blockchains, to facilitate secure, user-controlled, and privacy-centric data sharing between organizations. 

#### Functionality:
At its core, DataMart connects **data buyers** and **data providers** through a decentralized protocol:

1. **Data Buyer Request**: A company or organization, acting as a data buyer, submits a request for specific types of user-generated data with a well-defined data schema. This could be for purposes like analytics, machine learning training, or generating business insights.
   
2. **Data Provider Proposal**: Organizations that possess the requested data, termed as data providers, review the request. If they find the request viable, they submit a proposal to the data buyer.

3. **User Consent Flow**: Once the data buyer approves the proposal, the data provider prompts their users for consent. This process is designed to incentivize users, offering rewards such as free subscriptions or ad-free experiences in exchange for data sharing.

4. **Secure Data Transfer**: When the user consents, the requested data is shared via the metagraph. The data remains encrypted and is stored on the blockchain, ensuring that only the data buyer and the user can access the data. This eliminates the risks of unauthorized data access.

#### Impact on the Constellation Ecosystem:
DataMart brings **several key benefits** to the Constellation ecosystem:

- **Enhanced Data Liquidity**: By streamlining the process of secure data sharing, DataMart makes it easier for organizations to buy and sell user data in a transparent and legally compliant manner.
  
- **Privacy-Driven Architecture**: DataMart ensures that users are at the center of the data-sharing process. User consent is paramount, and all transactions are fully encrypted, aligning with privacy regulations such as GDPR.

- **Incentivized Data Sharing**: With users benefiting from the sale of their own data, DataMart empowers them to make informed decisions about how their data is used. This creates a **user-centric data economy** within the Constellation network, where users are not just passive participants but active beneficiaries.

- **Application-Specific Blockchains**: By utilizing metagraph technology, DataMart can scale flexibly to meet the specific data needs of various industries while maintaining high levels of security and decentralization. Each data-sharing process can run on its own application-specific blockchain, optimizing for performance and privacy.

- **Data Portability**: DataMart enables seamless data transfer between organizations, even when users switch between applications. This resolves the common issue of data silos and fragmentation, encouraging greater collaboration within the Constellation ecosystem.

#### Vision:
Our vision with DataMart is to **revolutionize how organizations and users engage with data**. We aim to make data more liquid, breaking down barriers between companies while ensuring user data privacy and control. By allowing organizations to share data securely and transparently, DataMart will foster innovation in data analytics, machine learning, and personalized user experiences, while maintaining regulatory compliance.

This project will be a **cornerstone for decentralized, privacy-driven data ecosystems**, allowing users and organizations to benefit from data liquidity in a compliant, secure, and user-friendly way.

