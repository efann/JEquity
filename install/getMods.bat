@echo off
echo Ensure that JEquity.jar file is not self-executing. In other words, contains no libraries.
jdeps --multi-release 12 -cp ..\libraries\controlsfx\*;..\libraries\commons-io\*;..\libraries\drivers\*;..\libraries\flyway\*;..\libraries\hibernate.5.4.2\*;..\libraries\jasper-reports\*;..\libraries\jsoup\*;   ..\out\artifacts\JEquity\JEquity.jar


