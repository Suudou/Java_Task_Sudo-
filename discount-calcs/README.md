# Uruchomienie
## Z użyciem fat-JAR
      Uruchom fat-JAR powershellu/cmd z podaniem ścieżek do plików JSON:
      java -jar target/discount-calcs-1.0-SNAPSHOT-jar-with-dependencies.jar C:/.../.../orders.json C:/.../.../paymentmethods.json
# Uruchomienie Testów
      w Powershellu/cmd komenda:
         mvn test
   
# Wymagania
      Java 21, Maven 3.9.x
   
# Jeśli fat-Jar nie działa
      w Powershellu/cmd:
      mvn clean package
      sprawdź czy w folderze target utworzył się plik discount-calcs-1.0-SNAPSHOT-jar-with-dependencies.jar
      następnie spróbuj ponownie uruchomić jak opisano w uruchomieniu z użyciem fat-Jar.
      
# Running the application
## Using fat-Jar
      Run fat-Jar with powershell/cmd with arguments which are paths to JSON files:
      java -jar target/discount-calcs-1.0-SNAPSHOT-jar-with-dependencies.jar C:/.../.../orders.json C:/.../.../paymentmethods.json
# Run Tests
      Put command below in cmd/powershell:
      mvn test
# Requirements
      Java 21, Maven 3.9.x
# If fat-Jar doesn't work
      In Powershell/cmd input following command:
      mvn clean package
      check if file discount-calcs-1.0-SNAPSHOT-jar-with-dependencies.jar has been made in target folder
      next try again with inputing the command as descripted in Using fat-Jar section.