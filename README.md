## Requirements

- Java 11
- Docker

### To run LocalStack(AWS s3 Mock) and Mysql locally

`docker-compose up -d`


### VM OPTIONS to run locally
`-Dlog4j.configurationFile=log4j2-local.xml -Dspring.profiles.active=local`

### VM OPTIONS to run locally with database
`-Dlog4j.configurationFile=log4j2-prod.xml -Dspring.profiles.active=local`



### using AWS CLI to test
 `aws --endpoint-url=http://localhost:4572 s3 cp juice.jpg s3://images`
 
 
 
### Testing the end point, try any of these urls
 `http://localhost:8080/image/show/detail-small/COCACOLA?reference=baboon.png`
 
`http://localhost:8080/image/show/detail-medium/COCACOLA?reference=mountain.png`

`http://localhost:8080/image/show/detail-large/COCACOLA?reference=girl.png`

`http://localhost:8080/image/show/thumbnail?reference=watch.png`

`http://localhost:8080/image/flush/thumbnail?reference=cat.png`
 
#### Source Image from

`https://homepages.cae.wisc.edu/~ece533/images/ `

