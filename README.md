# LoadBalancer
Load Balancer which can handle traffic for multiple replica to simulate parallel environment

---

This is a Spring boot application which acts as a load balancer and help simulate parallel environment for your code and test behaviour. This started out as a problem I dealt with during my work, where I was not able to properly test my code in parallel situations and identify race conditions due to complex business logic behind. Then I thought of creating a basic layer 7 Load balancer.

Initially, I implemented a random algorithm to route the traffic to any of the available containers, then I learnt a bit mroe about Load balancing algorithms from [CloudFlare](https://www.cloudflare.com/learning/performance/types-of-load-balancing-algorithms/) which got me curious in implementing the algorithms and come up with some new dynamic algorithm to balance the load.

---

## Algorithms

### Static Algorithms

The algorithms below work with no context of performance or latency of each server and does not account for failure of servers or ptimize the response time of the request.

1. Random Algorthm:
This is the simplest form of algorithm which directs the request to any of the available containers.

2. Round-Robin Algorithm:
This algorithm routes the request in a cyclic fashion so that each of the containers are getting equal number of requests. For example, if we have containers 1, 2, 3 and 4, and we have 5 requests, the order of serving containers will be 1, 2, 3, 4, 1.

### Dynamic Algorithms

1. Minimum Response Time:
This algorithm calculates the average of last 10 response time of all available containers and picks the one with minimum value. This algorithm picks the best performing server since last n requests and statiscally, the next request on this server will take lesser time than other. Although, this approach deals with starvation problem, if a server failed to respond or took more time due to unknow reasons, it is heavily penalized and not given a chance to serve more requests.
*The Average response time is calculated as follows (for last 5 response times), let the last 5 response times to serve the request for server S was (15, 23, 22, 18, 20) in chronological order. Then the cost of server is 19.6. Now a new request was served in 19ms then the response time window will look like (23, 22, 18, 20, 19).*

2. Weighted Response Time:
This alorithm is a variation of above algorithm which calculates the weighted average of last 10, 100, 1000 requests and then calcluate the cost, the weights are (1/10, 1/100, 1/1000) respectively.

3. Best Balance (Theoretical):
I made a custom algorithm which accounts for the **response time** and the **load or serves**. I made an observation that, in an optimal situation in infinite time, response time **T** will will tend to a constant (small value) and serve counts **C** will tend to infinity.
If **C** is getting larger the weight associated with it should get smaller, if **T** is getting smaller the weight associated with it should get larger.
Hence, `cost is proportional to w*T + (1-w)*C`, the value of w should lie between [0,1].
Let `F(x) = (e^x - 1)/(e^x + 1)`, then `w = F(C/(1+T))` here I have taken **C** as total serves (including failures) and **T** as weighted average of last 10, 100, 1000 response times.

---
# Setup and run

 - Make sure you have a containerized service/application which you want to replicate
 - The image should exist before running the application `docker build ...` or `docker pull ...`
 - Range of open ports should not be less than number of replicas of your app
 - Must have Java and Maven CLI installed
 - Should have postgres instance with balancer_transactions table
 
1. Clone the repository
2. Install dependencies `mvn install`
3. Run the spring boot application

*Configure settings in application.properties*
