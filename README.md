# sorrix-poc

#Como instalar as dependencias de jar no projeto

mvn install:install-file -Dfile=/home/wellington/app/sorrix/libs/webta-cripto-v.1.4.jar -DgroupId=br.com.bradesco.webta -DartifactId=webta-cripto -Dversion=1.4 -Dpackaging=jar -DcreateChecksum=true

mvn install:install-file -Dfile=/home/wellington/app/sorrix/libs/wswebta-client-v.1.0.jar -DgroupId=br.com.bradesco.webta -DartifactId=wswebta-client -Dversion=1.0 -Dpackaging=jar -DcreateChecksum=true

mvn install:install-file -Dfile=/home/wellington/app/sorrix/libs/log4j-1.2.15.jar -DgroupId=log4j -DartifactId=log4j -Dversion=1.2.15 -Dpackaging=jar -DcreateChecksum=true

mvn install:install-file -Dfile=/home/wellington/app/sorrix/libs/jaxrpc.jar -DgroupId=jaxrpc -DartifactId=jaxrpc -Dversion=1.1 -Dpackaging=jar -DcreateChecksum=true

mvn install:install-file -Dfile=/home/wellington/app/sorrix/libs/saaj.jar -DgroupId=saaj -DartifactId=saaj -Dversion=1.2 -Dpackaging=jar -DcreateChecksum=true

Na pasta config/ deve conter os arquivos .bin e o mesmo deve ser indicado no App.java

#Como rodar:
##opções
- pode executar o comando "mvn clean install exec:java", ou então configurar um run na sua IDE escolhendo a opção maven e colocando apenas o comando "clean install exec:java" (vai facilitarr o debug).
- java -jar target/poc-sorrix-0.0.1-SNAPSHOT.jar 