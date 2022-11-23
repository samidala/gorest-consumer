# How to setup
1. clone the git repo
2. build the project by running `mvn clean install` and this generates the war file
3. deploy the war file to any web server. I used tomcat for testing.
4. Once the application successfully deploys, access the REST endpoints.
5. You can find the rest points by swagger UI: http://host:port/swagger-ui.html and test from there

# Implementation details
## creating post and user in target system
1. Application validates if user is registered with given email ID by querying from target system
   1. if yes, the app uses user ID from response and tries to create the post in target system.
   2. if no, the app tries to create the user with given email and then tries to the post in target system.
2. App validates the user input
   1. Validates email, gender, name etc is not null before connecting to the target system.
   2. validates user ID, title, body is not null before connecting to the target system.
3. the **email validation** happens by connecting to target system and passes the **email ID** as *query param* and verifies.
   1. There is **known issue** that target system does **not** return the email though it exists and the creating the post in target system fails.
4. The application returns error if failed to create the entity in target system and http error code will be 422.
5. The app returns 400 http status code for invalid input
6. The app returns internal error for internal errors


## Getting user posts
1. The connects to target system and gets the user posts and process the response.
2. The app spans fetches users and user posts concurrently by spanning multiple threads.
   1. The no of threads can be configured in `application.properties`
      1. property `thread.pool.count`
      2. the property value is dependent on multiple constraints. 
         1. No of processors
         2. time spent in IO, for example, time spent in IO is 500 ms then value can no of processor / time spent in IO
         3. No of thread pools in the system
   2. The app first gets the total count of users and batches into multiple requests in different threads
      1. for example, if the count **2000** and the no of requests will be **2000/ 100** where **100 requests** per page in multiple threads.
      2. Same applicable for getting user posts.

## Junits
1. Added few junits for unit testing and uses mockito to mock REST service calls.

## Known issues and improvements
1. There is **known issue** that target system does **not** return the email though it exists and the creating the post in target system fails.
2. Validation in controller layer is not implemented however validations are present in service layer.
3. The libraries can be upgraded to latest version. 