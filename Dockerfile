FROM tomcat:8-jre8

ADD serviceregistry-roa.war /usr/local/tomcat/webapps

CMD ["catalina.sh", "run"]
