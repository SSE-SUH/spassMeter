@echo off

REM install wget into PATH
REM install maven into PATH
REM install GnuPGP
REM add your account settings for ossrh into maven setup (use authentication token)
REM copy this script into an empty directory
REM run this script
REM goto https://oss.sonatype.org/#welcome, staging repositories, deuni-hildesheim...*, close for check/deploy

SET LOCALREPO=http://projects.sse.uni-hildesheim.de/qm/maven/de/uni-hildesheim/sse/spassMeter
SET SPASS_VERSION=1.26
SET LOCUTOR_VERSION=1.11
SET DIR=.
SET TARGET=https://oss.sonatype.org/service/local/staging/deploy/maven2
SET REPO=ossrh
SET DEPLOYCMD=mvn gpg:sign-and-deploy-file -Durl=%TARGET% -DrepositoryId=%REPO%

call :DeployArtifact spass-meter %SPASS_VERSION%
call :DeployArtifact spass-meter-annotations %SPASS_VERSION%
call :DeployArtifact spass-meter-ant %SPASS_VERSION%
call :DeployArtifact spass-meter-boot %SPASS_VERSION%
call :DeployArtifact spass-meter-ia %SPASS_VERSION%
call :DeployArtifact spass-meter-rt %SPASS_VERSION%
call :DeployArtifact spass-meter-static %SPASS_VERSION%
call :DeployArtifact locutor %LOCUTOR_VERSION%

goto :end

:DeployArtifact
    setlocal
	SET ARTIFACTNAME=%1
	SET ARTIFACTVERSION=%2
	SET ARTIFACTPREFIX=%ARTIFACTNAME%-%ARTIFACTVERSION%
	SET POM=%ARTIFACTPREFIX%.pom
	SET JAR=%ARTIFACTPREFIX%.jar
	SET SOURCES=%ARTIFACTPREFIX%-sources.jar
	SET JAVADOC=%ARTIFACTPREFIX%-javadoc.jar
    SET URLPREFIX=%LOCALREPO%/%ARTIFACTNAME%/%ARTIFACTVERSION%

	echo wget %URLPREFIX%/%POM% -O %DIR%\%POM%
	echo wget %URLPREFIX%/%JAR% -O %DIR%\%JAR%
	echo wget %URLPREFIX%/%SOURCES% -O %DIR%\%SOURCES%
	echo wget %URLPREFIX%/%JAVADOC% -O %DIR%\%JAVADOC%
	echo %DEPLOYCMD% -DpomFile=%DIR%\%POM% -Dfile=%DIR%\%JAR%
	echo %DEPLOYCMD% -DpomFile=%DIR%\%POM% -Dfile=%DIR%\%SOURCES% -Dclassifier=sources
	echo %DEPLOYCMD% -DpomFile=%DIR%\%POM% -Dfile=%DIR%\%JAVADOC% -Dclassifier=javadoc

	endlocal
	goto :eof
	
:end