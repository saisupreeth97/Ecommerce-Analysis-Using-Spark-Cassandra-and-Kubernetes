**Deployment Instructions and Data Migration to Database:**

For the spark cluster to be set up locally using Kubernetes, first, we need to set up the minikube node locally and also the kubectl command line tool. The following are the steps to deploy the cluster:

1\. To install the kubectl and minikube node locally, follow the below official document of minikube:

• <https://minikube.sigs.k8s.io/docs/start/>

2\. Once minikube is installed locally, we can start the Minikube node using the following command:

• minikube start --driver=qemu --cpus=8 --memory=12000

This will start the minikube node with 8 CPU cores and 12GB of memory; we can configure custom values by passing them as parameters.

3\. Once minikube is started, we must add some add-ons to get the metrics and ingress controller. The following commands will add the add-on:

• minikube addons enable ingress

• minikube addons enable metrics-server

4\. Once add-ons are added, we can create a pod for Cassandra. Use the following git hub link to get the configuration files and run the following commands to create a pod.

• <https://github.com/saisupreeth97/Cassandra-k8s-config>

Now execute the following commands in the command line terminal to create the pod for the Cassandra.

• First, cd to the folder where config files are there and run the following commands:

• kubectl create -f cassandra-service.yaml

• kubectl create -f cassandra-statefulset.yaml

5\. To access the Kubernetes dashboard, run the following command in the command line terminal:

• minikube dashboard

This will open the dashboard in the localhost. Select the Cassandra pod and open the terminal and execute the next steps.

(or)

Or we can access the pod using the interactive terminal using the following command; this will also open the terminal for the pod.

• kubectl exec -it cassandra-0 bash

Then create a new directory to store the datafile by following the command in the Cassandra pod terminal

• mkdir datafile

6\. Now we must move the dataset file to the pod and add data to the Cassandra. To do that, first, we need to download the dataset from Kaggle using the following link:

• [https://www.kaggle.com/datasets/mkechinov/ecommerce-behavior-data-from-multi-category-store](https://www.kaggle.com/datasets/mkechinov/ecommerce-behavior-data-from-multi-category-store)

Download the files from the link and copy to the Cassandra using the following kubectl command:

• Kubectl cp [path-of-the-file] default/cassandra-0:/datafile

7\. Once the data file is moved now, open cqlsh by using the same terminal and run the following commands:

• cd bin

• cqlsh

Now create keyspace and table using the following commands

• create keyspace if not exists ecommerce with replication={'class':'SimpleStrategy','replication\_factor':1};

Next we can create the table using the following Cassandra create table command:

create table ecommerce.orders (

event\_time timestamp,

event\_type text,

product\_id bigint,

category\_id bigint,

category\_code text,

brand text,

price float,

user\_id bigint,

user\_session uuid,

primary key ((event\_type),category\_id,product\_id,category\_code,event\_time));

Now execute the below command to copy the data to Cassandra

• copy ecommerce.orders(event\_time,event\_type,product\_id,category\_id,category\_code,brand,price,user\_id,user\_session) from ‘/datafile/2019-Oct.csv’ with header=true;

8\. Next, we need to install a spark cluster using helm chart, an artifact hub for Kubernetes;the following link is to install the spark cluster with two executor nodes.

To install the helm on your machine, follow the instructions in the link:

• <https://helm.sh/docs/intro/install/>

Once the helm is installed, install spark using the artifact hub by following the below link:

• <https://artifacthub.io/packages/helm/bitnami/spark>

9\. Now open the IDE terminal and run the following command to build the application.

• ./gradlew :shadowJar

10\. In the build/libs folder it will generate a jar file with the name ecommerce-analysis-0.0.1-SNAPSHOT-all.jar. Now we need to move this file to master node of the spark using the following command:

Before moving the file, create a directory in the master node of Cassandra with the following command using the terminal in the Kubernetes dashboard or interactive terminal in the command line terminal

• mkdir myjars

Now execute the following command to move file from local machine to master spark node in the kubernetes

• Kubectl [path-to-the-jarfile] default/spark-release-master-0:/opt/bitnami/spark/myjars

11\. To create the dashboards for the output, use Tableau and create the Cassandra connection using ODBC driver and select commands (or) generate the CSV reports from the database and use the excel sheets to generate the graphs

**Steps to Run the Application (Spark-Submit):**

Before executing the spark job, we need to do port-forwarding to access the spark GUI in the local browser using the following commands:

• kubectl port-forward --address 0.0.0.0 pod/spark-release-master-0 30010:8080

• kubectl port-forward --address 0.0.0.0 pod/spark-release-worker-0 30011:8080

• kubectl port-forward --address 0.0.0.0 pod/spark-release-worker-0 30012:8080

We can access the host machine's 30010, 30011, and 30012 port numbers. Kubernetes's default port range is 30000 – 32767, which we can use only in this range. We must port forward all the spark pods since they are deployed independently. So, we can access the UI in the host machine using localhost:30010, localhost:30011, localhost:30012

Now again, open the terminal of the spark master node in the Kubernetes dashboard and run the

following command:

• ./bin/spark-submit --class

com.dbproject.ecommerceanalysis.EcommerceAnalysisApplication --master

spark://spark-release-master-0.spark-release-

headless.default.svc.cluster.local:7077 --num-executors 2 --driver-memory 1g --

driver-cores 1 --executor-memory 3g --executor-cores 3 myjars/ecommerce-

analysis-0.0.1-SNAPSHOT-all.jar [ip address of the database] "9042"

"electronics.smartphone" "purchase"

In the above command first command line argument is the port IP address of the database; we

can the IP address of Cassandra using the following kubectl command:

• kubectl get pods -o wide

Below is the screenshot for the sample:

https://private-user-images.githubusercontent.com/20046277/242178282-c69f5c79-4373-4a05-ba57-1c493eec25d8.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3MDQ2NDgyNjYsIm5iZiI6MTcwNDY0Nzk2NiwicGF0aCI6Ii8yMDA0NjI3Ny8yNDIxNzgyODItYzY5ZjVjNzktNDM3My00YTA1LWJhNTctMWM0OTNlZWMyNWQ4LnBuZz9YLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFWQ09EWUxTQTUzUFFLNFpBJTJGMjAyNDAxMDclMkZ1cy1lYXN0LTElMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjQwMTA3VDE3MTkyNlomWC1BbXotRXhwaXJlcz0zMDAmWC1BbXotU2lnbmF0dXJlPWQ1MThkOGFjOGYzZDBhZmU3ZGY4OWQ4MDAyMTUyMjg2ZjYxYWVlMzkwNWIyNDZhM2EzZWM2NGUxYTJkODA1ZGMmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JmFjdG9yX2lkPTAma2V5X2lkPTAmcmVwb19pZD0wIn0.rEVbtBMau9LReuCm1iJJSlXTKlawvJ3tS-fewGM9qEw

In the above screenshot, we can see the cluster IP of the database that we need to pass as a command line argument in the master mode while submitting the spark job.

The second argument in the spark-submit command is the port number of the database, which is by default “9042”. The third and fourth commands are the category\_code and the event\_type from the database, which filter the data accordingly.

Once the spark job is done, the output is saved in the database automatically. We can see the results in the cqlsh of the Cassandra pod using the select command.
