# DSP

Run the system:
run this command: java -jar local.jar inputFileName outputFileName n terminate
or :java -jar local.jar inputFileName outputFileName n (to run without terminate)
*The local start other java programs (as we mentiond in the working flow) and it uses other jar file (manager and worker) that we already upload to and S3 bucket.

The working flow:
Starting the program from the loacl.jar that we run it on our computer. The local inite the system back to make it possible for the other parts of the system to work. In more detales the local initiate the four SQS queues called w2m (worker to manager), l2m (local to manager), m2w (manager to worker), m2l (manager to local), each one of thus four is in charge for spasific kind of messages as mentioned in the assignment page. Then it make sure that there is a running instance of EC2 for the manager to run on it (more detales about the instance below). After that its make an uniqe bucket on s3 for the local app to upload files on it. In this case we upload the input file. When that is done, we wait for messages from the manager indicating that he finished his job.
The manager node always wait for message from local apps (until one of them send terminate message) each one of thus message contain the bucket name and the file to download from it.  The manager node takes that file and slpit it. For each line in the file the manager send a message for m2w queue, and using threads pool, it start the manager handler function which handle the output file and messages. The manager decide how much workers needed and make sure there are enough EC2 running instances for them.
Here strat the worker job, each one of them start pulling messages from m2w queue, each one of those messages contain the image url and local id of the local app. The worker use OCR program to convert the image to text and send the result to w2m queue. 
When the workers finish there job the manager takes the workers messages and put them in summary file and upload it to the same bucket for the local app.
The local app takes this file and create HTML file out of it and put it in the output file with name outputFileName.

System persistence:
since the commucation between all the system parts take place with sqs messages, all the actions are secure, meaning if one node fail at some point the message that is in charge for this node is still in the queue and we gonna make sure some other node is dealing with it.

System Scalability:
the AWS account that we got allows only 20 running ec2 instances. Looking at the system from this side make a downside for the system scalability. However, if we run the system with high number of image peer worker we could get a good results. From the other hand our system wokrs with only one manager. Its true that we use threads to make work more efficient but still thats make the manager works hard all the time.

Threads:
As we mentioed above the manager uses threads pool to deal with the workers output in order to make the manager works in parallel.

AWS resources:
we create an ec2 image based Ubuntu.
ec2 AMI id ami-0a441f9c993ec41b6 ,type t2.micro 
(the image include tesseract, java and AWS CLI)
System securtiy:
To make sure all the resources use the AWS credential in a secure way we pass them throug the IAM role.



