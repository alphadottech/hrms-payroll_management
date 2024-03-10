call mvn clean 
call mvn install
docker build -t ajaymishraadt/payroll:0.0.1-SNAPSHOT .
echo "Image created for payroll"