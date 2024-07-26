# TF-IDF Based Distributed Document Search Project

# ![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXfNz567OjsKiQfeWJUsiCOZO4d5iGdE9Fu7-V4gBT8L4Co7JsQdwQ348q2mLiV3n5I57-6yGPfFkM_0USC8CUnCWw5pmobDIxvuLTLw3yexlu_ePHgWLICGPYci4UUsUsp3GqlzgOusrzhtsPaj01mB-_u1?key=-f-cbJ1PCIJGR4KMIdSdGg)

# Quick Link

[**1, System Architecture Components**](#System-Architecture-Components)

[**2, Leader Election/Re-Election Algorithm Implementation with Failover**](#Leader-Election/Re-Election-Algorithm-Implementation-with-Failover)

[**3, Introduction to TFIDF (Term Frequency - Inverse Document Frequency)**](#introduction-to-tfidf-term-frequency---inverse-document-frequency)

[**4, Communication and Data Delivery Between Servers**](#Communication-and-Data-Delivery-Between-Servers)

[**5, Sample Request Flow**](#Sample-Request-Flow)


# System Architecture Components

**1, User's Browser**

- **Description**: The entry point for users to interact with the distributed document search service. Users can submit search queries and view search results through a user-friendly interface.

- **Function**: Sends search requests to the frontend server and displays results to the user.

**2, Frontend Server**

- **Description**: Acts as an intermediary between the user's browser and the backend services. It handles incoming search requests and forwards them to the appropriate backend components.

- **Function**: Manages client-server communication, processes user requests, and returns search results.

**3, ZooKeeper**

- **Description**: A centralized service for maintaining configuration information, naming, and providing distributed synchronization. It ensures high availability and reliable coordination between distributed components.

- **Function**: Manages service registries for coordinators and worker nodes, and facilitates leader election.

**4, Coordinators Service Registry**

- **Description**: A registry managed by ZooKeeper that keeps track of all available search coordinators. This allows dynamic discovery and failover handling.

- **Function**: Registers and deregisters coordinators, providing updated lists of active coordinators.

**5, Workers Service Registry**

- **Description**: A registry managed by ZooKeeper that tracks all worker nodes. This ensures that the coordinator can efficiently distribute tasks to available workers.

- **Function**: Registers and deregisters worker nodes, ensuring an up-to-date list of active workers.

**6, Search Coordinator / Leader**

- **Description**: The central component responsible for managing search tasks. It distributes tasks to worker nodes and aggregates their results.

- **Function**: Coordinates search requests, manages task distribution, and consolidates results from worker nodes.

**7, Worker Nodes**

- **Description**: Distributed nodes that handle the actual document processing. Each worker node performs TFIDF-based document searches and returns results to the coordinator.

- **Function**: Processes search tasks assigned by the coordinator and returns the results.


# Leader Election/Re-Election Algorithm Implementation with Failover

Leader election is a critical component in ensuring that the distributed system has a single coordinator managing tasks at any given time. The Watch-based Predecessor Notification algorithm uses ZooKeeper to elect a leader among the nodes.


### Steps and Methods

1. **Volunteer for Leadership Method**:

   - **ZNode Prefix**: Define the prefix as `/election/c_` where `c_` stands for candidate.

   - **ZooKeeper Create Method**:

     - **Parameters**:

       - ZNode Prefix

       - Data (empty byte array)

       - Access Control List (open unsafe enum value)

       - ZNode Creation Mode (ephemeral sequential)

     - **Return Value**: Full path and name of the created ZNode.

     - **Extract ZNode Name**: Store only the ZNode name without the path in a class member variable.

2. **Elect Leader Method**:

   - **Get Children ZNodes**: Call the `getChildren` method to retrieve a list of child ZNodes of the election ZNode.

   - **Sort ZNodes**: Sort the list of ZNodes lexicographically in ascending order.

   - **Determine Leader**:

     - Compare the ZNode name with the first ZNode in the sorted list.

     - If they match, the current node is the leader.

     - If not, identify and print the leader.

3. **Failover Mechanism**:

![](https://lh7-rt.googleusercontent.com/docsz/AD_4nXdEaTXKORxEZe3ONVBOtqNTCHWKu1uyQm82LNQXs_BN8j6KujQwQ5PvaLg3z5CRU3F8B6gn6Th2RNB1yVpYVJo_K8w8TeYTqEtleXORYpXGIZZARWaYrv1d6uKubfv_ijyGM6pO4pobMDmGXhWk7lf6LKY?key=-f-cbJ1PCIJGR4KMIdSdGg)

- **Watch-Based Predecessor Notification**: To avoid the herd effect, where many nodes wake up and try to become the leader simultaneously when the current leader fails, each node sets a watch on its predecessor ZNode.

- **Herd Effect**: This occurs when many nodes react simultaneously to the same event, causing performance degradation and overwhelming the system.

- **Re-election Process**:

  - If a node's predecessor ZNode is deleted (indicating a failure), only the next immediate node in the sequence reacts to the event.

  - This node will then check if it is now the leader or set a watch on its new predecessor.

4. **Main Method**:

   - Call the `volunteerForLeadership` and `electLeader` methods.

   - Implement failover mechanisms to handle leader failure by setting watches on predecessor nodes.

   - Run multiple instances of the application to test the leader election algorithm and failover mechanism.

By using a combination of ephemeral sequential ZNodes and the Watch-based Predecessor Notification, the system ensures efficient leader election and failover handling while avoiding the herd effect. This approach maintains a single active coordinator and provides a robust mechanism for leader re-election in case of node failures.


# Introduction to TFIDF (Term Frequency - Inverse Document Frequency)

The TFIDF algorithm is used to rank documents based on their relevance to a search query. It effectively balances the importance of terms within individual documents and across a collection of documents, providing a more accurate measure of relevance compared to simple word counts.


### Key Concepts

1. **Term Frequency (TF)**:

   - **Definition**: Measures how frequently a term appears in a document.

   - **Formula**: TF = (Number of times term appears in a document) / (Total number of terms in the document).

2. **Inverse Document Frequency (IDF)**:

   - **Definition**: Measures the importance of a term.

   - **Formula**: IDF = log(Total number of documents / Number of documents containing the term).

3. **TFIDF Calculation**:

   - **Combines TF and IDF** to give a weighted score.

   - **Formula**: TFIDF = TF \* IDF.


### Example

- **Term**: "plane"

- **Document Set**: 10 documents

- **Term "the"**: Appears in all 10 documents, IDF = log(10/10) = 0.

- **Term "big"**: Appears in 3 documents, IDF = log(10/3) = 0.52.

- **Term "plane"**: Appears in 2 documents, IDF = log(10/2) = 0.69.

Documents containing rare terms like "plane" score higher than those with common terms like "the".


### Why TFIDF is Used

The TFIDF algorithm offers several advantages over simple word count methods:

1. **Simple Word Count**:

   - **Approach**: Count occurrences of search terms in each document.

   - **Flaw**: Favors larger documents due to higher term counts, leading to biased results where longer documents appear more relevant regardless of actual relevance.

2. **Term Frequency (TF)**:

   - **Approach**: Calculate term frequency as the number of times a search term appears divided by the total number of words in the document.

   - **Improvement**: Reduces bias towards larger documents by normalizing term frequency, but does not account for the importance of terms across the entire document set.

3. **TFIDF Advantages**:

   - **Balances Local and Global Term Importance**: By combining TF and IDF, TFIDF accounts for both the frequency of terms within individual documents and the rarity of terms across the entire document set.

   - **Reduces Common Term Bias**: Common terms that appear in many documents (like "the") are given lower importance, while rare, informative terms are given higher importance.

   - **Improves Search Relevance**: Provides a more accurate measure of document relevance to a search query, resulting in better search results.


### Summary

The TFIDF algorithm is an effective tool for ranking documents based on their relevance to a search query. By combining term frequency and inverse document frequency, it addresses the shortcomings of simple word counts and offers a more balanced and accurate measure of relevance. This makes TFIDF particularly useful in search engines and information retrieval systems, where finding the most relevant documents quickly and accurately is essential.


# Communication and Data Delivery Between Servers

Effective communication and data delivery between the components in the distributed document search service are crucial for ensuring efficient task distribution and result aggregation. Here's an overview of the communication mechanisms and the rationale behind using Protocol Buffers (proto) for data serialization.


### General Communication Logic

1. **Custom Web Server Setup**:

   - **Initialization**: Each server component initializes a web server that listens for incoming HTTP requests.

   - **Endpoints**: Servers define specific endpoints for handling different types of requests. For example, a `/status` endpoint for health checks and `/task` endpoint for processing search tasks.

   - **Request Handling**:

     - **Status Checks**: Handle GET requests to provide a health check response, confirming that the server is alive.

     - **Task Handling**: Handle POST requests where the server processes incoming data, performs the required computation, and returns the result.

2. **Frontend Server Communication**:

   - **Query Handling**: The frontend server receives search queries from the user's browser and forwards them to the search coordinator.

   - **Coordinator Lookup**: The frontend server queries ZooKeeper to obtain the IP address of the current leader (Search Coordinator) from the Coordinators Service Registry.

   - **Forwarding Requests**: The search query is forwarded to the Search Coordinator via a POST request.

3. **Search Coordinator Communication**:

   - **Task Distribution**: The Search Coordinator receives the search query from the frontend server, divides the task into smaller subtasks, and distributes these to the Worker Nodes.

   - **Worker Nodes Lookup**: The Search Coordinator queries ZooKeeper to get the IP addresses of active Worker Nodes from the Workers Service Registry.

   - **Sending Tasks**: The coordinator sends tasks to Worker Nodes using POST requests.

4. **Worker Nodes Communication**:

   - **Task Processing**: Each Worker Node receives a subset of documents, processes the search task using the TFIDF algorithm, and computes the relevance scores.

   - **Result Submission**: Worker Nodes send the computed results back to the Search Coordinator via HTTP POST requests.

5. **Aggregation and Response**:

   - **Result Aggregation**: The Search Coordinator aggregates the results from all Worker Nodes, sorts the documents based on relevance, and prepares the final response.

   - **Response to Frontend**: The aggregated and sorted results are sent back to the frontend server, which formats and displays them to the user.


### Data Serialization with Protocol Buffers

For efficient and structured data transfer between servers, Protocol Buffers (proto) are used. Here’s why proto is chosen over JSON:

1. **Efficiency**:

   - **Compact Representation**: Proto messages are serialized into a compact binary format, which is much smaller than JSON. This reduces the amount of data transmitted over the network.

   - **Speed**: Serialization and deserialization of proto messages are significantly faster than JSON, leading to improved performance, especially important for high-throughput systems.

2. **Structured Data**:

   - **Strict Schema**: Proto enforces a strict schema, ensuring that the data structure is well-defined and consistent across different components.

   - **Backward Compatibility**: Proto provides built-in support for backward compatibility. Fields can be added or removed without breaking existing services.

3. **Cross-Language Support**:

   - **Language Independence**: Proto supports multiple programming languages, making it suitable for heterogeneous environments where different components might be implemented in different languages.

   - **Generated Code**: The proto compiler generates code for the defined schema, ensuring that data structures are correctly handled in the target programming language.


### Example Proto Definitions

Proto definitions specify the structure of the messages exchanged between servers. For instance:

- **Request Message**:

  - Represents a search query from the frontend to the search coordinator.

  - Fields include the search query string.

- **Response Message**:

  - Represents the search results sent from the search coordinator to the frontend.

  - Contains a list of relevant documents, each with fields such as document name, relevance score, document size, and author.

By leveraging Protocol Buffers for data serialization, the distributed document search service ensures efficient, structured, and cross-language compatible communication, which is essential for maintaining performance and reliability in a distributed system.


# Sample Request Flow

### Request Flow Between Frontend Server, Coordinator, Worker Nodes, and ZooKeeper

The request flow in the distributed document search service ensures efficient task distribution and result aggregation.

**1, User's Browser**:

- User submits a search query via the browser.

**2, Frontend Server**:

- Receives the search query from the user's browser.

- Queries the Coordinators Service Registry in ZooKeeper to get the IP address of the current leader (Search Coordinator).

- Forwards the search query to the Search Coordinator / Leader.

**3, Search Coordinator / Leader**:

- Receives the search query from the Frontend Server.

- Divides the search task into smaller tasks, each assigned to a subset of documents.

- Queries the Workers Service Registry in ZooKeeper to get the IP addresses of all active Worker Nodes.

- Distributes the search tasks to multiple Worker Nodes based on the document subsets.

**4, Worker Nodes**:

- Each Worker Node receives a subset of the search task from the Search Coordinator.

- Processes its assigned documents using the TFIDF algorithm to calculate relevance scores.

- Sends the processed results (document scores) back to the Search Coordinator.

**5, Search Coordinator / Leader**:

- Aggregates results from all Worker Nodes.

- Sorts the documents based on their relevance scores.

- Sends the aggregated and sorted results back to the Frontend Server.

**6, Frontend Server**:

- Receives the aggregated and sorted search results from the Search Coordinator.

- Formats the results into a user-friendly display.

- Sends the formatted search results back to the user's browser.

**ZooKeeper**:

- Manages service registries for Coordinators and Worker Nodes, keeping track of their IP addresses and statuses.

- Facilitates leader election to ensure a single coordinator is active at any given time.

- Uses Watch-based Predecessor Notification to avoid the herd effect, where only the immediate successor reacts to a leader's failure, ensuring efficient leader re-election.


### Summary

This sequence ensures efficient task distribution, result aggregation, and dynamic service discovery, providing a scalable and robust distributed document search service.

