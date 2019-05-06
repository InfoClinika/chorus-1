:: to download Procrun binaries and see documentation visit http://commons.apache.org/daemon/procrun.html

set CURRENT_PATH=%~dp0

::detect x86 or x64
echo off
IF PROCESSOR_ARCHITECTURE EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITEW6432 EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITECTURE EQU "amd64" GOTO IS_amd64
IF PROCESSOR_ARCHITEW6432 EQU "amd64" GOTO IS_amd64
IF DEFINED ProgramFiles(x86) GOTO IS_amd64
:IS_x86
set PR_INSTALL="%CURRENT_PATH%prunsrv.exe"
GOTO IS_x64End
:IS_amd64
set PR_INSTALL="%CURRENT_PATH%amd64\prunsrv.exe"
GOTO IS_x64End
:IS_ia64
set PR_INSTALL="%CURRENT_PATH%ia64\prunsrv.exe"
:IS_x64End
echo on

set SERVICE_NAME=ChorusAutoimporter
set MAIN_CLASS=Driver
set LOGS_PATH="%CURRENT_PATH%logs"
set SERVICE_CLASSPATH="%CURRENT_PATH%autoimporter-os-service-1.0.jar"
set LIBRARY_PATH="%CURRENT_PATH%lib"

%PR_INSTALL% //SS//%SERVICE_NAME%
%PR_INSTALL% //DS//%SERVICE_NAME%
%PR_INSTALL% //IS//%SERVICE_NAME% --LibraryPath=%LIBRARY_PATH% --LogPrefix=%SERVICE_NAME% --LogPath=%LOGS_PATH% --StdOutput=auto --StdError=auto --LogLevel=Info --Startup=auto --StartMode=jvm --StartClass=%MAIN_CLASS% --StartMethod=start --Jvm=auto --Classpath=%SERVICE_CLASSPATH% --StopMode=jvm --StopClass=%MAIN_CLASS% --StopMethod=stop --JvmMs=64m --JvmMx=128m --JvmSs=192m --DisplayName="Chorus Autoimporter Uploader" --Description="Desktop Autoimporter for Chorus Project"
%PR_INSTALL% //ES//%SERVICE_NAME%